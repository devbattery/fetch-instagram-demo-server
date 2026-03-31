package com.appg.influencerdemo.instagramlogin.dto;

public record InstagramStartResponse(
        String flowId,
        String authorizationUrl
) {
}
