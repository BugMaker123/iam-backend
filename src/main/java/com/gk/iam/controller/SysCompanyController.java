package com.gk.iam.controller;

import com.gk.iam.common.PageResult;
import com.gk.iam.common.Result;
import com.gk.iam.entity.SysCompany;
import com.gk.iam.service.SysCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies")
public class SysCompanyController {

    @Autowired
    private SysCompanyService companyService;

    @GetMapping
    public Result<PageResult<SysCompany>> getCompanyPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        return Result.success(companyService.getCompanyPage(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/all")
    public Result<List<SysCompany>> getAllActive() {
        return Result.success(companyService.getAllActiveCompanies());
    }

    @PostMapping
    public Result<SysCompany> createCompany(@RequestBody SysCompany company) {
        return Result.success("公司新增成功", companyService.saveCompany(company));
    }

    @PutMapping
    public Result<SysCompany> updateCompany(@RequestBody SysCompany company) {
        return Result.success("公司更新成功", companyService.saveCompany(company));
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteCompany(@PathVariable Long id) {
        boolean ok = companyService.deleteCompany(id);
        return ok ? Result.success("公司删除成功", null) : Result.error("删除失败");
    }
}

