package com.appg.influencerdemo.instagramlogin.service;

import com.appg.influencerdemo.common.config.DemoCorsProperties;
import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.common.exception.DemoErrorCode;
import com.appg.influencerdemo.instagramlogin.config.InstagramLoginProperties;
import com.appg.influencerdemo.instagramlogin.domain.InstagramAccountSnapshot;
import com.appg.influencerdemo.instagramlogin.domain.InstagramApprovalDecision;
import com.appg.influencerdemo.instagramlogin.domain.InstagramLoginFlow;
import com.appg.influencerdemo.instagramlogin.domain.InstagramLoginFlowResult;
import com.appg.influencerdemo.instagramlogin.domain.InstagramLoginStatus;
import com.appg.influencerdemo.instagramlogin.domain.InstagramProfile;
import com.appg.influencerdemo.instagramlogin.domain.InstagramReel;
import com.appg.influencerdemo.instagramlogin.domain.InstagramTokenBundle;
import com.appg.influencerdemo.instagramlogin.dto.InstagramAccountResponse;
import com.appg.influencerdemo.instagramlogin.dto.InstagramLoginConfigResponse;
import com.appg.influencerdemo.instagramlogin.dto.InstagramLoginFlowResponse;
import com.appg.influencerdemo.instagramlogin.dto.InstagramPopupPayload;
import com.appg.influencerdemo.instagramlogin.dto.InstagramPopupViewModel;
import com.appg.influencerdemo.instagramlogin.dto.InstagramReelResponse;
import com.appg.influencerdemo.instagramlogin.dto.InstagramStartResponse;
import com.appg.influencerdemo.instagramlogin.repository.InMemoryInstagramLoginRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class InstagramLoginService {

    private static final Duration TOKEN_REFRESH_WINDOW = Duration.ofDays(7);
    private static final String POPUP_MESSAGE_TYPE = "instagram_oauth_complete";

    private final InstagramLoginProperties properties;
    private final DemoCorsProperties demoCorsProperties;
    private final InMemoryInstagramLoginRepository repository;
    private final InstagramLoginClient instagramLoginClient;
    private final InstagramApprovalPolicy approvalPolicy;

    public InstagramLoginService(
            InstagramLoginProperties properties,
            DemoCorsProperties demoCorsProperties,
            InMemoryInstagramLoginRepository repository,
            InstagramLoginClient instagramLoginClient,
            InstagramApprovalPolicy approvalPolicy
    ) {
        this.properties = properties;
        this.demoCorsProperties = demoCorsProperties;
        this.repository = repository;
        this.instagramLoginClient = instagramLoginClient;
        this.approvalPolicy = approvalPolicy;
    }

    public InstagramLoginConfigResponse getConfig() {
        String note = properties.isReady()
                ? "Meta 앱 설정이 준비되어 있으면 팝업 로그인 후 서버에서 팔로워 수와 릴스를 바로 검증합니다."
                : "Meta 앱 ID/Secret 과 redirect URI 설정이 비어 있습니다. application.yml 또는 환경변수부터 채워야 실제 로그인이 동작합니다.";

        return new InstagramLoginConfigResponse(
                properties.isEnabled(),
                properties.isReady(),
                properties.getRedirectUri(),
                properties.getDefaultMinFollowers(),
                properties.getDefaultMinMediaCount(),
                properties.getScopes(),
                note
        );
    }

    public InstagramStartResponse createAuthorizationRequest(String origin, Integer minFollowers, Integer minMediaCount) {
        requireReady();
        validateOrigin(origin);

        int followerThreshold = normalizeThreshold(minFollowers, properties.getDefaultMinFollowers());
        int mediaThreshold = normalizeThreshold(minMediaCount, properties.getDefaultMinMediaCount());

        String flowId = UUID.randomUUID().toString();
        String state = UUID.randomUUID().toString().replace("-", "");

        repository.saveFlow(new InstagramLoginFlow(
                flowId,
                state,
                origin,
                followerThreshold,
                mediaThreshold,
                Instant.now()
        ));

        return new InstagramStartResponse(flowId, buildAuthorizationUrl(state));
    }

    public InstagramPopupViewModel completeAuthorization(
            String state,
            String code,
            String error,
            String errorReason,
            String errorDescription
    ) {
        InstagramLoginFlow flow = repository.findFlowByState(state)
                .orElseThrow(() -> new BusinessException(DemoErrorCode.INVALID_INSTAGRAM_OAUTH_STATE));

        repository.removeFlow(state);

        if (error != null && !error.isBlank()) {
            return saveFailure(flow, "Instagram 로그인에서 오류가 발생했습니다: " + buildErrorMessage(error, errorReason, errorDescription));
        }

        if (code == null || code.isBlank()) {
            return saveFailure(flow, "Instagram 로그인 code 를 받지 못했습니다.");
        }

        try {
            InstagramTokenBundle shortLivedToken = instagramLoginClient.exchangeAuthorizationCode(code, properties.getRedirectUri());
            InstagramTokenBundle longLivedToken = instagramLoginClient.exchangeForLongLivedToken(shortLivedToken.accessToken());
            String accessToken = longLivedToken.accessToken();
            Instant tokenExpiresAt = resolveExpiry(longLivedToken.expiresInSeconds());

            InstagramProfile profile = instagramLoginClient.fetchProfile(accessToken, properties.getProfileFields());
            List<InstagramReel> reels = filterPlayableReels(
                    instagramLoginClient.fetchMedia(accessToken, properties.getMediaFields(), properties.getMediaLimit())
            );

            InstagramApprovalDecision decision = approvalPolicy.evaluate(
                    profile,
                    flow.minFollowers(),
                    flow.minMediaCount()
            );

            InstagramAccountSnapshot account = new InstagramAccountSnapshot(
                    profile.accountId(),
                    profile.username(),
                    profile.displayName(),
                    profile.profilePictureUrl(),
                    profile.followerCount(),
                    profile.mediaCount(),
                    accessToken,
                    tokenExpiresAt,
                    Instant.now(),
                    flow.minFollowers(),
                    flow.minMediaCount(),
                    reels
            );

            repository.saveAccount(account);

            List<String> notes = List.of(
                    "로그인은 성공했고 서버가 access token 으로 계정 정보를 조회했습니다.",
                    "릴스는 플레이 가능한 media_url 이 있는 항목만 숏츠 탭으로 반영합니다.",
                    "OAuth 제품과 권한 조합에 따라 일부 필드는 Meta 정책상 비어 있을 수 있습니다."
            );

            InstagramLoginFlowResult result = new InstagramLoginFlowResult(
                    flow.flowId(),
                    flow.origin(),
                    InstagramLoginStatus.SUCCESS,
                    decision.approved(),
                    decision.message(),
                    account,
                    notes,
                    Instant.now()
            );

            repository.saveResult(result);
            return toPopupViewModel(result);
        } catch (BusinessException exception) {
            return saveFailure(flow, exception.getMessage());
        }
    }

    public InstagramLoginFlowResponse getFlowResult(String flowId) {
        InstagramLoginFlowResult result = repository.findResult(flowId)
                .orElseThrow(() -> new BusinessException(DemoErrorCode.INSTAGRAM_FLOW_NOT_FOUND));
        return toResponse(result);
    }

    public InstagramLoginFlowResponse syncAccount(String accountId) {
        requireReady();

        InstagramAccountSnapshot account = repository.findAccount(accountId)
                .orElseThrow(() -> new BusinessException(DemoErrorCode.INSTAGRAM_ACCOUNT_NOT_FOUND));

        String accessToken = account.accessToken();
        Instant tokenExpiresAt = account.tokenExpiresAt();

        /*
         * 데모에서도 long-lived token refresh 타이밍을 반영해 두면,
         * "가입 후 다시 OAuth 로그인을 안 해도 되는가"를 실제와 비슷하게 검증할 수 있습니다.
         */
        if (tokenExpiresAt != null && Instant.now().plus(TOKEN_REFRESH_WINDOW).isAfter(tokenExpiresAt)) {
            InstagramTokenBundle refreshed = instagramLoginClient.refreshLongLivedToken(accessToken);
            accessToken = refreshed.accessToken();
            tokenExpiresAt = resolveExpiry(refreshed.expiresInSeconds());
        }

        InstagramProfile profile = instagramLoginClient.fetchProfile(accessToken, properties.getProfileFields());
        List<InstagramReel> reels = filterPlayableReels(
                instagramLoginClient.fetchMedia(accessToken, properties.getMediaFields(), properties.getMediaLimit())
        );
        InstagramApprovalDecision decision = approvalPolicy.evaluate(
                profile,
                account.minFollowersRequired(),
                account.minMediaCountRequired()
        );

        InstagramAccountSnapshot updatedAccount = new InstagramAccountSnapshot(
                profile.accountId(),
                profile.username(),
                profile.displayName(),
                profile.profilePictureUrl(),
                profile.followerCount(),
                profile.mediaCount(),
                accessToken,
                tokenExpiresAt,
                Instant.now(),
                account.minFollowersRequired(),
                account.minMediaCountRequired(),
                reels
        );
        repository.saveAccount(updatedAccount);

        InstagramLoginFlowResult result = new InstagramLoginFlowResult(
                UUID.randomUUID().toString(),
                "",
                InstagramLoginStatus.SUCCESS,
                decision.approved(),
                "저장된 long-lived token 으로 계정 정보와 릴스를 다시 동기화했습니다.",
                updatedAccount,
                List.of(
                        "재로그인 없이 저장된 토큰으로 재동기화를 시도했습니다.",
                        "만료 임박 토큰은 refresh 후 새 만료시각으로 갱신했습니다."
                ),
                Instant.now()
        );

        repository.saveResult(result);
        return toResponse(result);
    }

    private void requireReady() {
        if (!properties.isReady()) {
            throw new BusinessException(DemoErrorCode.INSTAGRAM_LOGIN_NOT_READY);
        }
    }

    private void validateOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            throw new BusinessException(DemoErrorCode.INVALID_ORIGIN);
        }

        boolean allowed = demoCorsProperties.getAllowedOrigins().stream()
                .anyMatch(allowedOrigin -> allowedOrigin.equalsIgnoreCase(origin.trim()));
        if (!allowed) {
            throw new BusinessException(DemoErrorCode.INVALID_ORIGIN);
        }
    }

    private int normalizeThreshold(Integer value, int defaultValue) {
        int threshold = value == null ? defaultValue : value;
        if (threshold < 0) {
            throw new BusinessException(DemoErrorCode.INVALID_OAUTH_THRESHOLD);
        }
        return threshold;
    }

    private String buildAuthorizationUrl(String state) {
        return UriComponentsBuilder.fromUriString(properties.getAuthorizationUrl())
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(",", properties.getScopes()))
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    private Instant resolveExpiry(Long expiresInSeconds) {
        return expiresInSeconds == null ? null : Instant.now().plusSeconds(expiresInSeconds);
    }

    private List<InstagramReel> filterPlayableReels(List<InstagramReel> reels) {
        return reels.stream()
                .filter(InstagramReel::isPlayableReel)
                .collect(Collectors.toList());
    }

    private String buildErrorMessage(String error, String errorReason, String errorDescription) {
        StringBuilder builder = new StringBuilder(error);
        if (errorReason != null && !errorReason.isBlank()) {
            builder.append(" / ").append(errorReason);
        }
        if (errorDescription != null && !errorDescription.isBlank()) {
            builder.append(" / ").append(errorDescription);
        }
        return builder.toString();
    }

    private InstagramPopupViewModel saveFailure(InstagramLoginFlow flow, String message) {
        InstagramLoginFlowResult result = new InstagramLoginFlowResult(
                flow.flowId(),
                flow.origin(),
                InstagramLoginStatus.FAILURE,
                false,
                message,
                null,
                List.of(
                        "로그인 자체가 취소되었거나 Meta 쪽 token/profile 조회 단계에서 실패했습니다.",
                        "application.yml 의 endpoint, redirect URI, scope 를 다시 확인해보세요."
                ),
                Instant.now()
        );
        repository.saveResult(result);
        return toPopupViewModel(result);
    }

    private InstagramPopupViewModel toPopupViewModel(InstagramLoginFlowResult result) {
        boolean success = result.status() == InstagramLoginStatus.SUCCESS;
        String title = success ? "Instagram 로그인 완료" : "Instagram 로그인 실패";
        String description = result.message();

        return new InstagramPopupViewModel(
                title,
                description,
                result.origin(),
                new InstagramPopupPayload(
                        POPUP_MESSAGE_TYPE,
                        result.flowId(),
                        success,
                        description
                )
        );
    }

    private InstagramLoginFlowResponse toResponse(InstagramLoginFlowResult result) {
        InstagramAccountResponse account = result.account() == null
                ? null
                : new InstagramAccountResponse(
                result.account().accountId(),
                result.account().username(),
                result.account().displayName(),
                result.account().profilePictureUrl(),
                result.account().followerCount(),
                result.account().mediaCount(),
                result.account().tokenExpiresAt(),
                result.account().syncedAt(),
                result.account().minFollowersRequired(),
                result.account().minMediaCountRequired(),
                result.account().reels().size()
        );

        List<InstagramReelResponse> reels = result.account() == null
                ? List.of()
                : result.account().reels().stream()
                .map(reel -> new InstagramReelResponse(
                        reel.mediaId(),
                        reel.mediaType(),
                        reel.mediaProductType(),
                        reel.caption(),
                        reel.permalink(),
                        reel.mediaUrl(),
                        reel.thumbnailUrl(),
                        reel.timestamp(),
                        reel.isPlayableReel()
                ))
                .toList();

        return new InstagramLoginFlowResponse(
                result.flowId(),
                result.status(),
                result.approved(),
                result.message(),
                account,
                reels,
                result.notes(),
                result.completedAt()
        );
    }
}
