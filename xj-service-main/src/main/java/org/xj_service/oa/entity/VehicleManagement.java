package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
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
@TableName("oa_vehicle_management")
@ApiModel(value = "VehicleManagement对象", description = "")
public class VehicleManagement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("plate_number")
    private String plateNumber;

    private String brand;

    private String model;

    private String department;

    private Integer mileage;

    @TableField("fuel_cost")
    private BigDecimal fuelCost;

    @TableField("repair_cost")
    private BigDecimal repairCost;

    @TableField("insurance_status")
    private String insuranceStatus;

    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 新增字段，数据库已有列 */
    private String displacement;

    @TableField("fuel_type")
    private String fuelType;

    @TableField("vehicle_status")
    private String vehicleStatus;

    @TableField("purchase_date")
    private java.time.LocalDate purchaseDate;

    private String remark;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_flag")
    private Integer deletedFlag;

    private Integer version;

}
