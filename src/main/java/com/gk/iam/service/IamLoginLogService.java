package com.gk.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gk.iam.common.PageResult;
import com.gk.iam.entity.IamLoginLog;
import com.gk.iam.mapper.IamLoginLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class IamLoginLogService {

    @Autowired
    private IamLoginLogMapper loginLogMapper;

    public void saveLog(IamLoginLog loginLog) {
        if (loginLog.getLoginTime() == null) {
            loginLog.setLoginTime(LocalDateTime.now());
        }
        loginLogMapper.insert(loginLog);
    }

    public PageResult<IamLoginLog> getLogPage(int pageNum, int pageSize, String username, String ip, Integer status) {
        Page<IamLoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IamLoginLog> query = new LambdaQueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            query.like(IamLoginLog::getUsername, username.trim());
        }
        if (ip != null && !ip.trim().isEmpty()) {
            query.like(IamLoginLog::getIpAddress, ip.trim());
        }
        if (status != null) {
            query.eq(IamLoginLog::getLoginStatus, status);
        }
        query.orderByDesc(IamLoginLog::getLoginTime);

        Page<IamLoginLog> result = loginLogMapper.selectPage(page, query);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();

        // 今日时间区间
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 今日总登录数
        LambdaQueryWrapper<IamLoginLog> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.between(IamLoginLog::getLoginTime, startOfDay, endOfDay);
        Long todayTotal = loginLogMapper.selectCount(todayQuery);

        // 今日成功数
        LambdaQueryWrapper<IamLoginLog> todaySuccessQuery = new LambdaQueryWrapper<>();
        todaySuccessQuery.between(IamLoginLog::getLoginTime, startOfDay, endOfDay)
                .eq(IamLoginLog::getLoginStatus, 1);
        Long todaySuccess = loginLogMapper.selectCount(todaySuccessQuery);

        // 今日失败数
        Long todayFail = (todayTotal != null ? todayTotal : 0L) - (todaySuccess != null ? todaySuccess : 0L);

        // 成功率
        double successRate = todayTotal != null && todayTotal > 0
                ? Math.round(((double) todaySuccess / todayTotal) * 1000.0) / 10.0
                : 100.0;

        // 高危事件数 (riskScore >= 70)
        LambdaQueryWrapper<IamLoginLog> highRiskQuery = new LambdaQueryWrapper<>();
        highRiskQuery.ge(IamLoginLog::getRiskScore, 70);
        Long highRiskCount = loginLogMapper.selectCount(highRiskQuery);

        // 7天登录趋势
        List<Map<String, Object>> trends = null;
        try {
            trends = loginLogMapper.getLoginTrendsLast7Days();
        } catch (Exception e) {
            log.warn("查询7天趋势失败，回退为空集合: {}", e.getMessage());
            trends = Collections.emptyList();
        }

        stats.put("todayTotal", todayTotal != null ? todayTotal : 0);
        stats.put("todaySuccess", todaySuccess != null ? todaySuccess : 0);
        stats.put("todayFail", todayFail);
        stats.put("successRate", successRate);
        stats.put("highRiskCount", highRiskCount != null ? highRiskCount : 0);
        stats.put("sevenDaysTrends", trends != null ? trends : Collections.emptyList());

        return stats;
    }
}

