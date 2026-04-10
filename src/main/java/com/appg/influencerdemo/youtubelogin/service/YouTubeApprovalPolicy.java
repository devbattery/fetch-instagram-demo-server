package com.appg.influencerdemo.youtubelogin.service;

import com.appg.influencerdemo.youtubelogin.domain.YouTubeApprovalDecision;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeChannelProfile;
import org.springframework.stereotype.Component;

@Component
public class YouTubeApprovalPolicy {

    public YouTubeApprovalDecision evaluate(
            YouTubeChannelProfile profile,
            int minSubscribers,
            int minVideoCount
    ) {
        long subscriberCount = profile.subscriberCount() == null ? 0L : profile.subscriberCount();
        long videoCount = profile.videoCount() == null ? 0L : profile.videoCount();

        boolean approved = subscriberCount >= minSubscribers && videoCount >= minVideoCount;

        String message = approved
                ? "YouTube 채널 인증이 완료되었습니다. 구독자 수와 업로드 수 기준을 통과했습니다."
                : "YouTube 채널은 확인됐지만 구독자 수 또는 업로드 수 기준을 아직 충족하지 못했습니다.";

        return new YouTubeApprovalDecision(approved, message);
    }
}
