package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 与数据库中已存在的 `uploads` 表结构对齐的实体
 */
@Data
@TableName("uploads")
public class Upload {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 对应 SQL: `name`
    @TableField("name")
    private String name;

    // 对应 SQL: `mime_type`
    @TableField("mime_type")
    private String mimeType;

    @TableField("size")
    private Long size;

    // 对应 SQL: `storage_path`
    @TableField("storage_path")
    private String storagePath;

    // 可用于标记来源，例如 'dispatch', 'leave' 等
    @TableField("origin")
    private String origin;

    @TableField("origin_id")
    private Long originId;

    // 自定义元数据（JSON）
    @TableField("meta")
    private String meta;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
