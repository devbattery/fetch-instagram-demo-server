package com.appg.influencerdemo.youtubelogin.dto;

public record YouTubePopupPayload(
        String type,
        String flowId,
        boolean success,
        String message
) {
}
