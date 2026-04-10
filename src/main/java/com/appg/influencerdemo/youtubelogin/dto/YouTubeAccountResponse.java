package com.appg.influencerdemo.youtubelogin.dto;

import java.time.Instant;

public record YouTubeAccountResponse(
        String channelId,
        String title,
        String handle,
        String thumbnailUrl,
        Long subscriberCount,
        Long videoCount,
        Instant tokenExpiresAt,
        Instant syncedAt,
        int minSubscribersRequired,
        int minVideoCountRequired,
        int playableShortsCount,
        int fetchedVideoCount
) {
}
