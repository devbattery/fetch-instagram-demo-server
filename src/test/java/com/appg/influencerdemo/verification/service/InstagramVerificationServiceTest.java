package com.appg.influencerdemo.verification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.verification.domain.VerificationDecision;
import com.appg.influencerdemo.verification.dto.VerificationLaunchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstagramVerificationServiceTest {

    private InstagramVerificationService instagramVerificationService;

    @BeforeEach
    void setUp() {
        instagramVerificationService = new InstagramVerificationService();
    }

    @Test
    void shouldBuildLaunchUrlsForBrowserAndNative() {
        VerificationLaunchResponse response = instagramVerificationService.buildLaunchResponse(
                "@rose.atlas",
                "http://localhost:5173/verification/callback"
        );

        assertThat(response.handle()).isEqualTo("rose.atlas");
        assertThat(response.browserStartUrl()).contains("/demo/verification/instagram/start");
        assertThat(response.nativeStartUrl()).contains("demowebview://verification/callback");
        assertThat(response.automaticBadgeCheckSupported()).isFalse();
    }

    @Test
    void shouldBuildSuccessRedirectWithCode() {
        String redirectUrl = instagramVerificationService.buildCompletionRedirect(
                "rose.atlas",
                "http://localhost:5173/verification/callback",
                VerificationDecision.SUCCESS
        );

        assertThat(redirectUrl).contains("status=SUCCESS");
        assertThat(redirectUrl).contains("verified=true");
        assertThat(redirectUrl).contains("code=IG-SIM-SUCCESS-");
    }

    @Test
    void shouldRejectUnknownRedirectUri() {
        assertThatThrownBy(() -> instagramVerificationService.buildLaunchResponse(
                "rose.atlas",
                "https://malicious.example.com/callback"
        )).isInstanceOf(BusinessException.class);
    }
}
