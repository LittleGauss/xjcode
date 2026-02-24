package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
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
@TableName("oa_administrative_notice")
@ApiModel(value = "AdministrativeNotice对象", description = "")
public class AdministrativeNotice implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

    private String title;

    private String content;

    private LocalDate issueDate;

    private LocalDate effectiveDate;

    private LocalDate expireDate;

    private String department;

    private String status;

    @TableField(value = "created_at") //指定数据库的字段名称
    private LocalDateTime createdAt;

}
