package com.appg.influencerdemo.feed.domain;

import java.util.List;

public record InfluencerVideo(
        String id,
        String influencerName,
        String handle,
        boolean verified,
        PlatformType platformType,
        MediaType mediaType,
        PlaybackType playbackType,
        String title,
        String description,
        String profileUrl,
        String youtubeVideoId,
        String videoUrl,
        String posterUrl,
        List<String> tags,
        boolean simulation
) {
}
