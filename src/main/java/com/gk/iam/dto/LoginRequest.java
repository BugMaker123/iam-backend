package com.gk.iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "登录账号不能为空")
    private String username;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    @NotBlank(message = "验证码凭证ID不能为空")
    private String captchaId;

    @NotBlank(message = "图形验证码不能为空")
    private String captchaCode;

    // OIDC 授权码重定向参数 (可选)
    private String clientId;
    private String redirectUri;
    private String responseType;
    private String state;
    private String nonce;
    private String scope;
}

