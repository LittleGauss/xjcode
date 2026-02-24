package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("oa_leave_process")
public class LeaveProcess {
    /**
     * 请假申请实体类
     *
     * 该类用于表示员工的请假申请信息，包含请假的基本信息、审批状态和时间信息等。
     * 主要用于请假流程的管理和跟踪。
     */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                    // 请假申请唯一标识符

    private Integer userId;             // 申请用户的ID

    @TableField("user_name")
    private String username;            // 申请用户名称

    private String processInstanceId;   // 流程实例ID，用于关联工作流引擎中的流程实例

    private String leaveType;           // 请假类型

    private LocalDateTime startDate;    // 请假开始时间

    private LocalDateTime endDate;      // 请假结束时间
    
    private Double duration;

    private String reason;              // 请假原因

    private String status;              // 请假申请状态：DRAFT(草稿), SUBMITTED-待审批/APPROVED-已批准/REJECTED-已拒绝

    private String currentApprover;     // 当前审批人

    private String currentApproverName;     // 当前审批人姓名

    private LocalDateTime createdAt;    // 记录创建时间

    private LocalDateTime updatedAt;    // 记录最后更新时间
    
    private String departmentId;

    private String departmentName;
}
