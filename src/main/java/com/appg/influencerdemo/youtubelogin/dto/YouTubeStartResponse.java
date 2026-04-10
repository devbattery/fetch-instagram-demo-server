package com.appg.influencerdemo.youtubelogin.dto;

public record YouTubeStartResponse(
        String flowId,
        String authorizationUrl
) {
}
