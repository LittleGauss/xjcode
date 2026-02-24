package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 请假单附件实体类
 * 对应数据库表：oa_leave_attachment
 */
@Data
@TableName("oa_leave_attachment")
public class LeaveAttachment {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 关联的请假单ID（外键）
     */
    @TableField("leave_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long leaveId;

    /**
     * 文件名（原始文件名）
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件存储路径（如MinIO的bucket+objectName）
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型（MIME类型，如application/pdf、image/jpeg）
     */
    @TableField("file_type")
    private String fileType;

    // 新增：上传人ID（关联业务系统用户表）
    @TableField("upload_user_id") // 映射数据库字段
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uploadUserId;
    /**
     * 上传时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 非数据库字段：文件访问URL（用于前端下载，可动态生成）
     */
    @TableField(exist = false)
    private String fileUrl;
}