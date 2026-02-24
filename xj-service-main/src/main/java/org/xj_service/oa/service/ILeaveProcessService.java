package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.flowable.task.api.Task;
import org.xj_service.oa.entity.ApprovalComment;
import org.xj_service.oa.entity.LeaveProcess;

import java.util.List;
import java.util.Map;

public interface ILeaveProcessService extends IService<LeaveProcess> {

    /**
     * 【核心修改】启动请假流程
     * @param leaveProcess 请假单业务实体
     * @param variables    流程变量 Map (包含 days, isLeader, starter, assignees 等)
     * @return 流程实例 ID
     */
    String startLeaveProcess(LeaveProcess leaveProcess, Map<String, Object> variables);

    /**
     * 【核心修改】完成审批任务
     * @param taskId    任务 ID
     * @param variables 流程变量 (必须包含 approved, 可包含动态指定的 nextApprover)
     * @param comment   审批意见实体
     */
    void completeTask(String taskId, Map<String, Object> variables, ApprovalComment comment);

    // --- 查询类接口保持不变 ---

    /**
     * 获取指定用户的待办任务
     */
    List<Task> getTasksForUser(String userId);

    /**
     * 获取指定用户的请假记录
     */
    List<LeaveProcess> getMyLeaveProcesses(Long userId);

    /**
     * 获取指定用户的请假记录 (分页)
     */
    List<LeaveProcess> getMyLeaveProcesses(Long userId, Integer pageNum, Integer pageSize);

    // --- 草稿相关 ---

    /**
     * 保存草稿
     */
    Long saveDraft(LeaveProcess leaveProcess);

    /**
     * 删除草稿
     */
    boolean deleteDraft(Long draftId, Integer userId);

    /**
     * 【建议修改】从草稿提交
     * 为了支持复杂的审批人指定，建议也改为接收 variables Map
     */
    String submitFromDraft(Long draftId, String starter, String firstApprover, String secondApprover);
    
    // 如果想要更完美的支持草稿提交时的长流程审批，建议增加这个重载方法：
    // String submitFromDraft(Long draftId, Map<String, Object> variables);

    /**
     * 提交销假申请
     * @param taskId 任务ID
     * @param actualStartTime 实际开始时间
     * @param actualEndTime 实际结束时间
     */
    void submitReportBack(String taskId, java.time.LocalDateTime actualStartTime, java.time.LocalDateTime actualEndTime);

    /**
     * 撤回请假申请：只有在没有任何已通过的领导审批时允许撤回。
     * @param leaveId 请假记录ID
     * @param userId 发起人用户ID（用于校验所有权）
     * @return true 表示已成功撤回，false 表示无法撤回（如已存在批准）
     */
    boolean withdrawLeave(Long leaveId, Integer userId);
    // --- 已废弃或移除的方法 ---
    // void approveLeave(...); // 已被 completeTask 替代，建议删除
    // void completeTaskWithNextApprover(...); // 已被 completeTask 替代，建议删除
}