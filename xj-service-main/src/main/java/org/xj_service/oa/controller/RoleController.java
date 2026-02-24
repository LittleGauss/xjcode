package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xj_service.oa.common.Result;
import org.xj_service.oa.common.RequireRole;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.IRoleService;
import org.xj_service.oa.entity.Role;

@RestController
@RequestMapping("/role")
@Tag(name = "角色管理", description = "角色管理接口")
@RequireRole({"SUPER_ADMIN","ROLE_ADMIN_OFFICE","USER_ADMIN"}) // 整个控制器都需要管理员权限
public class RoleController {

    @Resource
    private IRoleService roleService;

    @Resource
    private FileStorageService minioFileStorageService;


    //根据角色的角色名获取数据库表的这个角色的所有属性
    @GetMapping("/name/{name}")
    public Result findByRolename(@PathVariable String name){
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",name);
        return Result.success(roleService.getOne(queryWrapper));
    }

    // 批量删除角色
    @Operation(summary = "批量删除角色", description = "根据ID列表批量删除角色")
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        return Result.success(roleService.removeBatchByIds(ids));
    }


    // 根据ID查询角色
    @Operation(summary = "查询单个角色", description = "根据ID查询单个角色")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(roleService.getById(id));
    }

    // 分页查询角色
    @Operation(summary = "分页查询角色", description = "分页查询角色信息")
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam String name) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        queryWrapper.orderByDesc("id");
        return Result.success(roleService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 获取角色列表
     * @return 角色列表
     */
    @Operation(summary = "获取角色列表", description = "获取角色列表")
    @GetMapping("/list")
    public Result getRoleList() {
        try {
            // 使用带权限列表的方法
            return Result.success(roleService.listWithPermissions());
        } catch (Exception e) {
            return Result.error("500", "获取角色列表失败");
        }
    }

    /**
     * 新增角色
     * @param role 角色信息
     * @return 操作结果
     */
    @Operation(summary = "新增角色", description = "新增角色")
    @PostMapping
    public Result addRole(@RequestBody Role role) {
        try {
            return Result.success(roleService.save(role));
        } catch (Exception e) {
            return Result.error("500", "新增角色失败");
        }
    }

    /**
     * 更新角色
     * @param id 角色ID
     * @param role 角色信息
     * @return 操作结果
     */
    @Operation(summary = "更新角色", description = "更新角色")
    @PutMapping("/{id}")
    public Result updateRole(@PathVariable Integer id, @RequestBody Role role) {
        try {
            role.setId(id);
            return Result.success(roleService.updateById(role));
        } catch (Exception e) {
            return Result.error("500", "更新角色失败");
        }
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色", description = "删除角色")
    @DeleteMapping("/{id}")
    public Result deleteRole(@PathVariable Integer id) {
        try {
            return Result.success(roleService.removeById(id));
        } catch (Exception e) {
            return Result.error("500", "删除角色失败");
        }
    }

    /**
     * 导出所有角色Excel文件
     * @return Minio下载链接
     */
    @Operation(summary = "导出角色Excel", description = "导出所有角色为Excel文件并返回下载链接")
    @GetMapping("/export")
    public Result exportRoles() {
        try {
            // 生成文件名
            String fileName = "roles_" + System.currentTimeMillis() + ".xlsx";

            // 创建临时字节数组输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 导出数据到输出流
            roleService.exportRolesToExcel(outputStream);


            // 转换为 MultipartFile 或直接使用 byte[]
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            String downloadUrl = minioFileStorageService.upload(inputStream, fileName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","oa-files", "role");


            // 返回下载链接
            Map<String, String> result = new HashMap<>();
            result.put("downloadUrl", downloadUrl);
            result.put("fileName", fileName);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "导出失败: " + e.getMessage());
        }
    }
}
