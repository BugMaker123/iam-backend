package com.gk.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gk.iam.common.PageResult;
import com.gk.iam.common.ResultCode;
import com.gk.iam.common.exception.BusinessException;
import com.gk.iam.entity.IamClientApp;
import com.gk.iam.mapper.IamClientAppMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IamClientAppService {

    @Autowired
    private IamClientAppMapper clientAppMapper;

    public PageResult<IamClientApp> getAppPage(int pageNum, int pageSize, String keyword, Integer status) {
        Page<IamClientApp> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IamClientApp> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.and(q -> q.like(IamClientApp::getAppName, keyword.trim())
                    .or().like(IamClientApp::getClientId, keyword.trim()));
        }
        if (status != null) {
            query.eq(IamClientApp::getStatus, status);
        }
        query.orderByDesc(IamClientApp::getId);

        Page<IamClientApp> result = clientAppMapper.selectPage(page, query);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public IamClientApp getByClientId(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            return null;
        }
        return clientAppMapper.selectOne(new LambdaQueryWrapper<IamClientApp>()
                .eq(IamClientApp::getClientId, clientId.trim()));
    }

    public IamClientApp saveApp(IamClientApp app) {
        if (app.getClientId() == null || app.getClientId().trim().isEmpty()) {
            app.setClientId("app_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        } else {
            // 校验唯一性
            IamClientApp exist = getByClientId(app.getClientId());
            if (exist != null && !exist.getId().equals(app.getId())) {
                throw new BusinessException("该应用 Client ID 已被占用");
            }
        }

        if (app.getClientSecret() == null || app.getClientSecret().trim().isEmpty()) {
            app.setClientSecret("sec_" + UUID.randomUUID().toString().replace("-", ""));
        }

        if (app.getStatus() == null) {
            app.setStatus(1);
        }
        if (app.getTokenLifetime() == null) {
            app.setTokenLifetime(7200);
        }
        if (app.getGrantTypes() == null || app.getGrantTypes().isEmpty()) {
            app.setGrantTypes("authorization_code,refresh_token");
        }

        if (app.getId() == null) {
            clientAppMapper.insert(app);
        } else {
            clientAppMapper.updateById(app);
        }
        return app;
    }

    public boolean updateApp(IamClientApp app) {
        if (app.getId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        return clientAppMapper.updateById(app) > 0;
    }

    public boolean deleteApp(Long id) {
        return clientAppMapper.deleteById(id) > 0;
    }

    public String resetSecret(Long id) {
        IamClientApp app = clientAppMapper.selectById(id);
        if (app == null) {
            throw new BusinessException(ResultCode.CLIENT_NOT_FOUND);
        }
        String newSecret = "sec_" + UUID.randomUUID().toString().replace("-", "");
        app.setClientSecret(newSecret);
        clientAppMapper.updateById(app);
        return newSecret;
    }

    public long getTotalCount() {
        return clientAppMapper.selectCount(null);
    }
}

