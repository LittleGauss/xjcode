package org.xj_service.oa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.dto.ContractProcessRequest;
import org.xj_service.oa.dto.TaskCompleteRequest; // 确保引入了这个 DTO
import org.xj_service.oa.service.IContractProcessService;
import org.xj_service.oa.service.IContractReviewService;

@RestController
@RequestMapping("/api/contract-process")
public class ContractProcessController {

    private static final Logger log = LoggerFactory.getLogger(ContractProcessController.class);

    @Resource
    private IContractProcessService contractProcessService;

    @Autowired
    private TaskService taskService;

    // 1. 启动流程接口 (保持不变)
    @PostMapping
    public Result startContractProcess(@RequestBody ContractProcessRequest req) {
        if (req == null || req.getContractReview() == null) {
            return Result.error("400", "请求体缺失 contractReview 字段");
        }
        try {
            String pid = contractProcessService.startContractReview(
                    req.getContractReview(),
                    req.getStarter(),
                    req.getGmUser(),
                    req.getLegalUser(),
                    req.getAdminUser()
            );
            return Result.success("流程启动成功: " + pid);
        } catch (Exception e) {
            log.error("启动合同流程失败", e);
            return Result.error("500", "启动流程失败: " + e.getMessage());
        }
    }

    // 2. 获取当前待办任务ID (保持不变)
    @Autowired
    private IContractReviewService contractReviewService; // 确保注入了业务Service

    @GetMapping("/task-by-contract/{contractId}")
    public Result getTaskByContract(@PathVariable Long contractId, @RequestParam String userId) {
        try {
            // 1. 先查业务表，获取 流程实例ID (ProcessInstanceId)
            // 这是最可靠的关联方式，比 BusinessKey 稳
            org.xj_service.oa.entity.ContractReview contract = contractReviewService.getById(contractId);
            
            if (contract == null) {
                return Result.error("404", "合同不存在");
            }
            
            String procInstId = contract.getProcessInstanceId();
            if (procInstId == null || procInstId.isEmpty()) {
                return Result.error("400", "该合同尚未启动审批流程");
            }

            // 2. 使用 processInstanceId 精准查询任务
            Task task = taskService.createTaskQuery()
                    .processInstanceId(procInstId) // <--- 关键修改：不用 BusinessKey 了
                    .taskAssignee(userId)          // 必须是当前用户
                    .singleResult();

            if (task != null) {
                return Result.success(task.getId());
            } else {
                // 调试信息：如果还是查不到，看看是不是任务已经完成了，或者在别人手里
                List<Task> anyTask = taskService.createTaskQuery()
                        .processInstanceId(procInstId)
                        .list();
                if (anyTask.isEmpty()) {
                    return Result.error("404", "流程可能已结束，未找到活跃任务");
                } else {
                    Task t = anyTask.get(0);
                    return Result.error("403", "任务存在但无权审批。当前处理人ID: " + t.getAssignee());
                }
            }

        } catch (Exception e) {
            log.error("查询任务失败", e);
            return Result.error("500", "查询任务失败: " + e.getMessage());
        }
    }

    // ==========================================
    // 【重点修改】完成审批接口
    // ==========================================
    @PostMapping("/complete")
    public Result completeTask(@RequestBody TaskCompleteRequest req) {
        try {
            if (req.getTaskId() == null || req.getTaskId().isEmpty()) {
                return Result.error("400", "任务ID不能为空");
            }

            // 【修改点】：直接调用 Service，只传 3 个参数
            // 删除了 req.getNextApprover()
            contractProcessService.completeTask(
                req.getTaskId(), 
                req.isApproved(), 
                req.getComment()
            );
            
            return Result.success("操作成功");
        } catch (Exception e) {
            log.error("审批失败", e);
            return Result.error("500", "审批失败: " + e.getMessage());
        }
    }

    // 4. 获取任务列表 (保持不变)
    @GetMapping("/tasks")
    public Result getTasks(@RequestParam String userId) {
        try {
            return Result.success(contractProcessService.getTasksForUser(userId));
        } catch (Exception e) {
            return Result.error("500", "获取任务失败: " + e.getMessage());
        }
    }
}