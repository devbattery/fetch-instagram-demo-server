package com.appg.influencerdemo.youtubelogin.controller;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.youtubelogin.dto.YouTubePopupViewModel;
import com.appg.influencerdemo.youtubelogin.dto.YouTubeStartResponse;
import com.appg.influencerdemo.youtubelogin.service.YouTubeLoginService;
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
@RequestMapping("/demo/youtube-login")
public class YouTubeLoginPageController {

    private final YouTubeLoginService youTubeLoginService;

    public YouTubeLoginPageController(YouTubeLoginService youTubeLoginService) {
        this.youTubeLoginService = youTubeLoginService;
    }

    @GetMapping("/start")
    public RedirectView start(
            @RequestParam String origin,
            @RequestParam(required = false) @Min(0) Integer minSubscribers,
            @RequestParam(required = false) @Min(0) Integer minVideoCount
    ) {
        YouTubeStartResponse response = youTubeLoginService.createAuthorizationRequest(
                origin,
                minSubscribers,
                minVideoCount
        );
        return new RedirectView(response.authorizationUrl());
    }

    @GetMapping("/callback")
    public String callback(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            Model model
    ) {
        if (state == null || state.isBlank()) {
            model.addAttribute("title", "YouTube 로그인 실패");
            model.addAttribute("description", "state 값이 없어 결과를 앱에 전달할 수 없습니다.");
            model.addAttribute("origin", "");
            model.addAttribute("payload", null);
            return "youtube-login-result";
        }

        try {
            YouTubePopupViewModel viewModel = youTubeLoginService.completeAuthorization(
                    state,
                    code,
                    error,
                    error_description
            );
            model.addAttribute("title", viewModel.title());
            model.addAttribute("description", viewModel.description());
            model.addAttribute("origin", viewModel.origin());
            model.addAttribute("payload", viewModel.payload());
        } catch (BusinessException exception) {
            model.addAttribute("title", "YouTube 로그인 실패");
            model.addAttribute("description", exception.getMessage());
            model.addAttribute("origin", "");
            model.addAttribute("payload", null);
        }

        return "youtube-login-result";
    }
}
