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
@TableName("oa_routine_inspection")
@ApiModel(value = "RoutineInspection对象", description = "")
public class RoutineInspection implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

    private String inspectorName;

    private LocalDate inspectDate;

    private String targetDepartment;

    private String result;

    private String remarks;

    private LocalDateTime createdAt;


}
