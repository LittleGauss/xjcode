package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.Department;
import org.xj_service.oa.mapper.DepartmentMapper;
import org.xj_service.oa.service.IDepartmentService;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

    /**
     * 生成新的部门编码
     * @return 新的部门编码，格式为 DEPT + 3位数字（如 DEPT007）
     */
    public String generateDepartmentCode() {
        // 查询当前最大编码
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("code");
        queryWrapper.likeRight("code", "DEPT");
        queryWrapper.orderByDesc("code");
        queryWrapper.last("LIMIT 1");

        Department department = getOne(queryWrapper);
        int nextNumber = 1;

        if (department != null && department.getCode() != null) {
            String currentCode = department.getCode();
            String numberPart = currentCode.substring(4); // 提取数字部分
            try {
                int currentNumber = Integer.parseInt(numberPart);
                nextNumber = currentNumber + 1;
            } catch (NumberFormatException e) {
                // 如果解析失败，使用默认值1
                nextNumber = 1;
            }
        }

        return String.format("DEPT%03d", nextNumber);
    }

    /**
     * 保存部门时自动生成编码
     */
    @Override
    public boolean save(Department department) {
        if (department.getCode() == null || department.getCode().isEmpty()) {
            department.setCode(generateDepartmentCode());
        }
        return super.save(department);
    }
}
