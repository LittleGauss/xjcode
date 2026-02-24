package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.Permission;
import org.xj_service.oa.entity.RolePermission;
import org.xj_service.oa.mapper.PermissionMapper;
import org.xj_service.oa.mapper.RolePermissionMapper;
import org.xj_service.oa.service.IPermissionService;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public void assignPermissions(Integer roleId, List<Integer> permissionIds) {
        // 先删除角色原有权限
        QueryWrapper<RolePermission> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("role_id", roleId);
        rolePermissionMapper.delete(deleteWrapper);

        // 添加新权限
        for (Integer permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }
    }
}
