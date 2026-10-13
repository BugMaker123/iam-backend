package com.gk.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gk.iam.common.ResultCode;
import com.gk.iam.common.exception.BusinessException;
import com.gk.iam.dto.OidcConfiguration;
import com.gk.iam.dto.TokenRequest;
import com.gk.iam.dto.TokenResponse;
import com.gk.iam.entity.IamAuthCode;
import com.gk.iam.entity.IamClientApp;
import com.gk.iam.entity.SysUser;
import com.gk.iam.mapper.IamAuthCodeMapper;
import com.gk.iam.mapper.SysUserMapper;
import com.gk.iam.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class OidcService {

    @Autowired
    private IamClientAppService clientAppService;

    @Autowired
    private IamAuthCodeMapper authCodeMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${iam.jwt.issuer:https://iam.sinopharm.com}")
    private String issuer;

    @Value("${server.port:8088}")
    private String serverPort;

    @Value("${iam.jwt.access-token-expiration:7200}")
    private long accessTokenExpiration;

    /**
     * OIDC 发现文档 (Well-Known Configuration)
     */
    public OidcConfiguration getOpenIdConfiguration(String requestBaseUrl) {
        String base = (requestBaseUrl != null && !requestBaseUrl.isEmpty())
                ? requestBaseUrl
                : "http://localhost:" + serverPort;

        return OidcConfiguration.builder()
                .issuer(issuer)
                .authorizationEndpoint(base + "/oauth2/authorize")
                .tokenEndpoint(base + "/oauth2/token")
                .userinfoEndpoint(base + "/oauth2/userinfo")
                .jwksUri(base + "/.well-known/jwks.json")
                .scopesSupported(Arrays.asList("openid", "profile", "email", "phone", "address"))
                .responseTypesSupported(Arrays.asList("code", "token", "id_token", "code id_token"))
                .grantTypesSupported(Arrays.asList("authorization_code", "refresh_token", "client_credentials"))
                .subjectTypesSupported(Collections.singletonList("public"))
                .idTokenSigningAlgValuesSupported(Arrays.asList("HS256", "RS256"))
                .tokenEndpointAuthMethodsSupported(Arrays.asList("client_secret_basic", "client_secret_post"))
                .claimsSupported(Arrays.asList("sub", "iss", "aud", "exp", "iat", "name", "preferred_username", "email", "role"))
                .build();
    }

    /**
     * JWKS 公钥配置
     */
    public Map<String, Object> getJwks() {
        Map<String, Object> jwks = new HashMap<>();
        List<Map<String, Object>> keys = new ArrayList<>();

        Map<String, Object> key = new HashMap<>();
        key.put("kty", "oct");
        key.put("use", "sig");
        key.put("alg", "HS256");
        key.put("kid", "gk-iam-hmac-key-1");
        keys.add(key);

        jwks.put("keys", keys);
        return jwks;
    }

    /**
     * OIDC / OAuth2 Token 端点处理: 兑换 Access Token & ID Token
     */
    public TokenResponse issueToken(TokenRequest request) {
        String grantType = request.getGrantType();
        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();

        if (clientId == null || clientId.trim().isEmpty()) {
            throw new BusinessException(ResultCode.CLIENT_NOT_FOUND);
        }

        // 校验客户端身份
        IamClientApp clientApp = clientAppService.getByClientId(clientId);
        if (clientApp == null || clientApp.getStatus() != 1) {
            throw new BusinessException(ResultCode.CLIENT_NOT_FOUND);
        }
        if (clientSecret != null && !clientSecret.equals(clientApp.getClientSecret())) {
            throw new BusinessException(ResultCode.CLIENT_SECRET_INVALID);
        }

        // 授权码模式 (authorization_code)
        if ("authorization_code".equalsIgnoreCase(grantType)) {
            String code = request.getCode();
            if (code == null || code.trim().isEmpty()) {
                throw new BusinessException(ResultCode.AUTH_CODE_INVALID);
            }

            IamAuthCode authCode = authCodeMapper.selectOne(new LambdaQueryWrapper<IamAuthCode>()
                    .eq(IamAuthCode::getAuthCode, code.trim()));

            if (authCode == null) {
                throw new BusinessException(ResultCode.AUTH_CODE_INVALID);
            }
            if (authCode.getUsed() != null && authCode.getUsed() == 1) {
                throw new BusinessException(ResultCode.AUTH_CODE_USED);
            }
            if (authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException("授权码已过期，请重新申请授权");
            }
            if (!authCode.getClientId().equals(clientId)) {
                throw new BusinessException("授权码与当前客户端不匹配");
            }

            // 标记单次使用
            authCode.setUsed(1);
            authCodeMapper.updateById(authCode);

            // 查询授权用户信息
            SysUser user = userMapper.selectById(authCode.getUserId());
            if (user == null || (user.getStatus() != null && user.getStatus() != 1)) {
                throw new BusinessException(ResultCode.USER_DISABLED);
            }

            // 签发 Token
            long tokenLifetime = clientApp.getTokenLifetime() != null ? clientApp.getTokenLifetime() : accessTokenExpiration;
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole(), clientId);
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
            String idToken = jwtUtil.generateIdToken(user.getId(), user.getUsername(), user.getRealName(), user.getEmail(), user.getRole(), clientId, authCode.getNonce());

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenLifetime)
                    .refreshToken(refreshToken)
                    .idToken(idToken)
                    .scope(authCode.getScope())
                    .build();
        }

        // 刷新令牌模式 (refresh_token)
        if ("refresh_token".equalsIgnoreCase(grantType)) {
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            SysUser user = userMapper.selectById(userId);
            if (user == null || (user.getStatus() != null && user.getStatus() != 1)) {
                throw new BusinessException(ResultCode.USER_DISABLED);
            }

            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole(), clientId);
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration)
                    .refreshToken(newRefreshToken)
                    .build();
        }

        throw new BusinessException(ResultCode.INVALID_GRANT_TYPE);
    }

    /**
     * OIDC UserInfo 端点: 通过 Bearer Access Token 获取用户标准化声明
     */
    public Map<String, Object> getUserInfo(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7).trim();
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Claims claims = jwtUtil.parseClaims(token);
        if (claims == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", String.valueOf(user.getId()));
        userInfo.put("name", user.getRealName());
        userInfo.put("preferred_username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone_number", user.getPhone());
        userInfo.put("role", user.getRole());
        userInfo.put("company_id", user.getCompanyId());
        userInfo.put("dept_id", user.getDeptId());
        userInfo.put("risk_level", user.getRiskLevel());
        userInfo.put("updated_at", System.currentTimeMillis() / 1000);

        return userInfo;
    }
}

