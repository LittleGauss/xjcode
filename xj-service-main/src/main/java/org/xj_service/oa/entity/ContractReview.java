package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import cn.hutool.core.annotation.Alias;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author 杜泽淇
 * @since 2025-08-13
 */
@Getter
@Setter
@TableName("oa_contract_review")
@ApiModel(value = "ContractReview对象", description = "")
public class ContractReview implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

    private String contractName;

    private String applicantName;

    private String department;

    private String contractType;

    private LocalDate submissionDate;

    private String status;

    private String reviewComments;

    private LocalDateTime createdAt;
    
    private String currentApprover;     // 当前审批人

    private String processInstanceId;   // 流程实例ID，用于关联工作流引擎中的流程实例



}
