package com.gk.iam.controller;

import com.gk.iam.dto.OidcConfiguration;
import com.gk.iam.dto.TokenRequest;
import com.gk.iam.dto.TokenResponse;
import com.gk.iam.entity.IamClientApp;
import com.gk.iam.service.IamClientAppService;
import com.gk.iam.service.OidcService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class OidcController {

    @Autowired
    private OidcService oidcService;

    @Autowired
    private IamClientAppService clientAppService;

    /**
     * OIDC 发现文档规范端点
     */
    @GetMapping("/.well-known/openid-configuration")
    public OidcConfiguration getConfiguration(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String baseUrl = scheme + "://" + serverName + (serverPort == 80 || serverPort == 443 ? "" : ":" + serverPort);
        return oidcService.getOpenIdConfiguration(baseUrl);
    }

    /**
     * JWKS 公钥元数据
     */
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        return oidcService.getJwks();
    }

    /**
     * OIDC / OAuth2 授权端点 (Authorization Endpoint)
     * 业务应用重定向至此端点发起统一登录授权，未登录时重定向至前端统一登录界面
     */
    @GetMapping("/oauth2/authorize")
    public void authorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "response_type", defaultValue = "code") String responseType,
            @RequestParam(value = "scope", defaultValue = "openid profile") String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "nonce", required = false) String nonce,
            HttpServletResponse response) throws IOException {

        // 校验应用是否存在
        IamClientApp app = clientAppService.getByClientId(clientId);
        if (app == null || app.getStatus() != 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid client_id: application not registered or disabled");
            return;
        }

        // 重定向至前端统一单点登录UI页面 (附带授权参数)
        StringBuilder redirectLoginUrl = new StringBuilder("/login?");
        redirectLoginUrl.append("client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
        redirectLoginUrl.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        redirectLoginUrl.append("&response_type=").append(URLEncoder.encode(responseType, StandardCharsets.UTF_8));
        redirectLoginUrl.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));
        if (state != null) {
            redirectLoginUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        if (nonce != null) {
            redirectLoginUrl.append("&nonce=").append(URLEncoder.encode(nonce, StandardCharsets.UTF_8));
        }

        // 前端运行于 5173 或相对路径
        response.sendRedirect("http://localhost:5173" + redirectLoginUrl.toString());
    }

    /**
     * OIDC / OAuth2 Token 端点 (Token Endpoint)
     * 支持以 JSON 或 application/x-www-form-urlencoded 传参
     */
    @PostMapping(value = "/oauth2/token")
    public ResponseEntity<TokenResponse> token(@ModelAttribute TokenRequest formRequest,
                                               @RequestBody(required = false) TokenRequest bodyRequest) {
        TokenRequest req = bodyRequest != null ? bodyRequest : formRequest;
        TokenResponse tokenResponse = oidcService.issueToken(req);
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * OIDC UserInfo 端点 (UserInfo Endpoint)
     */
    @RequestMapping(value = "/oauth2/userinfo", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> userinfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> userInfo = oidcService.getUserInfo(authHeader);
        return ResponseEntity.ok(userInfo);
    }
}

