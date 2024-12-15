package com.example.workflow.content.session;

import java.util.HashMap;
import java.util.Map;

/**
 * 存放验证码
 */
public class VerifyCodeSessionContent {
    private static final Map<String, Object> data = new HashMap<>();

    public static Object getVerifyCode(String key) {
        return data.get(key);
    }

    public static void setVerifyCode(String key, Object value) {
        data.put(key, value);
    }

    public static void clearVerifyCode(String key) {
        data.remove(key);
    }
}
