package com.gk.iam.controller;

import com.gk.iam.common.Result;
import com.gk.iam.entity.SysDepartment;
import com.gk.iam.service.SysDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
public class SysDepartmentController {

    @Autowired
    private SysDepartmentService departmentService;

    @GetMapping("/tree")
    public Result<List<SysDepartmentService.DeptTreeNode>> getTree(
            @RequestParam(required = false) Long companyId) {
        return Result.success(departmentService.getDepartmentTree(companyId));
    }

    @GetMapping("/list")
    public Result<List<SysDepartment>> getFlatList() {
        return Result.success(departmentService.getAllDepartments());
    }

    @PostMapping
    public Result<SysDepartment> createDepartment(@RequestBody SysDepartment department) {
        return Result.success("部门创建成功", departmentService.saveDepartment(department));
    }

    @PutMapping
    public Result<SysDepartment> updateDepartment(@RequestBody SysDepartment department) {
        return Result.success("部门更新成功", departmentService.saveDepartment(department));
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteDepartment(@PathVariable Long id) {
        boolean ok = departmentService.deleteDepartment(id);
        return ok ? Result.success("部门删除成功", null) : Result.error("删除失败");
    }
}

