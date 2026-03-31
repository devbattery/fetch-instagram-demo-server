package com.appg.influencerdemo.instagramlogin.service;

import com.appg.influencerdemo.instagramlogin.domain.InstagramApprovalDecision;
import com.appg.influencerdemo.instagramlogin.domain.InstagramProfile;
import org.springframework.stereotype.Component;

@Component
public class InstagramApprovalPolicy {

    public InstagramApprovalDecision evaluate(InstagramProfile profile, int minFollowers, int minMediaCount) {
        if (profile.followerCount() == null || profile.mediaCount() == null) {
            return InstagramApprovalDecision.rejected(
                    "OAuth 로그인은 성공했지만, 현재 권한/엔드포인트 조합에서는 팔로워 수 또는 미디어 수를 받지 못했습니다."
            );
        }

        if (profile.followerCount() < minFollowers) {
            return InstagramApprovalDecision.rejected(
                    "팔로워 수가 기준 미만입니다. "
                            + profile.followerCount() + " / " + minFollowers
            );
        }

        if (profile.mediaCount() < minMediaCount) {
            return InstagramApprovalDecision.rejected(
                    "게시 미디어 수가 기준 미만입니다. "
                            + profile.mediaCount() + " / " + minMediaCount
            );
        }

        return InstagramApprovalDecision.approved(
                "팔로워 수와 미디어 수 기준을 모두 통과했습니다."
        );
    }
}
