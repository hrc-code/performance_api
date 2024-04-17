package com.example.workflow.entity;

import lombok.Data;

//实体类
@Data
public class LoginDto {
    private String username; // 用户名或工号

    private String password; // 密码

    private String verifyCode;  // 图像验证码
}