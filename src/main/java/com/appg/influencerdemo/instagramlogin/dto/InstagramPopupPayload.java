package com.appg.influencerdemo.instagramlogin.dto;

public record InstagramPopupPayload(
        String type,
        String flowId,
        boolean success,
        String message
) {
}
