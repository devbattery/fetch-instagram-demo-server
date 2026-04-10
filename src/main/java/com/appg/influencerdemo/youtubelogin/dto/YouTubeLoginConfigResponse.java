package com.appg.influencerdemo.youtubelogin.dto;

import java.util.List;

public record YouTubeLoginConfigResponse(
        boolean enabled,
        boolean ready,
        String redirectUri,
        int defaultMinSubscribers,
        int defaultMinVideoCount,
        List<String> scopes,
        String note
) {
}
