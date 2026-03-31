package com.appg.influencerdemo.instagramlogin.domain;

import java.time.Instant;

public record InstagramLoginFlow(
        String flowId,
        String state,
        String origin,
        int minFollowers,
        int minMediaCount,
        Instant createdAt
) {
}
