package com.appg.influencerdemo.feed.dto;

import com.appg.influencerdemo.feed.domain.MediaType;
import com.appg.influencerdemo.feed.domain.PlatformType;
import com.appg.influencerdemo.feed.domain.PlaybackType;
import java.util.List;

public record FeedItemResponse(
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
        String embedUrl,
        String videoUrl,
        String posterUrl,
        List<String> tags,
        boolean simulation
) {
}
