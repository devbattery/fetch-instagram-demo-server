package com.appg.influencerdemo.feed.service;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.common.exception.DemoErrorCode;
import com.appg.influencerdemo.feed.domain.InfluencerVideo;
import com.appg.influencerdemo.feed.domain.PlatformType;
import com.appg.influencerdemo.feed.domain.UserRole;
import com.appg.influencerdemo.feed.dto.FeedItemResponse;
import com.appg.influencerdemo.feed.dto.FeedResponse;
import com.appg.influencerdemo.feed.repository.FeedRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FeedService {

    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 20;

    private final FeedRepository feedRepository;
    private final FeedOrderingPolicy feedOrderingPolicy;

    public FeedService(FeedRepository feedRepository, FeedOrderingPolicy feedOrderingPolicy) {
        this.feedRepository = feedRepository;
        this.feedOrderingPolicy = feedOrderingPolicy;
    }

    public FeedResponse getFeed(UserRole viewerRole, int limit) {
        validateLimit(limit);

        /*
         * 숏폼 피드는 "현재 카드와 인접 카드만 실제 플레이어를 붙이는" 방식이 가장 안정적이므로,
         * 서버는 최대한 단순한 재생 메타데이터만 내려주고 클라이언트가 preload 범위를 결정하게 둡니다.
         * 이렇게 하면 실제 YouTube embed 기반이어도 무거운 iframe을 한 번에 많이 띄우지 않아 웹뷰 체감 성능을 지킬 수 있습니다.
         */
        List<FeedItemResponse> items = feedOrderingPolicy.order(feedRepository.findAll()).stream()
                .limit(limit)
                .map(this::toResponse)
                .collect(Collectors.toList());

        List<String> deliveryNotes = List.of(
                "세로 피드는 CSS scroll-snap과 IntersectionObserver 조합으로 구현하는 전제를 둔 응답입니다.",
                "YouTube 카드는 iframe 제어를, Instagram 카드는 HTML5 video 시뮬레이션을 사용합니다.",
                "인스타 파란 체크 자동 판정은 공식 API 제약 때문에 시뮬레이션으로 분리했습니다."
        );

        return new FeedResponse(
                viewerRole,
                viewerRole == UserRole.INFLUENCER,
                false,
                deliveryNotes,
                items
        );
    }

    private void validateLimit(int limit) {
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new BusinessException(DemoErrorCode.INVALID_LIMIT);
        }
    }

    private FeedItemResponse toResponse(InfluencerVideo video) {
        String embedUrl = null;
        if (video.platformType() == PlatformType.YOUTUBE && video.youtubeVideoId() != null) {
            embedUrl = buildYouTubeEmbedUrl(video.youtubeVideoId());
        }

        return new FeedItemResponse(
                video.id(),
                video.influencerName(),
                video.handle(),
                video.verified(),
                video.platformType(),
                video.mediaType(),
                video.playbackType(),
                video.title(),
                video.description(),
                video.profileUrl(),
                embedUrl,
                video.videoUrl(),
                video.posterUrl(),
                video.tags(),
                video.simulation()
        );
    }

    private String buildYouTubeEmbedUrl(String videoId) {
        return "https://www.youtube.com/embed/" + videoId
                + "?playsinline=1"
                + "&enablejsapi=1"
                + "&autoplay=1"
                + "&mute=1"
                + "&rel=0"
                + "&controls=0"
                + "&loop=1"
                + "&playlist=" + videoId;
    }
}
