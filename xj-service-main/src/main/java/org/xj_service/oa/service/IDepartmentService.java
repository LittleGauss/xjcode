package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.Department;

public interface IDepartmentService extends IService<Department> {
    String generateDepartmentCode();
}
