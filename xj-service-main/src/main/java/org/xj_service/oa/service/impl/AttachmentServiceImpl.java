package org.xj_service.oa.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.entity.LeaveAttachment;
import org.xj_service.oa.mapper.LeaveAttachmentMapper;
import org.xj_service.oa.service.AttachmentService;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.IUserService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 附件服务实现类（核心业务逻辑）
 */
@Slf4j
@Service
public class AttachmentServiceImpl extends ServiceImpl<LeaveAttachmentMapper, LeaveAttachment>
        implements AttachmentService {

    // 注入文件存储服务（之前实现的通用文件服务）
    @Autowired
    @Qualifier("minioFileStorageService") // 或 "fileStorageService"，根据实际使用的实现类选择
    private FileStorageService fileService;
    // 注入用户服务（用于权限校验，根据实际业务调整）
    @Autowired
    private IUserService userService;

    /**
     * 上传附件并关联请假单
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务保证：文件上传和数据库记录一致
    public LeaveAttachment upload(MultipartFile file, Long leaveId, Long uploadUserId) {
        try {
            // 1. 校验参数
            if (uploadUserId == null) {
                throw new IllegalArgumentException("上传人ID不能为空");
            }

            // 2. 调用FileService上传文件到MinIO
            // 桶名：leave-attachments，业务类型：leave
            String filePath = fileService.upload(file, "leave-attachments", "leave");

            // 3. 构建附件对象
            LeaveAttachment attachment = new LeaveAttachment();
            attachment.setLeaveId(leaveId);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(filePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(file.getContentType());
            attachment.setCreatedAt(LocalDateTime.now());
            attachment.setUploadUserId(uploadUserId); // 必须赋值

            // 4. 保存到数据库
            baseMapper.insert(attachment);
            log.info("附件上传成功：{}，关联请假单：{}", attachment.getId(), leaveId);
            return attachment;
        } catch (Exception e) {
            log.error("附件上传失败", e);
            throw new RuntimeException("附件上传失败：" + e.getMessage());
        }
    }

    /**
     * 根据请假单ID查询附件列表（生成临时下载URL）
     */
    @Override
    public List<LeaveAttachment> getByLeaveId(Long leaveId) {
        // 1. 从数据库查询附件
        List<LeaveAttachment> attachments = baseMapper.selectByLeaveId(leaveId);
        if (attachments.isEmpty()) {
            return attachments;
        }

        // 2. 为每个附件生成临时下载URL（1小时有效）
        attachments.forEach(attachment -> {
            String tempUrl = fileService.getTempUrl(attachment.getFilePath(), 3600);
            attachment.setFileUrl(tempUrl);
        });
        return attachments;
    }

    /**
     * 删除附件（同时删除MinIO文件）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long attachmentId, int operatorId) {
        // 1. 查询附件信息
        LeaveAttachment attachment = baseMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("附件不存在");
        }

        // 2. 权限校验（仅上传人或管理员可删除）
        boolean isUploader = attachment.getUploadUserId().equals(operatorId);
        boolean isAdmin = userService.isAdmin(operatorId); // 假设UserService有此方法
        if (!isUploader && !isAdmin) {
            throw new RuntimeException("无权限删除此附件");
        }

        // 3. 删除MinIO中的文件
        fileService.delete(attachment.getFilePath());

        // 4. 删除数据库记录
        baseMapper.deleteById(attachmentId);
        log.info("附件删除成功：{}，操作人：{}", attachmentId, operatorId);
    }
}