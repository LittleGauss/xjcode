package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.ConsumableApplication;

import java.util.List;
import java.util.Map;

public interface ConsumableApplicationService extends IService<ConsumableApplication> {
    /**
     * 提交领用申请（自动填充申请时间、默认状态）
     */
    boolean submitApplication(ConsumableApplication application);

    /**
     * 根据申请人ID查询
     */
    List<ConsumableApplication> getByApplicantId(Integer applicantId);

    /**
     * 根据流程实例ID查询
     */
    ConsumableApplication getByProcessInstanceId(String processInstanceId);

    /**
     * 更新申请状态（流程审批通过/驳回后调用）
     */
    boolean updateStatus(Integer applicationId, String currentStatus);

    /**
     * 查询待审批的申请（状态为pending）
     */
    List<ConsumableApplication> getPendingByType(String consumableType);

    /**
     * 获取所有领用申请
     */
    List<ConsumableApplication> getAllApplications();

    /**
     * 根据状态筛选申请
     */
    List<ConsumableApplication> getByStatus(String status);

    /**
     * 查询申请单详情
     * @param id
     * @return
     */
    public ConsumableApplication getApplicationDetail(Integer id);
    /**
     * 根据用户id获取待办任务
     */
    public List<Map<String, Object>> getTodoTasks(String userId);

    public void completeFinalApproval(String taskId, Integer applicationId, boolean finalApproved, String comment, Integer actualDistributeQuantity) ;

    public void completeFirstApproval(String taskId, Integer applicationId, boolean firstApproved, String comment,Integer logisticsApprover);

    /**
     * 获取审批历史（根据审批人ID）
     */
    List<ConsumableApplication> getApprovalHistory(Integer approverId);
}
