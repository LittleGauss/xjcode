package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.dto.TodoTaskDTO;
import org.xj_service.oa.entity.SysOperLog;

import java.util.List;

public interface IPengingTaskService{
        /**
         * 获取用户所有待办任务
         */
        List<TodoTaskDTO> getUserTodoTasks(String userId);

        /**
         * 获取指定流程类型的待办任务
         */
        List<TodoTaskDTO> getUserTodoTasksByProcessType(String userId, String processDefinitionKey);

    /**
     * 分页获取用户待办任务
     */
    Page<TodoTaskDTO> getUserTodoTasksPage(Integer userId, Integer pageNum, Integer pageSize,
                                           String businessType, String keyword);


}
