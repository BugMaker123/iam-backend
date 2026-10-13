package com.gk.iam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String idToken;
    private Long expiresIn;
    private Map<String, Object> user;

    // 当发起方为 OIDC 授权码登录时返回
    private String authCode;
    private String redirectUrl;
    private String state;
}

