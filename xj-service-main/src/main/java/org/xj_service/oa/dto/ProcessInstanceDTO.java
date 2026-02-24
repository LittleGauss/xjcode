package org.xj_service.oa.dto;
import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * 流程实例数据传输对象
 * 用于在API层和前端之间传递流程实例信息
 */
@Data
public class ProcessInstanceDTO {

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 流程定义名称（如：请假流程、报销流程）
     */
    private String processDefinitionName;

    /**
     * 流程定义Key（如：leave_process、expense_process）
     */
    private String processDefinitionKey;

    /**
     * 业务键（关联业务数据的ID，如：请假单ID）
     */
    private String businessKey;

    /**
     * 流程启动时间
     */
    private Date startTime;

    /**
     * 流程结束时间（如果已结束）
     */
    private Date endTime;

    /**
     * 流程状态：ACTIVE（进行中）, SUSPENDED（挂起）, ENDED（已结束）
     */
    private String status;

    /**
     * 流程启动人ID
     */
    private String startUserId;

    /**
     * 流程启动人姓名
     */
    private String startUserName;

    /**
     * 当前处理人（如果有）
     */
    private String currentAssignee;

    /**
     * 当前节点名称
     */
    private String currentActivityName;

    /**
     * 流程耗时（如果已结束）
     */
    private Long duration;

    /**
     * 扩展字段：用于前端自定义显示
     */
    private Map<String, Object> extProperties;
}