package com.gk.iam.dto;

import lombok.Data;

@Data
public class TokenRequest {

    private String grant_type;
    private String code;
    private String client_id;
    private String client_secret;
    private String redirect_uri;
    private String refresh_token;

    // 兼容驼峰格式
    public String getGrantType() {
        return grant_type;
    }

    public void setGrantType(String grantType) {
        this.grant_type = grantType;
    }

    public String getClientId() {
        return client_id;
    }

    public void setClientId(String clientId) {
        this.client_id = clientId;
    }

    public String getClientSecret() {
        return client_secret;
    }

    public void setClientSecret(String clientSecret) {
        this.client_secret = clientSecret;
    }

    public String getRedirectUri() {
        return redirect_uri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirect_uri = redirectUri;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refresh_token = refreshToken;
    }
}

