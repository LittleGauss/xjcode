package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.IUserService;
import org.xj_service.oa.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户管理接口")
public class UserMagController {

    @Resource
    private IUserService userService;

    @Autowired
    @Qualifier("minioFileStorageService") // 或 "fileStorageService"，根据实际使用的实现类选择
    private FileStorageService fileService;
    @Operation(summary = "获取用户列表", description = "获取用户列表")
    @GetMapping("/list")
    public Result getUserList(@RequestParam Map<String, Object> params) {
        try {
            String username = (String) params.get("username");
            String roleCode = (String) params.get("roleCode");
            // 新增：获取前端传递的 status 参数
            Integer status = params.get("status") != null ?
                    Integer.valueOf(params.get("status").toString()) : null;

            Integer pageNum = params.get("page") != null ?
                    Integer.valueOf(params.get("page").toString()) : 1;
            Integer pageSize = params.get("size") != null ?
                    Integer.valueOf(params.get("size").toString()) : 10;

            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            // 处理 username 筛选
            if (username != null && !username.isEmpty()) {
                queryWrapper.like("username", username);
            }
            // 新增：处理 status 筛选（如果传递了 status，则只查询该状态的用户）
            if (status != null) {
                queryWrapper.eq("status", status); // 等价于 SQL 中的 WHERE status = ?
            }
            // 3. 角色编码筛选（核心：关联中间表 sys_user_role 和角色表 sys_role）
            if (roleCode != null && !roleCode.isEmpty()) {
                // 逻辑：先通过角色编码查角色ID，再通过角色ID查中间表的用户ID，最后筛选用户表
                queryWrapper.inSql(
                        "id", // 用户表的主键（对应sys_user.id）
                        "SELECT ur.user_id " +
                                "FROM sys_user_role ur " + // 中间表（字段是user_id、role_id）
                                "INNER JOIN sys_role r ON ur.role_id = r.id " + // 关联角色表（用INNER JOIN更精准，只查存在的角色）
                                "WHERE r.code = '" + roleCode + "'"
                );
            }
            queryWrapper.orderByAsc("id");

            Page<User> page = new Page<>(pageNum, pageSize);
            return Result.success(userService.page(page, queryWrapper));
        } catch (Exception e) {
            return Result.error("500", "获取用户列表失败");
        }
    }
    /**
     * 新增用户
     * @param user 用户信息
     * @return 操作结果
     */
    @Operation(summary = "新增用户", description = "新增用户")
    @PostMapping
    public Result addUser(@RequestBody User user) {
        try {
            return Result.success(userService.save(user));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "新增用户失败");
        }
    }

    /**
     * 编辑用户
     * @param id 用户ID
     * @param user 用户信息
     * @return 操作结果
     */
    @Operation(summary = "编辑用户", description = "编辑用户")
    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Integer id, @RequestBody User user) {
        try {
            user.setId(id);
            return Result.success(userService.updateById(user));
        } catch (Exception e) {
            return Result.error("500", "编辑用户失败");
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户", description = "删除用户")
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Integer id) {
        try {
            return Result.success(userService.removeById(id));
        } catch (Exception e) {
            return Result.error("500", "删除用户失败");
        }
    }

    /**
     * 切换用户状态
     * @param id 用户ID
     * @param params 状态
     * @return 操作结果
     */
    @Operation(summary = "切换用户状态", description = "切换用户状态")
    @PutMapping("/status/{id}")
    public Result toggleUserStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> params) {
        try {
            Integer status = params.get("status");
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            return Result.success(userService.updateById(user));
        } catch (Exception e) {
            return Result.error("500", "切换用户状态失败");
        }
    }

    /**
     * 导出用户
     */
    @Operation(summary = "导出用户", description = "导出用户")
    @GetMapping("/export")
    public Result exportUsers() {
            try {
                // 生成文件名
                String fileName = "users_" + System.currentTimeMillis() + ".xlsx";

                // 创建临时字节数组输出流
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                // 导出数据到输出流
                userService.exportUsers(outputStream);

                // 上传到MinIO
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                String downloadUrl = fileService.upload(
                        inputStream,
                        fileName,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","oa-files", "user"
                );

                // 返回下载链接
                Map<String, String> result = new HashMap<>();
                result.put("downloadUrl", fileService.getTempUrl(downloadUrl,300));
                result.put("fileName", fileName);

                return Result.success(result);
            } catch (Exception e) {
                return Result.error("500", "导出失败: " + e.getMessage());
            }
    }

    // 获取下载链接示例
    @GetMapping("/download-url")
    public Result getDownloadUrl(@RequestParam String filePath) {
        try {
            String downloadUrl = fileService.getTempUrl(filePath, 3600);
            return Result.success(downloadUrl);
        } catch (Exception e) {
            return Result.error("500", "获取下载链接失败");
        }
    }
    /**
     * 批量导入用户
     * @param file 导入文件
     * @return 操作结果
     */
    @Operation(summary = "批量导入用户", description = "批量导入用户")
    @PostMapping("/import")
    public Result importUsers(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("400", "请选择文件");
            }
            userService.importUsers(file);
            return Result.success("导入成功");
        } catch (Exception e) {
            return Result.error("500", "导入失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户角色（覆盖式）
     */
    @Operation(summary = "更新用户角色", description = "为用户设置角色（覆盖原有分配）")
    @PutMapping("/{id}/roles")
    public Result updateUserRoles(@PathVariable Integer id, @RequestBody Map<String, List<Integer>> body) {
        try {
            List<Integer> roleIds = body.get("roleIds");
            userService.updateUserRoles(id, roleIds);
            return Result.success("更新用户角色成功");
        } catch (Exception e) {
            return Result.error("500", "更新用户角色失败");
        }
    }

    /**
     * 根据角色名称查询用户列表
     * @param requestBody
     * @return 用户列表
     */
    @Operation(summary = "根据角色查询用户", description = "根据角色名称查询用户列表")
    @PostMapping("/getUsersByRole")
    public Result getUsersByRole(@RequestBody Map<String, List<String>> requestBody) {
        try {
            List<String> codeNames = requestBody.get("codeNames");
            List<User> users = userService.getUsersByRoleCode(codeNames);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("500", "查询用户列表失败: " + e.getMessage());
        }
    }
}
