package com.appg.influencerdemo.instagramlogin.service;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.common.exception.DemoErrorCode;
import com.appg.influencerdemo.instagramlogin.config.InstagramLoginProperties;
import com.appg.influencerdemo.instagramlogin.domain.InstagramProfile;
import com.appg.influencerdemo.instagramlogin.domain.InstagramReel;
import com.appg.influencerdemo.instagramlogin.domain.InstagramTokenBundle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MetaInstagramLoginClient implements InstagramLoginClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final InstagramLoginProperties properties;

    public MetaInstagramLoginClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            InstagramLoginProperties properties
    ) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public InstagramTokenBundle exchangeAuthorizationCode(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        JsonNode body = postForm(properties.getCodeExchangeUrl(), form, DemoErrorCode.INSTAGRAM_CODE_EXCHANGE_FAILED);
        return new InstagramTokenBundle(
                text(body, "access_token"),
                longValue(body, "expires_in").orElse(null),
                textOrFallback(body, List.of("user_id", "id"))
        );
    }

    @Override
    public InstagramTokenBundle exchangeForLongLivedToken(String shortLivedToken) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getLongLivedTokenUrl())
                .queryParam("grant_type", "ig_exchange_token")
                .queryParam("client_secret", properties.getClientSecret())
                .queryParam("access_token", shortLivedToken)
                .build(true)
                .toUri();

        JsonNode body = getJson(uri, DemoErrorCode.INSTAGRAM_LONG_LIVED_TOKEN_FAILED);
        return new InstagramTokenBundle(
                text(body, "access_token"),
                longValue(body, "expires_in").orElse(null),
                null
        );
    }

    @Override
    public InstagramTokenBundle refreshLongLivedToken(String longLivedToken) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getRefreshTokenUrl())
                .queryParam("grant_type", "ig_refresh_token")
                .queryParam("access_token", longLivedToken)
                .build(true)
                .toUri();

        JsonNode body = getJson(uri, DemoErrorCode.INSTAGRAM_TOKEN_REFRESH_FAILED);
        return new InstagramTokenBundle(
                text(body, "access_token"),
                longValue(body, "expires_in").orElse(null),
                null
        );
    }

    @Override
    public InstagramProfile fetchProfile(String accessToken, List<String> fields) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getGraphApiBaseUrl())
                .path("/me")
                .queryParam("fields", String.join(",", fields))
                .queryParam("access_token", accessToken)
                .build(true)
                .toUri();

        JsonNode body = getJson(uri, DemoErrorCode.INSTAGRAM_PROFILE_FETCH_FAILED);
        return new InstagramProfile(
                textOrFallback(body, List.of("user_id", "id")),
                text(body, "username"),
                textOrFallback(body, List.of("name", "username")),
                text(body, "profile_picture_url"),
                longValue(body, "followers_count").orElse(null),
                longValue(body, "media_count").orElse(null)
        );
    }

    @Override
    public List<InstagramReel> fetchMedia(String accessToken, List<String> fields, int limit) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getGraphApiBaseUrl())
                .path("/me/media")
                .queryParam("fields", String.join(",", fields))
                .queryParam("limit", limit)
                .queryParam("access_token", accessToken)
                .build(true)
                .toUri();

        JsonNode body = getJson(uri, DemoErrorCode.INSTAGRAM_MEDIA_FETCH_FAILED);
        List<InstagramReel> reels = new ArrayList<>();

        JsonNode data = body.path("data");
        if (!data.isArray()) {
            return reels;
        }

        data.forEach(node -> reels.add(new InstagramReel(
                textOrFallback(node, List.of("id", "media_id")),
                text(node, "media_type"),
                text(node, "media_product_type"),
                text(node, "caption"),
                text(node, "permalink"),
                text(node, "media_url"),
                textOrFallback(node, List.of("thumbnail_url", "media_url")),
                instant(node.path("timestamp").asText(null)).orElse(null)
        )));

        return reels;
    }

    private JsonNode postForm(String url, MultiValueMap<String, String> form, DemoErrorCode errorCode) {
        try {
            return objectMapper.readTree(
                    restClient.post()
                            .uri(url)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(form)
                            .retrieve()
                            .body(String.class)
            );
        } catch (Exception exception) {
            throw new BusinessException(errorCode);
        }
    }

    private JsonNode getJson(URI uri, DemoErrorCode errorCode) {
        try {
            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(response);
        } catch (RestClientException | java.io.IOException exception) {
            throw new BusinessException(errorCode);
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String textOrFallback(JsonNode node, List<String> fieldNames) {
        return fieldNames.stream()
                .map(fieldName -> text(node, fieldName))
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private Optional<Long> longValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(value.asLong());
    }

    private Optional<Instant> instant(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Instant.parse(rawValue));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }
}
