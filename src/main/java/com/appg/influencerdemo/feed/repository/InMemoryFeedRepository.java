package com.appg.influencerdemo.feed.repository;

import com.appg.influencerdemo.feed.domain.InfluencerVideo;
import com.appg.influencerdemo.feed.domain.MediaType;
import com.appg.influencerdemo.feed.domain.PlatformType;
import com.appg.influencerdemo.feed.domain.PlaybackType;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryFeedRepository implements FeedRepository {

    @Override
    public List<InfluencerVideo> findAll() {
        return List.of(
                new InfluencerVideo(
                        "yt-short-1",
                        "Studio Haneul",
                        "studio_haneul",
                        true,
                        PlatformType.YOUTUBE,
                        MediaType.SHORTFORM,
                        PlaybackType.YOUTUBE_IFRAME,
                        "브랜딩 숏폼 컷 편집 데모",
                        "공개 YouTube 영상을 세로 피드 카드에 맞게 재생하는 예시입니다.",
                        "https://www.youtube.com/@YouTube",
                        "M7lc1UVf-VE",
                        null,
                        "https://i.ytimg.com/vi/M7lc1UVf-VE/hqdefault.jpg",
                        List.of("youtube", "shortform", "verified"),
                        false
                ),
                new InfluencerVideo(
                        "yt-long-1",
                        "Nara Visual",
                        "nara_visual",
                        false,
                        PlatformType.YOUTUBE,
                        MediaType.LONGFORM,
                        PlaybackType.YOUTUBE_IFRAME,
                        "롱폼 브이로그 미리보기",
                        "롱폼도 같은 피드에 섞되, 레이아웃만 가로형으로 바꾸는 데모입니다.",
                        "https://www.youtube.com/@GoogleDevelopers",
                        "ScMzIvxBSi4",
                        null,
                        "https://i.ytimg.com/vi/ScMzIvxBSi4/hqdefault.jpg",
                        List.of("youtube", "longform"),
                        false
                ),
                new InfluencerVideo(
                        "ig-sim-1",
                        "Mellow Frame",
                        "mellow.frame",
                        true,
                        PlatformType.INSTAGRAM_SIMULATED,
                        MediaType.SHORTFORM,
                        PlaybackType.HTML5_VIDEO,
                        "인스타 릴스형 HTML5 재생 데모",
                        "공식 인스타 재생 API 대신, 시공간감과 UX를 먼저 검증하기 위한 시뮬레이션 카드입니다.",
                        "https://www.instagram.com/mellow.frame/",
                        null,
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                        "",
                        List.of("instagram", "simulated", "reels"),
                        true
                ),
                new InfluencerVideo(
                        "yt-short-2",
                        "Frame Motel",
                        "frame_motel",
                        true,
                        PlatformType.YOUTUBE,
                        MediaType.SHORTFORM,
                        PlaybackType.YOUTUBE_IFRAME,
                        "숏폼 상품 소개 포맷",
                        "활성 카드와 인접 카드만 플레이어를 마운트하도록 설계하면 웹뷰에서 체감 성능이 훨씬 좋아집니다.",
                        "https://www.youtube.com/@Google",
                        "aqz-KE-bpKQ",
                        null,
                        "https://i.ytimg.com/vi/aqz-KE-bpKQ/hqdefault.jpg",
                        List.of("youtube", "preload"),
                        false
                ),
                new InfluencerVideo(
                        "ig-sim-2",
                        "Rose Atlas",
                        "rose.atlas",
                        false,
                        PlatformType.INSTAGRAM_SIMULATED,
                        MediaType.SHORTFORM,
                        PlaybackType.HTML5_VIDEO,
                        "릴스 전환감 시뮬레이션",
                        "실제 인스타 미디어 연결 전 단계에서 세로 스와이프 UX를 검증할 때 사용하는 카드입니다.",
                        "https://www.instagram.com/rose.atlas/",
                        null,
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                        "",
                        List.of("instagram", "simulation"),
                        true
                ),
                new InfluencerVideo(
                        "yt-long-2",
                        "Orbit Daily",
                        "orbit_daily",
                        true,
                        PlatformType.YOUTUBE,
                        MediaType.LONGFORM,
                        PlaybackType.YOUTUBE_IFRAME,
                        "제품 후기 롱폼",
                        "영상 자체는 YouTube embed를 쓰되, 홈 피드에서는 Shorts와 같은 세로 탐색 리듬을 유지하는 예시입니다.",
                        "https://www.youtube.com/@YouTubeCreators",
                        "ysz5S6PUM-U",
                        null,
                        "https://i.ytimg.com/vi/ysz5S6PUM-U/hqdefault.jpg",
                        List.of("youtube", "review", "longform"),
                        false
                ),
                new InfluencerVideo(
                        "ig-sim-3",
                        "Luma Cabin",
                        "luma.cabin",
                        true,
                        PlatformType.INSTAGRAM_SIMULATED,
                        MediaType.SHORTFORM,
                        PlaybackType.HTML5_VIDEO,
                        "실계정 없이 테스트하는 릴스 카드",
                        "실제 인스타 인증 없이도 세로 피드 전환감과 자동 재생 체감을 확인할 수 있도록 추가한 카드입니다.",
                        "https://www.instagram.com/luma.cabin/",
                        null,
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                        "",
                        List.of("instagram", "simulation", "tester"),
                        true
                ),
                new InfluencerVideo(
                        "ig-sim-4",
                        "Mono Season",
                        "mono.season",
                        false,
                        PlatformType.INSTAGRAM_SIMULATED,
                        MediaType.SHORTFORM,
                        PlaybackType.HTML5_VIDEO,
                        "짧은 전환감 추가 샘플",
                        "숏츠 탭에서 연속 스크롤을 더 자연스럽게 체감할 수 있도록 추가한 짧은 카드입니다.",
                        "https://www.instagram.com/mono.season/",
                        null,
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                        "",
                        List.of("instagram", "simulation", "shorts"),
                        true
                )
        );
    }
}
