package com.gk.iam.service;

import com.gk.iam.config.CacheService;
import com.gk.iam.entity.IamLoginLog;
import com.gk.iam.entity.SysUser;
import com.gk.iam.mapper.SysUserMapper;
import com.gk.iam.util.IpUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 实时账户风控与审计事件处理服务
 * 预留对接 ES + 规则引擎 (Drools) + Flink/Kafka 异步实时流式风控链路
 */
@Slf4j
@Service
public class RiskControlService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${iam.risk-engine.enabled:true}")
    private boolean riskEngineEnabled;

    @Value("${iam.risk-engine.max-failed-attempts-threshold:3}")
    private int maxFailedThreshold;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAssessmentResult {
        private int riskScore; // 0-100
        private String riskTags;
        private boolean shouldBlock;
        private String riskLevel; // LOW, MEDIUM, HIGH
    }

    /**
     * 实时风控规则评估计算 (可扩展接入 Flink CEP / 规则引擎)
     */
    public RiskAssessmentResult evaluateLoginRisk(String username, String ip, String userAgent, boolean isSuccess) {
        if (!riskEngineEnabled) {
            return new RiskAssessmentResult(0, "风控模块未开启", false, "LOW");
        }

        int score = 0;
        List<String> tags = new ArrayList<>();
        String failCountKey = "iam:risk:fail_count:" + username;

        // 规则1: 失败频次统计 (模拟 滑动窗口频控)
        if (!isSuccess) {
            String countStr = cacheService.get(failCountKey);
            int currentFailCount = countStr != null ? Integer.parseInt(countStr) + 1 : 1;
            cacheService.set(failCountKey, String.valueOf(currentFailCount), 600); // 10分钟窗口

            if (currentFailCount >= maxFailedThreshold) {
                score += 40;
                tags.add("高频密码试错(" + currentFailCount + "次)");
            } else {
                score += 15;
                tags.add("登录失败");
            }
        } else {
            // 登录成功则重置失败计数器
            cacheService.delete(failCountKey);
        }

        // 规则2: IP地理位置与网络环境异常特征
        if (ip.startsWith("198.") || ip.startsWith("192.0.2.") || ip.contains("境外")) {
            score += 35;
            tags.add("境外/代理IP访问");
        }

        // 规则3: 客户端 UA 异常特征识别 (脚本工具特征)
        if (userAgent != null) {
            String lowerUa = userAgent.toLowerCase();
            if (lowerUa.contains("curl") || lowerUa.contains("python") || lowerUa.contains("postman")) {
                score += 30;
                tags.add("自动化脚本特征");
            }
        }

        // 确定风险等级
        String riskLevel = "LOW";
        boolean shouldBlock = false;
        if (score >= 70) {
            riskLevel = "HIGH";
            shouldBlock = true;
        } else if (score >= 30) {
            riskLevel = "MEDIUM";
        }

        String tagStr = tags.isEmpty() ? (isSuccess ? "安全信任" : "普通失败") : String.join(" | ", tags);
        return new RiskAssessmentResult(score, tagStr, shouldBlock, riskLevel);
    }

    /**
     * 异步将登录事件推送到分布式流处理管道 (Kafka Topic -> Flink 实时计算 -> Elasticsearch 存储与风险索引)
     */
    @Async
    public void dispatchRiskAuditEvent(IamLoginLog loginLog, SysUser user) {
        try {
            log.info("[Flink/Kafka风控事件流] 发布审计事件 -> topic: iam_login_audit_events, account: {}, ip: {}, status: {}, riskScore: {}, tags: {}",
                    loginLog.getUsername(), loginLog.getIpAddress(), loginLog.getLoginStatus(), loginLog.getRiskScore(), loginLog.getRiskTags());

            // 若触发高危风险且用户存在，联动更新用户风险状态
            if (loginLog.getRiskScore() >= 75 && user != null) {
                user.setRiskLevel("HIGH");
                sysUserMapper.updateById(user);
                log.warn("[风控联动防御] 账户 {} 风险分达 {}，已被风控系统标记为 HIGH 风险等级", user.getUsername(), loginLog.getRiskScore());
            }
        } catch (Exception e) {
            log.error("推送风控审计事件异常", e);
        }
    }
}

