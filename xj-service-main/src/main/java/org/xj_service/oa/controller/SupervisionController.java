package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import com.auth0.jwt.JWT;
import org.flowable.task.api.Task;
import java.util.List;
import org.xj_service.oa.common.Result;

import org.xj_service.oa.entity.SupervisionTask;
import org.xj_service.oa.entity.SupervisionFeedback;
import org.xj_service.oa.service.ISupervisionTaskService;
import org.xj_service.oa.mapper.SupervisionFeedbackMapper;
import org.xj_service.oa.service.UploadService;
import org.xj_service.oa.mapper.SupervisionAssignmentMapper;

@RestController
@RequestMapping("/supervision")
public class SupervisionController {

    @Resource
    private ISupervisionTaskService supervisionTaskService;

    @Resource
    private org.xj_service.oa.mapper.UserMapper userMapper;

    @Resource
    private SupervisionFeedbackMapper feedbackMapper;

    @Resource
    private org.xj_service.oa.mapper.SupervisionFeedbackUploadMapper feedbackUploadMapper;

    @Resource
    private SupervisionAssignmentMapper assignmentMapper;

    @Resource
    private UploadService uploadService;

    @Resource
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Resource
    private org.flowable.engine.RuntimeService runtimeService;

    @Resource
    private org.flowable.engine.TaskService taskService;

    @Resource
    private org.flowable.engine.HistoryService historyService;

    @Resource
    private org.xj_service.oa.service.ISupervisionProcessService supervisionProcessService;

    // 分页列表： GET /supervision/tasks?page=1&size=10&q=xxx
    // 仅返回当前用户发起的督察任务（createdBy）
    @GetMapping("/tasks")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String q,
            HttpServletRequest request) {
        QueryWrapper<SupervisionTask> qw = new QueryWrapper<>();
        if (q != null && !q.trim().isEmpty()) {
            qw.like("title", q.trim());
        }
        // 尝试从 Authorization token 中读取当前用户 id，按 createdBy 过滤
        try {
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                String userIdStr = com.auth0.jwt.JWT.decode(token).getAudience().get(0);
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        Long uid = Long.valueOf(userIdStr);
                        qw.eq("created_by", uid);
                    } catch (Exception ignore) {
                    }
                }
            }
        } catch (Exception ignore) {
        }

        qw.orderByDesc("id");
        Page<SupervisionTask> p = supervisionTaskService.page(new Page<>(page, size), qw);

        // enrich records with creatorName and assignments
        java.util.List<SupervisionTask> records = p.getRecords();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (SupervisionTask t : records) {
            java.util.Map<String, Object> dto = objectMapper.convertValue(t, java.util.Map.class);
            // creatorName
            try {
                if (t.getCreatedBy() != null) {
                    Integer uid = t.getCreatedBy().intValue();
                    org.xj_service.oa.entity.User u = userMapper.selectById(uid);
                    if (u != null)
                        dto.put("creatorName", u.getNickname() != null ? u.getNickname() : u.getUsername());
                    else
                        dto.put("creatorName", null);
                } else {
                    dto.put("creatorName", null);
                }
            } catch (Exception ignore) {
                dto.put("creatorName", null);
            }

            // assignments
            try {
                java.util.List<org.xj_service.oa.entity.SupervisionAssignment> assigns = assignmentMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.xj_service.oa.entity.SupervisionAssignment>()
                                .eq("task_id", t.getId()));
                java.util.List<java.util.Map<String, Object>> aout = new java.util.ArrayList<>();
                if (assigns != null) {
                    for (org.xj_service.oa.entity.SupervisionAssignment sa : assigns) {
                        java.util.Map<String, Object> am = new java.util.HashMap<>();
                        am.put("assigneeUserId", sa.getAssigneeUserId());
                        am.put("assigneeDeptId", sa.getAssigneeDeptId());
                        am.put("status", sa.getStatus());
                        am.put("flowableTaskId", sa.getFlowableTaskId());
                        if (sa.getAssigneeUserId() != null) {
                            try {
                                org.xj_service.oa.entity.User uu = userMapper
                                        .selectById(sa.getAssigneeUserId().intValue());
                                if (uu != null)
                                    am.put("assigneeUserName",
                                            uu.getNickname() != null ? uu.getNickname() : uu.getUsername());
                            } catch (Exception ignore) {
                            }
                        }
                        aout.add(am);
                    }
                }
                dto.put("assignments", aout);
            } catch (Exception ignore) {
                dto.put("assignments", new java.util.ArrayList<>());
            }

            // feedback summary: count and latest feedback
            try {
                long fbCount = feedbackMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionFeedback>()
                                .eq("task_id", t.getId()));
                dto.put("feedbackCount", fbCount);
                org.xj_service.oa.entity.SupervisionFeedback lastFb = feedbackMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionFeedback>()
                                .eq("task_id", t.getId()).orderByDesc("feedback_at").last("limit 1"));
                if (lastFb != null) {
                    java.util.Map<String, Object> fbm = new java.util.HashMap<>();
                    fbm.put("id", lastFb.getId());
                    fbm.put("remarks", lastFb.getRemarks());
                    fbm.put("feedbackAt", lastFb.getFeedbackAt());
                    try {
                        if (lastFb.getFeedbackBy() != null) {
                            org.xj_service.oa.entity.User fu = userMapper.selectById(lastFb.getFeedbackBy().intValue());
                            if (fu != null)
                                fbm.put("feedbackByName",
                                        fu.getNickname() != null ? fu.getNickname() : fu.getUsername());
                        }
                    } catch (Exception ignore) {
                    }
                    dto.put("lastFeedback", fbm);
                } else {
                    dto.put("lastFeedback", null);
                }
            } catch (Exception ignore) {
                dto.put("feedbackCount", 0);
                dto.put("lastFeedback", null);
            }

            // Detect if there's an active 'review' user task for this business (used to
            // show
            // approval button reliably on frontend). We look up Flowable tasks by
            // processInstanceId and taskDefinitionKey == 'review'.
            try {
                if (t.getProcessInstanceId() != null) {
                    java.util.List<org.flowable.task.api.Task> reviews = taskService.createTaskQuery()
                            .processInstanceId(t.getProcessInstanceId()).taskDefinitionKey("review").list();
                    if (reviews != null && !reviews.isEmpty()) {
                        dto.put("hasReviewTask", true);
                        java.util.List<String> rids = new java.util.ArrayList<>();
                        boolean assignedToStarter = false;
                        for (org.flowable.task.api.Task rt : reviews) {
                            rids.add(rt.getId());
                            try {
                                if (rt.getAssignee() != null && t.getCreatedBy() != null
                                        && rt.getAssignee().equals(String.valueOf(t.getCreatedBy())))
                                    assignedToStarter = true;
                            } catch (Exception ignore) {
                            }
                        }
                        dto.put("reviewTaskIds", rids);
                        dto.put("reviewAssignedToStarter", assignedToStarter);
                    } else {
                        dto.put("hasReviewTask", false);
                        dto.put("reviewTaskIds", new java.util.ArrayList<>());
                        dto.put("reviewAssignedToStarter", false);
                    }
                } else {
                    dto.put("hasReviewTask", false);
                    dto.put("reviewTaskIds", new java.util.ArrayList<>());
                    dto.put("reviewAssignedToStarter", false);
                }
            } catch (Exception ignore) {
                dto.put("hasReviewTask", false);
                dto.put("reviewTaskIds", new java.util.ArrayList<>());
                dto.put("reviewAssignedToStarter", false);
            }

            out.add(dto);
        }

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("records", out);
        resp.put("total", p.getTotal());
        resp.put("size", p.getSize());
        resp.put("current", p.getCurrent());
        return Result.success(resp);
    }

    @GetMapping("/tasks/{id}")
    public Result getOne(@PathVariable Long id) {
        SupervisionTask t = supervisionTaskService.getById(id);
        if (t == null)
            return Result.success(null);
        // load feedbacks
        List<SupervisionFeedback> feedbacks = feedbackMapper
                .selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionFeedback>()
                        .eq("task_id", id));

        // convert feedback entities to DTOs with readable fields for the frontend
        java.util.List<java.util.Map<String, Object>> fouts = new java.util.ArrayList<>();
        if (feedbacks != null) {
            for (SupervisionFeedback f : feedbacks) {
                java.util.Map<String, Object> fm = new java.util.HashMap<>();
                fm.put("id", f.getId());
                fm.put("remarks", f.getRemarks());
                if (f.getFeedbackAt() != null) {
                    try {
                        fm.put("feedbackAt", f.getFeedbackAt().toString());
                    } catch (Exception ignore) {
                        fm.put("feedbackAt", f.getFeedbackAt());
                    }
                } else {
                    fm.put("feedbackAt", null);
                }
                if (f.getFinishDate() != null) {
                    try {
                        fm.put("finishDate", f.getFinishDate().toString());
                    } catch (Exception ignore) {
                        fm.put("finishDate", f.getFinishDate());
                    }
                } else {
                    fm.put("finishDate", null);
                }
                // attach feedbackByName if possible
                if (f.getFeedbackBy() != null) {
                    try {
                        org.xj_service.oa.entity.User fu = userMapper.selectById(f.getFeedbackBy().intValue());
                        if (fu != null)
                            fm.put("feedbackByName", fu.getNickname() != null ? fu.getNickname() : fu.getUsername());
                        else
                            fm.put("feedbackBy", f.getFeedbackBy());
                    } catch (Exception ignore) {
                        fm.put("feedbackBy", f.getFeedbackBy());
                    }
                }
                fouts.add(fm);
            }
        }

        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("task", t);
        map.put("feedbacks", fouts);
        return Result.success(map);
    }

    // 列出当前用户的流程任务（流程引擎待办）
    @GetMapping("/process/tasks")
    public Result listMyProcessTasks(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                return Result.error("401", "未登录或token缺失");
            }
            String userId = JWT.decode(token).getAudience().get(0);

            List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(userId).list();
            java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
            for (Task t : tasks) {
                java.util.Map<String, Object> dto = new java.util.HashMap<>();
                dto.put("id", t.getId());
                // default name from flowable task
                dto.put("name", t.getName());
                // format createTime to server default timezone ISO string for easier display on
                // frontend
                if (t.getCreateTime() != null) {
                    try {
                        java.time.ZonedDateTime z = t.getCreateTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault());
                        dto.put("createTime", z.toString());
                    } catch (Exception ignore) {
                        dto.put("createTime", t.getCreateTime());
                    }
                } else {
                    dto.put("createTime", null);
                }
                dto.put("assignee", t.getAssignee());
                dto.put("processInstanceId", t.getProcessInstanceId());

                // Prefer using processInstance.businessKey to find the business id
                try {
                    org.flowable.engine.runtime.ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                            .processInstanceId(t.getProcessInstanceId()).singleResult();
                    if (pi != null && pi.getBusinessKey() != null) {
                        try {
                            Long bizId = Long.valueOf(pi.getBusinessKey());
                            dto.put("taskId", bizId);
                            SupervisionTask st = supervisionTaskService.getById(bizId);
                            dto.put("task", st);
                            // if business title exists, prefer it as display name
                            if (st != null && st.getTitle() != null && !st.getTitle().isEmpty()) {
                                dto.put("name", st.getTitle());
                            }
                            // add starterName and business metadata
                            if (st != null) {
                                dto.put("starterId", st.getCreatedBy());
                                try {
                                    if (st.getCreatedBy() != null) {
                                        org.xj_service.oa.entity.User su = userMapper
                                                .selectById(st.getCreatedBy().intValue());
                                        if (su != null)
                                            dto.put("starterName",
                                                    su.getNickname() != null ? su.getNickname() : su.getUsername());
                                    }
                                } catch (Exception ignore) {
                                }
                                // dueDate and overdue flag
                                try {
                                    if (st.getDueDate() != null) {
                                        java.time.LocalDate d = st.getDueDate();
                                        boolean overdue = d.isBefore(java.time.LocalDate.now())
                                                && !"completed".equals(st.getStatus());
                                        dto.put("bizDueDate", d.toString());
                                        dto.put("isOverdue", overdue);
                                    }
                                } catch (Exception ignore) {
                                }
                                // include business assignments
                                try {
                                    java.util.List<org.xj_service.oa.entity.SupervisionAssignment> assigns = assignmentMapper
                                            .selectList(
                                                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.xj_service.oa.entity.SupervisionAssignment>()
                                                            .eq("task_id", st.getId()));
                                    java.util.List<java.util.Map<String, Object>> aout = new java.util.ArrayList<>();
                                    if (assigns != null) {
                                        for (org.xj_service.oa.entity.SupervisionAssignment sa : assigns) {
                                            java.util.Map<String, Object> am = new java.util.HashMap<>();
                                            am.put("assigneeUserId", sa.getAssigneeUserId());
                                            am.put("assigneeDeptId", sa.getAssigneeDeptId());
                                            am.put("status", sa.getStatus());
                                            am.put("flowableTaskId", sa.getFlowableTaskId());
                                            if (sa.getAssigneeUserId() != null) {
                                                try {
                                                    org.xj_service.oa.entity.User uu = userMapper
                                                            .selectById(sa.getAssigneeUserId().intValue());
                                                    if (uu != null)
                                                        am.put("assigneeUserName",
                                                                uu.getNickname() != null ? uu.getNickname()
                                                                        : uu.getUsername());
                                                } catch (Exception ignore) {
                                                }
                                            }
                                            aout.add(am);
                                        }
                                    }
                                    dto.put("assignments", aout);
                                } catch (Exception ignore) {
                                }
                            }
                        } catch (Exception ignore) {
                        }
                    }
                    // isClaimable: true if process task has no assignee
                    try {
                        dto.put("isClaimable", t.getAssignee() == null);
                        dto.put("isClaimed", t.getAssignee() != null);
                    } catch (Exception ignore) {
                    }
                } catch (Exception ignore) {
                }

                items.add(dto);
            }
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("500", "获取任务失败: " + e.getMessage());
        }
    }

    // 根据业务 id 查询该业务对应的活动流程任务（可用于发起人查看审批入口）
    @GetMapping("/process/tasks/by-business/{bizId}")
    public Result getProcessTasksByBusiness(@PathVariable Long bizId) {
        try {
            if (bizId == null)
                return Result.success(new java.util.ArrayList<>());
            org.flowable.engine.runtime.ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceBusinessKey(String.valueOf(bizId)).singleResult();
            if (pi == null)
                return Result.success(new java.util.ArrayList<>());
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
            java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
            if (tasks != null) {
                for (Task t : tasks) {
                    java.util.Map<String, Object> dto = new java.util.HashMap<>();
                    dto.put("id", t.getId());
                    dto.put("name", t.getName());
                    dto.put("assignee", t.getAssignee());
                    dto.put("taskDefinitionKey", t.getTaskDefinitionKey());
                    dto.put("processInstanceId", t.getProcessInstanceId());
                    if (t.getCreateTime() != null) {
                        try {
                            java.time.ZonedDateTime z = t.getCreateTime().toInstant()
                                    .atZone(java.time.ZoneId.systemDefault());
                            dto.put("createTime", z.toString());
                        } catch (Exception ignore) {
                            dto.put("createTime", t.getCreateTime());
                        }
                    } else {
                        dto.put("createTime", null);
                    }
                    items.add(dto);
                }
            }
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("500", "查询流程任务失败: " + e.getMessage());
        }
    }

    // 完成流程任务并同步业务状态（简单示例）
    @PostMapping("/process/tasks/{taskId}/complete")
    public Result completeProcessTask(@PathVariable String taskId,
            @RequestBody(required = false) java.util.Map<String, Object> vars,
            HttpServletRequest request) {
        // Delegate to service layer which handles Flowable task completion and business
        // sync
        try {
            return supervisionProcessService.completeFlowableTask(taskId, vars, request);
        } catch (Exception e) {
            return Result.error("500", "任务完成失败: " + e.getMessage());
        }
    }

    @PostMapping("/tasks")
    public Result save(@RequestBody java.util.Map<String, Object> payload,
            @RequestParam(required = false) List<Long> uploadIds,
            HttpServletRequest request) {
        // delegate to service which handles transaction and process start
        return supervisionProcessService.createAndStart(payload, uploadIds, request);
    }

    @PutMapping("/tasks/{id}")
    public Result update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> payload,
            @RequestParam(required = false) List<Long> uploadIds, HttpServletRequest request) {
        try {
            SupervisionTask t = supervisionTaskService.getById(id);
            if (t == null)
                return Result.error("404", "任务不存在");

            // update basic fields if present
            try {
                if (payload.get("title") != null)
                    t.setTitle(String.valueOf(payload.get("title")));
                if (payload.get("description") != null)
                    t.setDescription(String.valueOf(payload.get("description")));
                if (payload.get("dueDate") != null) {
                    try {
                        // expect format 'yyyy-MM-dd' or ISO date
                        t.setDueDate(java.time.LocalDate.parse(String.valueOf(payload.get("dueDate"))));
                    } catch (Exception ignore) {
                    }
                }
                t.setLastUpdate(java.time.LocalDateTime.now());
            } catch (Exception ignore) {
            }

            boolean ok = supervisionTaskService.saveOrUpdate(t);

            // handle assignments if provided (replace existing assignments)
            try {
                Object assignsObj = payload.get("assignments");
                if (assignsObj instanceof java.util.List) {
                    // delete existing
                    assignmentMapper.delete(
                            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.xj_service.oa.entity.SupervisionAssignment>()
                                    .eq("task_id", id));
                    java.util.List<Object> assigns = (java.util.List<Object>) assignsObj;
                    for (Object ao : assigns) {
                        try {
                            java.util.Map<String, Object> amap = (java.util.Map<String, Object>) ao;
                            org.xj_service.oa.entity.SupervisionAssignment sa = new org.xj_service.oa.entity.SupervisionAssignment();
                            sa.setTaskId(id);
                            if (amap.get("type") != null && "user".equals(String.valueOf(amap.get("type")))) {
                                if (amap.get("id") != null)
                                    sa.setAssigneeUserId(Long.valueOf(String.valueOf(amap.get("id"))));
                            } else if (amap.get("type") != null && "dept".equals(String.valueOf(amap.get("type")))) {
                                if (amap.get("id") != null)
                                    sa.setAssigneeDeptId(Long.valueOf(String.valueOf(amap.get("id"))));
                            } else {
                                // fallback: try known keys
                                if (amap.get("assigneeUserId") != null)
                                    sa.setAssigneeUserId(Long.valueOf(String.valueOf(amap.get("assigneeUserId"))));
                                if (amap.get("assigneeDeptId") != null)
                                    sa.setAssigneeDeptId(Long.valueOf(String.valueOf(amap.get("assigneeDeptId"))));
                            }
                            // set assignedBy from token if possible
                            try {
                                String token = request.getHeader("Authorization");
                                if (token != null && !token.isEmpty()) {
                                    String userIdStr = com.auth0.jwt.JWT.decode(token).getAudience().get(0);
                                    if (userIdStr != null && !userIdStr.isEmpty())
                                        sa.setAssignedBy(Long.valueOf(userIdStr));
                                }
                            } catch (Exception ignore) {
                            }
                            sa.setAssignedAt(java.time.LocalDateTime.now());
                            sa.setStatus("open");
                            assignmentMapper.insert(sa);
                        } catch (Exception ignore) {
                        }
                    }
                }
            } catch (Exception ignore) {
            }

            if (ok) {
                java.util.Map<String, Object> dto = objectMapper.convertValue(t, java.util.Map.class);
                return Result.success(dto);
            }
            return Result.error("500", "更新保存失败");
        } catch (Exception e) {
            return Result.error("500", "更新任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/tasks/{id}/feedback")
    public Result submitFeedback(@PathVariable Long id, @RequestBody SupervisionFeedback fb,
            @RequestParam(required = false) List<Long> uploadIds, HttpServletRequest request) {
        // 关联到 task
        fb.setTaskId(id);
        System.out.println(
                "[DEBUG] submitFeedback called for taskId=" + id + ", payload=" + fb + ", uploadIds=" + uploadIds);
        // 尝试从 token 获取当前用户并赋值 feedbackBy
        try {
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                String userIdStr = com.auth0.jwt.JWT.decode(token).getAudience().get(0);
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        fb.setFeedbackBy(Long.valueOf(userIdStr));
                    } catch (Exception ignore) {
                    }
                }
            }
        } catch (Exception ignore) {
        }
        // 如果客户端未设置 feedbackAt，则使用当前时间
        try {
            if (fb.getFeedbackAt() == null)
                fb.setFeedbackAt(java.time.LocalDateTime.now());
        } catch (Exception ignore) {
        }

        boolean ok = feedbackMapper.insert(fb) > 0;
        System.out.println("[DEBUG] feedback insert ok=" + ok + ", feedbackId=" + fb.getId());
        if (ok && uploadIds != null && !uploadIds.isEmpty()) {
            // 插入到 supervision_feedback_upload
            uploadIds.forEach(uid -> {
                org.xj_service.oa.entity.SupervisionFeedbackUpload sfu = new org.xj_service.oa.entity.SupervisionFeedbackUpload();
                sfu.setFeedbackId(fb.getId());
                sfu.setUploadId(uid);
                feedbackUploadMapper.insert(sfu);
            });
        }
        // 返回刚插入的 feedback 对象，便于前端和调试确认
        if (ok)
            return Result.success(fb);
        return Result.error("500", "反馈保存失败");
    }

    // 撤销督察任务：设置业务状态为 cancelled，并清理/完成关联流程待办
    @PutMapping("/tasks/{id}/revoke")
    public Result revoke(@PathVariable Long id, HttpServletRequest request) {
        try {
            SupervisionTask t = supervisionTaskService.getById(id);
            if (t == null)
                return Result.error("404", "任务不存在");

            // 更新业务状态为 cancelled
            t.setStatus("cancelled");
            t.setLastUpdate(java.time.LocalDateTime.now());
            boolean ok = supervisionTaskService.saveOrUpdate(t);

            // 同步处理流程：如果存在流程实例，则将活跃任务标记完成或删除以防继续出现在待办
            try {
                if (t.getProcessInstanceId() != null) {
                    String pid = t.getProcessInstanceId();
                    java.util.List<org.flowable.task.api.Task> activeTasks = taskService.createTaskQuery()
                            .processInstanceId(pid).list();
                    for (org.flowable.task.api.Task at : activeTasks) {
                        try {
                            // 使用 complete 完成当前节点，或根据需要 delete
                            taskService.complete(at.getId());
                        } catch (Exception ignore) {
                            try {
                                taskService.deleteTask(at.getId(), "task revoked by business");
                            } catch (Exception ignore2) {
                            }
                        }
                    }
                    // 可选：终止流程实例
                    try {
                        runtimeService.deleteProcessInstance(pid, "business revoked");
                    } catch (Exception ignore) {
                    }
                }
            } catch (Exception ignore) {
            }

            if (ok) {
                java.util.Map<String, Object> dto = objectMapper.convertValue(t, java.util.Map.class);
                return Result.success(dto);
            }
            return Result.error("500", "撤销失败：业务状态未更新");
        } catch (Exception e) {
            return Result.error("500", "撤销异常：" + e.getMessage());
        }
    }

}
