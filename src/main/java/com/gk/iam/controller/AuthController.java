package com.gk.iam.controller;

import com.gk.iam.common.Result;
import com.gk.iam.common.ResultCode;
import com.gk.iam.dto.CaptchaResponse;
import com.gk.iam.dto.LoginRequest;
import com.gk.iam.dto.LoginResponse;
import com.gk.iam.entity.SysUser;
import com.gk.iam.service.AuthService;
import com.gk.iam.service.SysUserService;
import com.gk.iam.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    public Result<CaptchaResponse> getCaptcha() {
        return Result.success(authService.generateCaptcha());
    }

    /**
     * 统一登录接口
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        LoginResponse response = authService.login(request, servletRequest);
        return Result.success("登录成功", response);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole());
        data.put("companyId", user.getCompanyId());
        data.put("deptId", user.getDeptId());
        data.put("status", user.getStatus());
        data.put("riskLevel", user.getRiskLevel());

        return Result.success(data);
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("登出成功", null);
    }
}

