package com.appg.influencerdemo.verification.controller;

import com.appg.influencerdemo.verification.domain.VerificationDecision;
import com.appg.influencerdemo.verification.service.InstagramVerificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/demo/verification/instagram")
public class VerificationPageController {

    private final InstagramVerificationService instagramVerificationService;

    public VerificationPageController(InstagramVerificationService instagramVerificationService) {
        this.instagramVerificationService = instagramVerificationService;
    }

    @GetMapping("/start")
    public String startVerification(
            @RequestParam String handle,
            @RequestParam String redirectUri,
            Model model
    ) {
        String normalizedHandle = instagramVerificationService.normalizeHandle(handle);
        String normalizedRedirectUri = instagramVerificationService.validateRedirectUri(redirectUri);

        model.addAttribute("handle", normalizedHandle);
        model.addAttribute("redirectUri", normalizedRedirectUri);
        model.addAttribute("profileUrl", "https://www.instagram.com/" + normalizedHandle + "/");
        return "instagram-verification";
    }

    @GetMapping("/complete")
    public RedirectView completeVerification(
            @RequestParam String handle,
            @RequestParam String redirectUri,
            @RequestParam VerificationDecision decision
    ) {
        return new RedirectView(
                instagramVerificationService.buildCompletionRedirect(handle, redirectUri, decision)
        );
    }
}
