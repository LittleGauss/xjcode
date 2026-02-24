package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.dto.TodoTaskDTO;
import org.xj_service.oa.entity.SysOperLog;
import org.xj_service.oa.service.IPengingTaskService;

import java.util.HashMap;
import java.util.Map;

/**
 * 待办任务相关
 */
@RestController
@RequestMapping("/pending")
@Tag(name = "待办任务管理", description = "系统待办任务管理接口")
public class PengingTasksController {
    @Autowired
    private IPengingTaskService pengingTaskService;
    @Operation(summary = "分页查询待办", description = "分页查询待办信息")
    @GetMapping("/tasks")
    public Result pengdingTaskList(@RequestParam(value = "page", defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                              @RequestParam(value = "businessType", required = false) String businessType,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "assigneeId") Integer userId) {
        Page<TodoTaskDTO> pageResult = pengingTaskService.getUserTodoTasksPage(
                userId, pageNum, pageSize, businessType, keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("current", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        data.put("pages", pageResult.getPages());

        return Result.success(data);
    }

}