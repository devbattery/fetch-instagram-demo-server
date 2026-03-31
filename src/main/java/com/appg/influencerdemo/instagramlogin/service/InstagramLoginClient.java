package com.appg.influencerdemo.instagramlogin.service;

import com.appg.influencerdemo.instagramlogin.domain.InstagramProfile;
import com.appg.influencerdemo.instagramlogin.domain.InstagramReel;
import com.appg.influencerdemo.instagramlogin.domain.InstagramTokenBundle;
import java.util.List;

public interface InstagramLoginClient {

    InstagramTokenBundle exchangeAuthorizationCode(String code, String redirectUri);

    InstagramTokenBundle exchangeForLongLivedToken(String shortLivedToken);

    InstagramTokenBundle refreshLongLivedToken(String longLivedToken);

    InstagramProfile fetchProfile(String accessToken, List<String> fields);

    List<InstagramReel> fetchMedia(String accessToken, List<String> fields, int limit);
}
