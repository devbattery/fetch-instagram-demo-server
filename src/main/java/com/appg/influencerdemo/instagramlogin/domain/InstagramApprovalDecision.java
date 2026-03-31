package com.appg.influencerdemo.instagramlogin.domain;

public record InstagramApprovalDecision(
        boolean approved,
        String message
) {

    public static InstagramApprovalDecision approved(String message) {
        return new InstagramApprovalDecision(true, message);
    }

    public static InstagramApprovalDecision rejected(String message) {
        return new InstagramApprovalDecision(false, message);
    }
}
