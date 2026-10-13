package com.gk.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gk.iam.common.PageResult;
import com.gk.iam.common.ResultCode;
import com.gk.iam.common.exception.BusinessException;
import com.gk.iam.entity.SysCompany;
import com.gk.iam.mapper.SysCompanyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysCompanyService {

    @Autowired
    private SysCompanyMapper companyMapper;

    public PageResult<SysCompany> getCompanyPage(int pageNum, int pageSize, String keyword, Integer status) {
        Page<SysCompany> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysCompany> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.and(q -> q.like(SysCompany::getCompanyName, keyword.trim())
                    .or().like(SysCompany::getCompanyCode, keyword.trim()));
        }
        if (status != null) {
            query.eq(SysCompany::getStatus, status);
        }
        query.orderByAsc(SysCompany::getSortOrder).orderByDesc(SysCompany::getId);

        Page<SysCompany> result = companyMapper.selectPage(page, query);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public List<SysCompany> getAllActiveCompanies() {
        return companyMapper.selectList(new LambdaQueryWrapper<SysCompany>()
                .eq(SysCompany::getStatus, 1)
                .orderByAsc(SysCompany::getSortOrder));
    }

    public SysCompany saveCompany(SysCompany company) {
        // 校验编码唯一性
        if (company.getCompanyCode() != null) {
            LambdaQueryWrapper<SysCompany> query = new LambdaQueryWrapper<SysCompany>()
                    .eq(SysCompany::getCompanyCode, company.getCompanyCode());
            if (company.getId() != null) {
                query.ne(SysCompany::getId, company.getId());
            }
            if (companyMapper.selectCount(query) > 0) {
                throw new BusinessException("公司编码已被使用");
            }
        }
        if (company.getId() == null) {
            companyMapper.insert(company);
        } else {
            companyMapper.updateById(company);
        }
        return company;
    }

    public boolean deleteCompany(Long id) {
        return companyMapper.deleteById(id) > 0;
    }

    public SysCompany getById(Long id) {
        return companyMapper.selectById(id);
    }

    public long getTotalCount() {
        return companyMapper.selectCount(null);
    }
}

