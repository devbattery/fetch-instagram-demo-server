package com.appg.influencerdemo.feed.controller;

import com.appg.influencerdemo.common.api.ApiResponse;
import com.appg.influencerdemo.feed.domain.UserRole;
import com.appg.influencerdemo.feed.dto.FeedResponse;
import com.appg.influencerdemo.feed.service.FeedService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/feed")
    public ApiResponse<FeedResponse> getFeed(
            @RequestParam(defaultValue = "GENERAL_USER") String viewerRole,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return ApiResponse.success(feedService.getFeed(UserRole.from(viewerRole), limit));
    }
}
