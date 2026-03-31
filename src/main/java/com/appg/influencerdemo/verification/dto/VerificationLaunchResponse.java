package com.appg.influencerdemo.verification.dto;

public record VerificationLaunchResponse(
        String handle,
        String profileUrl,
        String browserStartUrl,
        String nativeStartUrl,
        boolean automaticBadgeCheckSupported,
        String limitationNote
) {
}
