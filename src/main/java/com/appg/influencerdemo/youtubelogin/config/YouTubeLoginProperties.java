package com.appg.influencerdemo.youtubelogin.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.youtube-login")
public class YouTubeLoginProperties {

    private boolean enabled;
    private String clientId;
    private String clientSecret;
    private String redirectUri = "http://localhost:8086/demo/youtube-login/callback";
    private String authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth";
    private String tokenUrl = "https://oauth2.googleapis.com/token";
    private String apiBaseUrl = "https://www.googleapis.com/youtube/v3";
    private int defaultMinSubscribers = 1000;
    private int defaultMinVideoCount = 9;
    private int videoLimit = 50;
    private int shortsMaxDurationSeconds = 180;
    private List<String> scopes = new ArrayList<>(List.of("https://www.googleapis.com/auth/youtube.readonly"));

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
        return trim(clientId);
    }

    public void setClientId(String clientId) {
        this.clientId = trim(clientId);
    }

    public String getClientSecret() {
        return trim(clientSecret);
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = trim(clientSecret);
    }

    public String getRedirectUri() {
        return trim(redirectUri);
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = trim(redirectUri);
    }

    public String getAuthorizationUrl() {
        return trim(authorizationUrl);
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = trim(authorizationUrl);
    }

    public String getTokenUrl() {
        return trim(tokenUrl);
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = trim(tokenUrl);
    }

    public String getApiBaseUrl() {
        return trim(apiBaseUrl);
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = trim(apiBaseUrl);
    }

    public int getDefaultMinSubscribers() {
        return defaultMinSubscribers;
    }

    public void setDefaultMinSubscribers(int defaultMinSubscribers) {
        this.defaultMinSubscribers = defaultMinSubscribers;
    }

    public int getDefaultMinVideoCount() {
        return defaultMinVideoCount;
    }

    public void setDefaultMinVideoCount(int defaultMinVideoCount) {
        this.defaultMinVideoCount = defaultMinVideoCount;
    }

    public int getVideoLimit() {
        return videoLimit;
    }

    public void setVideoLimit(int videoLimit) {
        this.videoLimit = videoLimit;
    }

    public int getShortsMaxDurationSeconds() {
        return shortsMaxDurationSeconds;
    }

    public void setShortsMaxDurationSeconds(int shortsMaxDurationSeconds) {
        this.shortsMaxDurationSeconds = shortsMaxDurationSeconds;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
