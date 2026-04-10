package com.appg.influencerdemo.youtubelogin.domain;

import java.time.Instant;
import java.util.List;

public record YouTubeLoginFlowResult(
        String flowId,
        String origin,
        YouTubeLoginStatus status,
        boolean approved,
        String message,
        YouTubeChannelSnapshot channel,
        List<String> notes,
        Instant completedAt
) {
}
