package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import org.xj_service.oa.common.Result;
import org.xj_service.oa.common.RequireRole;

import org.xj_service.oa.service.IPermissionService;
import org.xj_service.oa.entity.Permission;
import org.xj_service.oa.service.IRoleService;

@RestController
@RequestMapping("/permission")
@Tag(name = "权限管理", description = "权限管理接口")
@RequireRole({"SUPER_ADMIN","ROLE_ADMIN_OFFICE","USER_ADMIN"}) // 整个控制器都需要管理员权限
public class PermissionController {

    @Resource
    private IPermissionService permissionService;

    // 新增或修改权限
    @Operation(summary = "添加或修改权限", description = "添加或修改权限信息")
    @PostMapping
    public Result save(@RequestBody Permission permission){
        return Result.success(permissionService.saveOrUpdate(permission));
    }

    // 删除权限
    @Operation(summary = "删除权限", description = "根据ID删除权限")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(permissionService.removeById(id));
    }

    // 批量删除权限
    @Operation(summary = "批量删除权限", description = "根据ID列表批量删除权限")
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        return Result.success(permissionService.removeBatchByIds(ids));
    }


    // 根据ID查询权限
    @Operation(summary = "查询单个权限", description = "根据ID查询单个权限")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(permissionService.getById(id));
    }

    // 分页查询权限
    @Operation(summary = "分页查询权限", description = "分页查询权限信息")
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam String name) {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        queryWrapper.orderByAsc("id");
        return Result.success(permissionService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 获取权限列表
     * @return 权限列表
     */
    @Operation(summary = "获取权限列表", description = "获取权限列表")
    @GetMapping("/list")
    public Result getPermissionList() {
        try {
            QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByAsc("id");
            return Result.success(permissionService.list(queryWrapper));
        } catch (Exception e) {
            return Result.error("500", "获取权限列表失败");
        }
    }

    /**
     * 分配权限
     * @param roleId 角色ID
     * @param params 权限列表
     * @return 操作结果
     */
    @Operation(summary = "分配权限", description = "分配权限")
    @PostMapping("/assign/{roleId}")
    public Result assignPermissions(@PathVariable Integer roleId, @RequestBody Map<String, List<Integer>> params) {
        try {
            List<Integer> permissions = params.get("permissions");
            // 这里需要实现具体的权限分配逻辑
            // 例如：先删除角色原有权限，再添加新权限
            permissionService.assignPermissions(roleId, permissions);
            return Result.success("权限分配成功");
        } catch (Exception e) {
            return Result.error("500", "权限分配失败: " + e.getMessage());
        }
    }
}
