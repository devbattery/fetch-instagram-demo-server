package com.appg.influencerdemo.youtubelogin.service;

import com.appg.influencerdemo.common.config.DemoCorsProperties;
import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.common.exception.DemoErrorCode;
import com.appg.influencerdemo.youtubelogin.config.YouTubeLoginProperties;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeApprovalDecision;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeChannelProfile;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeChannelSnapshot;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeLoginFlow;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeLoginFlowResult;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeLoginStatus;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeTokenBundle;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeVideo;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeAccountResponse;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeLoginConfigResponse;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeLoginFlowResponse;
import com.appg.influencerdemo.youtubelogin.dto.YouTubePopupPayload;
import com.appg.influencerdemo.youtubelogin.dto.YouTubePopupViewModel;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeStartResponse;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeVideoResponse;
import com.appg.influencerdemo.youtubelogin.repository.InMemoryYouTubeLoginRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class YouTubeLoginService {

    private static final Duration TOKEN_REFRESH_WINDOW = Duration.ofDays(7);
    private static final String POPUP_MESSAGE_TYPE = "youtube_oauth_complete";

    private final YouTubeLoginProperties properties;
    private final DemoCorsProperties demoCorsProperties;
    private final InMemoryYouTubeLoginRepository repository;
    private final YouTubeLoginClient youTubeLoginClient;
    private final YouTubeApprovalPolicy approvalPolicy;

    public YouTubeLoginService(
            YouTubeLoginProperties properties,
            DemoCorsProperties demoCorsProperties,
            InMemoryYouTubeLoginRepository repository,
            YouTubeLoginClient youTubeLoginClient,
            YouTubeApprovalPolicy approvalPolicy
    ) {
        this.properties = properties;
        this.demoCorsProperties = demoCorsProperties;
        this.repository = repository;
        this.youTubeLoginClient = youTubeLoginClient;
        this.approvalPolicy = approvalPolicy;
    }

    public YouTubeLoginConfigResponse getConfig() {
        String note = properties.isReady()
                ? "Google OAuth 와 YouTube Data API 설정이 준비되어 있으면 채널 소유 확인 후 Shorts 후보와 롱폼을 바로 가져옵니다."
                : "Google OAuth client ID/secret 또는 redirect URI 설정이 비어 있습니다. 먼저 콘솔 설정을 마쳐야 합니다.";

        return new YouTubeLoginConfigResponse(
                properties.isEnabled(),
                properties.isReady(),
                properties.getRedirectUri(),
                properties.getDefaultMinSubscribers(),
                properties.getDefaultMinVideoCount(),
                properties.getScopes(),
                note
        );
    }

    public YouTubeStartResponse createAuthorizationRequest(String origin, Integer minSubscribers, Integer minVideoCount) {
        requireReady();
        validateOrigin(origin);

        int subscriberThreshold = normalizeThreshold(minSubscribers, properties.getDefaultMinSubscribers());
        int videoThreshold = normalizeThreshold(minVideoCount, properties.getDefaultMinVideoCount());

        String flowId = UUID.randomUUID().toString();
        String state = UUID.randomUUID().toString().replace("-", "");

        repository.saveFlow(new YouTubeLoginFlow(
                flowId,
                state,
                origin,
                subscriberThreshold,
                videoThreshold,
                Instant.now()
        ));

        return new YouTubeStartResponse(flowId, buildAuthorizationUrl(state));
    }

    public YouTubePopupViewModel completeAuthorization(
            String state,
            String code,
            String error,
            String errorDescription
    ) {
        YouTubeLoginFlow flow = repository.findFlowByState(state)
                .orElseThrow(() -> new BusinessException(DemoErrorCode.INVALID_YOUTUBE_OAUTH_STATE));

        repository.removeFlow(state);

        if (error != null && !error.isBlank()) {
            return saveFailure(flow, "Google 로그인에서 오류가 발생했습니다: " + buildErrorMessage(error, errorDescription));
        }

        if (code == null || code.isBlank()) {
            return saveFailure(flow, "Google authorization code 를 받지 못했습니다.");
        }

        try {
            YouTubeTokenBundle tokenBundle = youTubeLoginClient.exchangeAuthorizationCode(code, properties.getRedirectUri());
            YouTubeChannelProfile profile = youTubeLoginClient.fetchMyChannel(tokenBundle.accessToken());
            List<YouTubeVideo> videos = youTubeLoginClient.fetchUploads(
                    tokenBundle.accessToken(),
                    profile.uploadsPlaylistId(),
                    properties.getVideoLimit()
            );

            YouTubeApprovalDecision decision = approvalPolicy.evaluate(
                    profile,
                    flow.minSubscribers(),
                    flow.minVideoCount()
            );

            YouTubeChannelSnapshot channel = new YouTubeChannelSnapshot(
                    profile.channelId(),
                    profile.title(),
                    profile.handle(),
                    profile.thumbnailUrl(),
                    profile.subscriberCount(),
                    profile.videoCount(),
                    profile.uploadsPlaylistId(),
                    tokenBundle.accessToken(),
                    tokenBundle.refreshToken(),
                    resolveExpiry(tokenBundle.expiresInSeconds()),
                    Instant.now(),
                    flow.minSubscribers(),
                    flow.minVideoCount(),
                    videos
            );

            repository.saveChannel(channel);

            List<String> notes = List.of(
                    "Google OAuth 로 로그인한 본인 채널인지 channels.list?mine=true 로 확인했습니다.",
                    "업로드 플레이리스트를 기준으로 최신 공개 영상을 조회했습니다.",
                    "YouTube Data API는 Shorts 여부를 직접 주지 않으므로, 이 데모는 3분 이하 영상을 Shorts 후보로 분류합니다."
            );

            YouTubeLoginFlowResult result = new YouTubeLoginFlowResult(
                    flow.flowId(),
                    flow.origin(),
                    YouTubeLoginStatus.SUCCESS,
                    decision.approved(),
                    decision.message(),
                    channel,
                    notes,
                    Instant.now()
            );

            repository.saveResult(result);
            return toPopupViewModel(result);
        } catch (BusinessException exception) {
            return saveFailure(flow, exception.getMessage());
        }
    }

    public YouTubeLoginFlowResponse getFlowResult(String flowId) {
        YouTubeLoginFlowResult result = repository.findResult(flowId)
                .orElseThrow(() -> new BusinessException(DemoErrorCode.YOUTUBE_FLOW_NOT_FOUND));
        return toResponse(result);
    }

    public YouTubeLoginFlowResponse syncChannel(String channelId) {
        requireReady();

        YouTubeChannelSnapshot channel = repository.findChannel(channelId)
                .orElseThrow(() -> new BusinessException(DemoErrorCode.YOUTUBE_CHANNEL_NOT_FOUND));

        String accessToken = channel.accessToken();
        String refreshToken = channel.refreshToken();
        Instant tokenExpiresAt = channel.tokenExpiresAt();

        /*
         * YouTube access token 은 짧기 때문에, 실제 운영과 비슷한 데모를 위해
         * 만료 임박 시 refresh token 으로 새 access token 을 받아 재동기화합니다.
         */
        if (tokenExpiresAt != null && Instant.now().plus(TOKEN_REFRESH_WINDOW).isAfter(tokenExpiresAt)) {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new BusinessException(DemoErrorCode.YOUTUBE_REFRESH_TOKEN_MISSING);
            }

            YouTubeTokenBundle refreshed = youTubeLoginClient.refreshAccessToken(refreshToken);
            accessToken = refreshed.accessToken();
            refreshToken = refreshed.refreshToken() == null || refreshed.refreshToken().isBlank()
                    ? refreshToken
                    : refreshed.refreshToken();
            tokenExpiresAt = resolveExpiry(refreshed.expiresInSeconds());
        }

        YouTubeChannelProfile profile = youTubeLoginClient.fetchMyChannel(accessToken);
        List<YouTubeVideo> videos = youTubeLoginClient.fetchUploads(
                accessToken,
                profile.uploadsPlaylistId(),
                properties.getVideoLimit()
        );
        YouTubeApprovalDecision decision = approvalPolicy.evaluate(
                profile,
                channel.minSubscribersRequired(),
                channel.minVideoCountRequired()
        );

        YouTubeChannelSnapshot updatedChannel = new YouTubeChannelSnapshot(
                profile.channelId(),
                profile.title(),
                profile.handle(),
                profile.thumbnailUrl(),
                profile.subscriberCount(),
                profile.videoCount(),
                profile.uploadsPlaylistId(),
                accessToken,
                refreshToken,
                tokenExpiresAt,
                Instant.now(),
                channel.minSubscribersRequired(),
                channel.minVideoCountRequired(),
                videos
        );
        repository.saveChannel(updatedChannel);

        YouTubeLoginFlowResult result = new YouTubeLoginFlowResult(
                UUID.randomUUID().toString(),
                "",
                YouTubeLoginStatus.SUCCESS,
                decision.approved(),
                "저장된 Google refresh token/access token 으로 채널 영상 목록을 다시 동기화했습니다.",
                updatedChannel,
                List.of(
                        "재로그인 없이 저장된 토큰으로 채널과 영상 목록을 다시 읽었습니다.",
                        "Shorts 후보 분류는 3분 이하 영상 기준의 휴리스틱입니다."
                ),
                Instant.now()
        );

        repository.saveResult(result);
        return toResponse(result);
    }

    private void requireReady() {
        if (!properties.isReady()) {
            throw new BusinessException(DemoErrorCode.YOUTUBE_LOGIN_NOT_READY);
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
                .queryParam("scope", String.join(" ", properties.getScopes()))
                .queryParam("access_type", "offline")
                .queryParam("include_granted_scopes", "true")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    private Instant resolveExpiry(Long expiresInSeconds) {
        return expiresInSeconds == null ? null : Instant.now().plusSeconds(expiresInSeconds);
    }

    private String buildErrorMessage(String error, String errorDescription) {
        if (errorDescription == null || errorDescription.isBlank()) {
            return error;
        }
        return error + " / " + errorDescription;
    }

    private YouTubePopupViewModel saveFailure(YouTubeLoginFlow flow, String message) {
        YouTubeLoginFlowResult result = new YouTubeLoginFlowResult(
                flow.flowId(),
                flow.origin(),
                YouTubeLoginStatus.FAILURE,
                false,
                message,
                null,
                List.of(
                        "Google OAuth 동의가 취소되었거나 token/channel 조회 단계에서 실패했습니다.",
                        "Google Cloud Console 의 OAuth client, redirect URI, YouTube Data API 활성화 여부를 확인해보세요."
                ),
                Instant.now()
        );
        repository.saveResult(result);
        return toPopupViewModel(result);
    }

    private YouTubePopupViewModel toPopupViewModel(YouTubeLoginFlowResult result) {
        boolean success = result.status() == YouTubeLoginStatus.SUCCESS;
        return new YouTubePopupViewModel(
                success ? "YouTube 로그인 완료" : "YouTube 로그인 실패",
                result.message(),
                result.origin(),
                new YouTubePopupPayload(
                        POPUP_MESSAGE_TYPE,
                        result.flowId(),
                        success,
                        result.message()
                )
        );
    }

    private YouTubeLoginFlowResponse toResponse(YouTubeLoginFlowResult result) {
        YouTubeAccountResponse account = result.channel() == null
                ? null
                : new YouTubeAccountResponse(
                result.channel().channelId(),
                result.channel().title(),
                result.channel().handle(),
                result.channel().thumbnailUrl(),
                result.channel().subscriberCount(),
                result.channel().videoCount(),
                result.channel().tokenExpiresAt(),
                result.channel().syncedAt(),
                result.channel().minSubscribersRequired(),
                result.channel().minVideoCountRequired(),
                (int) result.channel().videos().stream()
                        .filter(YouTubeVideo::shortFormCandidate)
                        .filter(YouTubeVideo::playable)
                        .count(),
                result.channel().videos().size()
        );

        List<YouTubeVideoResponse> videos = result.channel() == null
                ? List.of()
                : result.channel().videos().stream()
                .map(video -> new YouTubeVideoResponse(
                        video.videoId(),
                        video.title(),
                        video.description(),
                        video.thumbnailUrl(),
                        video.watchUrl(),
                        video.embedUrl(),
                        video.durationSeconds(),
                        video.widthPixels(),
                        video.heightPixels(),
                        video.publishedAt(),
                        video.shortFormCandidate(),
                        video.playable()
                ))
                .toList();

        return new YouTubeLoginFlowResponse(
                result.flowId(),
                result.status(),
                result.approved(),
                result.message(),
                account,
                videos,
                result.notes(),
                result.completedAt()
        );
    }
}
