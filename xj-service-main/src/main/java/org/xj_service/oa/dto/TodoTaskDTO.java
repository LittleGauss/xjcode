package org.xj_service.oa.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;
/**
 * 任务信息实体类
 * 用于封装流程任务的相关信息，包括任务的基本属性、业务数据和流程变量等
 * assignee:"19"
 * businessData:Object
 * businessKey:"20"
 * createTime:"2025-12-03T15:56:36.012+00:00"
 * processDefinitionKey:"344f8695-d060-11f0-b250-005056c00008"
 * processInstanceId:"a56cbd78-d060-11f0-b250-005056c00008"
 * taskId:"a5798ec9-d060-11f0-b250-005056c00008"
 * taskName:"部门负责人审批"
 */
@Data
public class TodoTaskDTO {
    private String taskId;
    private String taskName;
    private String processInstanceId;
    private String processDefinitionKey;
    private String businessKey;
    private String businessType;
    private Object businessData; // 业务数据
    private Map<String, Object> variables;
    private Date createTime;
    private String assignee;

    // getters and setters
}
