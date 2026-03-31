package com.appg.influencerdemo.instagramlogin.controller;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.instagramlogin.dto.InstagramPopupViewModel;
import com.appg.influencerdemo.instagramlogin.dto.InstagramStartResponse;
import com.appg.influencerdemo.instagramlogin.service.InstagramLoginService;
import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Validated
@Controller
@RequestMapping("/demo/instagram-login")
public class InstagramLoginPageController {

    private final InstagramLoginService instagramLoginService;

    public InstagramLoginPageController(InstagramLoginService instagramLoginService) {
        this.instagramLoginService = instagramLoginService;
    }

    @GetMapping("/start")
    public RedirectView start(
            @RequestParam String origin,
            @RequestParam(required = false) @Min(0) Integer minFollowers,
            @RequestParam(required = false) @Min(0) Integer minMediaCount
    ) {
        InstagramStartResponse response = instagramLoginService.createAuthorizationRequest(
                origin,
                minFollowers,
                minMediaCount
        );
        return new RedirectView(response.authorizationUrl());
    }

    @GetMapping("/callback")
    public String callback(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_reason,
            @RequestParam(required = false) String error_description,
            Model model
    ) {
        if (state == null || state.isBlank()) {
            model.addAttribute("title", "Instagram 로그인 실패");
            model.addAttribute("description", "state 값이 없어 결과를 앱에 전달할 수 없습니다.");
            model.addAttribute("origin", "");
            model.addAttribute("payload", null);
            return "instagram-login-result";
        }

        try {
            InstagramPopupViewModel viewModel = instagramLoginService.completeAuthorization(
                    state,
                    code,
                    error,
                    error_reason,
                    error_description
            );
            model.addAttribute("title", viewModel.title());
            model.addAttribute("description", viewModel.description());
            model.addAttribute("origin", viewModel.origin());
            model.addAttribute("payload", viewModel.payload());
        } catch (BusinessException exception) {
            model.addAttribute("title", "Instagram 로그인 실패");
            model.addAttribute("description", exception.getMessage());
            model.addAttribute("origin", "");
            model.addAttribute("payload", null);
        }

        return "instagram-login-result";
    }
}
