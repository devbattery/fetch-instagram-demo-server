package com.appg.influencerdemo.youtubelogin.controller;

import com.appg.influencerdemo.common.api.ApiResponse;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeLoginConfigResponse;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeLoginFlowResponse;
import com.appg.influencerdemo.youtubelogin.service.YouTubeLoginService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/youtube-login")
public class YouTubeLoginApiController {

    private final YouTubeLoginService youTubeLoginService;

    public YouTubeLoginApiController(YouTubeLoginService youTubeLoginService) {
        this.youTubeLoginService = youTubeLoginService;
    }

    @GetMapping("/config")
    public ApiResponse<YouTubeLoginConfigResponse> getConfig() {
        return ApiResponse.success(youTubeLoginService.getConfig());
    }

    @GetMapping("/flows/{flowId}")
    public ApiResponse<YouTubeLoginFlowResponse> getFlow(@PathVariable String flowId) {
        return ApiResponse.success(youTubeLoginService.getFlowResult(flowId));
    }

    @GetMapping("/channels/{channelId}/sync")
    public ApiResponse<YouTubeLoginFlowResponse> syncChannel(@PathVariable String channelId) {
        return ApiResponse.success(
                "저장된 YouTube OAuth 토큰으로 다시 동기화했습니다.",
                youTubeLoginService.syncChannel(channelId)
        );
    }
}
