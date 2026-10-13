package com.gk.iam.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数请求错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "权限不足，拒绝访问"),
    NOT_FOUND(404, "请求资源未找到"),
    INTERNAL_SERVER_ERROR(500, "系统内部服务器异常"),

    // 业务错误码 (1000+)
    CAPTCHA_ERROR(1001, "图形验证码错误或已过期"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "账户或密码错误"),
    USER_DISABLED(1004, "账号已被禁用或停用"),
    USER_LOCKED(1005, "账号已被风控锁定，请联系管理员"),
    USER_EXISTS(1006, "用户登录名已存在"),
    CLIENT_NOT_FOUND(1007, "OIDC接入客户端不存在或已下线"),
    CLIENT_SECRET_INVALID(1008, "客户端密钥校验失败"),
    REDIRECT_URI_MISMATCH(1009, "回调地址redirect_uri不在接入白名单中"),
    AUTH_CODE_INVALID(1010, "授权码不存在或已失效"),
    AUTH_CODE_USED(1011, "授权码已被使用，禁止二次兑换"),
    INVALID_GRANT_TYPE(1012, "不支持的授权模式"),
    TOKEN_INVALID(1013, "Token令牌无效或已过期");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

