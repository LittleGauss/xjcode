package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.Role;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface IRoleService extends IService<Role> {
    /**
     * 获取包含权限列表的角色集合
     */
    List<Role> listWithPermissions();
    void exportRolesToExcel(OutputStream outputStream) throws IOException;
}
