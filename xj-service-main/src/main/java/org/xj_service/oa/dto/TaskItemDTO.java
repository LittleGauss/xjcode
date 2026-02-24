package org.xj_service.oa.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.xj_service.oa.entity.LeaveProcess;

import java.util.Date;

/**
 * 任务列表项（附带业务请假单信息）
 */
@Data
public class TaskItemDTO {
    private String id;
    private String name;
    private Date createTime;
    private String assignee;
    private String processInstanceId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long leaveId;

    // 简便起见直接返回完整的请假单实体（包含类型/起止时间/原因/申请人等）
    private LeaveProcess leave;

    private String taskDefinitionKey;
}
