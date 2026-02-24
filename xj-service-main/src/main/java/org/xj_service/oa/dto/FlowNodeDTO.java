package org.xj_service.oa.dto;

import lombok.Data;

import java.util.List;

@Data
public class FlowNodeDTO {
    /**
     * 节点ID
     */
    private String activityId;

    /**
     * 节点名称
     */
    private String activityName;

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 是否当前节点
     */
    private Boolean current = false;
}
