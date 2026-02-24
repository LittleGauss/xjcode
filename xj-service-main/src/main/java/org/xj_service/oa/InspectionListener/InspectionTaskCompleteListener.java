package org.xj_service.oa.InspectionListener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xj_service.oa.entity.InspectionTaskAssignment;
import org.xj_service.oa.mapper.InspectionTaskAssignmentMapper;
import java.time.LocalDateTime;


/**
 * 巡检任务完成监听器
 * 监听Flowable任务完成事件，更新任务分配表状态/完成时间等信息
 * @Component 注解名称：inspectionTaskCompleteListener
 */
@Component("inspectionTaskCompleteListener")
public class InspectionTaskCompleteListener implements TaskListener {

    @Autowired
    private InspectionTaskAssignmentMapper assignmentMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        // 仅处理任务完成事件（防止监听其他事件类型）
        if (!EVENTNAME_COMPLETE.equals(delegateTask.getEventName())) {
            return;
        }

        try {
            // 1. 获取流程变量和任务信息
            Long inspectionTaskId = (Long) delegateTask.getVariable("inspectionTaskId");
            Long inspectorId = Long.valueOf(delegateTask.getAssignee());
            String flowTaskId = delegateTask.getId(); // Flowable任务ID

            // 2. 构建查询条件，查询对应的任务分配记录
            QueryWrapper<InspectionTaskAssignment> wrapper = new QueryWrapper<>();
            wrapper.eq("inspection_task_id", inspectionTaskId)
                    .eq("inspector_id", inspectorId)
                    .eq("flow_task_id", flowTaskId); // 增加Flow任务ID条件，提高查询精度

            InspectionTaskAssignment assignment = assignmentMapper.selectOne(wrapper);
            if (assignment != null) {
                // 3. 更新任务完成相关字段（根据实际业务需求调整）
                assignment.setTaskStatus("SUBMIT"); // 任务状态：已提交
                assignment.setCompleteTime(LocalDateTime.now()); // 任务完成时间（需确保实体类有该字段）
                // 4. 执行更新操作
                assignmentMapper.updateById(assignment);
            } else {
                // 日志记录（建议引入日志框架，如SLF4J）
                System.err.printf("未找到对应的巡检任务分配记录：inspectionTaskId=%d, inspectorId=%d, flowTaskId=%s%n",
                        inspectionTaskId, inspectorId, flowTaskId);
            }
        } catch (Exception e) {
            // 异常处理（建议抛出自定义异常或记录日志）
            System.err.println("巡检任务完成监听器执行异常：" + e.getMessage());
            // 如需中断流程，可抛出异常：throw new RuntimeException("任务完成处理失败", e);
        }
    }
}