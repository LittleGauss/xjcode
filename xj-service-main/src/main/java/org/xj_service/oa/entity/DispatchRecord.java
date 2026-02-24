package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * 派车记录实体映射到数据库表 oa_vehicle_dispatch
 */
@Getter
@Setter
@TableName("oa_vehicle_dispatch")
@ApiModel(value = "DispatchRecord对象", description = "派车记录")
public class DispatchRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // database column uses `dispatch_no` in existing schema; map to that column
    @TableField("dispatch_no")
    private String dispatchNumber;

    @TableField("vehicle_id")
    private Long vehicleId;

    @TableField("plate_number")
    private String plateNumber;

    private String brand;

    private String model;

    @TableField("use_date")
    private LocalDate useDate;

    @TableField("departure_time")
    private String departureTime;

    @TableField("return_time")
    private String returnTime;

    private String department;

    @TableField("requester_name")
    private String requesterName;

    @TableField("start_mileage")
    private Long startMileage;

    @TableField("end_mileage")
    private Long endMileage;

    @TableField("trip_mileage")
    private Long tripMileage;

    @TableField("fuel_cost")
    private BigDecimal fuelCost;

    @TableField("toll_fee")
    private BigDecimal tollFee;

    @TableField("parking_fee")
    private BigDecimal parkingFee;

    @TableField("insurance_fee")
    private BigDecimal insuranceFee;

    @TableField("annual_inspection_fee")
    private BigDecimal annualInspectionFee;

    @TableField("repair_cost")
    private BigDecimal repairCost;

    private String remark;

    /**
     * 附件元数据，存 JSON 字符串或空
     */
    private String attachments;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_flag")
    private Integer deletedFlag;

    private Integer version;

}
