package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("oa_supervision_assignment")
public class SupervisionAssignment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long assigneeUserId;

    private Long assigneeDeptId;

    private Long assignedBy;

    private LocalDateTime assignedAt;

    private String status;

    private LocalDateTime completedAt;

    // 对应 Flowable 的 task id（如果有）
    private String flowableTaskId;
}
