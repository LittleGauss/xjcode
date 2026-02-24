package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.xj_service.oa.dto.InspectionTaskDTO;
import org.xj_service.oa.dto.SupervisionTaskDTO;
import org.xj_service.oa.dto.TodoTaskDTO;
import org.xj_service.oa.entity.*;
import org.xj_service.oa.mapper.*;
import org.xj_service.oa.service.IPengingTaskService;
import org.xj_service.oa.service.InspectionTaskService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PengingTaskServiceImpl implements IPengingTaskService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    // 注入相关业务的Mapper
    @Resource
    private ConsumableApplicationMapper applicationMapper;// 低值易耗品
    @Resource
    private LeaveProcessMapper leaveProcessMapper;// 请假
    @Resource
    private ContractReviewMapper contractReviewMapper;// 合同审核

    @Resource
    private SupervisionTaskMapper supervisionTaskMapper;// 督查

    @Resource
    private UserMapper userMapper;// 用户

    @Resource
    private DepartmentMapper departmentMapper;// 部门

    @Resource
    private InspectionTaskService inspectionTaskService;// 日常监督检查任务

    /**
     * 获取用户所有待办任务
     */
    @Override
    public List<TodoTaskDTO> getUserTodoTasks(String userId) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .list();

        return tasks.stream()
                .map(this::convertToTodoTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定流程类型的待办任务
     */
    @Override
    public List<TodoTaskDTO> getUserTodoTasksByProcessType(String userId, String processDefinitionKey) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .processDefinitionKey(processDefinitionKey)
                .list();

        return tasks.stream()
                .map(this::convertToTodoTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取用户待办任务
     */
    @Override
    public Page<TodoTaskDTO> getUserTodoTasksPage(Integer userId, Integer pageNum, Integer pageSize,
                                                  String businessType, String keyword) {
        System.out.println("启动流程前的变量userId: " + userId);
        System.out.println("pageSize: " +pageSize + " pageNum" + pageNum);
        System.out.println("businessType: " + businessType);
        System.out.println("keyword: " + keyword);
        // 创建分页对象
        Page<TodoTaskDTO> page = new Page<>(pageNum, pageSize);

        // 构建任务查询
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId));

        // 根据业务类型过滤
        if (StringUtils.hasText(businessType)) {
            taskQuery.processDefinitionKey(businessType);
        }

        // 获取总记录数
        long total = taskQuery.count();
        page.setTotal(total);
        System.out.println("total: " + total);
        // 分页查询任务
        List<Task> tasks = taskQuery
                .orderByTaskCreateTime().desc()
                .listPage((pageNum - 1) * pageSize, pageSize);

        // 转换为DTO
        List<TodoTaskDTO> records = tasks.stream()
                .map(this::convertToTodoTaskDTO)
                .filter(dto -> filterByKeyword(dto, keyword))
                .collect(Collectors.toList());

        page.setRecords(records);
        return page;
    }

    private TodoTaskDTO convertToTodoTaskDTO(Task task) {
        TodoTaskDTO dto = new TodoTaskDTO();
        dto.setTaskId(task.getId());
        dto.setTaskName(task.getName());
        dto.setProcessInstanceId(task.getProcessInstanceId());
        dto.setProcessDefinitionKey(task.getProcessDefinitionId());
        dto.setAssignee(task.getAssignee());
        dto.setCreateTime(task.getCreateTime());

        String businessKey = null;
        // 获取业务Key
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();

        if (processInstance != null) {
            businessKey = processInstance.getBusinessKey();
        } else {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            if (historicProcessInstance != null) {
                // 如果找到了历史流程实例，获取其业务Key
                businessKey = historicProcessInstance.getBusinessKey();
            }
        }
        dto.setBusinessKey(businessKey);
        System.out.println("businessKey: " + businessKey);
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        String processDefinitionKey = processDefinition.getKey();
        dto.setBusinessType(processDefinitionKey);
        System.out.println("processDefinitionKey: " + processDefinitionKey);
        // 根据流程类型获取对应的业务数据  当前用户也传进去
        Object businessData = getBusinessDataByProcessType(
                processDefinitionKey, businessKey ,task.getAssignee());
        dto.setBusinessData(businessData);
        // 获取流程变量
        Map<String, Object> variables = taskService.getVariables(task.getId());
        dto.setVariables(variables);

        return dto;
    }

    private boolean filterByKeyword(TodoTaskDTO dto, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        // 根据关键字过滤，可以搜索任务名称、业务数据等
        return dto.getTaskName().contains(keyword) ||
                (dto.getBusinessData() != null &&
                        dto.getBusinessData().toString().contains(keyword));
    }

    private Object getBusinessDataByProcessType(String processDefinitionKey, String businessKey,String assignee) {
        System.out.println("processDefinitionID: " + processDefinitionKey);
        System.out.println("businessKey: " + businessKey);
        System.out.println("当前用户assignee: " + assignee);
        if (!StringUtils.hasText(businessKey)) {
            // 非  businessKey不为null、不为空字符串、且不全为空白字符
            return null;
        }
        // 根据不同流程类型获取业务数据
        if (processDefinitionKey.contains("leaveAndCancelProcess")) {
            //请销假流程
            return leaveProcessMapper.selectById(Long.parseLong(businessKey));
        } else if (processDefinitionKey.contains("consumableApplicationProcess")) {
            // 低值易耗品流程
            return applicationMapper.selectById(Integer.parseInt(businessKey));
        } else if (processDefinitionKey.contains("contractReviewProcess")) {
            // 合同审核流程
            return contractReviewMapper.selectById(Integer.parseInt(businessKey));
        } else if (processDefinitionKey.contains("supervision")) {
            // 督查任务流程
            SupervisionTask task = supervisionTaskMapper.selectById(Long.parseLong(businessKey));
            if (task != null) {
                return enrichSupervisionTask(task);
            }
            return null;
        } else if (processDefinitionKey.contains("inspectionTaskProcess")) {
            // 日常监督检查任务流程
            InspectionTask inspectionTask =inspectionTaskService.getTaskDetail(Long.parseLong(businessKey.split("_")[1]));
            //再拿子任务的数据
            InspectionTaskAssignment inspectionTaskAssignment = inspectionTaskService.getAssignmentByTaskIdAndUseID(inspectionTask.getId(),Long.valueOf(assignee));
            Map<String, Object> taskPair = new HashMap<>();
            taskPair.put("assignment", inspectionTaskAssignment);
            taskPair.put("mainTask", inspectionTask);
            if (taskPair != null && taskPair.size() > 0) {
                return taskPair;
            }
            return null;
        }
        return null;
    }

    /**
     * 丰富督查任务信息，添加用户名和部门名
     */
    private SupervisionTaskDTO enrichSupervisionTask(SupervisionTask task) {
        SupervisionTaskDTO dto = new SupervisionTaskDTO();
        BeanUtils.copyProperties(task, dto);

        // 获取创建人信息
        if (task.getCreatedBy() != null) {
            // createdBy 为 Long，而用户ID为 Integer，类型不一致会导致查询不到
            Integer creatorId = null;
            try {
                creatorId = Math.toIntExact(task.getCreatedBy());
            } catch (ArithmeticException ex) {
                // 超出 Integer 范围则直接不设置，避免异常
            }
            User creator = creatorId != null ? userMapper.selectById(creatorId) : null;
            if (creator != null) {
                dto.setCreatorName(creator.getNickname() != null ? creator.getNickname() : creator.getUsername());

                // 获取创建人部门信息
                if (creator.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(creator.getDepartmentId());
                    if (department != null) {
                        dto.setCreatorDepartment(department.getName());
                    }
                }
            }
        }

        // 设置当前审核人（督查任务当前审核人就是创建人）
        dto.setCurrentApprover(dto.getCreatorName() != null ? dto.getCreatorName() : "");

        return dto;
    }
}