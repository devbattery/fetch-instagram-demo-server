package com.appg.influencerdemo.youtubelogin.service;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.common.exception.DemoErrorCode;
import com.appg.influencerdemo.youtubelogin.config.YouTubeLoginProperties;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeChannelProfile;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeTokenBundle;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeVideo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GoogleYouTubeLoginClient implements YouTubeLoginClient {

    private static final int API_PAGE_SIZE = 50;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final YouTubeLoginProperties properties;
    private final YouTubeShortsClassifier shortsClassifier;

    public GoogleYouTubeLoginClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            YouTubeLoginProperties properties,
            YouTubeShortsClassifier shortsClassifier
    ) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.shortsClassifier = shortsClassifier;
    }

    @Override
    public YouTubeTokenBundle exchangeAuthorizationCode(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("code", code);
        form.add("grant_type", "authorization_code");
        form.add("redirect_uri", redirectUri);

        JsonNode body = postForm(properties.getTokenUrl(), form, DemoErrorCode.YOUTUBE_CODE_EXCHANGE_FAILED);
        return new YouTubeTokenBundle(
                text(body, "access_token"),
                text(body, "refresh_token"),
                longValue(body, "expires_in").orElse(null),
                text(body, "scope"),
                text(body, "token_type")
        );
    }

    @Override
    public YouTubeTokenBundle refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");

        JsonNode body = postForm(properties.getTokenUrl(), form, DemoErrorCode.YOUTUBE_TOKEN_REFRESH_FAILED);
        return new YouTubeTokenBundle(
                text(body, "access_token"),
                text(body, "refresh_token"),
                longValue(body, "expires_in").orElse(null),
                text(body, "scope"),
                text(body, "token_type")
        );
    }

    @Override
    public YouTubeChannelProfile fetchMyChannel(String accessToken) {
        URI uri = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl())
                .path("/channels")
                .queryParam("part", "snippet,statistics,contentDetails")
                .queryParam("mine", true)
                .build(true)
                .toUri();

        JsonNode body = getJson(uri, accessToken, DemoErrorCode.YOUTUBE_CHANNEL_FETCH_FAILED);
        JsonNode item = firstItem(body);

        return new YouTubeChannelProfile(
                text(item, "id"),
                text(item.path("snippet"), "title"),
                textOrFallback(item.path("snippet"), List.of("customUrl", "title")),
                thumbnailUrl(item.path("snippet").path("thumbnails")),
                longValue(item.path("statistics"), "subscriberCount").orElse(null),
                longValue(item.path("statistics"), "videoCount").orElse(null),
                text(item.path("contentDetails").path("relatedPlaylists"), "uploads")
        );
    }

    @Override
    public List<YouTubeVideo> fetchUploads(String accessToken, String uploadsPlaylistId, int limit) {
        List<String> videoIds = new ArrayList<>();
        Map<String, Integer> ordering = new HashMap<>();
        String nextPageToken = null;

        while (videoIds.size() < limit) {
            int pageSize = Math.min(limit - videoIds.size(), API_PAGE_SIZE);
            URI playlistUri = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl())
                    .path("/playlistItems")
                    .queryParam("part", "contentDetails")
                    .queryParam("playlistId", uploadsPlaylistId)
                    .queryParam("maxResults", pageSize)
                    .queryParamIfPresent("pageToken", Optional.ofNullable(nextPageToken))
                    .build(true)
                    .toUri();

            JsonNode playlistBody = getJson(playlistUri, accessToken, DemoErrorCode.YOUTUBE_VIDEO_FETCH_FAILED);
            JsonNode playlistItems = playlistBody.path("items");
            if (!playlistItems.isArray() || playlistItems.isEmpty()) {
                break;
            }

            for (JsonNode item : playlistItems) {
                String videoId = text(item.path("contentDetails"), "videoId");
                if (videoId == null || videoId.isBlank() || ordering.containsKey(videoId)) {
                    continue;
                }
                ordering.put(videoId, ordering.size());
                videoIds.add(videoId);
            }

            nextPageToken = text(playlistBody, "nextPageToken");
            if (nextPageToken == null || nextPageToken.isBlank()) {
                break;
            }
        }

        if (videoIds.isEmpty()) {
            return List.of();
        }

        List<YouTubeVideo> videos = new ArrayList<>();

        for (int startIndex = 0; startIndex < videoIds.size(); startIndex += API_PAGE_SIZE) {
            List<String> batchVideoIds = videoIds.subList(startIndex, Math.min(startIndex + API_PAGE_SIZE, videoIds.size()));
            URI videosUri = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl())
                    .path("/videos")
                    .queryParam("part", "snippet,contentDetails,status,fileDetails")
                    .queryParam("id", String.join(",", batchVideoIds))
                    .queryParam("maxResults", batchVideoIds.size())
                    .build(true)
                    .toUri();

            JsonNode videosBody = getJson(videosUri, accessToken, DemoErrorCode.YOUTUBE_VIDEO_FETCH_FAILED);
            JsonNode items = videosBody.path("items");
            if (!items.isArray()) {
                continue;
            }

            items.forEach(item -> {
                String videoId = text(item, "id");
                Long durationSeconds = durationSeconds(text(item.path("contentDetails"), "duration")).orElse(null);
                boolean publicVideo = "public".equalsIgnoreCase(text(item.path("status"), "privacyStatus"));
                boolean embeddable = booleanValue(item.path("status"), "embeddable").orElse(true);
                String liveBroadcastContent = text(item.path("snippet"), "liveBroadcastContent");
                boolean isLive = liveBroadcastContent != null && !"none".equalsIgnoreCase(liveBroadcastContent);
                JsonNode firstVideoStream = item.path("fileDetails").path("videoStreams").isArray()
                        && !item.path("fileDetails").path("videoStreams").isEmpty()
                        ? item.path("fileDetails").path("videoStreams").get(0)
                        : null;
                Integer widthPixels = firstVideoStream == null ? null : intValue(firstVideoStream, "widthPixels").orElse(null);
                Integer heightPixels = firstVideoStream == null ? null : intValue(firstVideoStream, "heightPixels").orElse(null);
                String rotation = firstVideoStream == null ? null : text(firstVideoStream, "rotation");
                boolean shortFormCandidate = shortsClassifier.isShortsCandidate(
                        durationSeconds,
                        widthPixels,
                        heightPixels,
                        rotation,
                        properties.getShortsMaxDurationSeconds()
                );

                videos.add(new YouTubeVideo(
                        videoId,
                        text(item.path("snippet"), "title"),
                        text(item.path("snippet"), "description"),
                        thumbnailUrl(item.path("snippet").path("thumbnails")),
                        watchUrl(videoId),
                        embedUrl(videoId),
                        durationSeconds,
                        widthPixels,
                        heightPixels,
                        instant(text(item.path("snippet"), "publishedAt")).orElse(null),
                        shortFormCandidate,
                        publicVideo && embeddable && !isLive
                ));
            });
        }

        return videos.stream()
                .sorted(Comparator.comparingInt(video -> ordering.getOrDefault(video.videoId(), Integer.MAX_VALUE)))
                .collect(Collectors.toList());
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

    private JsonNode getJson(URI uri, String accessToken, DemoErrorCode errorCode) {
        try {
            String response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(response);
        } catch (RestClientException | java.io.IOException exception) {
            throw new BusinessException(errorCode);
        }
    }

    private JsonNode firstItem(JsonNode body) {
        JsonNode items = body.path("items");
        if (!items.isArray() || items.isEmpty()) {
            throw new BusinessException(DemoErrorCode.YOUTUBE_CHANNEL_FETCH_FAILED);
        }
        return items.get(0);
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
        try {
            return Optional.of(Long.parseLong(value.asText()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Optional<Integer> intValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.asText()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Optional<Boolean> booleanValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(value.asBoolean());
    }

    private String thumbnailUrl(JsonNode thumbnails) {
        if (thumbnails == null || thumbnails.isMissingNode()) {
            return "";
        }

        return List.of("maxres", "standard", "high", "medium", "default").stream()
                .map(key -> thumbnails.path(key).path("url").asText(""))
                .filter(url -> !url.isBlank())
                .findFirst()
                .orElse("");
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

    private Optional<Long> durationSeconds(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Duration.parse(rawValue).toSeconds());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String watchUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    private String embedUrl(String videoId) {
        return "https://www.youtube.com/embed/" + videoId + "?enablejsapi=1&playsinline=1&rel=0";
    }
}
