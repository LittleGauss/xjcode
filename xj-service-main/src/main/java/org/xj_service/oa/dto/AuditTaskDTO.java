package org.xj_service.oa.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;

@Data
public class AuditTaskDTO {
    @NotEmpty(message = "审核结果不能为空")
    private String auditResult; // PASS, REJECT

    private String rejectReason;

    @NotEmpty(message = "流程任务ID不能为空")
    private String flowTaskId;
}