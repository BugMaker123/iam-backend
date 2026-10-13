package com.gk.iam.controller;

import com.gk.iam.common.PageResult;
import com.gk.iam.common.Result;
import com.gk.iam.entity.IamLoginLog;
import com.gk.iam.service.IamClientAppService;
import com.gk.iam.service.IamLoginLogService;
import com.gk.iam.service.SysCompanyService;
import com.gk.iam.service.SysDepartmentService;
import com.gk.iam.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class IamLoginLogController {

    @Autowired
    private IamLoginLogService loginLogService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private IamClientAppService clientAppService;

    @Autowired
    private SysCompanyService companyService;

    @Autowired
    private SysDepartmentService departmentService;

    /**
     * 登录审计日志分页查询
     */
    @GetMapping("/logs")
    public Result<PageResult<IamLoginLog>> getLogPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status) {

        return Result.success(loginLogService.getLogPage(pageNum, pageSize, username, ip, status));
    }

    /**
     * 后台综合监控与审计数据看板总览
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getOverviewStats() {
        Map<String, Object> stats = loginLogService.getOverviewStats();
        // 汇聚系统实体总数
        stats.put("totalUsers", userService.getTotalCount());
        stats.put("totalApps", clientAppService.getTotalCount());
        stats.put("totalCompanies", companyService.getTotalCount());
        stats.put("totalDepartments", departmentService.getTotalCount());

        return Result.success(stats);
    }
}

