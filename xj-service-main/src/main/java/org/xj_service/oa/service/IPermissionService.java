package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.Permission;

import java.util.List;

public interface IPermissionService extends IService<Permission> {
    void assignPermissions(Integer roleId, List<Integer> permissionIds);
}
