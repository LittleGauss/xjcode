package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oa_consumable_scrap_item")
public class ConsumableScrapItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer scrapId;

    private Integer goodsId;
    private String goodsName;

    private Integer categoryId;
    private String categoryName;

    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

}
