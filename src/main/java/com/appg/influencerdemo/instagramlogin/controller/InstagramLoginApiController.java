package com.appg.influencerdemo.instagramlogin.controller;

import com.appg.influencerdemo.common.api.ApiResponse;
import com.appg.influencerdemo.instagramlogin.dto.InstagramLoginConfigResponse;
import com.appg.influencerdemo.instagramlogin.dto.InstagramLoginFlowResponse;
import com.appg.influencerdemo.instagramlogin.service.InstagramLoginService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/demo/instagram-login")
public class InstagramLoginApiController {

    private final InstagramLoginService instagramLoginService;

    public InstagramLoginApiController(InstagramLoginService instagramLoginService) {
        this.instagramLoginService = instagramLoginService;
    }

    @GetMapping("/config")
    public ApiResponse<InstagramLoginConfigResponse> getConfig() {
        return ApiResponse.success(instagramLoginService.getConfig());
    }

    @GetMapping("/flows/{flowId}")
    public ApiResponse<InstagramLoginFlowResponse> getFlow(@PathVariable String flowId) {
        return ApiResponse.success(instagramLoginService.getFlowResult(flowId));
    }

    @GetMapping("/accounts/{accountId}/sync")
    public ApiResponse<InstagramLoginFlowResponse> syncAccount(@PathVariable String accountId) {
        return ApiResponse.success(
                "저장된 Instagram long-lived token 으로 다시 동기화했습니다.",
                instagramLoginService.syncAccount(accountId)
        );
    }
}
