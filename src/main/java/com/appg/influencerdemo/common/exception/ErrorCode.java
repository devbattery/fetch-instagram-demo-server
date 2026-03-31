package com.appg.influencerdemo.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String code();

    String message();

    HttpStatus status();
}
