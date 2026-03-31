package com.appg.influencerdemo.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        OffsetDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", "요청이 성공했습니다.", data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "OK", message, data, OffsetDateTime.now());
    }

    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null, OffsetDateTime.now());
    }
}
