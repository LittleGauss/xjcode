package org.xj_service.oa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.dto.InspectionTaskDTO;
import org.xj_service.oa.entity.InspectionTask;
import org.xj_service.oa.entity.InspectionTaskAssignment;
import org.xj_service.oa.entity.InspectionTemplate;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.InspectionTaskService;
import org.xj_service.oa.service.InspectionTemplateService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/inspection")
public class InspectionTaskController {

    @Autowired
    private InspectionTaskService inspectionTaskService;

    @Resource
    private InspectionTemplateService inspectionTemplateService;

    @Resource
    private FileStorageService minioFileStorageService;

    // 发起检查任务
    @PostMapping("/initiate")
    public Result initiateTask(@ModelAttribute InspectionTaskDTO taskDTO) {
        try {
            // 1. 基础参数校验
            if (taskDTO.getTaskTitle() == null || taskDTO.getTaskTitle().trim().isEmpty()) {
                return Result.error("任务标题不能为空");
            }
            if (taskDTO.getInitiatorId() == null || taskDTO.getInitiatorId() <= 0) {
                return Result.error("发起人ID不能为空且必须为正数");
            }
            if (taskDTO.getDeadline() == null) {
                return Result.error("任务截止时间不能为空");
            }
            if (taskDTO.getInspectorIds() == null || taskDTO.getInspectorIds().isEmpty()) {
                return Result.error("检查员ID列表不能为空");
            }
            // 文件来源校验（二选一）
            if (taskDTO.getTemplateId() == null) {
                return Result.error("文件不能为空（请手动选择模板）");
            }

            // 2. 处理文件来源
            String fileUrl = null;
            if (taskDTO.getTemplateId() != null) {
                // 选用模板文件
                InspectionTemplate template = inspectionTemplateService.getTemplateById(taskDTO.getTemplateId());
                if (template == null) {
                    return Result.error("模板不存在，ID：" + taskDTO.getTemplateId());
                }
                fileUrl = template.getFilePath();
                log.info("选用模板文件，模板ID：{}，文件URL：{}", taskDTO.getTemplateId(), fileUrl);
            }

            // 3. 转换 DTO 为实体对象（如需）
            InspectionTask task = new InspectionTask();
            task.setTaskTitle(taskDTO.getTaskTitle());
            task.setTaskDesc(taskDTO.getTaskDesc());
            task.setInitiatorId(taskDTO.getInitiatorId());
            task.setInitiatorName(taskDTO.getInitiatorName());
            task.setDeadline(taskDTO.getDeadline());
            task.setInspectionFormUrl(fileUrl); // 设置文件URL

            // 4. 调用Service完成任务发起（核心业务逻辑）
            Long taskId = inspectionTaskService.initiateTask(task, taskDTO.getInspectorIds(), fileUrl);
            log.info("检查任务发起成功，任务ID：{}", taskId);
            return Result.success(taskId);
        } catch (Exception e) {
            log.error("发起检查任务失败", e);
            return Result.error("任务发起失败：" + e.getMessage());
        }
    }

    // 检查员接收任务
    @PostMapping("/receive/{assignmentId}")
    public Result receiveTask(@PathVariable Long assignmentId, @RequestParam Long inspectorId) {
        try {
            // 参数校验
            if (assignmentId == null || assignmentId <= 0) {
                return Result.error("任务分配ID不能为空且必须为正数");
            }
            if (inspectorId == null || inspectorId <= 0) {
                return Result.error("检查员ID不能为空且必须为正数");
            }
            boolean success = inspectionTaskService.receiveTask(assignmentId, inspectorId);
            if (success) {
                log.info("检查员{}接收任务分配{}成功", inspectorId, assignmentId);
                return Result.success("任务接收成功");
            } else {
                log.warn("检查员{}接收任务分配{}失败，可能任务不存在或已被接收", inspectorId, assignmentId);
                return Result.error("任务接收失败，可能任务不存在或已被接收");
            }
        } catch (Exception e) {
            log.error("检查员{}接收任务分配{}失败", inspectorId, assignmentId, e);
            return Result.error("任务接收失败：" + e.getMessage());
        }
    }

    // 检查员完成任务
    /**
     * 适配前端的完成任务接口
     * 修改点：
     * 1. 去掉路径参数{assignmentId}，改为从FormData接收
     * 2. 添加remark参数接收（前端传了备注，后端需接收）
     * 3. 所有参数通过@RequestParam从FormData中获取
     */
    @PostMapping("/complete") // 路径改为和前端一致：/inspection/complete（无路径参数）
    public Result completeTask(
            @RequestParam Long assignmentId, // 从FormData接收任务ID（替代@PathVariable）
            @RequestParam Long inspectorId, // 从FormData接收检查员ID
            @RequestParam("filledFormFile") MultipartFile filledFormFile
    ) {
        try {
            // 参数校验（补充assignmentId和inspectorId的校验，保留原有逻辑）
            if (assignmentId == null || assignmentId <= 0) {
                return Result.error("任务分配ID不能为空且必须为正数");
            }
            if (inspectorId == null || inspectorId <= 0) {
                return Result.error("检查员ID不能为空且必须为正数");
            }
            if (filledFormFile == null || filledFormFile.isEmpty()) {
                return Result.error("填写后的表单文件不能为空");
            }
            // 调用service层（如果需要把remark传给service，需同步修改service方法）
            // 注意：如果你的service层原本没有remark参数，需补充：
            boolean success = inspectionTaskService.completeTask(assignmentId, inspectorId, filledFormFile);
            // 若暂时不想改service，可先传null：inspectionTaskService.completeTask(assignmentId, inspectorId, filledFormFile, null);

            if (success) {
                log.info("检查员{}完成任务分配{}成功，备注：{}", inspectorId, assignmentId);
                return Result.success("任务完成成功，等待审核");
            } else {
                log.warn("检查员{}完成任务分配{}失败，备注：{}", inspectorId, assignmentId);
                return Result.error("任务完成失败，可能任务不存在或状态异常");
            }
        } catch (Exception e) {
            log.error("检查员{}完成任务分配{}失败，备注：{}", inspectorId, assignmentId, e);
            return Result.error("任务完成失败：" + e.getMessage());
        }
    }

    // 负责人审核任务
    @PostMapping("/audit/{assignmentId}")
    public Result auditTask(
            @PathVariable Long assignmentId,
            @RequestParam Long initiatorId,
            @RequestParam String auditResult,
            @RequestParam(required = false) String rejectReason) {
        try {
            // 参数校验
            if (assignmentId == null || assignmentId <= 0) {
                return Result.error("任务分配ID不能为空且必须为正数");
            }
            if (initiatorId == null || initiatorId <= 0) {
                return Result.error("发起人ID不能为空且必须为正数");
            }
            System.out.println("看下传递的参数"+auditResult);
            if (!"PASS".equals(auditResult) && !"REJECT".equals(auditResult)) {
                return Result.error("审核结果只能是 pass（通过）或 reject（驳回）");
            }
            if ("REJECT".equals(auditResult) && (rejectReason == null || rejectReason.trim().isEmpty())) {
                return Result.error("驳回任务时，驳回原因不能为空");
            }
            boolean success = inspectionTaskService.auditTask(assignmentId, initiatorId, auditResult, rejectReason);
            if (success) {
                log.info("发起人{}审核任务分配{}，结果：{}", initiatorId, assignmentId, auditResult);
                return Result.success("任务审核" + ("pass".equals(auditResult) ? "通过" : "驳回") + "成功");
            } else {
                log.warn("发起人{}审核任务分配{}失败", initiatorId, assignmentId);
                return Result.error("任务审核失败，可能任务不存在或状态异常");
            }
        } catch (Exception e) {
            log.error("发起人{}审核任务分配{}失败", initiatorId, assignmentId, e);
            return Result.error("任务审核失败：" + e.getMessage());
        }
    }

    // 检查员提交重做任务
//    @PostMapping("/redo/{assignmentId}")
//    public Result submitRedoTask(
//            @PathVariable Long assignmentId,
//            @RequestParam Long inspectorId,
//            @RequestPart("filledFormFile") MultipartFile filledFormFile) {
//        try {
//            // 参数校验
//            if (assignmentId == null || assignmentId <= 0) {
//                return Result.error("任务分配ID不能为空且必须为正数");
//            }
//            if (inspectorId == null || inspectorId <= 0) {
//                return Result.error("检查员ID不能为空且必须为正数");
//            }
//            if (filledFormFile == null || filledFormFile.isEmpty()) {
//                return Result.error("填写后的表单文件不能为空");
//            }
//            boolean success = inspectionTaskService.submitRedoTask(assignmentId, inspectorId, filledFormFile);
//            if (success) {
//                log.info("检查员{}提交重做任务分配{}成功", inspectorId, assignmentId);
//                return Result.success("重做任务提交成功，等待审核");
//            } else {
//                log.warn("检查员{}提交重做任务分配{}失败", inspectorId, assignmentId);
//                return Result.error("重做任务提交失败，可能任务不存在或状态异常");
//            }
//        } catch (Exception e) {
//            log.error("检查员{}提交重做任务分配{}失败", inspectorId, assignmentId, e);
//            return Result.error("重做任务提交失败：" + e.getMessage());
//        }
//    }

    // 动态调整检查人员
    @PostMapping("/adjust/{taskId}")
    public Result adjustInspectors(
            @PathVariable Long taskId,
            @RequestParam(value = "addInspectorIds", required = false) List<Long> addInspectorIds,
            @RequestParam(value = "removeInspectorIds", required = false) List<Long> removeInspectorIds) {
        try {
            // 参数校验
            if (taskId == null || taskId <= 0) {
                return Result.error("任务ID不能为空且必须为正数");
            }
            if ((addInspectorIds == null || addInspectorIds.isEmpty()) && (removeInspectorIds == null || removeInspectorIds.isEmpty())) {
                return Result.error("至少需要添加或删除一名检查员");
            }
            boolean success = inspectionTaskService.adjustInspectors(taskId, addInspectorIds, removeInspectorIds);
            if (success) {
                log.info("调整任务{}的检查人员成功，添加：{}，删除：{}", taskId, addInspectorIds, removeInspectorIds);
                return Result.success("检查人员调整成功");
            } else {
                log.warn("调整任务{}的检查人员失败", taskId);
                return Result.error("检查人员调整失败，可能任务不存在或状态异常或者任务已完成无法调整");
            }
        } catch (Exception e) {
            log.error("调整任务{}的检查人员失败", taskId, e);
            return Result.error("检查人员调整失败：" + e.getMessage());
        }
    }

    // 查询任务详情
    @GetMapping("/detail/{taskId}")
    public Result getTaskDetail(@PathVariable Long taskId) {
        try {
            // 参数校验
            if (taskId == null || taskId <= 0) {
                return Result.error("任务ID不能为空且必须为正数");
            }
            InspectionTask task = inspectionTaskService.getTaskDetail(taskId);
            if (task == null) {
                return Result.error("任务不存在，ID：" + taskId);
            }
            return Result.success(task);
        } catch (Exception e) {
            log.error("查询任务{}详情失败", taskId, e);
            return Result.error("查询任务详情失败：" + e.getMessage());
        }
    }

    // 查询检查员待办任务
    @GetMapping("/todo/{inspectorId}")
    public Result getTodoTasks(@PathVariable Long inspectorId) {
        try {
            // 参数校验
            if (inspectorId == null || inspectorId <= 0) {
                return Result.error("检查员ID不能为空且必须为正数");
            }
            List<Map<String, Object>> resultData = inspectionTaskService.getTodoTasksMap(inspectorId);
            return Result.success(resultData);
        } catch (Exception e) {
            log.error("查询检查员{}待办任务失败", inspectorId, e);
            return Result.error("查询待办任务失败：" + e.getMessage());
        }
    }

    // 计算完成率
    @GetMapping("/completionRate/{taskId}")
    public Result calculateCompletionRate(@PathVariable Long taskId) {
        try {
            // 参数校验
            if (taskId == null || taskId <= 0) {
                return Result.error("任务ID不能为空且必须为正数");
            }
            BigDecimal completionRate = inspectionTaskService.calculateCompletionRate(taskId);
            return Result.success(completionRate);
        } catch (Exception e) {
            log.error("计算任务{}完成率失败", taskId, e);
            return Result.error("计算完成率失败：" + e.getMessage());
        }
    }

    // 终止任务
    @PostMapping("/terminate/{taskId}")
    public Result terminateTask(
            @PathVariable Long taskId,
            @RequestParam String reason,
            @RequestParam Long initiatorId) {
        try {
            // 参数校验
            if (taskId == null || taskId <= 0) {
                return Result.error("任务ID不能为空且必须为正数");
            }
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error("终止原因不能为空");
            }
            if (initiatorId == null || initiatorId <= 0) {
                return Result.error("发起人ID不能为空且必须为正数");
            }
            // 权限校验
            InspectionTask task = inspectionTaskService.getTaskDetail(taskId);
            if (task == null) {
                return Result.error("任务不存在，ID：" + taskId);
            }
            if (!task.getInitiatorId().equals(initiatorId)) {
                return Result.error("无权限终止任务，仅发起人可操作");
            }
            // 执行终止
            boolean success = inspectionTaskService.terminateTask(taskId, reason);
            if (success) {
                log.info("发起人{}终止任务{}成功，原因：{}", initiatorId, taskId, reason);
                return Result.success("任务终止成功");
            } else {
                log.warn("发起人{}终止任务{}失败", initiatorId, taskId);
                return Result.error("任务终止失败，可能任务状态异常");
            }
        } catch (Exception e) {
            log.error("发起人{}终止任务{}失败", initiatorId, taskId, e);
            return Result.error("任务终止失败：" + e.getMessage());
        }
    }

    // 查询流程状态
    @GetMapping("/process/status/{taskId}")
    public Result getProcessStatus(@PathVariable Long taskId) {
        try {
            // 参数校验
            if (taskId == null || taskId <= 0) {
                return Result.error("任务ID不能为空且必须为正数");
            }
            String status = inspectionTaskService.getProcessStatus(taskId);
            return Result.success(status);
        } catch (Exception e) {
            log.error("查询任务{}流程状态失败", taskId, e);
            return Result.error("查询流程状态失败：" + e.getMessage());
        }
    }

    // 手动触发逾期更新（测试用）
    @GetMapping("/updateOverdue")
    public Result updateOverdueStatus() {
        try {
            inspectionTaskService.updateOverdueStatus();
            log.info("手动触发逾期状态更新成功");
            return Result.success("逾期状态更新完成");
        } catch (Exception e) {
            log.error("手动触发逾期状态更新失败", e);
            return Result.error("逾期状态更新失败：" + e.getMessage());
        }
    }

    // 根据发起人ID查询任务列表
    @GetMapping("/list")
    public Result getTaskListByInitiatorId(@RequestParam Long initiatorId) {
        try {
            // 参数校验
            if (initiatorId == null || initiatorId <= 0) {
                return Result.error("发起人ID不能为空且必须为正数");
            }
            System.out.println("发起人initiatorId = " + initiatorId);
            List<InspectionTask> taskList = inspectionTaskService.getTaskListByInitiatorId(initiatorId);
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error("查询任务列表失败：" + e.getMessage());
        }
    }

    // 根据任务ID查询分配列表
    @GetMapping("/assignments/{taskId}")
    public Result getAssignmentsByTaskId(@PathVariable Long taskId) {
        try {
            // 参数校验
            if (taskId == null || taskId <= 0) {
                return Result.error("任务ID不能为空且必须为正数");
            }
            List<?> assignments = inspectionTaskService.getAssignmentsByTaskId(taskId);//拿到了子任务表中的所有记录oa_inspection_task_assignment
            return Result.success(assignments);
        } catch (Exception e) {
            log.error("查询任务{}的分配列表失败", taskId, e);
            return Result.error("查询分配列表失败：" + e.getMessage());
        }
    }
}