package com.appg.influencerdemo.instagramlogin.domain;

public record InstagramTokenBundle(
        String accessToken,
        Long expiresInSeconds,
        String appScopedUserId
) {
}
