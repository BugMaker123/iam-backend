package com.gk.iam.controller;

import com.gk.iam.common.PageResult;
import com.gk.iam.common.Result;
import com.gk.iam.entity.IamClientApp;
import com.gk.iam.service.IamClientAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/apps")
public class IamClientAppController {

    @Autowired
    private IamClientAppService clientAppService;

    @GetMapping
    public Result<PageResult<IamClientApp>> getAppPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        return Result.success(clientAppService.getAppPage(pageNum, pageSize, keyword, status));
    }

    @PostMapping
    public Result<IamClientApp> createApp(@RequestBody IamClientApp app) {
        return Result.success("应用接入配置创建成功", clientAppService.saveApp(app));
    }

    @PutMapping
    public Result<?> updateApp(@RequestBody IamClientApp app) {
        boolean ok = clientAppService.updateApp(app);
        return ok ? Result.success("应用接入配置更新成功", null) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteApp(@PathVariable Long id) {
        boolean ok = clientAppService.deleteApp(id);
        return ok ? Result.success("应用删除成功", null) : Result.error("删除失败");
    }

    @PostMapping("/{id}/reset-secret")
    public Result<Map<String, String>> resetSecret(@PathVariable Long id) {
        String newSecret = clientAppService.resetSecret(id);
        return Result.success("应用密钥已重新生成", Collections.singletonMap("clientSecret", newSecret));
    }
}

