package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@TableName("oa_consumable_goods")
@ApiModel(value = "ConsumableGoods对象", description = "")
public class ConsumableGoods implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

    private String itemName;
    private Integer categoryId;
    private String spec;
    private Integer quantity;
    private String unit;
    private LocalDate purchaseDate;
    private String status;
    private String brand;
    private String supplier;
    private BigDecimal unitPrice;

    @DateTimeFormat(pattern = "yyyy-MM-dd") // 新增注解
    private LocalDate expireDate;
    private Integer warningValue;

    @DateTimeFormat(pattern = "yyyy-MM-dd") // 新增注解
    private LocalDateTime createdAt;
    // 非数据库字段，用于前端预警展示
    @TableField(exist = false)
    private Boolean stockWarning; // 库存预警标记

    @TableField(exist = false)
    private Boolean expireWarning; // 保质期预警标记
    // ========== 新增字段：审批相关 ==========
    @TableField(exist = false)  // 非数据库字段，用于接收前端传值
    private Boolean needApproval; // 是否需要审批

    @TableField(exist = false)  // 非数据库字段，用于接收前端传值
    private Integer logisticsApproverId; // 后保部审批人ID

    @TableField(exist = false)  // 非数据库字段，用于接收前端传值
    private String remark; // 入库说明

    @TableField(exist = false)
    private Integer operatorId; // 操作人ID（从Token解析）
}