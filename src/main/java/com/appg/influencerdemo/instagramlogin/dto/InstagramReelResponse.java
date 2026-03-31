package com.appg.influencerdemo.instagramlogin.dto;

import java.time.Instant;

public record InstagramReelResponse(
        String mediaId,
        String mediaType,
        String mediaProductType,
        String caption,
        String permalink,
        String mediaUrl,
        String thumbnailUrl,
        Instant timestamp,
        boolean playable
) {
}
