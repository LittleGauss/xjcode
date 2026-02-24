package org.xj_service.oa.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TaskAssignmentDTO {
    private Long assignmentId;
    private String action; // RECEIVE, REJECT, COMPLETE
    private String rejectReason;
    private MultipartFile templateFiles;// 完成任务时上传的文件
}