package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.service.IDepartmentService;
import org.xj_service.oa.entity.Department;

@RestController
@RequestMapping("/department")
@Tag(name = "部门管理", description = "部门管理接口")
public class DepartmentController {

    @Resource
    private IDepartmentService departmentService;

    @PostMapping
    public Result save(@RequestBody Department department){
        // 如果前端没有传编码，后端自动生成
        if (department.getCode() == null || department.getCode().isEmpty()) {
            department.setCode(departmentService.generateDepartmentCode());
        }
        return Result.success(departmentService.save(department));
    }

    // 删除部门
    @Operation(summary = "删除部门", description = "根据ID删除部门")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            boolean result = departmentService.removeById(id);
            return Result.success(result);
        }  catch (Exception e) {
            return Result.error("500", "删除失败,数据库依赖,请将依赖数据删除后再执行本操作。");
    }
    }

    // 批量删除部门
    @Operation(summary = "批量删除部门", description = "根据ID列表批量删除部门")
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        return Result.success(departmentService.removeBatchByIds(ids));
    }

    // 查询所有部门
    @Operation(summary = "查询所有部门", description = "查询所有部门信息")
    @GetMapping("list")
    public Result findAll() {
        return Result.success(departmentService.list());
    }

    // 根据ID查询部门
    @Operation(summary = "查询单个部门", description = "根据ID查询单个部门")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(departmentService.getById(id));
    }

    // 分页查询部门
    @Operation(summary = "分页查询部门", description = "分页查询部门信息")
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam String name) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        queryWrapper.orderByDesc("id");
        return Result.success(departmentService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }
}
