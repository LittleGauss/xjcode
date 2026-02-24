package org.xj_service.oa.service.impl;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xj_service.oa.entity.ApprovalComment;
import org.xj_service.oa.service.IApprovalCommentService;
import org.xj_service.oa.entity.ContractReview;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.mapper.ContractReviewMapper;
import org.xj_service.oa.service.IContractProcessService;
import org.xj_service.oa.service.IContractReviewService;
import org.xj_service.oa.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractProcessServiceImpl extends ServiceImpl<ContractReviewMapper, ContractReview>
    implements IContractProcessService {
    
    private static final Logger log = LoggerFactory.getLogger(ContractProcessServiceImpl.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IContractReviewService contractReviewService;

    @Autowired
    private IApprovalCommentService approvalCommentService;

    @Autowired
    private IUserService userService;

    /**
     * 启动流程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startContractReview(ContractReview review, String starter, String gmUser, String legalUser, String adminUser) {
        // 1. 保存业务单
        review.setStatus("待综合管理部审批");
        if (review.getCreatedAt() == null) review.setCreatedAt(LocalDateTime.now());
        contractReviewService.saveOrUpdate(review);

        // 2. 准备流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("requester", starter);
        variables.put("gmUser", gmUser);
        variables.put("legalUser", legalUser);
        
        // 自动处理 adminUser 为空的情况，默认给法务
        if (adminUser == null || adminUser.isEmpty()) {
            variables.put("adminUser", legalUser); 
        } else {
            variables.put("adminUser", adminUser);
        }
        variables.put("contractId", review.getId());

        // 3. 启动流程
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("contractReviewProcess", review.getId().toString(), variables);
        log.info("流程启动成功: ProcessId={}, ContractId={}", pi.getId(), review.getId());

        // 4. 回写流程实例ID
        try {
            review.setProcessInstanceId(pi.getId());
            contractReviewService.updateById(review);
        } catch (Exception ex) {
            log.warn("回写 processInstanceId 失败", ex);
        }

        // 5. 自动完成 "upload" 节点
        try {
            Task startTask = taskService.createTaskQuery()
                    .processInstanceId(pi.getId())
                    .taskDefinitionKey("upload")
                    .singleResult();
            
            if (startTask != null) {
                taskService.complete(startTask.getId());
            }

            // 保存初始提交记录
            ApprovalComment comment = new ApprovalComment();
            comment.setProcessInstanceId(pi.getId());
            comment.setBusinessId(review.getId().intValue());
            comment.setBusinessType("contract");
            try {
                comment.setApproverId(Integer.parseInt(starter)); 
            } catch (Exception e) {
                comment.setApproverId(0);
            }
            comment.setApproverName(review.getApplicantName());
            comment.setApprovalNode("使用部门上传合同");
            String userComment = review.getReviewComments();
            comment.setComment((userComment != null && !userComment.isEmpty()) ? userComment : "发起流程提交申请");
            comment.setApprovalResult("APPROVED");
            comment.setApprovedTime(LocalDateTime.now());
            comment.setCreatedAt(LocalDateTime.now());
            
            approvalCommentService.saveComment(comment);

            // 6. 更新当前处理人
            review.setCurrentApprover(gmUser);
            contractReviewService.updateById(review);

        } catch (Exception ex) {
            log.error("自动完成起始节点或保存记录失败", ex);
            throw new RuntimeException("流程启动后续处理失败: " + ex.getMessage());
        }

        return pi.getId();
    }

    @Override
    public List<Task> getTasksForUser(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime().desc()
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, boolean approved, String userComment) {
        
        // 1. 获取当前任务详情
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new RuntimeException("任务不存在");
        
        String processInstanceId = task.getProcessInstanceId();
        String taskDefKey = task.getTaskDefinitionKey();

        // 2. 准备流程变量
        Map<String, Object> vars = new HashMap<>();
        if ("gmSign1".equals(taskDefKey)) {
            vars.put("gmApprovedFirst", approved); 
        } else {
            vars.put("approved", approved);
        }

        // 3. 保存审批记录 (ApprovalComment)
        // 【修改点1】：将 contractId 变量定义提到 try 外面，以便后续传递
        Long contractId = null;
        try {
            Object cidObj = runtimeService.getVariable(processInstanceId, "contractId");
            if (cidObj instanceof Number) contractId = ((Number) cidObj).longValue();

            ApprovalComment comment = new ApprovalComment();
            comment.setProcessInstanceId(processInstanceId);
            if (contractId != null) comment.setBusinessId(contractId.intValue());
            comment.setBusinessType("contract");
            
            // 设置审批人ID和名称
            String assignee = task.getAssignee();
            try {
                Integer approverId = Integer.parseInt(assignee);
                comment.setApproverId(approverId);
                
                // 根据ID查询用户真实姓名
                User user = userService.getById(approverId);
                if (user != null) {
                    String approverName = user.getNickname();
                    if (approverName == null || approverName.isEmpty()) {
                        approverName = user.getUsername();
                    }
                    comment.setApproverName(approverName);
                } else {
                    comment.setApproverName("用户" + approverId);
                }
            } catch (NumberFormatException e) {
                // 如果不是数字ID，直接使用assignee作为名称
                comment.setApproverId(null);
                comment.setApproverName(assignee);
            }
            
            comment.setApprovalNode(task.getName());
            
            String finalComment = (userComment != null && !userComment.isEmpty()) ? userComment : (approved ? "同意" : "驳回");
            comment.setComment(finalComment);
            comment.setApprovalResult(approved ? "APPROVED" : "REJECTED");
            comment.setApprovedTime(java.time.LocalDateTime.now());
            
            approvalCommentService.saveComment(comment);
            log.info("审批意见保存成功: contractId={}, approver={}, result={}", contractId, assignee, comment.getApprovalResult());
        } catch (Exception ex) {
            log.error("保存审批记录失败", ex);
            throw new RuntimeException("保存审批记录失败: " + ex.getMessage(), ex);
        }

        // 4. 完成任务 (如果是终审，流程实例在此刻被删除)
        taskService.complete(taskId, vars);

        // 5. 更新业务表状态
        // 【修改点2】：传入第4个参数 contractId，不再让辅助方法去查 Flowable
        updateStatusAfterCompletion(processInstanceId, taskDefKey, approved, contractId);
    }

    /**
     * 辅助方法：根据流程当前位置更新业务表状态和当前审批人
     * 【修改点3】：增加 Long contractId 参数
     */
    private void updateStatusAfterCompletion(String processInstanceId, String completedTaskKey, boolean approved, Long contractId) {
        // 【修改点4】：直接判断参数，而不是去 runtimeService 查
        if (contractId == null) return;

        ContractReview cr = contractReviewService.getById(contractId);
        if (cr == null) return;

        // 2. 处理综管部驳回的情况 (XML逻辑: 综管部驳回直接结束)
        if (!approved && "gmSign1".equals(completedTaskKey)) {
            cr.setStatus("已驳回");
            cr.setCurrentApprover(null);
            contractReviewService.updateById(cr);
            return;
        }

        // 3. 查询流程实例状态
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (pi == null) {
            // 流程实例已不存在 -> 流程正常结束
            // 【修改点5】：确保只有 approved=true 时才设为 "已完成"
            if (approved) {
                cr.setStatus("已完成"); 
            } else {
                cr.setStatus("已驳回"); 
            }
            cr.setCurrentApprover(null); // 流程结束，清空审批人
        } else {
            // 4. 流程还在运行 -> 查询当前活跃的任务
            List<Task> activeTasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();

            if (!activeTasks.isEmpty()) {
                Task nextTask = activeTasks.get(0);
                String nextTaskKey = nextTask.getTaskDefinitionKey();
                String nextAssignee = nextTask.getAssignee(); 

                cr.setCurrentApprover(nextAssignee);

                switch (nextTaskKey) {
                    case "gmSign1":
                        cr.setStatus("待综合管理部审批");
                        break;
                    case "legalReview":
                        cr.setStatus("待法务审核");
                        break;
                    case "userModify":
                        cr.setStatus("待使用部门修改");
                        break;
                    case "adminFinal":
                        cr.setStatus("待行政办终审");
                        break;
                    default:
                        cr.setStatus("审批中");
                }
            }
        }
        
        // 5. 执行更新
        contractReviewService.updateById(cr);
    }
}