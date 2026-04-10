package com.appg.influencerdemo.youtubelogin.service;

import com.appg.influencerdemo.youtubelogin.domain.YouTubeChannelProfile;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeTokenBundle;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeVideo;
import java.util.List;

public interface YouTubeLoginClient {

    YouTubeTokenBundle exchangeAuthorizationCode(String code, String redirectUri);

    YouTubeTokenBundle refreshAccessToken(String refreshToken);

    YouTubeChannelProfile fetchMyChannel(String accessToken);

    List<YouTubeVideo> fetchUploads(String accessToken, String uploadsPlaylistId, int limit);
}
