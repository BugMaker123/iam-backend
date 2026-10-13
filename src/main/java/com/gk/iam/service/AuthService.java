package com.gk.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gk.iam.config.CacheService;
import com.gk.iam.dto.CaptchaResponse;
import com.gk.iam.dto.LoginRequest;
import com.gk.iam.dto.LoginResponse;
import com.gk.iam.entity.IamAuthCode;
import com.gk.iam.entity.IamClientApp;
import com.gk.iam.entity.IamLoginLog;
import com.gk.iam.entity.SysUser;
import com.gk.iam.mapper.IamAuthCodeMapper;
import com.gk.iam.mapper.SysUserMapper;
import com.gk.iam.util.CaptchaUtil;
import com.gk.iam.util.IpUtil;
import com.gk.iam.util.JwtUtil;
import com.gk.iam.util.Md5Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private IamClientAppService clientAppService;

    @Autowired
    private IamAuthCodeMapper authCodeMapper;

    @Autowired
    private IamLoginLogService loginLogService;

    @Autowired
    private RiskControlService riskControlService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${iam.captcha.expiration:300}")
    private long captchaExpiration;

    @Value("${iam.captcha.width:130}")
    private int captchaWidth;

    @Value("${iam.captcha.height:48}")
    private int captchaHeight;

    @Value("${iam.captcha.char-length:4}")
    private int captchaLength;

    @Value("${iam.jwt.access-token-expiration:7200}")
    private long accessTokenExpiration;

    /**
     * 生成图形验证码
     */
    public CaptchaResponse generateCaptcha() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        CaptchaUtil.CaptchaImage captcha = CaptchaUtil.generateCaptcha(captchaWidth, captchaHeight, captchaLength);

        // 存入缓存系统 (Redis / 内存)
        cacheService.set("iam:captcha:" + captchaId, captcha.getCode().toLowerCase(), captchaExpiration);

        return CaptchaResponse.builder()
                .captchaId(captchaId)
                .captchaImage(captcha.getBase64DataUri())
                .build();
    }

    /**
     * 统一登录入口 (支持普通账号密码登录 与 OIDC 授权码重定向模式)
     */
    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        String ip = IpUtil.getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");
        String browser = IpUtil.getBrowser(userAgent);
        String os = IpUtil.getOperatingSystem(userAgent);
        String location = IpUtil.getLocation(ip);

        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String clientId = request.getClientId();

        // 1. 严格校验图形验证码
        String cachedCode = cacheService.get("iam:captcha:" + request.getCaptchaId());
        // 验证码单次消费，防止重放爆破
        cacheService.delete("iam:captcha:" + request.getCaptchaId());

        if (cachedCode == null || !cachedCode.equalsIgnoreCase(request.getCaptchaCode().trim())) {
            recordAndDispatchLog(username, null, ip, location, browser, os, userAgent, clientId, 0, "图形验证码错误或已失效");
            throw new RuntimeException("图形验证码错误或已过期，请点击图片刷新重试");
        }

        // 2. 账号查询
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            recordAndDispatchLog(username, null, ip, location, browser, os, userAgent, clientId, 0, "账号不存在");
            throw new RuntimeException("账户或密码错误");
        }

        // 3. 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            recordAndDispatchLog(username, user.getId(), ip, location, browser, os, userAgent, clientId, 0, "账号已停用");
            throw new RuntimeException("该账号已被管理员禁用，请联系技术人员");
        }
        if (user.getStatus() != null && user.getStatus() == 2) {
            recordAndDispatchLog(username, user.getId(), ip, location, browser, os, userAgent, clientId, 0, "账号已被风控锁定");
            throw new RuntimeException("该账号存在高危安全风险，已被风控系统锁定");
        }

        // 4. 密码校验 (采用 MD5)
        if (!Md5Util.matches(request.getPassword(), user.getPassword())) {
            recordAndDispatchLog(username, user.getId(), ip, location, browser, os, userAgent, clientId, 0, "密码错误");
            throw new RuntimeException("账户或密码错误");
        }

        // 5. 登录成功处理
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        // 记录登录审计日志并派发风控流事件
        IamLoginLog successLog = recordAndDispatchLog(username, user.getId(), ip, location, browser, os, userAgent, clientId, 1, "登录成功");

        // 6. 生成标准 JWT Access Token & Refresh Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole(), clientId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 7. 组装用户信息对象
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        userInfo.put("companyId", user.getCompanyId());
        userInfo.put("deptId", user.getDeptId());
        userInfo.put("riskLevel", user.getRiskLevel());

        LoginResponse.LoginResponseBuilder respBuilder = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiration)
                .user(userInfo);

        // 8. OIDC 授权码模式对接支持: 如果请求包含 clientId 且包含 redirectUri
        if (clientId != null && !clientId.trim().isEmpty() && request.getRedirectUri() != null) {
            IamClientApp clientApp = clientAppService.getByClientId(clientId);
            if (clientApp != null && clientApp.getStatus() == 1) {
                // 生成临时 OIDC 授权码 (有效期 10 分钟)
                String authCode = "ac_" + UUID.randomUUID().toString().replace("-", "");
                IamAuthCode codeEntity = IamAuthCode.builder()
                        .authCode(authCode)
                        .clientId(clientId)
                        .userId(user.getId())
                        .username(user.getUsername())
                        .redirectUri(request.getRedirectUri())
                        .scope(request.getScope() != null ? request.getScope() : "openid profile email")
                        .state(request.getState())
                        .nonce(request.getNonce())
                        .expiresAt(LocalDateTime.now().plusMinutes(10))
                        .used(0)
                        .createTime(LocalDateTime.now())
                        .build();
                authCodeMapper.insert(codeEntity);

                // 生成 OIDC ID Token
                String idToken = jwtUtil.generateIdToken(
                        user.getId(),
                        user.getUsername(),
                        user.getRealName(),
                        user.getEmail(),
                        user.getRole(),
                        clientId,
                        request.getNonce()
                );

                // 拼装跳转回调链接
                String separator = request.getRedirectUri().contains("?") ? "&" : "?";
                String redirectUrl = request.getRedirectUri() + separator + "code=" + authCode;
                if (request.getState() != null) {
                    redirectUrl += "&state=" + URLEncoder.encode(request.getState(), StandardCharsets.UTF_8);
                }

                respBuilder.authCode(authCode)
                        .idToken(idToken)
                        .redirectUrl(redirectUrl)
                        .state(request.getState());
            }
        }

        return respBuilder.build();
    }

    /**
     * 写入审计日志并触发异步风控流
     */
    private IamLoginLog recordAndDispatchLog(String username, Long userId, String ip, String location,
                                             String browser, String os, String userAgent, String clientId,
                                             int status, String message) {
        // 评估风控评分与标签
        RiskControlService.RiskAssessmentResult risk = riskControlService.evaluateLoginRisk(username, ip, userAgent, status == 1);

        IamLoginLog logEntity = IamLoginLog.builder()
                .username(username)
                .userId(userId)
                .ipAddress(ip)
                .location(location)
                .browser(browser)
                .os(os)
                .userAgent(userAgent)
                .clientId(clientId)
                .loginStatus(status)
                .errorMessage(message)
                .riskScore(risk.getRiskScore())
                .riskTags(risk.getRiskTags())
                .loginTime(LocalDateTime.now())
                .build();

        loginLogService.saveLog(logEntity);

        // 异步派发风控事件 (对接 Kafka/Flink/ES 链路)
        SysUser user = userId != null ? userMapper.selectById(userId) : null;
        riskControlService.dispatchRiskAuditEvent(logEntity, user);

        return logEntity;
    }
}

