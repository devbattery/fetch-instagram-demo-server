package com.appg.influencerdemo.feed.domain;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.common.exception.DemoErrorCode;

public enum UserRole {
    GENERAL_USER,
    INFLUENCER;

    public static UserRole from(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return GENERAL_USER;
        }

        try {
            return UserRole.valueOf(rawRole.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(DemoErrorCode.INVALID_ROLE);
        }
    }
}
