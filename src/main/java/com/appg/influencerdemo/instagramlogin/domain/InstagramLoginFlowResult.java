package com.appg.influencerdemo.instagramlogin.domain;

import java.time.Instant;
import java.util.List;

public record InstagramLoginFlowResult(
        String flowId,
        String origin,
        InstagramLoginStatus status,
        boolean approved,
        String message,
        InstagramAccountSnapshot account,
        List<String> notes,
        Instant completedAt
) {
}
