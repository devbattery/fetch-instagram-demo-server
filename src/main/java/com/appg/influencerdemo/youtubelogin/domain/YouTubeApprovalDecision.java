package com.appg.influencerdemo.youtubelogin.domain;

public record YouTubeApprovalDecision(
        boolean approved,
        String message
) {
}
