package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.entity.LeaveAttachment;

import java.util.List;

/**
 * 附件服务接口（定义业务方法）
 */
public interface AttachmentService extends IService<LeaveAttachment> {

    /**
     * 上传附件并关联请假单
     * @param file 上传的文件
     * @param leaveId 请假单ID
     * @param uploadUserId 上传人ID
     * @return 保存后的附件对象
     */
    LeaveAttachment upload(MultipartFile file, Long leaveId, Long uploadUserId);

    /**
     * 根据请假单ID查询附件列表（包含临时下载URL）
     */
    List<LeaveAttachment> getByLeaveId(Long leaveId);

    /**
     * 删除附件（同时删除MinIO中的文件）
     */
    void delete(Long attachmentId, int operatorId);
}