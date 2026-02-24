package org.xj_service.oa.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.dto.TodoTaskDTO;
import org.xj_service.oa.entity.InspectionTask;
import org.xj_service.oa.entity.InspectionTaskAssignment;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.mapper.InspectionTaskAssignmentMapper;
import org.xj_service.oa.mapper.InspectionTaskMapper;
import org.xj_service.oa.mapper.UserMapper;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.InspectionTaskService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InspectionTaskServiceImpl extends ServiceImpl<InspectionTaskMapper, InspectionTask> implements InspectionTaskService {
    private static final String PROCESS_KEY = "inspectionTaskProcess";
    private static final String TASK_STATUS_INITIATED = "INITIATED";
    private static final String TASK_STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String ASSIGN_STATUS_PENDING = "PENDING";
    private static final String ASSIGN_STATUS_RECEIVED = "RECEIVED";
    private static final String ASSIGN_STATUS_SUBMIT = "SUBMIT";
    private static final String ASSIGN_STATUS_COMPLETED = "COMPLETED";//REDO/OVERDUE/CANCELLED  重做中、逾期、取消
    private static final String OVERDUE_FLAG_NO = "NO";

    @Autowired
    private InspectionTaskMapper taskMapper;

    @Autowired
    private InspectionTaskAssignmentMapper assignmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Transactional
    public Long initiateTask(InspectionTask task, List<Long> inspectorIds, String formFileUrl) {
        // 1. 入参校验
        if (task == null || CollectionUtils.isEmpty(inspectorIds) || formFileUrl == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 2. 初始化任务
        task.setInspectionFormUrl(formFileUrl);
        task.setStatus(TASK_STATUS_INITIATED);
        task.setTotalInspectors(inspectorIds.size());
        task.setCompletedInspectors(0);
        task.setOverdueFlag(OVERDUE_FLAG_NO);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.insert(task);
        Long taskId = task.getId();
        log.info("任务主表创建成功，ID：{}", taskId);


        Map<String, Object> variables = new HashMap<>();
        variables.put("inspectionTaskId", taskId);
        variables.put("inspectorIdsList", inspectorIds);//放集合试一试
        variables.put("initiatorId", task.getInitiatorId());
        variables.put("deadline", task.getDeadline());
        variables.put("taskTitle", task.getTaskTitle());


        // 4. 启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, "task_" + task.getId(),variables);
        String processInstanceId = processInstance.getId();
        log.info("Flowable流程启动成功，实例ID：{}", processInstanceId);

        // 5. 更新任务流程信息
        task.setProcessInstanceId(processInstanceId);
        task.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        task.setStatus(TASK_STATUS_IN_PROGRESS);
        taskMapper.updateById(task);

        // 6. 获取流程任务列表
        List<Task> flowTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
        log.info("流程任务数量：{}", flowTasks.size());

        // 7. 批量创建任务分配记录
        List<InspectionTaskAssignment> assignmentList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        Map<Long, Task> taskMap = flowTasks.stream()
                .collect(Collectors.toMap(t -> Long.valueOf(t.getAssignee()), t -> t));

        for (Long inspectorId : inspectorIds) {
            User inspector = userMapper.selectById(inspectorId);
            if (inspector == null) {
                throw new RuntimeException("检查员不存在，ID：" + inspectorId);
            }

            InspectionTaskAssignment assignment = new InspectionTaskAssignment();
            assignment.setInspectionTaskId(taskId);
            assignment.setInspectorId(inspectorId);
            assignment.setInspectorName(inspector.getUsername());
            assignment.setTaskStatus(ASSIGN_STATUS_PENDING);
            assignment.setRedoCount(0);
            assignment.setCreateTime(now);
            assignment.setUpdateTime(now);
            // 关联Flowable任务ID
            if (taskMap.containsKey(inspectorId)) {
                assignment.setFlowTaskId(taskMap.get(inspectorId).getId());
            }
            assignmentList.add(assignment);
        }

        // 8. 批量插入分配记录
        if (!assignmentList.isEmpty()) {
            assignmentMapper.batchInsert(assignmentList);
            log.info("任务分配记录创建成功，数量：{}", assignmentList.size());
        }

        return taskId;
    }

    @Override
    public boolean receiveTask(Long assignmentId, Long inspectorId) {

        InspectionTaskAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !assignment.getInspectorId().equals(inspectorId)) {
            log.error("任务分配记录不存在或无权操作，ID：{}，检查员ID：{}", assignmentId, inspectorId);
            return false;
        }
        if (!"PENDING".equals(assignment.getTaskStatus())) {
            log.error("任务分配不是待接收状态，无权操作，ID：{}，检查员ID：{}", assignmentId, inspectorId);
            return false;
        }

        // 2. 更新状态
        assignment.setTaskStatus(ASSIGN_STATUS_RECEIVED);
        assignment.setReceiveTime(LocalDateTime.now());
        int updateCount = assignmentMapper.updateById(assignment);
        log.info("检查员{}接收任务{}，更新结果：{}", inspectorId, assignmentId, updateCount > 0);
        return updateCount > 0;
    }

    @Override
    @Transactional
    public boolean completeTask(Long assignmentId, Long inspectorId, MultipartFile filledFormFile) {
        // 1. 查询分配记录
        InspectionTaskAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !assignment.getInspectorId().equals(inspectorId)) {
            log.error("任务分配记录不存在或无权操作，ID：{}，检查员ID：{}", assignmentId, inspectorId);
            return false;
        }
        String filledFormUrl = fileStorageService.upload(filledFormFile,"oa-inspection",filledFormFile.getOriginalFilename());
        assignment.setFilledFormUrl(filledFormUrl);
        // 2. 完成Flowable任务
        if (assignment.getFlowTaskId() != null) {
            taskService.complete(assignment.getFlowTaskId());
            log.info("Flowable任务完成，ID：{}", assignment.getFlowTaskId());
            InspectionTask instask = taskMapper.selectById(assignment.getInspectionTaskId());
            // 获取审核任务并关联
            Task auditTask = taskService.createTaskQuery()
                    .processInstanceBusinessKey("task_" + assignment.getInspectionTaskId())
                    .taskAssignee(instask.getInitiatorId().toString())//这里传的应该是主任务发起人的id
                    .singleResult();
            System.out.println("给发起人的审核任务auditTask = " + auditTask);
            if (auditTask != null) {
                assignment.setAuditFlowTaskId(auditTask.getId());
            }
        }

        // 3. 更新分配记录
        assignment.setTaskStatus(ASSIGN_STATUS_SUBMIT);//变提交态，属于提交状态的发起人审核 （拒绝后冲提交变redoing）REDOING状态的的也能审核
        assignment.setFilledFormUrl(filledFormUrl);
        assignment.setUpdateTime(LocalDateTime.now());//完成时间是 审核通过后更新  这里更新刷新时间即可
        assignmentMapper.updateById(assignment);
        return true;
    }

    /**
     * 任务发起人审核任务，可以通过 也可以拒绝  拒绝后需要重新提交
     * @param assignmentId
     * @param initiatorId
     * @param auditResult
     * @param rejectReason
     * @return
     */
    @Override
    @Transactional
    public boolean auditTask(Long assignmentId, Long initiatorId, String auditResult, String rejectReason) {
        // 1. 验证权限
        InspectionTaskAssignment assignment = assignmentMapper.selectById(assignmentId);
        InspectionTask task = taskMapper.selectById(assignment.getInspectionTaskId());
        if (assignment == null || !task.getInitiatorId().equals(initiatorId)) {
            return false;
        }

        // 2. 更新分配表状态
        if ("REJECT".equals(auditResult)) {
            assignment.setTaskStatus("REDO");//驳回就直接重做
            assignment.setRejectReason(rejectReason);
        } else if ("PASS".equals(auditResult)) {
            assignment.setTaskStatus("COMPLETED");
            // 更新主表完成数
            task.setCompletedInspectors(task.getCompletedInspectors() + 1);
            // 4. 更新任务主表的时间 子任务刷新了下  如果子任务都完成
            task.setUpdateTime(LocalDateTime.now());
            // 检查是否所有检查员都已完成
            if (task.getCompletedInspectors().equals(task.getTotalInspectors())) {
                task.setStatus("COMPLETED");
            }
            taskMapper.updateById(task);
        }
        assignment.setUpdateTime(LocalDateTime.now());
        assignmentMapper.updateById(assignment);

        // 3. 完成审核任务，设置流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("auditResult", auditResult);
        taskService.complete(assignment.getAuditFlowTaskId(), variables);//这里拿的是审核任务的id，在用户提交检查时需要设置的

        return true;
    }

    /**
     * 此方法与提交任务差不多，没有用到
     * @param assignmentId
     * @param inspectorId
     * @param filledFormFile
     * @return
     */
    @Override
    @Transactional
    public boolean submitRedoTask(Long assignmentId, Long inspectorId, MultipartFile filledFormFile) {
        // 1. 验证状态
        InspectionTaskAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !assignment.getInspectorId().equals(inspectorId) || !"REDO".equals(assignment.getTaskStatus())) {
            return false;
        }

        // 2. 上传重做文件
        String filledFormUrl = fileStorageService.upload(filledFormFile,"oa_inspection",filledFormFile.getOriginalFilename());
        assignment.setFilledFormUrl(filledFormUrl);
        assignment.setTaskStatus(ASSIGN_STATUS_SUBMIT);
        assignment.setUpdateTime(LocalDateTime.now());
        assignmentMapper.updateById(assignment);

        // 3. 完成重做任务
        taskService.complete(assignment.getFlowTaskId());

        return true;
    }

    @Override
    @Transactional
    public boolean adjustInspectors(Long taskId, List<Long> addInspectorIds, List<Long> removeInspectorIds) {
        InspectionTask task = taskMapper.selectById(taskId);
        //// IN_PROGRESS/COMPLETED/CANCELLED/OVERDUE  进行中的才可调整
        if (task == null || "COMPLETED".equals(task.getStatus())|| "CANCELLED".equals(task.getStatus())|| "OVERDUE".equals(task.getStatus())) {
            return false;
        }

        // 1. 移除检查员
        if (removeInspectorIds != null && !removeInspectorIds.isEmpty()) {
            for (Long removeId : removeInspectorIds) {
                QueryWrapper<InspectionTaskAssignment> wrapper = new QueryWrapper<>();
                wrapper.eq("inspection_task_id", taskId).eq("inspector_id", removeId);
                InspectionTaskAssignment assignment = assignmentMapper.selectOne(wrapper);
                if (assignment != null) {
                    // 完成Flowable任务
                    if (assignment.getFlowTaskId() != null) {
                        taskService.complete(assignment.getFlowTaskId());
                    }
                    // 删除分配记录
                    assignmentMapper.deleteById(assignment.getId());
                    // 更新总数
                    task.setTotalInspectors(task.getTotalInspectors() - 1);
                }
            }
        }

        // 2. 新增检查员
        if (addInspectorIds != null && !addInspectorIds.isEmpty()) {
            for (Long addId : addInspectorIds) {
                User inspector = userMapper.selectById(addId);
                // 新增分配记录
                InspectionTaskAssignment assignment = new InspectionTaskAssignment();
                assignment.setInspectionTaskId(taskId);
                assignment.setInspectorId(addId);
                assignment.setInspectorName(inspector.getUsername());
                assignment.setTaskStatus("RECEIVED");
                assignment.setReceiveTime(LocalDateTime.now());
                assignment.setRedoCount(0);
                assignment.setCreateTime(LocalDateTime.now());
                assignmentMapper.insert(assignment);

                // 动态创建Flowable任务
                Task newTask = taskService.newTask();
                newTask.setName("执行检查任务（新增）");
                newTask.setAssignee(addId.toString());
                taskService.saveTask(newTask);

                // 更新分配表的Flowable任务ID
                assignment.setFlowTaskId(newTask.getId());
                assignmentMapper.updateById(assignment);

                // 更新总数
                task.setTotalInspectors(task.getTotalInspectors() + 1);
            }
        }

        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        return true;
    }

    @Override
    public InspectionTask getTaskDetail(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    public List<Map<String, Object>> getTodoTasksMap(Long inspectorId){
        // 构建返回数据列表，每个元素包含子任务和主任务
        List<Map<String, Object>> resultData = new ArrayList<>();
        QueryWrapper<InspectionTaskAssignment> wrapper = new QueryWrapper<>();
        wrapper.eq("inspector_id", inspectorId);
        List<InspectionTaskAssignment> todoTasks = assignmentMapper.selectList(wrapper);
        for (InspectionTaskAssignment taskAssignment : todoTasks) {
            // 获取每个子任务对应的主任务
            InspectionTask mainTask = getTaskDetail(taskAssignment.getInspectionTaskId());

            // 创建包含子任务和主任务的Map
            Map<String, Object> taskPair = new HashMap<>();
            taskPair.put("assignment", taskAssignment);
            taskPair.put("mainTask", mainTask);

            resultData.add(taskPair);
        }
        return resultData;
    }

    /**
     * 根据子任务负责人（检查员ID）查询所有子任务，并封装为 TodoTaskDTO
     *这是给主页的待办使用的
     * @param assignee 子任务负责人ID（检查员ID）
     * @return 封装后的 TodoTaskDTO 列表
     */
    @Override
    public List<TodoTaskDTO> getTodoDTOTasks(Long assignee) {
        System.out.println("任务执行人: " + assignee);

        // 2. 查询 Flowable 中该负责人的所有任务
        TaskQuery taskQuery = taskService.createTaskQuery().processDefinitionKey(PROCESS_KEY)
                .taskAssignee(String.valueOf(assignee)) // 根据负责人筛选
                .includeProcessVariables(); // 按创建时间倒序

        List<Task> flowableTasks = taskQuery.list();

        System.out.println("工作流任务列表flowableTasks: " +flowableTasks+"size: "+flowableTasks.size());
        if (CollectionUtils.isEmpty(flowableTasks)) {
            return new ArrayList<>(); // 无数据返回空列表
        }


        // 3. 转换 Flowable 任务为 TodoTaskDTO，并关联业务数据
        List<TodoTaskDTO> todoTaskDTOList = new ArrayList<>();
        for (Task flowableTask : flowableTasks) {
            TodoTaskDTO dto = convertToTodoTaskDTO(flowableTask);
            todoTaskDTOList.add(dto);
        }

        return todoTaskDTOList;
    }

    /**
     * 将 Flowable Task 转换为 TodoTaskDTO，并填充 businessData（主任务数据）
     *
     * @param flowableTask Flowable 任务对象
     * @return 封装后的 TodoTaskDTO
     */
    private TodoTaskDTO convertToTodoTaskDTO(Task flowableTask) {
        // 1. 初始化 DTO 并拷贝基础属性
        TodoTaskDTO dto = new TodoTaskDTO();
        // 2. 设置 DTO 特有属性
        dto.setTaskId(flowableTask.getId());
        dto.setTaskName(flowableTask.getName());
        dto.setProcessInstanceId(flowableTask.getProcessInstanceId());
        dto.setProcessDefinitionKey(flowableTask.getTaskDefinitionKey());
        dto.setCreateTime(flowableTask.getCreateTime());
        dto.setAssignee(flowableTask.getAssignee());
        dto.setVariables(flowableTask.getProcessVariables()); // 流程变量

        System.out.println("Task ID: " + dto.getTaskId());
        System.out.println("Task Name: " + dto.getTaskName());
        System.out.println("Process Instance ID: " + dto.getProcessInstanceId());
        System.out.println("Process Definition Key: " + dto.getProcessDefinitionKey());
        System.out.println("Create Time: " + dto.getCreateTime());
        System.out.println("Assignee: " + dto.getAssignee());
        System.out.println("Variables: " + dto.getVariables());
        Long inspectorId = (Long) flowableTask.getProcessVariables().get("initiatorId");
        List<InspectionTaskAssignment>  taskAssignment = assignmentMapper.selectByFlowTaskId(inspectorId, flowableTask.getId());
        if(taskAssignment==null || taskAssignment.size()!=1){
            log.error("任务分配记录异常");
        } else {
            //在这里给待办的页面设置各种可以查到的属性  待完成
        }


        // 3. 从流程变量中获取主任务ID，查询主任务数据并设置到 businessData
        Long inspectionTaskId = (Long) flowableTask.getProcessVariables().get("inspectionTaskId");
        if (inspectionTaskId != null) {
            InspectionTask mainTask = taskMapper.selectById(inspectionTaskId);
            dto.setBusinessData(mainTask); // 主任务数据存入 businessData
            dto.setBusinessType("inspectionTaskProcess"); // 巡检任务类型标识
        } else {
            // 无主任务ID时，businessData 设为 null 或空对象
            dto.setBusinessData(null);
            dto.setBusinessType(null);
        }

        return dto;
    }

    @Override
    public BigDecimal calculateCompletionRate(Long taskId) {
        InspectionTask task = taskMapper.selectById(taskId);
        if (task.getTotalInspectors() == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(task.getCompletedInspectors())
                .divide(new BigDecimal(task.getTotalInspectors()), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
    }

    /**
     * 整一个定时器，如果超时了 自动结束所有任务变为逾期状态即可  工作流的任务也结束
     */
    @Override
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    public void updateOverdueStatus() {
        QueryWrapper<InspectionTask> taskWrapper = new QueryWrapper<>();
        taskWrapper.eq("status", "IN_PROGRESS");
        List<InspectionTask> taskList = taskMapper.selectList(taskWrapper);

        LocalDateTime now = LocalDateTime.now();
        for (InspectionTask task : taskList) {
            if (task.getDeadline() != null && task.getDeadline().isBefore(now)) {
                // 更新主表逾期状态
                task.setOverdueFlag("YES");
                taskMapper.updateById(task);

                // 更新分配表逾期状态  PENDING//RECEIVED/SUBMIT/COMPLETED/REDO/OVERDUE/CANCELLED
                QueryWrapper<InspectionTaskAssignment> assignmentWrapper = new QueryWrapper<>();
                assignmentWrapper.eq("inspection_task_id", task.getId())
                        .notIn("task_status", Arrays.asList("PENDING", "RECEIVED", "REDO"));//其他状态的保留，只有这些状态变逾期
                List<InspectionTaskAssignment> assignments = assignmentMapper.selectList(assignmentWrapper);
                for (InspectionTaskAssignment assignment : assignments) {
                    assignment.setTaskStatus("OVERDUE");
                    assignmentMapper.updateById(assignment);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 确保异常时回滚
    public boolean terminateTask(Long taskId, String reason) {
        InspectionTask task = taskMapper.selectById(taskId);
        if (task == null || task.getProcessInstanceId() == null) {
            return false;
        }
        try {
            // 2. 终止 Flowable 流程实例（Flowable 会自动终止该实例下的所有活动任务）
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), reason);

            // 3. 更新主任务状态为已取消
            task.setStatus("CANCELLED");
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);

            // 4. 查询该主任务下的所有子任务（巡检任务分配记录）
            QueryWrapper<InspectionTaskAssignment> wrapper = new QueryWrapper<>();
            wrapper.eq("inspection_task_id", taskId);
            List<InspectionTaskAssignment> assignmentList = assignmentMapper.selectList(wrapper);

            // 5. 批量更新子任务状态为已取消
            if (!assignmentList.isEmpty()) {
                for (InspectionTaskAssignment assignment : assignmentList) {
                    // 只更新未完成的任务状态
                    if (!"COMPLETED".equals(assignment.getTaskStatus()) &&
                            !"CANCELLED".equals(assignment.getTaskStatus())) {
                        assignment.setTaskStatus("CANCELLED"); // 与主任务状态一致
                        assignment.setCompleteTime(LocalDateTime.now()); // 记录终止时间
                        assignment.setUpdateTime(LocalDateTime.now());
                        assignment.setRejectReason("任务被发起人终止: " + reason);
                        // 处理流程任务
                        if (assignment.getFlowTaskId() != null) {
                            try {
                                Task flowTask = taskService.createTaskQuery()
                                        .taskId(assignment.getFlowTaskId())
                                        .singleResult();

                                if (flowTask != null) {
                                    // 设置终止变量
                                    Map<String, Object> variables = new HashMap<>();
                                    variables.put("terminated", true);
                                    variables.put("terminationReason", reason);
                                    // 完成任务或删除
                                    taskService.complete(assignment.getFlowTaskId(), variables);
                                }
                            } catch (Exception e) {
                                log.warn("处理流程任务失败: {}", assignment.getFlowTaskId(), e);
                            }
                        }
                        assignmentMapper.updateById(assignment);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("终止巡检任务失败", e); // 抛出异常触发事务回滚
        }
    }

    @Override
    public String getProcessStatus(Long taskId) {
        InspectionTask task = taskMapper.selectById(taskId);
        if (task == null || task.getProcessInstanceId() == null) {
            return "UNKNOWN";
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();

        if (processInstance == null) {
            return "COMPLETED";
        } else {
            return "RUNNING";
        }
    }

    /*查看发起人发起的任务列表
        <select id="selectByInitiatorId" resultType="org.xj_service.oa.entity.InspectionTask">
        SELECT * FROM oa_inspection_task WHERE initiator_id = #{initiatorId} ORDER BY create_time DESC
    </select>
     */
    @Override
    public List<InspectionTask> getTaskListByInitiatorId(Long initiatorId) {
        return taskMapper.selectByInitiatorId(initiatorId);
    }

    /*
    根据主任务ID查找所有子任务
        <select id="selectByTaskId" resultType="org.xj_service.oa.entity.InspectionTaskAssignment">
        SELECT * FROM oa_inspection_task_assignment WHERE inspection_task_id = #{taskId}
    </select>
     */
    @Override
    public List<InspectionTaskAssignment> getAssignmentsByTaskId(Long taskId) {
        return assignmentMapper.selectByTaskId(taskId);
    }

    //根据主任务ID 加上当前处理人ID 得到子任务实例
    @Override
    public InspectionTaskAssignment getAssignmentByTaskIdAndUseID(Long taskId, Long userId){
          return assignmentMapper.getAssignmentByTaskIdAndUseID(taskId, userId);
    }

    /**
     * 检查逾期任务 执行终止
     */
    @Override
    @Transactional
    public void checkAndHandleOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<InspectionTask> overdueTasks = taskMapper.findOverdueTasks(now);

        for (InspectionTask task : overdueTasks) {
            // 更新主任务状态
            try {
                // 1. 终止工作流实例
                terminateProcessInstance(task);

                // 2. 更新主任务状态
                task.setStatus("OVERDUE");
                task.setOverdueFlag("YES");
                task.setUpdateTime(now);
                taskMapper.updateById(task);

                // 3. 更新所有未完成的子任务状态
                updateOverdueAssignments(task.getId(), now);

                log.info("任务已标记为逾期并终止流程，任务ID: {}", task.getId());

            } catch (Exception e) {
                log.error("处理逾期任务失败，任务ID: {}", task.getId(), e);
                // 记录错误但不中断其他任务的处理
            }
        }
    }
    /**
     * 终止流程实例
     */
    private void terminateProcessInstance(InspectionTask task) {
        if (task.getProcessInstanceId() == null) {
            return;
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();

        if (processInstance != null && !processInstance.isEnded()) {
            try {
                // 终止流程实例并设置原因
                runtimeService.deleteProcessInstance(
                        task.getProcessInstanceId(),
                        "任务逾期自动终止"
                );
                log.info("已终止流程实例: {}", task.getProcessInstanceId());
            } catch (Exception e) {
                log.error("终止流程实例失败: {}", task.getProcessInstanceId(), e);
                throw new RuntimeException("终止流程实例失败", e);
            }
        }
    }

    /**
     * 更新逾期子任务状态
     */
    private void updateOverdueAssignments(Long taskId, LocalDateTime now) {
        List<InspectionTaskAssignment> assignments = assignmentMapper.selectByTaskId(taskId);

        for (InspectionTaskAssignment assignment : assignments) {
            // 只更新未完成的任务状态 PENDING//RECEIVED/SUBMIT/COMPLETED/REDO/OVERDUE/CANCELLED
            if (!"COMPLETED".equals(assignment.getTaskStatus()) &&
                    !"CANCELLED".equals(assignment.getTaskStatus())) {

                assignment.setTaskStatus("OVERDUE");
                assignment.setUpdateTime(now);

                // 如果流程任务存在，也需要处理
                if (assignment.getFlowTaskId() != null) {
                    try {
                        // 可以尝试完成或删除流程任务
                        Task flowTask = taskService.createTaskQuery()
                                .taskId(assignment.getFlowTaskId())
                                .singleResult();

                        if (flowTask != null) {
                            // 设置流程变量标记为逾期
                            Map<String, Object> variables = new HashMap<>();
                            variables.put("overdue", true);
                            variables.put("overdueReason", "任务逾期自动终止");

                            // 完成或删除任务
                            taskService.complete(assignment.getFlowTaskId(), variables);
                        }
                    } catch (Exception e) {
                        log.warn("处理流程任务失败: {}", assignment.getFlowTaskId(), e);
                    }
                }

                assignmentMapper.updateById(assignment);
            }
        }
    }
}