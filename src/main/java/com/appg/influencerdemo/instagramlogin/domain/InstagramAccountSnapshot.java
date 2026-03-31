package com.appg.influencerdemo.instagramlogin.domain;

import java.time.Instant;
import java.util.List;

public record InstagramAccountSnapshot(
        String accountId,
        String username,
        String displayName,
        String profilePictureUrl,
        Long followerCount,
        Long mediaCount,
        String accessToken,
        Instant tokenExpiresAt,
        Instant syncedAt,
        int minFollowersRequired,
        int minMediaCountRequired,
        List<InstagramReel> reels
) {
}
