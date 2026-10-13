package com.gk.iam.controller;

import com.gk.iam.common.Result;
import com.gk.iam.service.RiskControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/risk")
public class RiskController {

    @Autowired
    private RiskControlService riskControlService;

    /**
     * 获取风控引擎架构及规则定义
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getRiskEngineStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("engineName", "宇宙中心 (GK IAM) Flink+Kafka+Drools 账户风控流式计算引擎");
        status.put("version", "v2.5-RealTime");
        status.put("pipelineStatus", "ACTIVE");
        status.put("kafkaTopic", "gk_iam_login_audit_events");
        status.put("flinkJobState", "RUNNING");
        status.put("esIndex", "gk_iam_risk_events_v1");

        List<Map<String, Object>> rules = new ArrayList<>();
        rules.add(createRule("R101", "滑动窗口短时连续密码试错 (CEP)", "10分钟内同账号失败超过3次判定为试错爆破", "高", 40));
        rules.add(createRule("R102", "异地网络与跨国代理访问异常", "登录IP属于非可信跨国网段或已知开放代理列表", "中", 35));
        rules.add(createRule("R103", "非标准UA自动化爬虫脚本特征", "User-Agent 携带 curl/python/postman 等特征", "中", 30));
        rules.add(createRule("R104", "不可能位移速度检测 (Impossible Travel)", "两次登录相距500公里以上且时间差小于1小时", "极高", 60));

        status.put("activeRules", rules);
        return Result.success(status);
    }

    private Map<String, Object> createRule(String code, String name, String desc, String severity, int weight) {
        Map<String, Object> r = new HashMap<>();
        r.put("ruleCode", code);
        r.put("ruleName", name);
        r.put("description", desc);
        r.put("severity", severity);
        r.put("riskWeight", weight);
        r.put("status", "ENABLED");
        return r;
    }

    /**
     * 在线风控计算仿真沙箱 (用于演示和规则验证)
     */
    @PostMapping("/simulate")
    public Result<RiskControlService.RiskAssessmentResult> simulateRisk(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.getOrDefault("username", "test_user");
        String ip = (String) payload.getOrDefault("ip", "198.51.100.12");
        String userAgent = (String) payload.getOrDefault("userAgent", "curl/7.88.1");
        boolean isSuccess = Boolean.TRUE.equals(payload.get("isSuccess"));

        RiskControlService.RiskAssessmentResult result = riskControlService.evaluateLoginRisk(username, ip, userAgent, isSuccess);
        return Result.success(result);
    }
}

