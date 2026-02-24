package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("oa_approval_comment")
@ApiModel(value = "ApprovalComment对象", description = "审批意见表")
public class ApprovalComment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("流程实例ID")
    private String processInstanceId;

    @ApiModelProperty("关联业务ID")
    private Integer businessId;

    @ApiModelProperty("业务类型(leave/contract等)")
    private String businessType;

    @ApiModelProperty("审批人ID")
    private Integer approverId;

    @ApiModelProperty("审批人名称")
    private String approverName;

    @ApiModelProperty("审批节点")
    private String approvalNode;

    @ApiModelProperty("审批意见内容")
    private String comment;

    @ApiModelProperty("审批结果(APPROVED/REJECTED)")
    private String approvalResult;

    @ApiModelProperty("审批时间")
    private LocalDateTime approvedTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}