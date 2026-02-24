package org.xj_service.oa.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.xj_service.oa.entity.DispatchRecord;
import org.xj_service.oa.entity.VehicleManagement;
import org.xj_service.oa.entity.Upload;
import org.xj_service.oa.entity.DispatchAttachment;
import org.xj_service.oa.service.IDispatchRecordService;
import org.xj_service.oa.service.IVehicleManagementService;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.UploadService;
import org.xj_service.oa.service.DispatchAttachmentService;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.common.Constants;
import org.xj_service.oa.common.RequireRole;

@RestController
@Tag(name = "派车管理", description = "派车记录相关接口")
public class DispatchRecordController {

    @Resource
    private IDispatchRecordService dispatchRecordService;

    @Resource
    private IVehicleManagementService vehicleManagementService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private UploadService uploadService;

    @Resource
    private DispatchAttachmentService dispatchAttachmentService;

    // 生成派单编号（简单时间戳策略，保证短期内唯一）
    private String generateDispatchNumber() {
        return "PD" + System.currentTimeMillis();
    }

    @GetMapping({ "/api/dispatch-records", "/dispatch-records" })
    @Operation(summary = "获取派车记录列表", description = "获取所有派车记录或按 vehicleId 过滤")
    public Result list(@RequestParam(required = false) Long vehicleId) {
        try {
            List<DispatchRecord> list;
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            if (vehicleId != null) {
                list = dispatchRecordService.lambdaQuery().eq(DispatchRecord::getVehicleId, vehicleId).list();
            } else {
                list = dispatchRecordService.list();
            }

            // 将 attachments 字段（如果为 JSON 字符串）解析并生成临时访问 URL
            List<Map<String, Object>> out = new java.util.ArrayList<>();
            for (DispatchRecord r : list) {
                Map<String, Object> m = mapper.convertValue(r, Map.class);
                // 兼容前端字段：同时返回 userName（等同 requesterName）
                Object requesterName = m.get("requesterName");
                if (requesterName != null && !m.containsKey("userName")) {
                    m.put("userName", requesterName);
                }
                Object atts = r.getAttachments();
                if (atts != null) {
                    try {
                        List parsed = mapper.readValue(atts.toString(), List.class);
                        // 为每个附件生成临时 URL（如果包含 filePath）
                        for (Object o : parsed) {
                            if (o instanceof Map) {
                                Map a = (Map) o;
                                Object fp = a.get("filePath");
                                if (fp != null) {
                                    try {
                                        String url = fileStorageService.getTempUrl(fp.toString(), 3600);
                                        a.put("fileUrl", url);
                                    } catch (Exception ex) {
                                        // ignore URL generation failure
                                    }
                                }
                            }
                        }
                        m.put("attachments", parsed);
                    } catch (Exception e) {
                        // 如果解析失败，则保持原始字符串
                        m.put("attachments", atts);
                    }
                }
                out.add(m);
            }
            return Result.success(out);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.error("500", "服务器处理派车记录列表时发生错误: " + ex.getMessage());
        }
    }

    @GetMapping({ "/api/dispatch-record/{id}", "/dispatch-record/{id}" })
    @Operation(summary = "获取单条派车记录", description = "根据ID获取派车记录详情")
    public Result get(@PathVariable Long id) {
        DispatchRecord record = dispatchRecordService.getById(id);
        // 返回时把 attachments 解析为数组并附带临时 URL
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            Map<String, Object> m = mapper.convertValue(record, Map.class);
            // 兼容前端字段：同时返回 userName（等同 requesterName）
            Object requesterName = m.get("requesterName");
            if (requesterName != null && !m.containsKey("userName")) {
                m.put("userName", requesterName);
            }
            Object atts = record.getAttachments();
            if (atts != null) {
                try {
                    List parsed = mapper.readValue(atts.toString(), List.class);
                    for (Object o : parsed) {
                        if (o instanceof Map) {
                            Map a = (Map) o;
                            Object fp = a.get("filePath");
                            if (fp != null) {
                                try {
                                    String url = fileStorageService.getTempUrl(fp.toString(), 3600);
                                    a.put("fileUrl", url);
                                } catch (Exception ex) {
                                }
                            }
                        }
                    }
                    m.put("attachments", parsed);
                } catch (Exception e) {
                    m.put("attachments", atts);
                }
            }
            return Result.success(m);
        } catch (Exception ex) {
            return Result.success(record);
        }
    }

    @PostMapping({ "/api/dispatch-record", "/dispatch-record" })
    @Operation(summary = "新增派车记录", description = "新增一条派车记录，attachments 支持数组或 JSON 字符串")
    public Result create(@RequestBody(required = false) String rawPayload, HttpServletRequest request) {
        try {
            String body = rawPayload == null ? "" : rawPayload.trim();
            System.out.println("[DispatchRecordController] raw create payload: " + body);

            ObjectMapper parser = new ObjectMapper();
            parser.registerModule(new JavaTimeModule());
            parser.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            parser.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

            Map<String, Object> payload = new HashMap<>();
            if (!body.isEmpty()) {
                // parse into a map; if parsing fails, we'll capture exception below
                payload = parser.readValue(body, Map.class);
            }
            System.out.println("[DispatchRecordController] parsed payload map: " + payload);
            // 兼容前端：如果传入 userName 则映射为 requesterName
            if (payload.containsKey("userName") && !payload.containsKey("requesterName")) {
                payload.put("requesterName", payload.get("userName"));
            }
            // Normalize attachments to JSON string if provided as array/list
            Object attachments = payload.get("attachments");
            if (attachments != null && !(attachments instanceof String)) {
                ObjectMapper tmp = new ObjectMapper();
                String attachmentsJson = tmp.writeValueAsString(attachments);
                payload.put("attachments", attachmentsJson);
            }

            // Resolve vehicleId from plateNumber when missing
            if ((payload.get("vehicleId") == null || payload.get("vehicleId").toString().isEmpty())
                    && payload.get("plateNumber") != null) {
                String plate = payload.get("plateNumber").toString();
                VehicleManagement vm = vehicleManagementService.lambdaQuery()
                        .eq(VehicleManagement::getPlateNumber, plate).one();
                if (vm != null) {
                    payload.put("vehicleId", vm.getId());
                }
            }

            // Ensure dispatchNumber is present and unique; if missing or duplicate,
            // auto-generate
            Object dnObj = payload.get("dispatchNumber");
            String dn = dnObj == null ? "" : dnObj.toString().trim();
            if (dn.isEmpty()
                    || dispatchRecordService.lambdaQuery().eq(DispatchRecord::getDispatchNumber, dn).exists()) {
                String newDn = generateDispatchNumber();
                payload.put("dispatchNumber", newDn);
                System.out.println("[DispatchRecordController] dispatchNumber set/updated to: " + newDn);
            }

            DispatchRecord record = parser.convertValue(payload, DispatchRecord.class);
            boolean ok = dispatchRecordService.save(record);
            // 如果保存成功，处理附件关联（如果前端已经上传并返回 uploadId）
            if (ok && payload.get("attachments") != null) {
                try {
                    ObjectMapper tmp = new ObjectMapper();
                    List<Map<String, Object>> parsed = tmp.readValue(payload.get("attachments").toString(), List.class);
                    int idx = 0;
                    for (Map<String, Object> a : parsed) {
                        idx++;
                        Long uploadId = null;
                        if (a.get("uploadId") != null) {
                            uploadId = Long.parseLong(a.get("uploadId").toString());
                        } else if (a.get("filePath") != null) {
                            // 如果没有 uploadId，但有 filePath，则把该信息创建到 uploads 表
                            Upload u = new Upload();
                            u.setName(a.getOrDefault("name", "").toString());
                            u.setStoragePath(a.get("filePath").toString());
                            Object fs = a.get("fileSize");
                            if (fs != null) {
                                try {
                                    u.setSize(Long.parseLong(fs.toString()));
                                } catch (Exception ex) {
                                }
                            }
                            u.setMimeType(
                                    a.getOrDefault("fileType", null) == null ? null : a.get("fileType").toString());
                            u.setOrigin("dispatch");
                            uploadService.create(u);
                            uploadId = u.getId();
                        }
                        if (uploadId != null) {
                            DispatchAttachment da = new DispatchAttachment();
                            da.setDispatchId(record.getId());
                            da.setUploadId(uploadId);
                            da.setName(a.getOrDefault("name", "").toString());
                            da.setSortOrder(idx);
                            dispatchAttachmentService.create(da);
                        }
                    }
                } catch (Exception ex) {
                    // ignore attachment processing errors
                }
            }
            return ok ? Result.success(true) : Result.error(Constants.CODE_500, "create failed");
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.error(Constants.CODE_600, "invalid payload: " + ex.getMessage());
        }
    }

    @PutMapping({ "/api/dispatch-record/{id}", "/dispatch-record/{id}" })
    @Operation(summary = "更新派车记录", description = "根据ID更新派车记录，attachments 支持数组或 JSON 字符串")
    public Result update(@PathVariable Long id, @RequestBody(required = false) String rawPayload,
            HttpServletRequest request) {
        try {
            String body = rawPayload == null ? "" : rawPayload.trim();
            System.out.println("[DispatchRecordController] raw update payload: " + body);

            ObjectMapper parser = new ObjectMapper();
            parser.registerModule(new JavaTimeModule());
            parser.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            parser.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

            Map<String, Object> payload = new HashMap<>();
            if (!body.isEmpty()) {
                payload = parser.readValue(body, Map.class);
            }
            System.out.println("[DispatchRecordController] parsed payload map: " + payload);
            // 兼容前端：如果传入 userName 则映射为 requesterName
            if (payload.containsKey("userName") && !payload.containsKey("requesterName")) {
                payload.put("requesterName", payload.get("userName"));
            }
            // Normalize attachments to JSON string if provided as array/list
            Object attachments = payload.get("attachments");
            if (attachments != null && !(attachments instanceof String)) {
                ObjectMapper tmp = new ObjectMapper();
                String attachmentsJson = tmp.writeValueAsString(attachments);
                payload.put("attachments", attachmentsJson);
            }

            // Resolve vehicleId from plateNumber when missing
            if ((payload.get("vehicleId") == null || payload.get("vehicleId").toString().isEmpty())
                    && payload.get("plateNumber") != null) {
                String plate = payload.get("plateNumber").toString();
                VehicleManagement vm = vehicleManagementService.lambdaQuery()
                        .eq(VehicleManagement::getPlateNumber, plate).one();
                if (vm != null) {
                    payload.put("vehicleId", vm.getId());
                }
            }

            DispatchRecord record = parser.convertValue(payload, DispatchRecord.class);
            record.setId(id);
            boolean ok = dispatchRecordService.updateById(record);
            // 更新后重新写入附件映射：先删除已有的 mapping，然后插入新的
            if (ok) {
                try {
                    dispatchAttachmentService.deleteByDispatchId(id);
                    if (payload.get("attachments") != null) {
                        ObjectMapper tmp = new ObjectMapper();
                        List<Map<String, Object>> parsed = tmp.readValue(payload.get("attachments").toString(),
                                List.class);
                        int idx = 0;
                        for (Map<String, Object> a : parsed) {
                            idx++;
                            Long uploadId = null;
                            if (a.get("uploadId") != null) {
                                uploadId = Long.parseLong(a.get("uploadId").toString());
                            } else if (a.get("filePath") != null) {
                                Upload u = new Upload();
                                u.setName(a.getOrDefault("name", "").toString());
                                u.setStoragePath(a.get("filePath").toString());
                                Object fs = a.get("fileSize");
                                if (fs != null) {
                                    try {
                                        u.setSize(Long.parseLong(fs.toString()));
                                    } catch (Exception ex) {
                                    }
                                }
                                u.setMimeType(
                                        a.getOrDefault("fileType", null) == null ? null : a.get("fileType").toString());
                                u.setOrigin("dispatch");
                                uploadService.create(u);
                                uploadId = u.getId();
                            }
                            if (uploadId != null) {
                                DispatchAttachment da = new DispatchAttachment();
                                da.setDispatchId(id);
                                da.setUploadId(uploadId);
                                da.setName(a.getOrDefault("name", "").toString());
                                da.setSortOrder(idx);
                                dispatchAttachmentService.create(da);
                            }
                        }
                    }
                } catch (Exception ex) {
                    // ignore
                }
            }
            return ok ? Result.success(true) : Result.error(Constants.CODE_500, "update failed");
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.error(Constants.CODE_600, "invalid payload: " + ex.getMessage());
        }
    }

    @RequireRole({ "SUPER_ADMIN" })
    @DeleteMapping({ "/api/dispatch-record/{id}", "/dispatch-record/{id}" })
    @Operation(summary = "删除派车记录", description = "根据ID删除派车记录")
    public Result delete(@PathVariable Long id) {
        boolean ok = dispatchRecordService.removeById(id);
        return ok ? Result.success(true) : Result.error("500", "delete failed");
    }
}
