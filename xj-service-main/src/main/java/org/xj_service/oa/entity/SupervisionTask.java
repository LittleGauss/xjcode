package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("oa_supervision_task")
public class SupervisionTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDate dueDate;

    private String status;

    private Integer progress;

    private LocalDateTime lastUpdate;

    private Long noticeId;

    private String processInstanceId;

    // Explicit getter/setter to avoid Lombok-processing issues during build
    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
