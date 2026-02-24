package org.xj_service.oa.service;


import com.baomidou.mybatisplus.extension.service.IService;

import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.dto.TodoTaskDTO;
import org.xj_service.oa.entity.InspectionTask;
import org.xj_service.oa.entity.InspectionTaskAssignment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface InspectionTaskService extends IService<InspectionTask> {
    // 发起检查任务
    Long initiateTask(InspectionTask task, List<Long> inspectorIds, String formFile);

    // 检查员接收任务
    boolean receiveTask(Long assignmentId, Long inspectorId);

    // 检查员完成任务
    boolean completeTask(Long assignmentId, Long inspectorId, MultipartFile filledFormFile);

    // 负责人审核任务（通过/驳回）
    boolean auditTask(Long assignmentId, Long initiatorId, String auditResult, String rejectReason);

    // 检查员提交重做任务
    boolean submitRedoTask(Long assignmentId, Long inspectorId, MultipartFile filledFormFile);

    // 动态调整检查人员
    boolean adjustInspectors(Long taskId, List<Long> addInspectorIds, List<Long> removeInspectorIds);

    // 查询任务详情
    InspectionTask getTaskDetail(Long taskId);

    // 查询检查员的待办任务
    List<Map<String, Object>> getTodoTasksMap(Long inspectorId);

    List<TodoTaskDTO> getTodoDTOTasks(Long inspectorId);

    // 计算任务完成率
    BigDecimal calculateCompletionRate(Long taskId);

    // 定时更新逾期状态
    void updateOverdueStatus();

    // 终止任务
    boolean terminateTask(Long taskId, String reason);

    // 查询流程状态
    String getProcessStatus(Long taskId);

    // 根据发起人ID查询任务列表
    List<InspectionTask> getTaskListByInitiatorId(Long initiatorId);
    // 定时检查并处理逾期任务
    void checkAndHandleOverdueTasks();
    // 根据任务ID查询分配列表
    List<InspectionTaskAssignment> getAssignmentsByTaskId(Long taskId);

    // 根据任务ID查询分配列表
    InspectionTaskAssignment getAssignmentByTaskIdAndUseID(Long taskId, Long userId);
}