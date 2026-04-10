package com.appg.influencerdemo.youtubelogin.domain;

import java.time.Instant;

public record YouTubeLoginFlow(
        String flowId,
        String state,
        String origin,
        int minSubscribers,
        int minVideoCount,
        Instant createdAt
) {
}
