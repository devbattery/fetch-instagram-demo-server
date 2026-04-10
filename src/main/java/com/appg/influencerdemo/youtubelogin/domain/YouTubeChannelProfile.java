package com.appg.influencerdemo.youtubelogin.domain;

public record YouTubeChannelProfile(
        String channelId,
        String title,
        String handle,
        String thumbnailUrl,
        Long subscriberCount,
        Long videoCount,
        String uploadsPlaylistId
) {
}
