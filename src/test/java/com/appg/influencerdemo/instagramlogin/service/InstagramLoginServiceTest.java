package com.appg.influencerdemo.instagramlogin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.appg.influencerdemo.common.config.DemoCorsProperties;
import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.instagramlogin.config.InstagramLoginProperties;
import com.appg.influencerdemo.instagramlogin.domain.InstagramLoginStatus;
import com.appg.influencerdemo.instagramlogin.domain.InstagramProfile;
import com.appg.influencerdemo.instagramlogin.domain.InstagramReel;
import com.appg.influencerdemo.instagramlogin.domain.InstagramTokenBundle;
import com.appg.influencerdemo.instagramlogin.dto.InstagramLoginFlowResponse;
import com.appg.influencerdemo.instagramlogin.dto.InstagramPopupViewModel;
import com.appg.influencerdemo.instagramlogin.dto.InstagramStartResponse;
import com.appg.influencerdemo.instagramlogin.repository.InMemoryInstagramLoginRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class InstagramLoginServiceTest {

    private InstagramLoginClient instagramLoginClient;
    private InstagramLoginService instagramLoginService;

    @BeforeEach
    void setUp() {
        InstagramLoginProperties properties = new InstagramLoginProperties();
        properties.setEnabled(true);
        properties.setClientId("client-id");
        properties.setClientSecret("client-secret");
        properties.setRedirectUri("http://localhost:8086/demo/instagram-login/callback");
        properties.setDefaultMinFollowers(5000);
        properties.setDefaultMinMediaCount(9);

        DemoCorsProperties corsProperties = new DemoCorsProperties();
        corsProperties.setAllowedOrigins(List.of("http://localhost:5173"));

        instagramLoginClient = Mockito.mock(InstagramLoginClient.class);
        instagramLoginService = new InstagramLoginService(
                properties,
                corsProperties,
                new InMemoryInstagramLoginRepository(),
                instagramLoginClient,
                new InstagramApprovalPolicy()
        );
    }

    @Test
    void 허용된_origin이면_authorization_url을_생성한다() {
        InstagramStartResponse response = instagramLoginService.createAuthorizationRequest(
                "http://localhost:5173",
                12000,
                15
        );

        assertThat(response.flowId()).isNotBlank();
        assertThat(response.authorizationUrl()).contains("client_id=client-id");
        assertThat(response.authorizationUrl()).contains("scope=instagram_business_basic");
        assertThat(response.authorizationUrl()).contains("state=");
    }

    @Test
    void 검증_기준을_통과하면_성공_결과와_릴스를_저장한다() {
        InstagramStartResponse start = instagramLoginService.createAuthorizationRequest(
                "http://localhost:5173",
                5000,
                9
        );
        String state = start.authorizationUrl().replaceFirst("^.*state=", "");

        when(instagramLoginClient.exchangeAuthorizationCode(anyString(), anyString()))
                .thenReturn(new InstagramTokenBundle("short-token", 3600L, "1789"));
        when(instagramLoginClient.exchangeForLongLivedToken(anyString()))
                .thenReturn(new InstagramTokenBundle("long-token", 5184000L, "1789"));
        when(instagramLoginClient.fetchProfile(anyString(), anyList()))
                .thenReturn(new InstagramProfile(
                        "17890001",
                        "creator.lab",
                        "Creator Lab",
                        "https://example.com/profile.jpg",
                        12000L,
                        34L
                ));
        when(instagramLoginClient.fetchMedia(anyString(), anyList(), anyInt()))
                .thenReturn(List.of(
                        new InstagramReel(
                                "media-1",
                                "VIDEO",
                                "REELS",
                                "테스트 릴스",
                                "https://www.instagram.com/reel/demo-1/",
                                "https://cdn.example.com/reel.mp4",
                                "https://cdn.example.com/thumb.jpg",
                                Instant.parse("2026-03-31T09:00:00Z")
                        )
                ));

        InstagramPopupViewModel popupViewModel = instagramLoginService.completeAuthorization(
                state,
                "auth-code",
                null,
                null,
                null
        );

        InstagramLoginFlowResponse result = instagramLoginService.getFlowResult(popupViewModel.payload().flowId());

        assertThat(result.status()).isEqualTo(InstagramLoginStatus.SUCCESS);
        assertThat(result.approved()).isTrue();
        assertThat(result.account()).isNotNull();
        assertThat(result.account().accountId()).isEqualTo("17890001");
        assertThat(result.reels()).hasSize(1);
        assertThat(result.reels().get(0).playable()).isTrue();
    }

    @Test
    void 허용되지_않은_origin이면_예외가_발생한다() {
        assertThatThrownBy(() -> instagramLoginService.createAuthorizationRequest(
                "https://evil.example.com",
                1,
                1
        )).isInstanceOf(BusinessException.class);
    }
}
