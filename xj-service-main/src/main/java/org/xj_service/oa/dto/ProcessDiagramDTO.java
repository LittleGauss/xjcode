package org.xj_service.oa.dto;


import lombok.Data;

import java.util.List;

@Data
public class ProcessDiagramDTO {
    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * Base64格式的流程图
     */
    private String diagramImage;

    /**
     * 当前节点列表
     */
    private List<FlowNodeDTO> currentNodes;

    /**
     * 流程定义名称
     */
    private String processName;
}