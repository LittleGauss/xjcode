package org.xj_service.oa.dto;

import lombok.Data;

@Data
public class TaskCompleteRequest {
    private String taskId;      // Flowable的任务ID (必须)
    private Long contractId;    // 合同ID (用于查找任务，如果前端没传taskId)
    private boolean approved;   // true=通过, false=驳回
    private String comment;     // 审批意见
    // private String nextApprover;// 下一步审批人 (可选)
}