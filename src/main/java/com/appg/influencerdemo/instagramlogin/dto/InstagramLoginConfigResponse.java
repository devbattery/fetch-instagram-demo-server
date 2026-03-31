package com.appg.influencerdemo.instagramlogin.dto;

import java.util.List;

public record InstagramLoginConfigResponse(
        boolean enabled,
        boolean ready,
        String redirectUri,
        int defaultMinFollowers,
        int defaultMinMediaCount,
        List<String> scopes,
        String note
) {
}
