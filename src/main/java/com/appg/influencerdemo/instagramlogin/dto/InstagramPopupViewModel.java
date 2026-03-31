package com.appg.influencerdemo.instagramlogin.dto;

public record InstagramPopupViewModel(
        String title,
        String description,
        String origin,
        InstagramPopupPayload payload
) {
}
