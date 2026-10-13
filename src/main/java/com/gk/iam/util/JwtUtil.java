package com.gk.iam.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${iam.jwt.secret}")
    private String secret;

    @Value("${iam.jwt.issuer}")
    private String issuer;

    @Value("${iam.jwt.access-token-expiration:7200}")
    private long accessTokenExpiration;

    @Value("${iam.jwt.refresh-token-expiration:604800}")
    private long refreshTokenExpiration;

    @Value("${iam.jwt.id-token-expiration:7200}")
    private long idTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成通用访问令牌 Access Token (JWT)
     */
    public String generateAccessToken(Long userId, String username, String role, String clientId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        if (clientId != null) {
            claims.put("client_id", clientId);
        }

        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenExpiration * 1000);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新令牌 Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTokenExpiration * 1000);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("token_type", "refresh_token")
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成符合 OpenID Connect 核心规范的 ID Token (JWT)
     */
    public String generateIdToken(Long userId, String username, String realName, String email,
                                  String role, String clientId, String nonce) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + idTokenExpiration * 1000);

        var builder = Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .audience().add(clientId != null ? clientId : "gk-default-client").and()
                .claim("preferred_username", username)
                .claim("name", realName)
                .claim("email", email)
                .claim("role", role)
                .claim("auth_time", now.getTime() / 1000)
                .issuedAt(now)
                .expiration(exp);

        if (nonce != null && !nonce.isEmpty()) {
            builder.claim("nonce", nonce);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    /**
     * 解析 JWT Claims
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("解析JWT令牌异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return false;
        }
        return claims.getExpiration().after(new Date());
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) return null;
        try {
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            Object userId = claims.get("userId");
            return userId != null ? Long.valueOf(userId.toString()) : null;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) return null;
        Object username = claims.get("username");
        if (username != null) return username.toString();
        Object pref = claims.get("preferred_username");
        return pref != null ? pref.toString() : null;
    }
}

