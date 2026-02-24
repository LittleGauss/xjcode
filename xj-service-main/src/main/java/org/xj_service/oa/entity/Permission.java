package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("sys_permission")
@ApiModel(value = "Permission对象", description = "权限表")
public class Permission {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("权限编码")
    private String code;

    @ApiModelProperty("权限名称")
    private String name;

    @ApiModelProperty("权限描述")
    private String description;

    @ApiModelProperty("资源类型(菜单/按钮/API等)")
    private String resourceType;

    @ApiModelProperty("资源路径")
    private String resourcePath;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
