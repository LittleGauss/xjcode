package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oa_inspection_task") // 新增oa_前缀
public class InspectionTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String processInstanceId; // Flowable流程实例ID
    private String processDefinitionId; // Flowable流程定义ID
    private String taskTitle;
    private String taskDesc;
    private Long initiatorId;
    private String initiatorName;
    private String inspectionFormUrl;
    private String status; // DRAFT/IN_PROGRESS/COMPLETED/CANCELLED
    private Integer totalInspectors;
    private Integer completedInspectors;
    private LocalDateTime deadline; // 截止日期
    private String overdueFlag; // YES/NO
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}