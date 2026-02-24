package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检查任务模板实体
 */
@Data
@TableName("oa_inspection_template")
public class InspectionTemplate {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 文件类型（如docx、pdf、xlsx）
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MinIO存储路径（bucket/objectName）
     */
    private String filePath;

    /**
     * 上传人ID
     */
    private Long uploaderId;

    /**
     * 上传人名称
     */
    private String uploaderName;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 业务类型（固定为inspection-template）
     */
    private String bizType = "inspection-template";

    /**
     * 存储桶名称
     */
    private String bucketName;
}