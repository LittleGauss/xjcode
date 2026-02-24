package org.xj_service.oa.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@TableName("supervision_feedback_upload")
public class SupervisionFeedbackUpload implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableField("feedback_id")
    private Long feedbackId;
    @TableField("upload_id")
    private Long uploadId;
}
