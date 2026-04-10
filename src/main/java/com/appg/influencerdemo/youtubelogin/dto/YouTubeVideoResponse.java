package com.appg.influencerdemo.youtubelogin.dto;

import java.time.Instant;

public record YouTubeVideoResponse(
        String videoId,
        String title,
        String description,
        String thumbnailUrl,
        String watchUrl,
        String embedUrl,
        Long durationSeconds,
        Integer widthPixels,
        Integer heightPixels,
        Instant publishedAt,
        boolean shortFormCandidate,
        boolean playable
) {
}
