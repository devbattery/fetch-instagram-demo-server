package com.appg.influencerdemo.instagramlogin.domain;

public record InstagramProfile(
        String accountId,
        String username,
        String displayName,
        String profilePictureUrl,
        Long followerCount,
        Long mediaCount
) {
}
