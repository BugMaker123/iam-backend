package com.gk.iam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gk.iam.entity.SysDepartment;
import com.gk.iam.mapper.SysDepartmentMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDepartmentService {

    @Autowired
    private SysDepartmentMapper departmentMapper;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DeptTreeNode extends SysDepartment {
        private List<DeptTreeNode> children = new ArrayList<>();
    }

    public List<DeptTreeNode> getDepartmentTree(Long companyId) {
        LambdaQueryWrapper<SysDepartment> query = new LambdaQueryWrapper<>();
        if (companyId != null && companyId > 0) {
            query.eq(SysDepartment::getCompanyId, companyId);
        }
        query.orderByAsc(SysDepartment::getSortOrder).orderByAsc(SysDepartment::getId);
        List<SysDepartment> list = departmentMapper.selectList(query);

        // 转化为树形结构
        List<DeptTreeNode> nodeMapList = list.stream().map(dept -> {
            DeptTreeNode node = new DeptTreeNode();
            BeanUtils.copyProperties(dept, node);
            return node;
        }).collect(Collectors.toList());

        Map<Long, DeptTreeNode> map = nodeMapList.stream()
                .collect(Collectors.toMap(SysDepartment::getId, node -> node));

        List<DeptTreeNode> rootNodes = new ArrayList<>();
        for (DeptTreeNode node : nodeMapList) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0 || !map.containsKey(parentId)) {
                rootNodes.add(node);
            } else {
                map.get(parentId).getChildren().add(node);
            }
        }
        return rootNodes;
    }

    public List<SysDepartment> getAllDepartments() {
        return departmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .orderByAsc(SysDepartment::getSortOrder));
    }

    public SysDepartment saveDepartment(SysDepartment department) {
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
        if (department.getId() == null) {
            departmentMapper.insert(department);
        } else {
            departmentMapper.updateById(department);
        }
        return department;
    }

    public boolean deleteDepartment(Long id) {
        return departmentMapper.deleteById(id) > 0;
    }

    public SysDepartment getById(Long id) {
        return departmentMapper.selectById(id);
    }

    public long getTotalCount() {
        return departmentMapper.selectCount(null);
    }
}

