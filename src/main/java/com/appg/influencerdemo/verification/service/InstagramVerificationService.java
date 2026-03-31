package com.appg.influencerdemo.verification.service;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.common.exception.DemoErrorCode;
import com.appg.influencerdemo.verification.domain.VerificationDecision;
import com.appg.influencerdemo.verification.dto.VerificationLaunchResponse;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class InstagramVerificationService {

    private static final Pattern HANDLE_PATTERN = Pattern.compile("^[A-Za-z0-9._]{1,30}$");
    private static final Set<String> LOCAL_WEB_HOSTS = Set.of("localhost", "127.0.0.1");
    private static final Set<String> ALLOWED_CUSTOM_SCHEMES = Set.of("demowebview", "taeyangdemo", "taeyang-demo");

    public VerificationLaunchResponse buildLaunchResponse(String rawHandle, String browserRedirectUri) {
        String handle = normalizeHandle(rawHandle);
        String redirectUri = validateRedirectUri(browserRedirectUri);
        String profileUrl = buildProfileUrl(handle);

        String browserStartUrl = UriComponentsBuilder.fromPath("/demo/verification/instagram/start")
                .queryParam("handle", handle)
                .queryParam("redirectUri", redirectUri)
                .build(true)
                .toUriString();

        String nativeStartUrl = UriComponentsBuilder.fromPath("/demo/verification/instagram/start")
                .queryParam("handle", handle)
                .queryParam("redirectUri", "demowebview://verification/callback")
                .build(true)
                .toUriString();

        return new VerificationLaunchResponse(
                handle,
                profileUrl,
                browserStartUrl,
                nativeStartUrl,
                false,
                "공식 API 기준으로는 외부 인스타 프로필 URL만으로 파란 체크 자동 판정을 신뢰성 있게 확정하기 어려워서, 데모에서는 인증 완료 후 앱 복귀 콜백만 검증합니다."
        );
    }

    public String buildCompletionRedirect(String rawHandle, String rawRedirectUri, VerificationDecision decision) {
        String handle = normalizeHandle(rawHandle);
        String redirectUri = validateRedirectUri(rawRedirectUri);

        /*
         * 실제 네이티브 연동에서는 이 redirectUri 자리에 custom scheme 또는 universal link가 들어갑니다.
         * 데모는 브라우저에서도 검증할 수 있도록 localhost callback 과 custom scheme 둘 다 허용합니다.
         */
        boolean success = decision == VerificationDecision.SUCCESS;
        String resultCode = (success ? "IG-SIM-SUCCESS-" : "IG-SIM-FAIL-")
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("source", "instagram_simulated")
                .queryParam("handle", handle)
                .queryParam("status", decision.name())
                .queryParam("verified", success)
                .queryParam("code", resultCode)
                .build(true)
                .toUriString();
    }

    public String normalizeHandle(String rawHandle) {
        if (rawHandle == null) {
            throw new BusinessException(DemoErrorCode.INVALID_HANDLE);
        }

        String handle = rawHandle.trim().replaceFirst("^@", "");
        if (!HANDLE_PATTERN.matcher(handle).matches()) {
            throw new BusinessException(DemoErrorCode.INVALID_HANDLE);
        }
        return handle;
    }

    public String validateRedirectUri(String rawRedirectUri) {
        if (rawRedirectUri == null || rawRedirectUri.isBlank()) {
            throw new BusinessException(DemoErrorCode.INVALID_REDIRECT_URI);
        }

        URI uri = URI.create(rawRedirectUri.trim());
        String scheme = uri.getScheme();

        if (scheme == null || scheme.isBlank()) {
            throw new BusinessException(DemoErrorCode.INVALID_REDIRECT_URI);
        }

        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            String host = uri.getHost();
            if (host != null && LOCAL_WEB_HOSTS.contains(host.toLowerCase())) {
                return uri.toString();
            }
            throw new BusinessException(DemoErrorCode.INVALID_REDIRECT_URI);
        }

        if (ALLOWED_CUSTOM_SCHEMES.contains(scheme.toLowerCase())) {
            return uri.toString();
        }

        throw new BusinessException(DemoErrorCode.INVALID_REDIRECT_URI);
    }

    private String buildProfileUrl(String handle) {
        return "https://www.instagram.com/" + handle + "/";
    }
}
