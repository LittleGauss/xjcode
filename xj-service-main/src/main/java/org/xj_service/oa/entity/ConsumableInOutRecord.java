package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oa_consumable_in_out_record")
public class ConsumableInOutRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer goodsId;//物品id
    private String itemName;//物品名
    private Integer categoryId;
    private String categoryName;

    private String operationType; // IN-入库, OUT-出库

    private Integer quantityChange;
    private Integer quantityBefore;
    private Integer quantityAfter;

    private Integer operatorId;//入库人id
    private String operatorName;//入库人名
    private String operatorDept;//入库人部门
    // 新增：操作人昵称（从user表关联查询）
    @TableField(exist = false)
    private String operatorNickname;

    private LocalDateTime operationTime;//入库时间

    private String supplier;
    private BigDecimal unitPrice; //单价
    private BigDecimal totalPrice;

    private String remark;

}