package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_leave_report_back")
@ApiModel(value = "LeaveReportBack对象", description = "请假销假归档表")
public class LeaveReportBack {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("关联请假单ID")
    private Long leaveId;

    @ApiModelProperty("流程实例ID")
    private String processInstanceId;

    @ApiModelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @ApiModelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @ApiModelProperty("销假申请时间")
    private LocalDateTime reportBackTime;

    @ApiModelProperty("状态: PENDING-待确认, CONFIRMED-已确认, ARCHIVED-已归档")
    private String status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
