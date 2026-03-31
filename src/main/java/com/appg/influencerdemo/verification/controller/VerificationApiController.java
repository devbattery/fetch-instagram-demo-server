package com.appg.influencerdemo.verification.controller;

import com.appg.influencerdemo.common.api.ApiResponse;
import com.appg.influencerdemo.verification.dto.VerificationLaunchResponse;
import com.appg.influencerdemo.verification.service.InstagramVerificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo/verification")
public class VerificationApiController {

    private final InstagramVerificationService instagramVerificationService;

    public VerificationApiController(InstagramVerificationService instagramVerificationService) {
        this.instagramVerificationService = instagramVerificationService;
    }

    @GetMapping("/instagram/launch")
    public ApiResponse<VerificationLaunchResponse> getLaunchInfo(
            @RequestParam String handle,
            @RequestParam(defaultValue = "http://localhost:5173/verification/callback") String redirectUri
    ) {
        return ApiResponse.success(
                instagramVerificationService.buildLaunchResponse(handle, redirectUri)
        );
    }
}
