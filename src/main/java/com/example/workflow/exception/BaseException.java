package com.example.workflow.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    // 错误状态码
    private final String code;
    // 错误消息
    private final String message;

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
