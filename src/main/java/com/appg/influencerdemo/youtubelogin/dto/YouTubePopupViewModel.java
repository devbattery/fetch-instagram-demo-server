package com.appg.influencerdemo.youtubelogin.dto;

public record YouTubePopupViewModel(
        String title,
        String description,
        String origin,
        YouTubePopupPayload payload
) {
}
