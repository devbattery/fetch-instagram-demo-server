package com.appg.influencerdemo.youtubelogin.domain;

import java.time.Instant;
import java.util.List;

public record YouTubeChannelSnapshot(
        String channelId,
        String title,
        String handle,
        String thumbnailUrl,
        Long subscriberCount,
        Long videoCount,
        String uploadsPlaylistId,
        String accessToken,
        String refreshToken,
        Instant tokenExpiresAt,
        Instant syncedAt,
        int minSubscribersRequired,
        int minVideoCountRequired,
        List<YouTubeVideo> videos
) {
}
