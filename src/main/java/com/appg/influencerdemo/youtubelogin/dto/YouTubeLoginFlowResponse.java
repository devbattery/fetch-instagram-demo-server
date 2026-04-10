package com.appg.influencerdemo.youtubelogin.dto;

import com.appg.influencerdemo.youtubelogin.domain.YouTubeLoginStatus;
import java.time.Instant;
import java.util.List;

public record YouTubeLoginFlowResponse(
        String flowId,
        YouTubeLoginStatus status,
        boolean approved,
        String message,
        YouTubeAccountResponse account,
        List<YouTubeVideoResponse> videos,
        List<String> notes,
        Instant completedAt
) {
}
