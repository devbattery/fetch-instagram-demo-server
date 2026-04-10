package com.appg.influencerdemo.youtubelogin.domain;

public record YouTubeTokenBundle(
        String accessToken,
        String refreshToken,
        Long expiresInSeconds,
        String scope,
        String tokenType
) {
}
