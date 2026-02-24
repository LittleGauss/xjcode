package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xj_service.oa.entity.ApprovalComment;
import org.xj_service.oa.entity.LeaveProcess;
import org.xj_service.oa.mapper.LeaveProcessMapper;
import org.xj_service.oa.service.IApprovalCommentService;
import org.xj_service.oa.service.ILeaveProcessService;

import org.xj_service.oa.entity.LeaveReportBack;
import org.xj_service.oa.service.ILeaveReportBackService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaveProcessServiceImpl extends ServiceImpl<LeaveProcessMapper, LeaveProcess>
        implements ILeaveProcessService {
    
    private static final Logger log = LoggerFactory.getLogger(LeaveProcessServiceImpl.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Resource
    private IApprovalCommentService approvalCommentService;

    @Resource
    private ILeaveReportBackService leaveReportBackService;

    /**
     * 【核心修改 1】启动流程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startLeaveProcess(LeaveProcess leaveProcess, Map<String, Object> variables) {
        // 1. 保存/更新业务实体
        leaveProcess.setStatus("SUBMITTED");
        
        if (leaveProcess.getDuration() == null && variables.containsKey("days")) {
             leaveProcess.setDuration(Double.valueOf(variables.get("days").toString()));
        }
        
        if (leaveProcess.getCreatedAt() == null) {
            leaveProcess.setCreatedAt(LocalDateTime.now());
        }
        leaveProcess.setUpdatedAt(LocalDateTime.now());
        saveOrUpdate(leaveProcess); 

        // 2. 补充流程内置变量
        variables.put("leaveId", leaveProcess.getId());
        String starter = (String) variables.get("starter"); 
        
        log.info("Starting process instance with variables: {}", variables);

        // 3. 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "leaveAndCancelProcess", 
                leaveProcess.getId().toString(), 
                variables
        );

        // 4. 回写流程实例ID
        leaveProcess.setProcessInstanceId(processInstance.getId());
        updateById(leaveProcess);

        // 5. 自动完成第一个节点 "submit"
        Task submitTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey("submit") 
                .singleResult();
        
        if (submitTask != null) {
            taskService.complete(submitTask.getId());
        }

        // 6. 保存提交申请的历史记录
        saveSubmitComment(leaveProcess, processInstance.getId(), starter);

        // --- 【新增】流程启动后，立即更新当前的审批人（如：部门主管） ---
        updateCurrentApprover(processInstance.getId());

        return processInstance.getId();
    }

    /**
     * 【核心修改 2】完成任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, Map<String, Object> variables, ApprovalComment comment) {
        // 1. 查询当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在或已被处理");
        }
        
        String processInstanceId = task.getProcessInstanceId();
        boolean approved = (boolean) variables.getOrDefault("approved", false);
        
        log.info("Completing task: id={}, name={}, approved={}", taskId, task.getName(), approved);

        // 2. 完成任务
        taskService.complete(taskId, variables);

        // 3. 保存审批意见，确保重要字段都被设置（落表字段与业务关联）
        if (comment != null) {

            comment.setId(null);
            
            if (comment.getProcessInstanceId() == null) comment.setProcessInstanceId(processInstanceId);
            if (comment.getApprovalNode() == null) comment.setApprovalNode(task.getName());

            // 关联业务单据：根据 processInstanceId 查找 leave 的 id
            try {
                if (comment.getBusinessId() == null) {
                    LeaveProcess leave = getOne(new QueryWrapper<LeaveProcess>().eq("process_instance_id", processInstanceId));
                    if (leave != null && leave.getId() != null) {
                        comment.setBusinessId(leave.getId().intValue());
                    }
                }
            } catch (Exception e) {
                log.warn("查找 LeaveProcess 以设置 businessId 时出错", e);
            }

            // 业务类型固定为 leave
            if (comment.getBusinessType() == null) comment.setBusinessType("leave");

            // 如果没有传入 approverName，则使用任务的处理人
            if (comment.getApproverName() == null) comment.setApproverName(task.getAssignee());

            // 根据流程变量或 approved 值自动设置 approvalResult
            if (comment.getApprovalResult() == null) {
                comment.setApprovalResult(approved ? "APPROVED" : "REJECTED");
            }

            if (comment.getApprovedTime() == null) comment.setApprovedTime(LocalDateTime.now());

            approvalCommentService.saveComment(comment);
        }

        // 4. 更新业务单据状态
        updateStatusAfterCompletion(processInstanceId, approved);

        // --- 【新增】如果审批通过，更新下一位审批人到数据库 ---
        if (approved) {
            updateCurrentApprover(processInstanceId);
            
            // 如果是销假归档节点完成，更新归档表状态
            if ("reportBackAdmin".equals(task.getTaskDefinitionKey())) {
                LeaveReportBack rb = leaveReportBackService.getOne(
                    new QueryWrapper<LeaveReportBack>().eq("process_instance_id", processInstanceId)
                );
                if (rb != null) {
                    rb.setStatus("ARCHIVED");
                    leaveReportBackService.updateById(rb);
                }
            }
        } else {
            // 如果驳回，通常流程结束或回到发起人，这里清空或设为发起人，
            // 具体取决于 updateCurrentApprover 的逻辑（如果流程未结束，它能查到回到某人手里了）
            updateCurrentApprover(processInstanceId);
        }
    }

    /**
     * 【新增辅助方法】查询流程当前任务，并更新数据库中的"当前审批人"
     */
    private void updateCurrentApprover(String processInstanceId) {
        // 查询该实例当前活跃的任务列表
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
        
        LeaveProcess updateEntity = new LeaveProcess();

        if (tasks != null && !tasks.isEmpty()) {
            Task t = tasks.get(0);
            String assignee = t.getAssignee();

            String currentApproverVal = "";
            if (assignee != null && !assignee.isEmpty()) {
                currentApproverVal = assignee;
            } else {
                // 没有直接 assignee，尝试获取候选用户/组（Identity Links）
                try {
                    List<?> links = taskService.getIdentityLinksForTask(t.getId());
                    if (links != null && !links.isEmpty()) {
                        // 优先取候选用户，其次取候选组（记录为 group:groupId）
                        for (Object link : links) {
                            try {
                                Method mUser = link.getClass().getMethod("getUserId");
                                Object uid = mUser.invoke(link);
                                if (uid != null) {
                                    
                                    currentApproverVal = uid.toString();
                                    break;
                                }
                            } catch (NoSuchMethodException nsme) {
                                // ignore - implementation may not expose getUserId
                            } catch (Exception ex) {
                                log.warn("反射读取 IdentityLink.getUserId 失败", ex);
                            }

                            try {
                                Method mGroup = link.getClass().getMethod("getGroupId");
                                Object gid = mGroup.invoke(link);
                                if (gid != null && currentApproverVal.isEmpty()) {
                                    currentApproverVal = "group:" + gid.toString();
                                }
                            } catch (NoSuchMethodException nsme) {
                                // ignore
                            } catch (Exception ex) {
                                log.warn("反射读取 IdentityLink.getGroupId 失败", ex);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取任务 IdentityLinks 失败", e);
                }
            }

            updateEntity.setCurrentApprover(currentApproverVal);
        } else {
            // 如果没任务，说明流程结束了，清空审批人
            updateEntity.setCurrentApprover("");
        }

        updateEntity.setUpdatedAt(LocalDateTime.now());

        // 执行数据库更新
        update(updateEntity, new QueryWrapper<LeaveProcess>().eq("process_instance_id", processInstanceId));
    }

    /**
     * 辅助方法：保存"提交申请"的记录
     */
    private void saveSubmitComment(LeaveProcess leave, String processInstanceId, String starter) {
        try {
            ApprovalComment comment = new ApprovalComment();
            if (leave.getId() != null) {
                comment.setBusinessId(leave.getId().intValue());
            }
            comment.setBusinessType("leave");
            comment.setProcessInstanceId(processInstanceId);
            // 优先使用传入的 starter（通常为发起人名称或账号），否则回退为申请用户ID
            if (starter != null && !starter.isEmpty()) {
                comment.setApproverName(starter);
            } else if (leave.getUserId() != null) {
                comment.setApproverName(leave.getUserId().toString());
            }
            comment.setApprovalNode("提交申请");
            comment.setComment("发起请假申请");
            comment.setApprovalResult("SUBMITTED");
            comment.setApprovedTime(LocalDateTime.now());

            comment.setCreatedAt(LocalDateTime.now());
            approvalCommentService.saveComment(comment);
            log.info("已生成提交审批意见: processInstanceId={}, businessId={}, approver={}", processInstanceId, comment.getBusinessId(), comment.getApproverName());
        } catch (Exception e) {
            log.error("生成提交记录失败", e);
        }
    }

    /**
     * 辅助方法：状态更新
     */
    private void updateStatusAfterCompletion(String processInstanceId, boolean approved) {
        try {
            if (!approved) {
                updateLeaveStatusByProcessId(processInstanceId, "REJECTED");
                return;
            }

            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            
            if (pi == null) {
                updateLeaveStatusByProcessId(processInstanceId, "APPROVED");
            } else {
                // 检查是否进入销假阶段
                List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
                boolean isReportBack = false;
                for (Task t : tasks) {
                    String key = t.getTaskDefinitionKey();
                    if ("reportBackApply".equals(key) || 
                        "reportBackManager".equals(key) || 
                        "reportBackAdmin".equals(key)) {
                        isReportBack = true;
                        break;
                    }
                }
                
                if (isReportBack) {
                    updateLeaveStatusByProcessId(processInstanceId, "APPROVED");
                } else {
                    updateLeaveStatusByProcessId(processInstanceId, "SUBMITTED");
                }
            }
        } catch (Exception e) {
            log.error("Failed to update status", e);
        }
    }

    private void updateLeaveStatusByProcessId(String processInstanceId, String status) {
        LeaveProcess updateEntity = new LeaveProcess();
        updateEntity.setStatus(status);
        updateEntity.setUpdatedAt(LocalDateTime.now());
        update(updateEntity, new QueryWrapper<LeaveProcess>().eq("process_instance_id", processInstanceId));
    }

    // --- 其他查询接口 ---

    @Override
    public List<Task> getTasksForUser(String userId) {
        return taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
    }

    @Override
    public List<LeaveProcess> getMyLeaveProcesses(Long userId) {
        return list(new QueryWrapper<LeaveProcess>().eq("user_id", userId).orderByDesc("created_at"));
    }

    @Override
    public List<LeaveProcess> getMyLeaveProcesses(Long userId, Integer pageNum, Integer pageSize) {
        Page<LeaveProcess> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return page(page, new QueryWrapper<LeaveProcess>().eq("user_id", userId).orderByDesc("created_at")).getRecords();
    }

    @Override
    public Long saveDraft(LeaveProcess leaveProcess) {
        leaveProcess.setStatus("DRAFT");
        leaveProcess.setCreatedAt(LocalDateTime.now());
        leaveProcess.setUpdatedAt(LocalDateTime.now());
        saveOrUpdate(leaveProcess);
        return leaveProcess.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawLeave(Long leaveId, Integer userId) {
        try {
            LeaveProcess leave = getById(leaveId);
            if (leave == null) return false;

            // 仅允许申请人自己撤回
            if (leave.getUserId() == null || !leave.getUserId().equals(userId)) {
                return false;
            }

            // 查询该请假单的审批意见，若存在任何已同意的记录则不可撤回
            List<ApprovalComment> comments = approvalCommentService.getByBusiness(leaveId.intValue(), "leave");
            if (comments != null) {
                for (ApprovalComment c : comments) {
                    if ("APPROVED".equalsIgnoreCase(c.getApprovalResult())) {
                        return false;
                    }
                }
            }

            String processInstanceId = leave.getProcessInstanceId();
            if (processInstanceId != null && !processInstanceId.isEmpty()) {
                try {
                    // 删除流程实例（撤回）
                    runtimeService.deleteProcessInstance(processInstanceId, "withdrawn by user");
                } catch (Exception e) {
                    log.warn("删除流程实例失败，但继续更新业务状态", e);
                }
            }

            // 更新请假单状态为 WITHDRAWN
            LeaveProcess update = new LeaveProcess();
            update.setStatus("已撤回");
            update.setUpdatedAt(LocalDateTime.now());
            update(update, new QueryWrapper<LeaveProcess>().eq("id", leaveId));

            // 保存撤回的意见记录
            try {
                ApprovalComment withdrawComment = new ApprovalComment();
                withdrawComment.setBusinessId(leaveId.intValue());
                withdrawComment.setBusinessType("leave");
                withdrawComment.setApprovalNode("撤回");
                withdrawComment.setApproverName(userId == null ? "" : userId.toString());
                withdrawComment.setApprovalResult("WITHDRAWN");
                withdrawComment.setComment("申请人撤回申请");
                withdrawComment.setApprovedTime(LocalDateTime.now());
                approvalCommentService.saveComment(withdrawComment);
            } catch (Exception ignore) {
                log.warn("保存撤回意见失败", ignore);
            }

            return true;
        } catch (Exception e) {
            log.error("撤回请假失败", e);
            return false;
        }
    }
    
    @Override
    public String submitFromDraft(Long draftId, String starter, String firstApprover, String secondApprover) {
        LeaveProcess draft = getById(draftId);
        if (draft == null) throw new RuntimeException("Draft not found");
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("starter", starter);
        vars.put("deptManager", firstApprover);
        
        if (draft.getDuration() != null) {
            vars.put("days", draft.getDuration());
        } else {
             vars.put("days", 1.0);
        }
        vars.put("isLeader", false);
        vars.put("adminUser", "admin"); 

        return startLeaveProcess(draft, vars);
    }

    @Override
    public boolean deleteDraft(Long draftId, Integer userId) {
        return remove(new QueryWrapper<LeaveProcess>().eq("id", draftId).eq("user_id", userId).eq("status", "DRAFT"));
    }

    /**
     * 【新增】提交销假申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReportBack(String taskId, LocalDateTime actualStartTime, LocalDateTime actualEndTime) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        String processInstanceId = task.getProcessInstanceId();
        
        // 1. 创建销假归档记录
        LeaveReportBack reportBack = new LeaveReportBack();
        reportBack.setProcessInstanceId(processInstanceId);
        reportBack.setActualStartTime(actualStartTime);
        reportBack.setActualEndTime(actualEndTime);
        reportBack.setReportBackTime(LocalDateTime.now());
        reportBack.setStatus("PENDING");
        
        // 关联 LeaveId
        LeaveProcess leave = getOne(new QueryWrapper<LeaveProcess>().eq("process_instance_id", processInstanceId));
        if (leave != null) {
            reportBack.setLeaveId(leave.getId());
        }
        
        leaveReportBackService.save(reportBack);
        
        // 2. 完成任务
        taskService.complete(taskId);
        
        // 3. 更新审批人
        updateCurrentApprover(processInstanceId);
    }
}