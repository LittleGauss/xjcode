package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oa_inspection_task_assignment") // 新增oa_前缀
public class InspectionTaskAssignment implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long inspectionTaskId;
    private Long inspectorId;
    private String inspectorName;
    private String flowTaskId; // Flowable执行任务ID
    private String auditFlowTaskId; // Flowable审核任务ID
    private String taskStatus; // RECEIVED/COMPLETED/REJECTED/REDOING/OVERDUE
    private String rejectReason;
    private Integer redoCount;
    private String filledFormUrl;
    private LocalDateTime receiveTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}