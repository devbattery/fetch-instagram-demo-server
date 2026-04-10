package com.appg.influencerdemo.youtubelogin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.appg.influencerdemo.common.config.DemoCorsProperties;
import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.youtubelogin.config.YouTubeLoginProperties;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeChannelProfile;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeLoginStatus;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeTokenBundle;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeVideo;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeLoginFlowResponse;
import com.appg.influencerdemo.youtubelogin.dto.YouTubePopupViewModel;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeStartResponse;
import com.appg.influencerdemo.youtubelogin.repository.InMemoryYouTubeLoginRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class YouTubeLoginServiceTest {

    private YouTubeLoginClient youTubeLoginClient;
    private YouTubeLoginService youTubeLoginService;

    @BeforeEach
    void setUp() {
        YouTubeLoginProperties properties = new YouTubeLoginProperties();
        properties.setEnabled(true);
        properties.setClientId("youtube-client-id");
        properties.setClientSecret("youtube-client-secret");
        properties.setRedirectUri("http://localhost:8086/demo/youtube-login/callback");
        properties.setDefaultMinSubscribers(1000);
        properties.setDefaultMinVideoCount(9);

        DemoCorsProperties corsProperties = new DemoCorsProperties();
        corsProperties.setAllowedOrigins(List.of("http://localhost:5173"));

        youTubeLoginClient = Mockito.mock(YouTubeLoginClient.class);
        youTubeLoginService = new YouTubeLoginService(
                properties,
                corsProperties,
                new InMemoryYouTubeLoginRepository(),
                youTubeLoginClient,
                new YouTubeApprovalPolicy()
        );
    }

    @Test
    void 허용된_origin이면_google_authorization_url을_생성한다() {
        YouTubeStartResponse response = youTubeLoginService.createAuthorizationRequest(
                "http://localhost:5173",
                2000,
                12
        );

        assertThat(response.flowId()).isNotBlank();
        assertThat(response.authorizationUrl()).contains("client_id=youtube-client-id");
        assertThat(response.authorizationUrl()).contains("access_type=offline");
        assertThat(response.authorizationUrl()).contains("scope=https://www.googleapis.com/auth/youtube.readonly");
        assertThat(response.authorizationUrl()).contains("state=");
    }

    @Test
    void 검증_기준을_통과하면_채널과_영상목록을_저장한다() {
        YouTubeStartResponse start = youTubeLoginService.createAuthorizationRequest(
                "http://localhost:5173",
                1000,
                9
        );
        String state = start.authorizationUrl().replaceFirst("^.*state=", "");

        when(youTubeLoginClient.exchangeAuthorizationCode(anyString(), anyString()))
                .thenReturn(new YouTubeTokenBundle("access-token", "refresh-token", 3600L, "scope", "Bearer"));
        when(youTubeLoginClient.fetchMyChannel(anyString()))
                .thenReturn(new YouTubeChannelProfile(
                        "UC-demo-channel",
                        "Creator Tube",
                        "CreatorTube",
                        "https://yt3.example.com/avatar.jpg",
                        15000L,
                        42L,
                        "UU-demo-channel"
                ));
        when(youTubeLoginClient.fetchUploads(anyString(), anyString(), anyInt()))
                .thenReturn(List.of(
                        new YouTubeVideo(
                                "shorts-video",
                                "첫 번째 Shorts 후보",
                                "짧은 영상",
                                "https://i.ytimg.com/vi/shorts-video/hqdefault.jpg",
                                "https://www.youtube.com/watch?v=shorts-video",
                                "https://www.youtube.com/embed/shorts-video?enablejsapi=1&playsinline=1&rel=0",
                                59L,
                                1080,
                                1920,
                                Instant.parse("2026-04-09T02:00:00Z"),
                                true,
                                true
                        ),
                        new YouTubeVideo(
                                "long-video",
                                "롱폼 영상",
                                "긴 영상",
                                "https://i.ytimg.com/vi/long-video/hqdefault.jpg",
                                "https://www.youtube.com/watch?v=long-video",
                                "https://www.youtube.com/embed/long-video?enablejsapi=1&playsinline=1&rel=0",
                                600L,
                                1920,
                                1080,
                                Instant.parse("2026-04-08T02:00:00Z"),
                                false,
                                true
                        )
                ));

        YouTubePopupViewModel popupViewModel = youTubeLoginService.completeAuthorization(
                state,
                "auth-code",
                null,
                null
        );

        YouTubeLoginFlowResponse result = youTubeLoginService.getFlowResult(popupViewModel.payload().flowId());

        assertThat(result.status()).isEqualTo(YouTubeLoginStatus.SUCCESS);
        assertThat(result.approved()).isTrue();
        assertThat(result.account()).isNotNull();
        assertThat(result.account().channelId()).isEqualTo("UC-demo-channel");
        assertThat(result.account().playableShortsCount()).isEqualTo(1);
        assertThat(result.videos()).hasSize(2);
        assertThat(result.videos().get(0).shortFormCandidate()).isTrue();
    }

    @Test
    void 허용되지_않은_origin이면_예외가_발생한다() {
        assertThatThrownBy(() -> youTubeLoginService.createAuthorizationRequest(
                "https://evil.example.com",
                1,
                1
        )).isInstanceOf(BusinessException.class);
    }
}
