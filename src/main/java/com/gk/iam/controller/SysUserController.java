package com.gk.iam.controller;

import com.gk.iam.common.PageResult;
import com.gk.iam.common.Result;
import com.gk.iam.entity.SysUser;
import com.gk.iam.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @GetMapping
    public Result<PageResult<SysUser>> getUserPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String riskLevel) {

        return Result.success(userService.getUserPage(pageNum, pageSize, keyword, companyId, deptId, status, riskLevel));
    }

    @PostMapping
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        return Result.success("用户创建成功", userService.createUser(user));
    }

    @PutMapping
    public Result<?> updateUser(@RequestBody SysUser user) {
        boolean ok = userService.updateUser(user);
        return ok ? Result.success("用户更新成功", null) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        boolean ok = userService.deleteUser(id);
        return ok ? Result.success("用户删除成功", null) : Result.error("删除失败");
    }

    @PostMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String newPassword = body != null ? body.get("newPassword") : "admin";
        boolean ok = userService.resetPassword(id, newPassword);
        return ok ? Result.success("密码重置成功，新密码已生效", null) : Result.error("重置失败");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        boolean ok = userService.updateStatus(id, status);
        return ok ? Result.success("状态更新成功", null) : Result.error("更新失败");
    }
}

