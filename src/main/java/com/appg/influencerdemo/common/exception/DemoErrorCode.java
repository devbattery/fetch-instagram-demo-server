package com.appg.influencerdemo.common.exception;

import org.springframework.http.HttpStatus;

public enum DemoErrorCode implements ErrorCode {
    INVALID_ROLE("DEMO-400-ROLE", "지원하지 않는 역할입니다.", HttpStatus.BAD_REQUEST),
    INVALID_LIMIT("DEMO-400-LIMIT", "limit 값은 1 이상 20 이하만 허용됩니다.", HttpStatus.BAD_REQUEST),
    INVALID_HANDLE("DEMO-400-HANDLE", "인스타그램 핸들 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REDIRECT_URI("DEMO-400-REDIRECT", "허용되지 않은 redirectUri 입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORIGIN("DEMO-400-ORIGIN", "허용되지 않은 origin 입니다.", HttpStatus.BAD_REQUEST),
    INVALID_OAUTH_THRESHOLD("DEMO-400-OAUTH-THRESHOLD", "팔로워 수와 미디어 수 기준은 0 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    INSTAGRAM_LOGIN_NOT_READY("DEMO-503-IG-CONFIG", "Instagram Login 설정이 아직 준비되지 않았습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_INSTAGRAM_OAUTH_STATE("DEMO-400-IG-STATE", "Instagram OAuth state 가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    INSTAGRAM_FLOW_NOT_FOUND("DEMO-404-IG-FLOW", "Instagram 로그인 결과를 찾지 못했습니다.", HttpStatus.NOT_FOUND),
    INSTAGRAM_ACCOUNT_NOT_FOUND("DEMO-404-IG-ACCOUNT", "저장된 Instagram 계정을 찾지 못했습니다.", HttpStatus.NOT_FOUND),
    INSTAGRAM_CODE_EXCHANGE_FAILED("DEMO-502-IG-CODE", "Instagram authorization code 교환에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    INSTAGRAM_LONG_LIVED_TOKEN_FAILED("DEMO-502-IG-LONG", "Instagram long-lived token 발급에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    INSTAGRAM_TOKEN_REFRESH_FAILED("DEMO-502-IG-REFRESH", "Instagram long-lived token refresh 에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    INSTAGRAM_PROFILE_FETCH_FAILED("DEMO-502-IG-PROFILE", "Instagram 계정 정보 조회에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    INSTAGRAM_MEDIA_FETCH_FAILED("DEMO-502-IG-MEDIA", "Instagram 릴스/미디어 조회에 실패했습니다.", HttpStatus.BAD_GATEWAY);

    private final String code;
    private final String message;
    private final HttpStatus status;

    DemoErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
