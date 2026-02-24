package org.xj_service.oa.service.impl;

import com.auth0.jwt.JWT;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.SupervisionAssignment;
import org.xj_service.oa.entity.SupervisionFeedback;
import org.xj_service.oa.entity.SupervisionTask;
import org.xj_service.oa.mapper.SupervisionAssignmentMapper;
import org.xj_service.oa.mapper.SupervisionFeedbackMapper;
import org.xj_service.oa.service.ISupervisionProcessService;
import org.xj_service.oa.service.ISupervisionTaskService;
import org.xj_service.oa.service.UploadService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SupervisionProcessServiceImpl implements ISupervisionProcessService {

    @Resource
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Resource
    private ISupervisionTaskService supervisionTaskService;

    @Resource
    private SupervisionAssignmentMapper assignmentMapper;

    @Resource
    private org.flowable.engine.RuntimeService runtimeService;

    @Resource
    private org.flowable.engine.TaskService taskService;

    @Resource
    private org.flowable.engine.HistoryService historyService;

    @Resource
    private SupervisionFeedbackMapper feedbackMapper;

    @Resource
    private UploadService uploadService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createAndStart(Map<String, Object> payload, List<Long> uploadIds, HttpServletRequest request) {
        try {
            SupervisionTask task = objectMapper.convertValue(payload, SupervisionTask.class);
            // set createdAt if null
            if (task.getCreatedAt() == null)
                task.setCreatedAt(LocalDateTime.now());

            // read starter from request token and set createdBy before initial save
            String starter = null;
            String token = request == null ? null : request.getHeader("Authorization");
            // 临时调试日志：打印收到的 Authorization header 与解析结果，便于排查 createdBy 未写入的原因
            try {
                System.out.println("[DEBUG] /supervision/tasks Authorization header: '" + token + "'");
            } catch (Exception ignore) {
            }
            if (token != null && !token.isEmpty()) {
                try {
                    starter = JWT.decode(token).getAudience().get(0);
                    System.out.println("[DEBUG] parsed starter from token: '" + starter + "'");
                } catch (Exception e) {
                    System.out.println("[DEBUG] failed to decode JWT: " + e.getMessage());
                }
            }
            if (task.getCreatedBy() == null && starter != null && !starter.isEmpty()) {
                try {
                    task.setCreatedBy(Long.valueOf(starter));
                } catch (Exception ignore) {
                }
            }
            try {
                System.out.println("[DEBUG] task before save: id='" + task.getId() + "', title='" + task.getTitle()
                        + "', createdBy='" + task.getCreatedBy() + "'");
            } catch (Exception ignore) {
            }

            boolean ok = supervisionTaskService.saveOrUpdate(task);
            if (!ok)
                return Result.error("500", "保存任务失败");

            // determine assignee (first user assignment)
            String assignedUser = null;
            Object assignsObj = payload.get("assignments");
            java.util.List<java.util.Map<String, Object>> assigns = null;
            if (assignsObj instanceof java.util.List) {
                assigns = (java.util.List<java.util.Map<String, Object>>) assignsObj;
                for (java.util.Map<String, Object> m : assigns) {
                    if ("user".equals(String.valueOf(m.get("type")))) {
                        Object idObj = m.get("id");
                        if (idObj != null) {
                            assignedUser = String.valueOf(idObj);
                            break;
                        }
                    }
                }
            }

            // build vars and start process with businessKey
            Map<String, Object> vars = new HashMap<>();
            vars.put("taskId", task.getId());
            vars.put("assignee", assignedUser);
            vars.put("starter", starter);
            vars.put("title", task.getTitle());
            vars.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);

            String businessKey = String.valueOf(task.getId());
            org.flowable.engine.runtime.ProcessInstance pi = runtimeService
                    .startProcessInstanceByKey("supervision", businessKey, vars);

            task.setProcessInstanceId(pi.getId());
            supervisionTaskService.saveOrUpdate(task);

            // persist assignments
            if (assigns != null) {
                // delete old
                assignmentMapper
                        .delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionAssignment>()
                                .eq("task_id", task.getId()));
                for (java.util.Map<String, Object> a : assigns) {
                    String type = a.get("type") == null ? "" : a.get("type").toString();
                    Long aid = null;
                    try {
                        aid = a.get("id") == null ? null : Long.valueOf(a.get("id").toString());
                    } catch (Exception ignore) {
                    }
                    if (aid == null)
                        continue;
                    SupervisionAssignment sa = new SupervisionAssignment();
                    sa.setTaskId(task.getId());
                    if ("dept".equals(type))
                        sa.setAssigneeDeptId(aid);
                    else if ("user".equals(type))
                        sa.setAssigneeUserId(aid);
                    sa.setAssignedAt(LocalDateTime.now());
                    sa.setStatus("assigned");
                    assignmentMapper.insert(sa);
                }
                // map flowable task ids back to assignments (simple 1:1 by user)
                List<Task> flowTasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
                if (flowTasks != null && !flowTasks.isEmpty()) {
                    // for each flowTask, try to map to a user assignment
                    for (Task ft : flowTasks) {
                        // if ft has assignee, try match
                        String ftAssignee = ft.getAssignee();
                        if (ftAssignee != null) {
                            // find assignment with that assigneeUserId
                            SupervisionAssignment cond = assignmentMapper.selectOne(
                                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionAssignment>()
                                            .eq("assignee_user_id", Long.valueOf(ftAssignee))
                                            .eq("task_id", task.getId()));
                            if (cond != null) {
                                cond.setFlowableTaskId(ft.getId());
                                assignmentMapper.updateById(cond);
                                continue;
                            }
                        }
                        // otherwise assign to first assignment lacking flowableTaskId
                        SupervisionAssignment first = assignmentMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionAssignment>()
                                        .eq("task_id", task.getId()).isNull("flowable_task_id"));
                        if (first != null) {
                            first.setFlowableTaskId(ft.getId());
                            assignmentMapper.updateById(first);
                        }
                    }
                }
            }

            // associate uploads if present
            if (uploadIds != null && !uploadIds.isEmpty()) {
                try {
                    uploadService.updateOriginIds(uploadIds, task.getId());
                } catch (Exception ignore) {
                }
            }

            // return the saved task so frontend can immediately use the object without
            // re-query
            return Result.success(task);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "创建并启动流程失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result completeFlowableTask(String flowableTaskId, Map<String, Object> vars, HttpServletRequest request) {
        try {
            String token = request == null ? null : request.getHeader("Authorization");
            String currentUserId = null;
            if (token != null && !token.isEmpty()) {
                try {
                    currentUserId = JWT.decode(token).getAudience().get(0);
                } catch (Exception ignore) {
                }
            }

            Task t = taskService.createTaskQuery().taskId(flowableTaskId).singleResult();
            // 如果按 taskId 没有找到，尝试把传入 id 当作 processInstanceId 去查找活动任务（容错前端可能传了
            // processInstanceId）
            if (t == null) {
                System.out
                        .println("[DEBUG] no task found by id=" + flowableTaskId + ", trying as processInstanceId...");
                java.util.List<Task> candidates = taskService.createTaskQuery().processInstanceId(flowableTaskId)
                        .list();
                if (candidates != null && !candidates.isEmpty()) {
                    t = candidates.get(0);
                    System.out.println("[DEBUG] resolved processInstanceId to taskId=" + t.getId());
                    // replace flowableTaskId with actual task id to complete
                    flowableTaskId = t.getId();
                }
            }
            if (t == null)
                return Result.error("400", "任务不存在");
            if (t.getAssignee() != null && currentUserId != null && !t.getAssignee().equals(currentUserId)) {
                return Result.error("403", "非任务处理人无权操作");
            }

            String defKey = null;
            try {
                defKey = t.getTaskDefinitionKey();
            } catch (Exception ignore) {
            }

            if (vars == null)
                vars = new HashMap<>();

            // optional: save feedback
            if (vars.containsKey("feedback")) {
                SupervisionFeedback fb = new SupervisionFeedback();
                Object bid = runtimeService.getVariable(t.getProcessInstanceId(), "taskId");
                if (bid instanceof Number)
                    fb.setTaskId(((Number) bid).longValue());
                if (currentUserId != null)
                    try {
                        fb.setFeedbackBy(Long.valueOf(currentUserId));
                    } catch (Exception ignore) {
                    }
                fb.setRemarks(String.valueOf(vars.get("feedback")));
                fb.setFeedbackAt(LocalDateTime.now());
                // 如果前端传入 finishDate（可能为字符串或 Date 对象序列化后的 ISO 字符串），尝试解析并保存到 feedback.finishDate
                try {
                    Object fd = vars.get("finishDate");
                    if (fd != null) {
                        // 支持多种表现形式：LocalDate, java.util.Date, ISO 字符串 "yyyy-MM-dd" 或含时间的 ISO
                        if (fd instanceof java.time.LocalDate) {
                            fb.setFinishDate((java.time.LocalDate) fd);
                        } else if (fd instanceof java.util.Date) {
                            java.util.Date d = (java.util.Date) fd;
                            fb.setFinishDate(d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                        } else {
                            // 字符串解析，尽量兼容含时区/时间的 ISO 字符串
                            String s = String.valueOf(fd);
                            try {
                                // 先尝试按 yyyy-MM-dd 解析
                                fb.setFinishDate(java.time.LocalDate.parse(s));
                            } catch (Exception ex) {
                                try {
                                    // 再尝试解析为 OffsetDateTime/LocalDateTime 并转换
                                    java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(s);
                                    fb.setFinishDate(odt.toLocalDate());
                                } catch (Exception ex2) {
                                    try {
                                        java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s);
                                        fb.setFinishDate(ldt.toLocalDate());
                                    } catch (Exception ex3) {
                                        // ignore parsing failure
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
                feedbackMapper.insert(fb);
            }

            // cache processInstanceId and attempt to read business id BEFORE completing the
            // task
            String processInstanceId = t.getProcessInstanceId();
            Object bid = null;
            try {
                bid = runtimeService.getVariable(processInstanceId, "taskId");
            } catch (Exception ex) {
                // execution may be missing or variable not set yet; we'll try fallbacks after
                System.out.println("[DEBUG] unable to read runtime variable before complete: " + ex.getMessage());
            }

            // complete flowable task
            try {
                System.out.println("[DEBUG] about to complete flowableTaskId=" + flowableTaskId + ", vars=" + vars);
                taskService.complete(flowableTaskId, vars);
            } catch (Exception e) {
                System.out.println("[ERROR] completing flowable task failed for id=" + flowableTaskId + ", exception="
                        + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            // sync business status - 根据当前完成的任务定义决定业务状态
            // If we couldn't read the business id before completing (or execution removed),
            // try to recover it from runtime/process instance or historic data.
            if (bid == null) {
                try {
                    org.flowable.engine.runtime.ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                            .processInstanceId(processInstanceId).singleResult();
                    if (pi != null && pi.getBusinessKey() != null) {
                        try {
                            bid = Long.valueOf(pi.getBusinessKey());
                        } catch (Exception ignore) {
                            bid = pi.getBusinessKey();
                        }
                    }
                } catch (Exception ignore) {
                }
            }
            if (bid == null) {
                try {
                    Object hpiObj = historyService.createHistoricProcessInstanceQuery()
                            .processInstanceId(processInstanceId).singleResult();
                    if (hpiObj != null) {
                        try {
                            java.lang.reflect.Method m = hpiObj.getClass().getMethod("getBusinessKey");
                            Object bk = m.invoke(hpiObj);
                            if (bk != null) {
                                try {
                                    bid = Long.valueOf(String.valueOf(bk));
                                } catch (Exception ignore) {
                                    bid = bk;
                                }
                            }
                        } catch (Exception ignore) {
                        }
                    }
                } catch (Exception ignore) {
                }
            }
            if (bid instanceof Number) {
                Long bizId = ((Number) bid).longValue();
                SupervisionTask st = supervisionTaskService.getById(bizId);
                if (st != null) {
                    boolean isReview = "review".equals(defKey);
                    boolean isHandle = "handle".equals(defKey);

                    Boolean approved = null;
                    try {
                        if (vars != null && vars.containsKey("approved")) {
                            approved = Boolean.valueOf(String.valueOf(vars.get("approved")));
                        } else {
                            Object av = runtimeService.getVariable(t.getProcessInstanceId(), "approved");
                            if (av != null)
                                approved = Boolean.valueOf(String.valueOf(av));
                        }
                    } catch (Exception ignore) {
                    }

                    if (isReview) {
                        if (approved != null && approved) {
                            st.setStatus("completed");
                            st.setProgress(100);
                            // mark all assignments as completed
                            try {
                                java.util.List<SupervisionAssignment> assigns = assignmentMapper.selectList(
                                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionAssignment>()
                                                .eq("task_id", st.getId()));
                                if (assigns != null) {
                                    for (SupervisionAssignment asg : assigns) {
                                        asg.setStatus("completed");
                                        asg.setCompletedAt(LocalDateTime.now());
                                        assignmentMapper.updateById(asg);
                                    }
                                }
                            } catch (Exception ignore) {
                            }
                        } else {
                            st.setStatus("in_progress");
                            st.setProgress(50);
                            // if rejected, reset assignments back to assigned so handler can continue
                            try {
                                java.util.List<SupervisionAssignment> assigns = assignmentMapper.selectList(
                                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionAssignment>()
                                                .eq("task_id", st.getId()));
                                if (assigns != null) {
                                    for (SupervisionAssignment asg : assigns) {
                                        asg.setStatus("assigned");
                                        asg.setCompletedAt(null);
                                        assignmentMapper.updateById(asg);
                                    }
                                }
                            } catch (Exception ignore) {
                            }
                        }
                        st.setLastUpdate(LocalDateTime.now());
                        supervisionTaskService.saveOrUpdate(st);
                    } else if (isHandle) {
                        // 处理人完成后，进入待发起人审批状态
                        st.setStatus("feedback");
                        st.setProgress(80);
                        st.setLastUpdate(LocalDateTime.now());
                        supervisionTaskService.saveOrUpdate(st);

                        // update assignment only when handler completed their task
                        SupervisionAssignment asg = assignmentMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionAssignment>()
                                        .eq("flowable_task_id", flowableTaskId));
                        if (asg != null) {
                            asg.setStatus("completed");
                            asg.setCompletedAt(LocalDateTime.now());
                            assignmentMapper.updateById(asg);
                        }
                    } else {
                        // 默认兼容旧逻辑
                        st.setStatus("completed");
                        st.setProgress(100);
                        st.setLastUpdate(LocalDateTime.now());
                        supervisionTaskService.saveOrUpdate(st);
                    }
                }
            }

            // prepare response with updated business object and feedbacks
            try {
                java.util.Map<String, Object> resp = new java.util.HashMap<>();
                if (bid instanceof Number) {
                    Long bizId = ((Number) bid).longValue();
                    SupervisionTask updated = supervisionTaskService.getById(bizId);
                    resp.put("task", updated);
                    java.util.List<SupervisionFeedback> fbs = feedbackMapper.selectList(
                            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SupervisionFeedback>()
                                    .eq("task_id", bizId).orderByDesc("feedback_at"));
                    resp.put("feedbacks", fbs);
                }
                return Result.success(resp);
            } catch (Exception ignore) {
                return Result.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "完成任务失败: " + e.getMessage());
        }
    }

    @Override
    public Result listMyProcessTasks(HttpServletRequest request) {
        try {
            String token = request == null ? null : request.getHeader("Authorization");
            if (token == null || token.isEmpty())
                return Result.error("401", "未登录");
            String userId = JWT.decode(token).getAudience().get(0);

            List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(userId).orderByTaskCreateTime()
                    .desc().list();
            java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
            for (Task ft : tasks) {
                java.util.Map<String, Object> dto = new java.util.HashMap<>();
                dto.put("id", ft.getId());
                dto.put("name", ft.getName());
                dto.put("assignee", ft.getAssignee());
                dto.put("createTime", ft.getCreateTime());
                dto.put("processInstanceId", ft.getProcessInstanceId());
                // business id
                org.flowable.engine.runtime.ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(ft.getProcessInstanceId()).singleResult();
                if (pi != null && pi.getBusinessKey() != null) {
                    try {
                        Long bizId = Long.valueOf(pi.getBusinessKey());
                        dto.put("taskId", bizId);
                        dto.put("task", supervisionTaskService.getById(bizId));
                    } catch (Exception ignore) {
                    }
                }
                items.add(dto);
            }
            return Result.success(items);
        } catch (Exception e) {
            return Result.error("500", "获取待办失败: " + e.getMessage());
        }
    }
}
