package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import org.xj_service.oa.common.Result;

import org.xj_service.oa.service.IAdministrativeNoticeReadService;
import org.xj_service.oa.service.IAdministrativeNoticeService;
import org.xj_service.oa.entity.AdministrativeNotice;

import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 杜泽淇
 * @since 2025-08-13
 */
@RestController
@RequestMapping("/administrative-notice")
@Tag(name = "行政公示", description = "行政公示接口")
public class AdministrativeNoticeController {
    private static final Logger logger = LoggerFactory.getLogger(AdministrativeNoticeController.class);
    @Resource
    private IAdministrativeNoticeService administrativeNoticeService;

    @Resource
    private IAdministrativeNoticeReadService administrativeNoticeReadService;

    @Autowired
    @Qualifier("minioFileStorageService")
    private org.xj_service.oa.service.FileStorageService fileService;
    @Resource
    private org.xj_service.oa.service.UploadService uploadService;
    @Resource
    private ObjectMapper objectMapper;

    // 新增/修改行政公示
    @Operation(summary = "添加修改行政公示", description = "添加行政公示，根据id修改行政公示")
    @PostMapping
    public Result save(@RequestBody java.util.Map<String, Object> payload) {
        try {
            // payload 包含 administrativeNotice 对象和可选的 uploadIds 列表
            ObjectMapper mapper = this.objectMapper;
            AdministrativeNotice administrativeNotice = mapper.convertValue(payload.get("administrativeNotice"),
                    AdministrativeNotice.class);
            // 如果调用方直接传了 flat 对象（旧客户端），尝试把整个 payload 转为 AdministrativeNotice
            if (administrativeNotice == null || administrativeNotice.getTitle() == null) {
                administrativeNotice = mapper.convertValue(payload, AdministrativeNotice.class);
            }

            boolean saved = administrativeNoticeService.saveOrUpdate(administrativeNotice);

            // 关联上传的文件（如果提供 uploadIds）
            Object uploadIdsObj = payload.get("uploadIds");
            if (uploadIdsObj != null && administrativeNotice.getId() != null) {
                java.util.List<Long> uploadIds = mapper.convertValue(uploadIdsObj,
                        mapper.getTypeFactory().constructCollectionType(java.util.List.class, Long.class));
                if (uploadIds != null && !uploadIds.isEmpty()) {
                    // 使用 UploadService 更新 origin_id
                    try {
                        uploadService.updateOriginIds(uploadIds, administrativeNotice.getId().longValue());
                    } catch (Exception ex) {
                        // 记录异常，便于排查为什么 uploads 未被正确关联
                        logger.error("Failed to update upload origin_ids for notice {}. uploadIds={}",
                                administrativeNotice.getId(), uploadIds, ex);
                    }
                }
            }

            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("500", "保存公示失败: " + e.getMessage());
        }
    }

    // 删除
    @Operation(summary = "删除", description = "根据id删除行政公示")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            return Result.success(administrativeNoticeService.removeById(id));
        } catch (Exception e) {
            return Result.error("500", "删除失败: " + e.getMessage());
        }
    }

    // 批量删除
    @Operation(summary = "批量删除", description = "根据id批量删除行政公示")
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        try {
            return Result.success(administrativeNoticeService.removeBatchByIds(ids));
        } catch (Exception e) {
            return Result.error("500", "批量删除失败: " + e.getMessage());
        }
    }

    // 查询所有或按条件查询（支持分页和非分页两种模式）
    @Operation(summary = "查询列表", description = "查询行政公示，支持分页和按标题/部门/status模糊查询")
    @GetMapping
    public Result list(@RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        try {
            QueryWrapper<AdministrativeNotice> queryWrapper = new QueryWrapper<>();
            if (title != null && !title.isEmpty()) {
                queryWrapper.like("title", title);
            }
            if (department != null && !department.isEmpty()) {
                queryWrapper.like("department", department);
            }
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }
            queryWrapper.orderBy(true, true, "id");

            if (page != null && size != null) {
                Page<AdministrativeNotice> p = new Page<>(page, size);
                return Result.success(administrativeNoticeService.page(p, queryWrapper));
            } else {
                List<AdministrativeNotice> list = administrativeNoticeService.list(queryWrapper);
                return Result.success(list);
            }
        } catch (Exception e) {
            return Result.error("500", "查询失败: " + e.getMessage());
        }
    }

    // 查询单个
    @Operation(summary = "查询单个", description = "根据id查询单个行政公示")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        try {
            AdministrativeNotice notice = administrativeNoticeService.getById(id);
            if (notice == null) {
                return Result.success(null);
            }
            // 查询并附带附件（origin = 'notice'），并为每个附件生成临时访问 URL (fileUrl)
            java.util.List<org.xj_service.oa.entity.Upload> atts = uploadService.listByOrigin("notice",
                    notice.getId().longValue());
            java.util.List<java.util.Map<String, Object>> attMaps = new java.util.ArrayList<>();
            if (atts != null) {
                for (org.xj_service.oa.entity.Upload u : atts) {
                    java.util.Map<String, Object> am = new java.util.HashMap<>();
                    am.put("id", u.getId());
                    am.put("name", u.getName());
                    am.put("storagePath", u.getStoragePath());
                    am.put("mimeType", u.getMimeType());
                    am.put("size", u.getSize());
                    // 尝试生成临时访问 URL（若 fileService 可用）
                    try {
                        if (fileService != null && u.getStoragePath() != null) {
                            String url = fileService.getTempUrl(u.getStoragePath(), 3600);
                            am.put("fileUrl", url);
                        } else {
                            am.put("fileUrl", null);
                        }
                    } catch (Exception ex) {
                        // 生成临时 URL 失败，不影响主体返回
                        logger.warn("Failed to generate temp url for upload {}", u.getId(), ex);
                        am.put("fileUrl", null);
                    }
                    attMaps.add(am);
                }
            }
            // 将 notice 转为 Map 并注入 attachments 字段，保持前端现有使用方式（res.data 为对象）
            java.util.Map<String, Object> m = objectMapper.convertValue(notice, java.util.Map.class);
            m.put("attachments", attMaps);
            return Result.success(m);
        } catch (Exception e) {
            return Result.error("500", "查询失败: " + e.getMessage());
        }
    }

    // 标记为已读：POST /administrative-notice/{id}/read?userId=xxx
    @Operation(summary = "标记已读", description = "标记当前用户已读此行政公示")
    @PostMapping("/{id}/read")
    public Result markRead(@PathVariable Integer id, @RequestParam Integer userId) {
        try {
            boolean ok = administrativeNoticeReadService.markRead(id, userId);
            return Result.success(ok);
        } catch (Exception e) {
            return Result.error("500", "标记已读失败: " + e.getMessage());
        }
    }

    @Operation(summary = "已读统计", description = "获取某条公示的已读数量")
    @GetMapping("/{id}/read/count")
    public Result getReadCount(@PathVariable Integer id) {
        try {
            int c = administrativeNoticeReadService.getReadCountByNoticeId(id);
            java.util.Map<String, Integer> r = new java.util.HashMap<>();
            r.put("readCount", c);
            return Result.success(r);
        } catch (Exception e) {
            return Result.error("500", "获取已读数失败: " + e.getMessage());
        }
    }

    @Operation(summary = "未读用户列表", description = "获取某条公示的未读用户列表")
    @GetMapping("/{id}/read/unread-users")
    public Result getUnreadUsers(@PathVariable Integer id) {
        try {
            java.util.List<org.xj_service.oa.entity.User> list = administrativeNoticeReadService
                    .getUnreadUsersByNoticeId(id);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("500", "获取未读用户失败: " + e.getMessage());
        }
    }

    @Operation(summary = "已读用户列表", description = "获取某条公示的已读用户列表")
    @GetMapping("/{id}/read/read-users")
    public Result getReadUsers(@PathVariable Integer id) {
        try {
            java.util.List<org.xj_service.oa.entity.User> users = administrativeNoticeReadService
                    .getReadUsersByNoticeId(id);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("500", "获取已读用户失败: " + e.getMessage());
        }
    }

    @Operation(summary = "导出未读用户", description = "导出未读用户为 XLSX 并以二进制流返回")
    @GetMapping("/{id}/read/export")
    public void exportUnread(@PathVariable Integer id, javax.servlet.http.HttpServletResponse response) {
        try {
            java.io.ByteArrayOutputStream out = administrativeNoticeReadService.exportUnreadToExcel(id);
            byte[] bytes = out.toByteArray();
            String fileName = java.net.URLEncoder.encode("unread_users_notice_" + id + ".xlsx", "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // Use RFC5987 encoding for filename to support UTF-8
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
            response.setContentLength(bytes.length);
            javax.servlet.ServletOutputStream sos = response.getOutputStream();
            sos.write(bytes);
            sos.flush();
        } catch (Exception e) {
            try {
                response.reset();
                response.setStatus(500);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"500\",\"message\":\"导出未读用户失败: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Operation(summary = "查询用户未读公示数量", description = "根据 userId 获取该用户未查看的公示数量")
    @GetMapping("/read/unread-count")
    public Result getUnreadCount(@RequestParam(required = false) Integer userId, HttpServletRequest request) {
        try {
            // 如果没有通过参数传入 userId，则从 Authorization token 中解析
            if (userId == null) {
                String token = request.getHeader("Authorization");
                if (token == null || token.isEmpty()) {
                    return Result.error("401", "无token，请重新登录");
                }
                try {
                    String uid = com.auth0.jwt.JWT.decode(token).getAudience().get(0);
                    userId = Integer.valueOf(uid);
                } catch (Exception ex) {
                    return Result.error("401", "token解析失败");
                }
            }
            int c = administrativeNoticeReadService.countUnreadForUser(userId);
            java.util.Map<String, Integer> r = new java.util.HashMap<>();
            r.put("unreadCount", c);
            return Result.success(r);
        } catch (Exception e) {
            return Result.error("500", "获取未读数量失败: " + e.getMessage());
        }
    }

    // 分页（兼容旧接口）
    @Operation(summary = "分页", description = "根据pageNum，pageSize以及模糊查询得到分页数据")
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
            @RequestParam Integer pageSize) {
        try {
            QueryWrapper<AdministrativeNotice> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderBy(true, true, "id");
            return Result.success(administrativeNoticeService.page(new Page<>(pageNum, pageSize), queryWrapper));
        } catch (Exception e) {
            return Result.error("500", "分页查询失败: " + e.getMessage());
        }
    }

    // 导出行政公示（示例：导出为 Excel 并上传文件存储，返回下载链接）
    @Operation(summary = "导出", description = "导出行政公示为文件并返回下载链接")
    @GetMapping("/export")
    public Result export() {
        try {
            String fileName = "administrative_notices_" + System.currentTimeMillis() + ".xlsx";
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            // 假设 service 提供导出方法
            administrativeNoticeService.exportNotices(outputStream);

            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(outputStream.toByteArray());
            String downloadPath = fileService.upload(inputStream, fileName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "oa-files", "notice");

            java.util.Map<String, String> result = new java.util.HashMap<>();
            result.put("downloadUrl", fileService.getTempUrl(downloadPath, 300));
            result.put("fileName", fileName);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("500", "导出失败: " + e.getMessage());
        }
    }

    // 批量导入（接收文件）
    @Operation(summary = "导入", description = "批量导入行政公示")
    @PostMapping("/import")
    public Result importNotices(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("400", "请选择文件");
            }
            administrativeNoticeService.importNotices(file);
            return Result.success("导入成功");
        } catch (Exception e) {
            return Result.error("500", "导入失败: " + e.getMessage());
        }
    }

    /*
    查询当前用户能看到的3条近期公示
     */
    @Operation(summary = "近期公示", description = "获取最新的用户可见的三条公示")
    @GetMapping("/current_three")
    public Result getCurrentThree() {
        try {
            QueryWrapper<AdministrativeNotice> queryWrapper = new QueryWrapper<>();
            // 添加条件：当前时间大于 effectiveDate  生效时间  （未生效的不显示）
            queryWrapper.lt("effective_date", new Date());
            queryWrapper.orderByDesc("created_at");
            // 限制结果为3条
            queryWrapper.last("LIMIT 3");
            List<AdministrativeNotice> list = administrativeNoticeService.list(queryWrapper);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("500", "获取近期公示失败: " + e.getMessage());
        }
    }
}
