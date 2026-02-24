package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_dispatch_attachment")
public class DispatchAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("dispatch_id")
    private Long dispatchId;

    @TableField("upload_id")
    private Long uploadId;

    @TableField("name")
    private String name;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
