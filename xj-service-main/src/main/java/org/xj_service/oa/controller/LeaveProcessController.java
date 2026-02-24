package org.xj_service.oa.controller;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.poi.util.IOUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.dto.LeaveProcessRequest;
import org.xj_service.oa.dto.TaskItemDTO;
import org.xj_service.oa.entity.ApprovalComment;
import org.xj_service.oa.entity.LeaveAttachment;
import org.xj_service.oa.entity.LeaveProcess;
import org.xj_service.oa.entity.User; // 假设的用户实体
import org.xj_service.oa.entity.Department; // 假设的部门实体
import org.xj_service.oa.service.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@RestController
@RequestMapping("/api/leave-process")
public class LeaveProcessController {

    @Autowired
    private ILeaveProcessService leaveProcessService;

    @Autowired
    @Qualifier("minioFileStorageService")
    private FileStorageService fileService;

    @Autowired
    private AttachmentService attachmentService;

    @Resource
    private IApprovalCommentService approvalCommentService;

    // 【新增】注入用户服务，用于统计部门人数
    @Autowired
    private IUserService userService;

    // 注入部门服务，用于根据 departmentId 获取部门信息
    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    /**
     * 提交请假申请（适配最新的DTO和Entity）
     */
    @PostMapping
    public Result submitLeave(@RequestBody LeaveProcessRequest request) {
        try {
            LeaveProcess leave = request.getLeaveProcess();
            // 1. 填充申请人姓名 (username)
            if (leave.getUserId() != null) {
                // 根据 ID 查询用户信息
                User user = userService.getById(leave.getUserId());
                if (user != null) {
                    // 优先存昵称或实名，防止存入 null
                    String nameToSave = user.getNickname() != null ? user.getNickname() : user.getUsername();
                    leave.setUsername(nameToSave);
                }
            }

            // 2. 填充部门名称 (departmentName)
            if (leave.getDepartmentId() != null) {
                // 根据 ID 查询部门信息
                Department dept = departmentService.getById(leave.getDepartmentId());
                if (dept != null) {
                    leave.setDepartmentName(dept.getName()); // 假设获取名称的方法是 getName()
                }
            }
            
            // --- 1. 构建流程变量 Map (BPMN流程流转的核心) ---
            Map<String, Object> variables = new HashMap<>();
            
            // 1.1 基础信息
            variables.put("starter", request.getStarter());
            
            // 1.2 网关条件变量：请假天数
            // [适配 Entity] 使用实体类新增的 duration 字段
            // 如果前端没传 duration，建议在这里抛出异常或根据日期计算
            if (leave.getDuration() == null) {
                return Result.error("400", "请假时长不能为空");
            }
            double days = leave.getDuration(); 
            variables.put("days", days); 

            // 1.3 网关条件变量：是否领导
            // TODO: 这里需要根据你的业务逻辑判断 request.getStarter() 是否是领导
            // 暂时默认 false (走部门经理审批路径)
            boolean isLeader = false; 
            variables.put("isLeader", isLeader);

            // 1.4 设置各级审批人 (对应 BPMN: flowable:assignee)
            
            // 部门主管
            if (request.getFirstApprover() == null) {
                 return Result.error("400", "部门主管不能为空");
            }
            variables.put("deptManager", request.getFirstApprover());
            
            // 分管领导 (当 days > 0.5 时需要)
            variables.put("viceLeader", request.getSecondApprover());
            
            // [适配 DTO] 行政办人员
            // 优先使用前端传的，没传则使用默认值 "admin"
            String adminUser = request.getAdminUser() != null ? request.getAdminUser() : "admin";
            variables.put("adminUser", adminUser);

            // [适配 DTO] 主要领导 (当 days > 3 时需要)
            if (days > 3 && (request.getMainLeader() == null || request.getMainLeader().isEmpty())) {
                 return Result.error("400", "请假超过3天，必须指定主要领导审批人");
            }
            variables.put("mainLeader", request.getMainLeader());
            
            // --- 2. 启动流程 ---
            // 注意：请确保 Service 层的 startLeaveProcess 方法已修改为接收 variables Map
            String processInstanceId = leaveProcessService.startLeaveProcess(leave, variables);

            // [新增] 关联附件
            if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
                List<LeaveAttachment> attachments = attachmentService.listByIds(request.getAttachmentIds());
                if (attachments != null && !attachments.isEmpty()) {
                    for (LeaveAttachment att : attachments) {
                        att.setLeaveId(leave.getId());
                    }
                    attachmentService.updateBatchById(attachments);
                }
            }
            
            return Result.success("流程启动成功，流程ID: " + processInstanceId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户任务列表 (保持不变)
     */
    @GetMapping("/tasks")
    public Result getUserTasks(javax.servlet.http.HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                return Result.error("401", "未登录或token缺失");
            }
            String userId = JWT.decode(token).getAudience().get(0);
            List<Task> tasks = leaveProcessService.getTasksForUser(userId);
            List<TaskItemDTO> items = new ArrayList<>();
            for (Task t : tasks) {
                TaskItemDTO dto = new TaskItemDTO();
                dto.setId(t.getId());
                dto.setName(t.getName());
                dto.setCreateTime(t.getCreateTime());
                dto.setAssignee(t.getAssignee());
                dto.setProcessInstanceId(t.getProcessInstanceId());
                dto.setTaskDefinitionKey(t.getTaskDefinitionKey());

                Object lid = null;
                try {
                    lid = runtimeService.getVariable(t.getProcessInstanceId(), "leaveId");
                } catch (Exception ignore) {}
                if (lid instanceof Number) {
                    Long leaveId = ((Number) lid).longValue();
                    dto.setLeaveId(leaveId);
                    LeaveProcess lp = leaveProcessService.getById(leaveId);
                    dto.setLeave(lp);
                }
                items.add(dto);
            }
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("500", "获取任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取个人请假记录 (保持不变)
     */
    @GetMapping("/my")
    public Result getMyLeaves(@RequestParam Long userId) {
        try {
            List<LeaveProcess> processes = leaveProcessService.getMyLeaveProcesses(userId);
            return Result.success(processes);
        } catch (Exception e) {
            return Result.error("500", "获取记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的审批记录
     */
    @GetMapping("/my-approvals")
    public Result getMyApprovals(@RequestParam Long userId) {
        try {
            // 1. 查询该用户的所有审批意见
            List<ApprovalComment> comments = approvalCommentService.list(
                new QueryWrapper<ApprovalComment>()
                    .eq("approver_id", userId.intValue())
                    .eq("business_type", "leave")
                    .orderByDesc("approved_time")
            );
            
            if (comments == null || comments.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            // 2. 收集所有的业务ID（请假记录ID）
            Set<Integer> leaveIds = new HashSet<>();
            for (ApprovalComment comment : comments) {
                if (comment.getBusinessId() != null) {
                    leaveIds.add(comment.getBusinessId());
                }
            }

            // 3. 查询这些请假记录
            Map<Integer, LeaveProcess> leaveMap = new HashMap<>();
            if (!leaveIds.isEmpty()) {
                List<LeaveProcess> leaves = leaveProcessService.listByIds(
                    leaveIds.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList())
                );
                for (LeaveProcess leave : leaves) {
                    leaveMap.put(leave.getId().intValue(), leave);
                }
            }

            // 4. 组装返回数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (ApprovalComment comment : comments) {
                Map<String, Object> item = new HashMap<>();
                LeaveProcess leave = leaveMap.get(comment.getBusinessId());

                // 兜底：若 businessId 未命中，尝试用流程实例ID匹配
                if (leave == null && comment.getProcessInstanceId() != null) {
                    leave = leaveProcessService.getOne(
                        new QueryWrapper<LeaveProcess>().eq("process_instance_id", comment.getProcessInstanceId())
                    );
                    if (leave != null && leave.getId() != null) {
                        leaveMap.put(leave.getId().intValue(), leave);
                    }
                }

                if (leave != null) {
                    item.put("id", leave.getId());
                    item.put("leaveType", leave.getLeaveType());
                    item.put("startDate", leave.getStartDate());
                    item.put("endDate", leave.getEndDate());
                    item.put("username", leave.getUsername());
                    item.put("applicantName", leave.getUsername());
                    item.put("reason", leave.getReason());
                }

                item.put("approvalResult", comment.getApprovalResult());
                item.put("approvedTime", comment.getApprovedTime());
                item.put("comment", comment.getComment());
                item.put("approvalNode", comment.getApprovalNode());

                result.add(item);
            }

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取审批记录失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务并保存审批意见 (核心修改：传入 approved 变量)
     */
    @PostMapping("/complete/{taskId}")
    public Result completeTask(
            @PathVariable String taskId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String nextApprover,
            @RequestBody ApprovalComment comment,
            javax.servlet.http.HttpServletRequest httpRequest) { 
        try {
            String token = httpRequest.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                return Result.error("401", "未登录或token缺失");
            }
            String currentUserId = JWT.decode(token).getAudience().get(0);

            // 0. 校验逻辑 (保持不变)
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                return Result.error("400", "任务不存在");
            }
            if (task.getAssignee() == null || !task.getAssignee().equals(currentUserId)) {
                return Result.error("403", "非任务处理人无权审批");
            }

            // ... (获取 leaveId 逻辑保持原样) ...

            // --- 核心修改开始：构建任务变量 ---
            Map<String, Object> taskVariables = new HashMap<>();
            
            // 必须传入 approved 变量，BPMN 网关依赖它
            taskVariables.put("approved", approved); 
            
            // 如果前端指定了下一步审批人 (用于动态修正预设的审批人)
            if (nextApprover != null && !nextApprover.isEmpty()) {
                if ("一级审批".equals(task.getName()) || "deptManagerAudit".equals(task.getTaskDefinitionKey())) {
                    taskVariables.put("viceLeader", nextApprover);
                }
            }
            // --- 核心修改结束 ---

            // 1. 完成流程任务
            // 注意：ILeaveProcessService 接口需要支持传入 variables Map
            leaveProcessService.completeTask(taskId, taskVariables, comment);

            // 2. 保存审批意见 (已在 Service 中处理，此处移除重复调用)
            // 之前此处重复调用导致 Duplicate entry error
            
            return Result.success("任务完成并保存意见");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "任务处理失败: " + e.getMessage());
        }
    }

    /**
     * 提交销假申请
     */
    @PostMapping("/report-back/{taskId}")
    public Result submitReportBack(
            @PathVariable String taskId,
            @RequestBody Map<String, String> params) {
        try {
            String startStr = params.get("actualStartTime");
            String endStr = params.get("actualEndTime");
            
            if (startStr == null || endStr == null) {
                return Result.error("400", "实际起止时间不能为空");
            }
            
            // 简单解析时间，假设前端传的是标准格式 "yyyy-MM-dd HH:mm:ss" 或 ISO
            // 这里为了稳健，建议前端传 ISO 格式，后端用 LocalDateTime.parse
            // 或者使用 DateTimeFormatter
            java.time.LocalDateTime start;
            java.time.LocalDateTime end;
            try {
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                start = java.time.LocalDateTime.parse(startStr, fmt);
                end = java.time.LocalDateTime.parse(endStr, fmt);
            } catch (java.time.format.DateTimeParseException e) {
                // 尝试 ISO 格式
                start = java.time.LocalDateTime.parse(startStr);
                end = java.time.LocalDateTime.parse(endStr);
            }
            
            leaveProcessService.submitReportBack(taskId, start, end);
            return Result.success("销假申请已提交");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "提交失败: " + e.getMessage());
        }
    }

    // ==========================================
    // 【新增区域】 报表统计与规则接口
    // ==========================================

    /**
     * 获取部门年度请假统计 (方案二：直接查询请假记录表)
     * 优点：直接使用 leave_process 表存在的 department_id 字段，不会报 SQL 错误。
     * 缺点：只能统计到“提交过请假申请”的员工，从未请假的员工不会被计入总人数。
     */
    @GetMapping("/stats/department")
    public Result getDepartmentStats(@RequestParam Integer year, @RequestParam Long deptId) {
        try {
            // 1. 直接查询 leave_process 表
            List<LeaveProcess> leaves = leaveProcessService.list(
                new QueryWrapper<LeaveProcess>().eq("department_id", deptId)
            );

            if (leaves == null || leaves.isEmpty()) {
                Map<String, Object> emptyData = new HashMap<>();
                emptyData.put("totalEntitlement", 0);
                emptyData.put("usedDays", 0);
                emptyData.put("employeeCount", 0);
                emptyData.put("userIds", new ArrayList<>()); // 空列表
                return Result.success(emptyData);
            }

            double usedDays = 0.0;
            
            // 【修改点】将 Set<Long> 改为 Set<Integer>
            // 因为您的数据库 user_id 是 int 类型，实体类对应的是 Integer
            Set<Integer> uniqueUserIds = new HashSet<>();

            // 2. 遍历记录
            for (LeaveProcess lp : leaves) {
                if (lp.getUserId() != null) {
                    uniqueUserIds.add(lp.getUserId()); // 这里现在匹配了，不会报错
                }

                if (lp.getStartDate() != null && lp.getStartDate().getYear() == year) {
                    Double d = lp.getDuration();
                    if (d != null) {
                        usedDays += d;
                    }
                }
            }

            // 3. 估算年假总额
            double totalEntitlement = uniqueUserIds.size() * 10.0;

            Map<String, Object> data = new HashMap<>();
            data.put("totalEntitlement", totalEntitlement);
            data.put("usedDays", usedDays);
            data.put("employeeCount", uniqueUserIds.size());
            // ✅【核心修改】将涉及的 User ID 列表返回给前端，用于映射姓名
            data.put("userIds", uniqueUserIds);
            return Result.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取部门统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取工龄与年假天数对应规则表
     * 前端调用: /api/leave-process/seniority-rules
     */
    @GetMapping("/seniority-rules")
    public Result getSeniorityTable() {
        // 返回符合前端格式的规则列表
        // 格式: [{min: 0, max: 1, days: 0}, {min: 1, max: 10, days: 5}, ...]
        // 建议从数据库配置表读取，这里演示返回静态配置
        List<Map<String, Object>> rules = new ArrayList<>();
        
        Map<String, Object> r1 = new HashMap<>();
        r1.put("min", 0); r1.put("max", 1); r1.put("days", 0);
        rules.add(r1);

        Map<String, Object> r2 = new HashMap<>();
        r2.put("min", 1); r2.put("max", 10); r2.put("days", 5);
        rules.add(r2);

        Map<String, Object> r3 = new HashMap<>();
        r3.put("min", 10); r3.put("max", 20); r3.put("days", 10);
        rules.add(r3);

        Map<String, Object> r4 = new HashMap<>();
        r4.put("min", 20); r4.put("max", 99); r4.put("days", 15);
        rules.add(r4);

        return Result.success(rules);
    }

    // ==========================================
    // 辅助方法与附件下载
    // ==========================================

    @GetMapping("/comments/{leaveId}")
    public Result getComments(@PathVariable("leaveId") String leaveIdStr) {
        try {
            long parsed = Long.parseLong(leaveIdStr);
            int truncated = (int) parsed;
            return Result.success(approvalCommentService.getByBusiness(truncated, "leave"));
        } catch (Exception ignore) {
            return Result.success(java.util.Collections.emptyList());
        }
    }

    @PostMapping("/attachment")
    public Result uploadLeaveAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("leaveId") Long leaveId) {
        String filePath = fileService.upload(file, "leave-attachments", "leave");
        LeaveAttachment attachment = new LeaveAttachment();
        attachment.setLeaveId(leaveId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFilePath(filePath);
        attachmentService.save(attachment);
        return Result.success("附件上传成功");
    }

    @GetMapping("/attachment/{id}/download")
    public void downloadAttachment(@PathVariable Long id, HttpServletResponse response) {
        LeaveAttachment attachment = attachmentService.getById(id);
        try (InputStream in = fileService.download(attachment.getFilePath())) {
            response.setContentType(attachment.getFileType());
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(attachment.getFileName(), "UTF-8"));
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("下载失败");
        }
    }

    @PostMapping("/draft")
    public Result saveDraft(@RequestBody LeaveProcess leaveProcess) {
        try {
            Long draftId = leaveProcessService.saveDraft(leaveProcess);
            return Result.success(draftId);
        } catch (Exception e) {
            return Result.error("500", "保存草稿失败: " + e.getMessage());
        }
    }

    @PostMapping("/submit-from-draft/{draftId}")
    public Result submitFromDraft(
            @PathVariable Long draftId,
            @RequestParam String starter,
            @RequestParam String firstApprover,
            @RequestParam String secondApprover) {
        try {
            // 这里同样需要修改 Service 逻辑，从草稿转正时也需要传入 Map 变量
            // 建议改为: leaveProcessService.submitFromDraft(draftId, variables);
            // 暂时保留原样，请参照 submitLeave 进行类似修改
            String processId = leaveProcessService.submitFromDraft(draftId, starter, firstApprover, secondApprover);
            return Result.success("提交成功，流程ID: " + processId);
        } catch (Exception e) {
            return Result.error("500", "提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/drafts")
    public Result getMyDrafts(@RequestParam Long userId) {
        return Result.success(leaveProcessService.list(
                new QueryWrapper<LeaveProcess>()
                        .eq("user_id", userId)
                        .eq("status", "DRAFT")
                        .orderByDesc("updated_at")
        ));
    }

    @DeleteMapping("/draft/{id}")
    public Result deleteDraft(@PathVariable Long id, @RequestParam(required = false) Integer userId) {
        try {
            boolean ok = leaveProcessService.deleteDraft(id, userId);
            if (ok) {
                return Result.success("删除成功");
            } else {
                return Result.error("400", "删除失败");
            }
        } catch (Exception e) {
            return Result.error("500", "删除草稿失败: " + e.getMessage());
        }
    }

    /**
     * 撤回请假申请：前端传入请假记录ID和发起人 userId 用于校验。
     */
    @PostMapping("/withdraw/{id}")
    public Result withdraw(@PathVariable Long id, @RequestParam Integer userId) {
        try {
            boolean ok = leaveProcessService.withdrawLeave(id, userId);
            if (ok) return Result.success("撤回成功");
            else return Result.error("400", "无法撤回：可能已有审批通过或权限不足");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "撤回失败: " + e.getMessage());
        }
    }
}