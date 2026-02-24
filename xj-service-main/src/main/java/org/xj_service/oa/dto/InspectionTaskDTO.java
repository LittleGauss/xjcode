package org.xj_service.oa.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 检查任务发起请求DTO
 */
@Data
public class InspectionTaskDTO {
    // 基础任务信息
    private String taskTitle; // 任务标题
    private String taskDesc; // 任务描述
    private Long initiatorId; // 发起人ID
    private String initiatorName; // 发起人名称

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline; // 截止时间（适配前端格式化字符串）

    private Long templateId; // 模板ID
    private List<Long> inspectorIds; // 检查员ID列表
}