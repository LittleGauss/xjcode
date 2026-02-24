package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.RequireRole;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.SysOperLog;
import org.xj_service.oa.service.ISysOperLogService;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/operlog")
@Tag(name = "操作日志管理", description = "系统操作日志管理接口")
public class OperlogController {
    @Autowired
    private ISysOperLogService sysOperLogService;
    @RequireRole({"SUPER_ADMIN"})
    @Operation(summary = "分页查询日志", description = "分页查询日志信息")
    @GetMapping("/list")
    public Result operlogList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                              @RequestParam(value = "title", required = false) String title,
                              @RequestParam(value = "operName", required = false) String operName) {
        Page<SysOperLog> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysOperLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("oper_id");
        //  应用查询条件 (非空判断)
        if (StringUtils.hasText(title)) {
            // 使用 like 模糊查询 系统模块 title
            queryWrapper.like("title", title);
        }
        if (StringUtils.hasText(operName)) {
            // 使用 like 模糊查询 操作人员 oper_name
            // 注意：数据库字段名通常是小写加下划线，请根据您的数据库表结构调整字段名（例如：oper_name 或 operName）
            queryWrapper.like("oper_name", operName);
        }
        Page<SysOperLog> result = sysOperLogService.page(page, queryWrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(result);
    }

    /**
     * 删除操作日志
     */
    @RequireRole({"SUPER_ADMIN"})
    @DeleteMapping("/{operIds}")
    public Result remove(@PathVariable Long[] operIds) {
        System.out.println("准备删除的ID: " + java.util.Arrays.toString(operIds));
        // 2. 执行删除
        boolean success = sysOperLogService.removeByIds(java.util.Arrays.asList(operIds));
        // 3. 检查Service返回值
        System.out.println("删除操作结果: " + success);
        return success ? Result.success() : Result.error("删除失败");
    }

    @GetMapping("/userIndex")
    public Result operUserIndex(){
        int userIndex = sysOperLogService.getCurrentUserIndex();
        return Result.success(userIndex);
    }
}