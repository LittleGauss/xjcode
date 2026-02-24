package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("oa_consumable_stock_in_approval")
public class ConsumableStockInApproval {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String applyNo;           // 入库单号
    private Integer applicantId;      // 申请人ID
    private String applicantName;     // 申请人姓名
    private String applicantDept;     // 申请人部门
    private String remark;           // 入库说明

    private String status;           // 状态：PENDING, APPROVED, REJECTED
    private Integer approverId;      // 审批人ID(后保部)
    private String approverName;     // 审批人姓名
    private String approveRemark;    // 审批意见
    private LocalDateTime approveTime; // 审批时间

    // 或者分开字段存储
    private String itemName;
    private Integer categoryId;
    private String categoryName;
    private String spec;
    private Integer quantity;
    private String unit;
    private String brand;
    private String supplier;
    private BigDecimal unitPrice;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private Integer warningValue;

    private LocalDateTime applyTime;  // 申请时间
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
    @TableField(exist = false)
    private String applicantNickname;
}
