package org.xj_service.oa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.LeaveAttachment;
import org.xj_service.oa.service.AttachmentService;
import org.xj_service.oa.service.FileStorageService;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 附件控制器（提供API接口）
 */
@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    @Qualifier("minioFileStorageService")
    private FileStorageService fileService;
    @Autowired
    private org.xj_service.oa.service.UploadService uploadService;

    /**
     * 上传请假单附件
     */
    @PostMapping("/leave")
    public Result uploadLeaveAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "leaveId", required = false) Long leaveId,
            @RequestParam("uploadUserId") Long uploadUserId) {

        LeaveAttachment attachment = attachmentService.upload(file, leaveId, uploadUserId);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("attachment", attachment);
        return Result.success(resultData);
    }

    /**
     * 上传派车记录附件（直接上传到 MinIO，不写入独立表）
     * 返回附件元数据：{ name, filePath, fileSize, fileType, fileUrl }
     */
    @PostMapping("/dispatch")
    public Result uploadDispatchAttachment(@RequestParam("file") MultipartFile file) {
        // 上传到 MinIO（桶名：dispatch-attachments，业务类型：dispatch）
        String filePath = fileService.upload(file, "dispatch-attachments", "dispatch");
        Map<String, Object> meta = new HashMap<>();
        meta.put("name", file.getOriginalFilename());
        meta.put("filePath", filePath);
        meta.put("fileSize", file.getSize());
        meta.put("fileType", file.getContentType());
        // 生成临时访问 URL（1 小时）
        try {
            String url = fileService.getTempUrl(filePath, 3600);
            meta.put("fileUrl", url);
        } catch (Exception e) {
            // 忽略临时 URL 失败，仍返回 filePath
            meta.put("fileUrl", null);
        }
        // 持久化到 uploads 表，返回 uploadId
        try {
            org.xj_service.oa.entity.Upload u = new org.xj_service.oa.entity.Upload();
            // 对应数据库字段：name, storage_path, mime_type, size, origin
            u.setName(file.getOriginalFilename());
            u.setStoragePath(filePath);
            u.setMimeType(file.getContentType());
            u.setSize(file.getSize());
            u.setOrigin("dispatch");
            // created_by 可以从登录上下文获取，暂不设置
            uploadService.create(u);
            meta.put("uploadId", u.getId());
        } catch (Exception ex) {
            // 如果持久化失败，仍返回上传的 filePath 给前端
        }
        Map<String, Object> result = new HashMap<>();
        result.put("attachment", meta);
        return Result.success(result);
    }

    /**
     * 上传行政公示附件（上传到 MinIO 并写入 uploads 表）
     * 支持可选的 noticeId 参数用于立即与某条公示关联：/api/attachments/notice?noticeId=123
     */
    @PostMapping("/notice")
    public Result uploadNoticeAttachment(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "noticeId", required = false) Long noticeId) {
        // 上传到 MinIO（桶名：notice-attachments，业务类型：notice）
        String filePath = fileService.upload(file, "notice-attachments", "notice");
        java.util.Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("name", file.getOriginalFilename());
        meta.put("filePath", filePath);
        meta.put("fileSize", file.getSize());
        meta.put("fileType", file.getContentType());
        try {
            String url = fileService.getTempUrl(filePath, 3600);
            meta.put("fileUrl", url);
        } catch (Exception e) {
            meta.put("fileUrl", null);
        }
        // 持久化到 uploads 表，返回 uploadId
        try {
            org.xj_service.oa.entity.Upload u = new org.xj_service.oa.entity.Upload();
            u.setName(file.getOriginalFilename());
            u.setStoragePath(filePath);
            u.setMimeType(file.getContentType());
            u.setSize(file.getSize());
            u.setOrigin("notice");
            u.setOriginId(noticeId);
            uploadService.create(u);
            meta.put("uploadId", u.getId());
        } catch (Exception ex) {
            // 忽略持久化失败，仍返回元数据
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("attachment", meta);
        return Result.success(result);
    }

    /**
     * 删除派车附件（根据存储路径删除 MinIO 对象）
     */
    @DeleteMapping("/dispatch")
    public Result deleteDispatchAttachment(@RequestParam("filePath") String filePath) {
        fileService.delete(filePath);
        return Result.success("删除成功");
    }

    /**
     * 预览附件（代理访问、流式返回）。
     * 目的：为普通用户提供只能在浏览器中查看但尽量减少直接下载的能力。
     * 注意：无法完全阻止用户保存文件，此接口通过 Content-Disposition:inline
     * 和尽量少暴露真实存储路径来降低被直接下载的便利性。
     */
    @GetMapping("/preview/{id}")
    public void previewAttachment(@PathVariable("id") Long id, javax.servlet.http.HttpServletResponse response) {
        try {
            org.xj_service.oa.entity.Upload u = uploadService.getById(id);
            if (u == null || u.getStoragePath() == null) {
                response.setStatus(404);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"404\",\"message\":\"附件不存在\"}");
                return;
            }
            String filePath = u.getStoragePath();
            java.io.InputStream in = fileService.download(filePath);
            if (in == null) {
                response.setStatus(404);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"404\",\"message\":\"附件不存在\"}");
                return;
            }
            // 设置为 inline，建议浏览器在内嵌查看
            response.setContentType(u.getMimeType() != null ? u.getMimeType() : "application/pdf");
            String filename = u.getName() == null ? ("file_" + id) : u.getName();
            // 使用 inline，尽量避免 attachment
            response.setHeader("Content-Disposition",
                    "inline; filename=\"" + java.net.URLEncoder.encode(filename, "UTF-8") + "\"");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            // 防止浏览器直接下载的提示（非标准）
            response.setHeader("X-Download-Options", "noopen");

            // 流式写出
            java.io.OutputStream out = response.getOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            try {
                in.close();
            } catch (Exception ignore) {
            }
        } catch (Exception e) {
            try {
                response.reset();
                response.setStatus(500);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"500\",\"message\":\"预览失败: " + e.getMessage() + "\"}");
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 查询请假单的所有附件
     */
    @GetMapping("/leave/{leaveId}")
    public Result getLeaveAttachments(@PathVariable Long leaveId) {
        List<LeaveAttachment> attachments = attachmentService.getByLeaveId(leaveId);
        return Result.success(attachments);
    }

    /**
     * 删除附件
     */
    @DeleteMapping("/{id}")
    public Result deleteAttachment(
            @PathVariable Long id,
            @RequestParam("operatorId") int operatorId) {

        attachmentService.delete(id, operatorId);
        return Result.success("附件删除成功");
    }
}