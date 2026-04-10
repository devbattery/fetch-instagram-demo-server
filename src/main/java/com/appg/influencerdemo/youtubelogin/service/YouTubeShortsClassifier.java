package com.appg.influencerdemo.youtubelogin.service;

import org.springframework.stereotype.Component;

@Component
public class YouTubeShortsClassifier {

    /*
     * YouTube Data API는 Shorts 전용 플래그를 주지 않기 때문에,
     * 데모 단계에서는 누락을 줄이는 쪽을 우선해 3분 이하 영상이면 모두 Shorts 후보로 간주합니다.
     * 실제 서비스에서는 운영 정책에 따라 비율, 관리자 승인, 별도 분류 로직을 다시 붙이면 됩니다.
     */
    public boolean isShortsCandidate(
            Long durationSeconds,
            Integer widthPixels,
            Integer heightPixels,
            String rotation,
            int shortsMaxDurationSeconds
    ) {
        return durationSeconds != null && durationSeconds <= shortsMaxDurationSeconds;
    }
}
