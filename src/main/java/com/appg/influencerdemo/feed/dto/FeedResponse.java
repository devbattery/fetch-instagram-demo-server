package com.appg.influencerdemo.feed.dto;

import com.appg.influencerdemo.feed.domain.UserRole;
import java.util.List;

public record FeedResponse(
        UserRole viewerRole,
        boolean canLaunchInstagramVerification,
        boolean instagramAutomaticBadgeCheckSupported,
        List<String> deliveryNotes,
        List<FeedItemResponse> items
) {
}
