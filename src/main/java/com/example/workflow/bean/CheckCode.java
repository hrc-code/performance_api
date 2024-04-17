package com.example.workflow.bean;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CheckCode {

    // 验证码字符
    private String code;
    // 过期时间
    private LocalDateTime expireTime;

    /**
     * @param code 验证码字符
     * @param expireTime 过期时间，单位秒
     */
    public CheckCode(String code, int expireTime) {
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expireTime);
    }

    public CheckCode(String code) {
        // 默认验证码 60 秒后过期
        this(code, 60);
    }

    // 是否过期
    public boolean isExpired() {
        return this.expireTime.isBefore(LocalDateTime.now());
    }
}

