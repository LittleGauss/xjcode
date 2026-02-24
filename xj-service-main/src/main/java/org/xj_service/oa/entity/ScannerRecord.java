package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@TableName("oa_scanner_record")
@ApiModel(value = "ScannerRecord对象", description = "")
public class ScannerRecord implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

    private String scannerId;

    private String operatorName;

    private LocalDateTime useDate;

    private String functionSelected;

    private String cameraSelected;

    private String remarks;

    private LocalDateTime createdAt;


}
