package com.appg.influencerdemo.instagramlogin.dto;

import java.time.Instant;

public record InstagramAccountResponse(
        String accountId,
        String username,
        String displayName,
        String profilePictureUrl,
        Long followerCount,
        Long mediaCount,
        Instant tokenExpiresAt,
        Instant syncedAt,
        int minFollowersRequired,
        int minMediaCountRequired,
        int playableReelCount
) {
}
