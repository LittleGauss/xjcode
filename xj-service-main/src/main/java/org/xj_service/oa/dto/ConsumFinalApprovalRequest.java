package org.xj_service.oa.dto;

import lombok.Data;

/**
 * 耗材申请 最终审批请求类
 */
@Data
public class ConsumFinalApprovalRequest {
    private String taskId;
    private Integer applicationId;
    private boolean finalApproved;
    private String comment;
    private Integer actualDistributeQuantity;

}
