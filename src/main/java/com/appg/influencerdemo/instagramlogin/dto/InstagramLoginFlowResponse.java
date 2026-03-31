package com.appg.influencerdemo.instagramlogin.dto;

import com.appg.influencerdemo.instagramlogin.domain.InstagramLoginStatus;
import java.time.Instant;
import java.util.List;

public record InstagramLoginFlowResponse(
        String flowId,
        InstagramLoginStatus status,
        boolean approved,
        String message,
        InstagramAccountResponse account,
        List<InstagramReelResponse> reels,
        List<String> notes,
        Instant completedAt
) {
}
