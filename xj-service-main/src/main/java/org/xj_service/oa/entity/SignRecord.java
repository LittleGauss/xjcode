package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("oa_sign_record")
@ApiModel(value = "SignRecord对象", description = "电子签名记录表")
public class SignRecord {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("签名人 ID 关联sys_user.id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("签名图片存储路径 MinIO路径")
    @TableField("sign_url")
    private String signUrl;

    @ApiModelProperty("签名时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("扩展字段 1 可存储签名备注")
    @TableField("ext1")
    private String ext1;
}
