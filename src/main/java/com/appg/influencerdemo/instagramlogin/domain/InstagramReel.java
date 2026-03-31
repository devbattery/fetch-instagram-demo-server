package com.appg.influencerdemo.instagramlogin.domain;

import java.time.Instant;

public record InstagramReel(
        String mediaId,
        String mediaType,
        String mediaProductType,
        String caption,
        String permalink,
        String mediaUrl,
        String thumbnailUrl,
        Instant timestamp
) {

    public boolean isPlayableReel() {
        return mediaUrl != null
                && !mediaUrl.isBlank()
                && ("REELS".equalsIgnoreCase(mediaProductType)
                || "VIDEO".equalsIgnoreCase(mediaType)
                || (permalink != null && permalink.contains("/reel/")));
    }
}
