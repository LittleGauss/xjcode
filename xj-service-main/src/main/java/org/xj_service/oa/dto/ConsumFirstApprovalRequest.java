package org.xj_service.oa.dto;

import lombok.Data;

/**
 * 耗材申请 审批请求类
 */
@Data
public class ConsumFirstApprovalRequest {
    private String taskId;//工作流任务id
    private Integer applicationId;//业务表申请记录
    private boolean firstApproved;//通过还是驳回
    private String comment;//审批意见
    private Integer logisticsApprover;//最终审批人

}
