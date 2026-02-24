package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("oa_consumable_category")
public class ConsumableCategory implements Serializable {
    private Integer id;
    private String categoryName;
    private Integer sort;
}