package org.xj_service.oa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.dto.ConsumFinalApprovalRequest;
import org.xj_service.oa.dto.ConsumFirstApprovalRequest;
import org.xj_service.oa.entity.ConsumableApplication;
import org.xj_service.oa.mapper.ConsumableApplicationMapper;
import org.xj_service.oa.service.ConsumableApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumable/application")
public class ConsumableApplicationController {

    @Autowired
    private ConsumableApplicationService applicationService;
    @Autowired
    private ConsumableApplicationMapper applicationMapper;
    private static final Logger log = LoggerFactory.getLogger(ConsumableApplicationController.class);

    /**
     * 提交领用申请
     */
    @PostMapping("/submit")
    public Result submit(@RequestBody ConsumableApplication application) {
        boolean success = applicationService.submitApplication(application);
        return success ? Result.success("申请提交成功，等待审批") : Result.error("500","申请提交失败");
    }

    /**
     * 根据申请人ID查询我的申请
     */
    @GetMapping("/my")
    public Result getMyApplications(@RequestParam Integer applicantId) {
        List<ConsumableApplication> list = applicationService.getByApplicantId(applicantId);
        return Result.success(list);
    }

    // 获取待办任务
    @GetMapping("/todo-tasks")
    public Result getTodoTasks(@RequestParam String userId) {
        List<Map<String, Object>> list =applicationService.getTodoTasks(userId);
        return Result.success(list);
    }

    /**
     * 获取待审批的申请（按耗材类型）
     */
    @GetMapping("/pending")
    public Result getPendingApplications(@RequestParam String consumableType) {
        List<ConsumableApplication> list = applicationService.getPendingByType(consumableType);
        return Result.success(list);
    }

    /**
     * 根据ID查询申请详情
     */
    @GetMapping("/{id}")
    public Result getDetail(@PathVariable Integer id) {
        ConsumableApplication application = applicationService.getById(id);
        return application != null ? Result.success(application) : Result.error("500","申请不存在");
    }

    /**
     * 更新申请状态（工作流审批后调用）
     */
    @PutMapping("/status")
    public Result updateStatus(
            @RequestParam Integer applicationId,
            @RequestParam String currentStatus
    ) {
        boolean success = applicationService.updateStatus(applicationId, currentStatus);
        return success ? Result.success("状态更新成功") : Result.error("500","状态更新失败");
    }

    /**
     * 获取所有领用申请
     */
    @GetMapping("/all")
    public Result getAllApplications() {
        List<ConsumableApplication> list = applicationService.getAllApplications();
        return Result.success(list);
    }

    /**
     * 删除申请（仅待审批状态可删除）
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        ConsumableApplication application = applicationService.getById(id);
        if (application == null) {
            return Result.error("500","申请不存在");
        }
        if (!"pending_first_approval".equals(application.getFinalStatus())) {
            return Result.error("500","仅待一级审批状态的申请可删除");
        }
        boolean success = applicationService.removeById(id);
        return success ? Result.success("申请删除成功") : Result.error("500","申请删除失败");
    }

    // 一级审批String taskId, Integer applicationId, boolean firstApproved, String comment,Integer logisticsApprover
    @PostMapping("/first-approval")
    public Result firstApproval(@RequestBody ConsumFirstApprovalRequest request) {
        applicationService.completeFirstApproval(request.getTaskId(),
                request.getApplicationId(),
                request.isFirstApproved(),
                request.getComment(),
                request.getLogisticsApprover()
        );
        return Result.success("审批成功") ;
    }

    // 二级审批
    @PostMapping("/final-approval")
    public Result finalApproval(@RequestBody ConsumFinalApprovalRequest request) {
        //actualDistributeQuantity 实际发放耗材数量
        applicationService.completeFinalApproval(
                request.getTaskId(),
                request.getApplicationId(),
                request.isFinalApproved(),
                request.getComment(),
                request.getActualDistributeQuantity());
        return Result.success("审批完成，流程结束") ;
    }

    // 查询申请单详情
    @GetMapping("/findbyId/{id}")
    public Result getApplicationDetail(@PathVariable Integer id) {
        ConsumableApplication application =applicationService.getApplicationDetail(id);
        return Result.success(application);
    }

    /**
     * 获取审批历史（根据审批人ID）
     */

    @GetMapping("/approval-history")
    public Result getApprovalHistory(@RequestParam(required = false) Integer approverId) {
        // 1. 校验参数合法性（避免无效参数查库）
        if (approverId == null || approverId <= 0) {
            log.warn("查询审批历史失败：approverId不合法，值为：{}", approverId);
            return Result.error("400", "审批人ID不合法（不能为空且必须为正整数）");
        }

        try {
            // 2. 执行查询逻辑
            List<ConsumableApplication> history = applicationService.getApprovalHistory(approverId);

            // 3. 处理查询结果为null的情况（避免返回null导致前端处理异常）
            if (history == null) {
                history = new ArrayList<>();
            }

            log.info("查询审批历史成功，approverId={}，共查询到{}条记录", approverId, history.size());
            return Result.success(history);
        } catch (Exception e) {
            // 4. 捕获所有异常，记录详细日志并返回友好提示
            log.error("查询审批历史失败，approverId={}", approverId, e);
            return Result.error("500", "服务器查询审批历史时出错，请稍后重试");
        }
    }
}