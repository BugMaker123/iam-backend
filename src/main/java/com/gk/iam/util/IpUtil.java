package com.gk.iam.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    };

    /**
     * 获取客户端真实IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 多级反向代理时，取第一个IP
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr != null ? remoteAddr : "127.0.0.1";
    }

    /**
     * 根据IP获取粗略地理位置标签
     */
    public static String getLocation(String ip) {
        if (ip == null || "127.0.0.1".equals(ip) || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.16.")) {
            return "中国·广东·深圳 (内网专线)";
        }
        if (ip.startsWith("113.") || ip.startsWith("183.")) {
            return "中国·广东·深圳";
        }
        if (ip.startsWith("198.") || ip.startsWith("192.")) {
            return "美国·加利福尼亚";
        }
        return "中国·北京";
    }

    /**
     * 解析浏览器信息
     */
    public static String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) return "Microsoft Edge";
        if (ua.contains("chrome/") && !ua.contains("edg/")) return "Google Chrome";
        if (ua.contains("safari/") && !ua.contains("chrome/")) return "Apple Safari";
        if (ua.contains("firefox/")) return "Mozilla Firefox";
        if (ua.contains("micromessenger")) return "WeChat Browser";
        if (ua.contains("curl")) return "cURL Client";
        if (ua.contains("postman")) return "PostmanRuntime";
        return "Standard Browser";
    }

    /**
     * 解析操作系统
     */
    public static String getOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows nt 10.0")) return "Windows 10/11";
        if (ua.contains("windows nt 6.3")) return "Windows 8.1";
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os x")) return "macOS";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        if (ua.contains("linux")) return "Linux";
        return "Unknown OS";
    }
}

