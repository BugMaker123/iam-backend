package com.gk.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gk.iam.common.PageResult;
import com.gk.iam.common.ResultCode;
import com.gk.iam.common.exception.BusinessException;
import com.gk.iam.entity.SysUser;
import com.gk.iam.mapper.SysUserMapper;
import com.gk.iam.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    public PageResult<SysUser> getUserPage(int pageNum, int pageSize, String keyword, Long companyId, Long deptId, Integer status, String riskLevel) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.and(q -> q.like(SysUser::getUsername, keyword.trim())
                    .or().like(SysUser::getRealName, keyword.trim())
                    .or().like(SysUser::getPhone, keyword.trim())
                    .or().like(SysUser::getEmail, keyword.trim()));
        }
        if (companyId != null && companyId > 0) {
            query.eq(SysUser::getCompanyId, companyId);
        }
        if (deptId != null && deptId > 0) {
            query.eq(SysUser::getDeptId, deptId);
        }
        if (status != null) {
            query.eq(SysUser::getStatus, status);
        }
        if (riskLevel != null && !riskLevel.trim().isEmpty()) {
            query.eq(SysUser::getRiskLevel, riskLevel.trim());
        }
        query.orderByDesc(SysUser::getId);

        Page<SysUser> result = userMapper.selectPage(page, query);
        // 清除敏感密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public SysUser getByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username.trim()));
    }

    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    public SysUser createUser(SysUser user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        // 校验唯一性
        SysUser exist = getByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        // 默认密码为 admin (MD5加密)
        String rawPassword = (user.getPassword() != null && !user.getPassword().trim().isEmpty())
                ? user.getPassword().trim()
                : "admin";
        user.setPassword(Md5Util.encode(rawPassword));

        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("USER");
        }
        if (user.getRiskLevel() == null || user.getRiskLevel().trim().isEmpty()) {
            user.setRiskLevel("LOW");
        }

        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    public boolean updateUser(SysUser user) {
        if (user.getId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        // 不允许直接在此接口改密码
        user.setPassword(null);
        return userMapper.updateById(user) > 0;
    }

    public boolean resetPassword(Long userId, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String pwd = (newPassword != null && !newPassword.trim().isEmpty()) ? newPassword.trim() : "admin";
        user.setPassword(Md5Util.encode(pwd));
        return userMapper.updateById(user) > 0;
    }

    public boolean updateStatus(Long userId, Integer status) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    public long getTotalCount() {
        return userMapper.selectCount(null);
    }
}

