package com.appg.influencerdemo.instagramlogin.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.instagram-login")
public class InstagramLoginProperties {

    private boolean enabled;
    private String clientId;
    private String clientSecret;
    private String redirectUri = "http://localhost:8086/demo/instagram-login/callback";
    private String authorizationUrl = "https://www.instagram.com/oauth/authorize";
    private String codeExchangeUrl = "https://api.instagram.com/oauth/access_token";
    private String longLivedTokenUrl = "https://graph.instagram.com/access_token";
    private String refreshTokenUrl = "https://graph.instagram.com/refresh_access_token";
    private String graphApiBaseUrl = "https://graph.instagram.com";
    private int defaultMinFollowers = 5000;
    private int defaultMinMediaCount = 9;
    private int mediaLimit = 12;
    private List<String> scopes = new ArrayList<>(List.of("instagram_business_basic"));
    private List<String> profileFields = new ArrayList<>(List.of(
            "user_id",
            "username",
            "name",
            "profile_picture_url",
            "followers_count",
            "media_count"
    ));
    private List<String> mediaFields = new ArrayList<>(List.of(
            "id",
            "caption",
            "media_type",
            "media_product_type",
            "media_url",
            "thumbnail_url",
            "permalink",
            "timestamp"
    ));

    public boolean isReady() {
        return enabled
                && hasText(clientId)
                && hasText(clientSecret)
                && hasText(redirectUri);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getCodeExchangeUrl() {
        return codeExchangeUrl;
    }

    public void setCodeExchangeUrl(String codeExchangeUrl) {
        this.codeExchangeUrl = codeExchangeUrl;
    }

    public String getLongLivedTokenUrl() {
        return longLivedTokenUrl;
    }

    public void setLongLivedTokenUrl(String longLivedTokenUrl) {
        this.longLivedTokenUrl = longLivedTokenUrl;
    }

    public String getRefreshTokenUrl() {
        return refreshTokenUrl;
    }

    public void setRefreshTokenUrl(String refreshTokenUrl) {
        this.refreshTokenUrl = refreshTokenUrl;
    }

    public String getGraphApiBaseUrl() {
        return graphApiBaseUrl;
    }

    public void setGraphApiBaseUrl(String graphApiBaseUrl) {
        this.graphApiBaseUrl = graphApiBaseUrl;
    }

    public int getDefaultMinFollowers() {
        return defaultMinFollowers;
    }

    public void setDefaultMinFollowers(int defaultMinFollowers) {
        this.defaultMinFollowers = defaultMinFollowers;
    }

    public int getDefaultMinMediaCount() {
        return defaultMinMediaCount;
    }

    public void setDefaultMinMediaCount(int defaultMinMediaCount) {
        this.defaultMinMediaCount = defaultMinMediaCount;
    }

    public int getMediaLimit() {
        return mediaLimit;
    }

    public void setMediaLimit(int mediaLimit) {
        this.mediaLimit = mediaLimit;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> getProfileFields() {
        return profileFields;
    }

    public void setProfileFields(List<String> profileFields) {
        this.profileFields = profileFields;
    }

    public List<String> getMediaFields() {
        return mediaFields;
    }

    public void setMediaFields(List<String> mediaFields) {
        this.mediaFields = mediaFields;
    }
}
