package org.xj_service.oa.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.xj_service.oa.entity.SupervisionTask;

/**
 * 督查任务DTO，包含用户和部门信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupervisionTaskDTO extends SupervisionTask {

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 创建人部门名称
     */
    private String creatorDepartment;

    /**
     * 当前审核人姓名
     */
    private String currentApprover;
}
