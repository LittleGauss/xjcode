package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.xj_service.oa.entity.ConsumableApplication;
import org.xj_service.oa.entity.ConsumableGoods;
import org.xj_service.oa.entity.ConsumableInOutRecord;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.mapper.ConsumableApplicationMapper;
import org.xj_service.oa.mapper.ConsumableCategoryMapper;
import org.xj_service.oa.mapper.UserMapper;
import org.xj_service.oa.service.ConsumableApplicationService;
import org.xj_service.oa.service.ConsumableInOutRecordService;
import org.xj_service.oa.service.IConsumableGoodsService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsumableApplicationServiceImpl
        extends ServiceImpl<ConsumableApplicationMapper, ConsumableApplication>
        implements ConsumableApplicationService {
    @Resource
    private ConsumableApplicationMapper applicationMapper;

    @Resource
    private UserMapper userMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private IConsumableGoodsService goodsService;

    @Autowired
    private ConsumableInOutRecordService recordService;

    @Autowired
    private ConsumableCategoryMapper categoryMapper;

    /**
     * 提交领用申请（自动填充申请时间、默认状态）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitApplication(ConsumableApplication application) {
        // 1. 保存申请数据
        application.setApplyTime(LocalDateTime.now());
        application.setFinalStatus("pending_first_approval");//等待一级审批
        applicationMapper.insert(application);

        // 2. 启动Flowable流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicantId", application.getApplicantId().toString()); // 申请人ID（字符串类型）
        variables.put("consumableType", application.getConsumableType());
        variables.put("infoApprover", application.getAssigneeId().toString()); // 信息审核人
        // 添加调试信息
//        System.out.println("启动流程前的变量: " + variables);
//        System.out.println("申请人ID: " + application.getApplicantId());
//        System.out.println("审核人ID: " + application.getAssigneeId());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "consumableApplicationProcess", // 流程定义ID
                application.getId().toString(), // 业务Key（关联申请单ID）
                variables
        );
        // 打印流程实例信息
//        System.out.println("流程实例ID: " + processInstance.getId());
//        System.out.println("流程实例业务Key: " + processInstance.getBusinessKey());
        // 3. 更新流程实例ID
        application.setProcessInstanceId(processInstance.getId());
        applicationMapper.updateById(application);

        Task submitTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey("submitApplication")
                .singleResult();

        if (submitTask != null) {
            taskService.complete(submitTask.getId());
            System.out.println("已完成提交申请任务");
        }
        return true;
    }

    /**
     * 根据申请人ID查询
     */
    @Override
    public List<ConsumableApplication> getByApplicantId(Integer applicantId) {
        return applicationMapper.selectByApplicantId(applicantId);
    }

    /**
     * 根据流程实例ID查询
     */
    @Override
    public ConsumableApplication getByProcessInstanceId(String processInstanceId) {
        return applicationMapper.selectByProcessInstanceId(processInstanceId);
    }

    /**
     * 更新申请状态（流程审批通过/驳回后调用）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Integer applicationId, String currentStatus) {
        ConsumableApplication application = getById(applicationId);
        if (application == null) {
            return false;
        }
        application.setFinalStatus(currentStatus);
        return updateById(application);
    }

    /**
     * 查询待审批的申请（状态为pending）
     */
    @Override
    public List<ConsumableApplication> getPendingByType(String consumableType) {
        return applicationMapper.selectByStatusAndType("pending", consumableType);
    }

    /**
     * 获取所有领用申请
     */
    @Override
    public List<ConsumableApplication> getAllApplications() {
        return applicationMapper.selectAllApplications();
    }

    /**
     * 根据状态筛选申请
     */
    @Override
    public List<ConsumableApplication> getByStatus(String status) {
        return applicationMapper.selectByStatus(status);
    }


    // 获取用户待办任务

    /**
     * 返回的结果
     * applicantId: 申请人ID - 发起耗材申请的用户标识，值为"19"表示申请人ID是19
     * applicationId: 申请单ID - 耗材申请单的唯一标识，值为"5"表示这是第5号申请单  对应这里的记录oa_consumable_application
     * assignee: 当前处理人 - 当前需要处理该任务的用户ID，值为27表示当前分配给ID为27的用户处理
     * consumableType: 耗材类型 - 申请的耗材分类，值为"electronic"表示电子类耗材
     * processInstanceId: 流程实例ID - Flowable工作流引擎中该申请流程的唯一标识
     * taskId: 任务ID - 当前待办任务在Flowable中的唯一标识
     * taskName: 任务名称 - 当前待办任务的名称，"提交申请"表示这是提交申请阶段的任务
     * consumableApption
         * applicantDept:"技术研发部"
         * applicantId:19
         * applicantName:"super_admin"
         * applyTime:"2025-12-03T03:02:52"
         * assigneeId:27
         * assigneeName:"it_center"  审核人
         * consumableType:"electronic" 耗材类型 工作流使用
         * finalStatus:"pending"
         * id:5
         * itemId:4
         * itemName:"无线鼠标"
         * processInstanceId:"807e09b8-cfb1-11f0-baf1-005056c00008"
         * purpose:"是是是"
         * quantity:1       申请数量
     * @param userId
     * @return
     */
    @Override
    public List<Map<String, Object>> getTodoTasks(String userId) {
        // 1. 查询分配给指定用户的所有任务
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .processDefinitionKey("consumableApplicationProcess")  // 只查询耗材申请流程的任务
                .list();
//        System.out.println("=== 查询待办任务 ===");
//        System.out.println("查询用户ID: " + userId);
//        System.out.println("查询到的任务总数: " + tasks.size());
//        System.out.println("tasks = "+tasks);
        // 过滤掉申请人自己的任务
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks) {
            // 获取流程变量判断是否为审批任务
            Map<String, Object> variables = taskService.getVariables(task.getId());
//            System.out.println("任务详情:");
//            System.out.println("  任务ID: " + task.getId());
//            System.out.println("  任务名称: " + task.getName());
//            System.out.println("  流程实例ID: " + task.getProcessInstanceId());
//            System.out.println("  分配给: " + task.getAssignee());
//            System.out.println("  流程变量: " + variables);
            Object applicantIdObj = variables.get("applicantId");
            String applicantId = applicantIdObj == null ? "" : String.valueOf(applicantIdObj);
            // 如果当前用户不是申请人，则为审批任务
            if (!userId.equals(applicantId)) {
                filteredTasks.add(task);
            }
        }
        // 2. 创建用于存储结果的列表
        List<Map<String, Object>> result = new ArrayList<>();

        // 3. 遍历每个任务，构建任务信息
        for (Task task : filteredTasks) {
            // 3.1 创建任务信息Map
            Map<String, Object> taskInfo = new HashMap<>();

            // 3.2 添加任务基本信息
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("processInstanceId", task.getProcessInstanceId());

            // 3.3 获取业务Key（申请单ID）
            String businessKey = null;

            // 3.3.1 先尝试从活跃流程实例获取业务Key
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();

            if (processInstance != null) {
                // 如果找到了活跃流程实例，获取其业务Key
                businessKey = processInstance.getBusinessKey();
            } else {
                // 3.3.2 如果没有找到活跃流程实例，尝试从历史流程实例获取
                HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .singleResult();

                if (historicProcessInstance != null) {
                    // 如果找到了历史流程实例，获取其业务Key
                    businessKey = historicProcessInstance.getBusinessKey();
                }
            }
            if (businessKey != null) {
                Integer appId = Integer.parseInt(businessKey);
                taskInfo.put("applicationId", appId);

                // 查询申请单详情（带昵称）
                ConsumableApplication taskApplication = getApplicationDetailWithNickname(appId);
                taskInfo.put("consumableApption", taskApplication);
            }



            // 3.5 获取任务的流程变量
            Map<String, Object> variables = taskService.getVariables(task.getId());

            // 3.6 将所有流程变量添加到任务信息中
            taskInfo.putAll(variables);

            // 3.7 将构建好的任务信息添加到结果列表中
            result.add(taskInfo);
        }

        // 4. 返回结果列表
        return result;
    }

    public ConsumableApplication getApplicationDetailWithNickname(Integer id) {
        // 1. 先查询申请单
        ConsumableApplication application = applicationMapper.selectById(id);
        if (application == null) {
            return null;
        }

        // 2. 查询申请人昵称
        if (application.getApplicantId() != null) {
            User applicant = userMapper.selectById(application.getApplicantId());
            if (applicant != null) {
                application.setApplicantNickname(applicant.getNickname());
            }
        }

        // 3. 查询审核人昵称
        if (application.getAssigneeId() != null) {
            User assignee = userMapper.selectById(application.getAssigneeId());
            if (assignee != null) {
                application.setAssigneeNickname(assignee.getNickname());
            }
        }

        return application;
    }

    // 完成一级审批任务
    public void completeFirstApproval(String taskId, Integer applicationId, boolean firstApproved, String comment,Integer logisticsApprover) {
        // 打印输入参数
//        System.out.println("=== 一级审批参数 ===");
//        System.out.println("taskId: " + taskId);
//        System.out.println("applicationId: " + applicationId);
//        System.out.println("firstApproved: " + firstApproved);
//        System.out.println("comment: " + comment);
//        System.out.println("logisticsApprover: " + logisticsApprover);
//        // 1. 获取任务信息
//        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        // 2. 构造流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstApproved", firstApproved);//驳回直接结束
        variables.put("firstComment", comment);
        if(firstApproved){
            //审批通过才能走下一步，设置最终审批人
            variables.put("logisticsApprover", logisticsApprover);
        }
        // 3. 完成任务
        taskService.complete(taskId, variables);

        // 4. 更新申请单状态
        ConsumableApplication application = applicationMapper.selectById(applicationId);
        if (firstApproved) {
            application.setAssigneeId(logisticsApprover);
            application.setAssigneeName(userMapper.selectById(logisticsApprover).getUsername());
            application.setFinalStatus("pending_final_approval");//等待二级审批
        } else {
            application.setFinalStatus("first_rejected");//一级驳回直接结束
        }
        applicationMapper.updateById(application);
    }

    // 完成二级审批（后保部）
    public void completeFinalApproval(String taskId, Integer applicationId, boolean finalApproved, String comment, Integer actualDistributeQuantity) {
//        System.out.println("=== 二级审批参数 ===");
//        System.out.println("taskId: " + taskId);
//        System.out.println("applicationId: " + applicationId);
//        System.out.println("firstApproved: " + finalApproved);
//        System.out.println("comment: " + comment);
//        System.out.println("actualDistributeQuantity: " + actualDistributeQuantity);
        // 1. 完成任务
        Map<String, Object> variables = new HashMap<>();
        variables.put("finalApproved", finalApproved);
        variables.put("finalComment", comment);
        variables.put("distributeQuantity", actualDistributeQuantity);
        taskService.complete(taskId, variables);


        // ========= 2. 查询申请单（必须判空） =========
        ConsumableApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请单不存在，applicationId=" + applicationId);
        }

        // ========= 3. 审批未通过，直接结束 =========
        if (!finalApproved) {
            application.setFinalStatus("final_rejected");
            applicationMapper.updateById(application);
            return;
        }

        // ========= 4. 更新申请单状态 =========
        application.setActualDistributeQuantity(actualDistributeQuantity);
        application.setFinalStatus("issued");
        applicationMapper.updateById(application);

        // ========= 5. 查询耗材（必须判空） =========
        if (application.getItemId() == null) {
            throw new RuntimeException("申请单 itemId 为空，applicationId=" + applicationId);
        }

        ConsumableGoods goods = goodsService.getById(application.getItemId());
        if (goods == null) {
            throw new RuntimeException("耗材不存在，itemId=" + application.getItemId());
        }

        // ========= 6. 扣库存 =========
        int beforeQty = goods.getQuantity();
        int afterQty = beforeQty - actualDistributeQuantity;

        if (afterQty < 0) {
            throw new RuntimeException("库存不足，当前库存=" + beforeQty);
        }

        goods.setQuantity(afterQty);
        goodsService.updateById(goods);

        // ========= 7. 写出库记录 =========
        ConsumableInOutRecord record = new ConsumableInOutRecord();
        record.setGoodsId(goods.getId());
        record.setItemName(goods.getItemName());
        if (goods.getCategoryId() != null) {
            String categoryName = categoryMapper.selectById(goods.getCategoryId()) != null
                    ? categoryMapper.selectById(goods.getCategoryId()).getCategoryName()
                    : null;
            record.setCategoryId(goods.getCategoryId());
            record.setCategoryName(categoryName);
        }
        record.setOperationType("OUT");
        record.setQuantityBefore(beforeQty);
        record.setQuantityChange(-actualDistributeQuantity);
        record.setQuantityAfter(afterQty);
        // supplier 可能为空
        record.setSupplier(goods.getSupplier() != null ? goods.getSupplier() : "未知供应商");

        // 出库人 = 申领人
        record.setOperatorId(application.getApplicantId());
        record.setOperatorName(application.getApplicantName());
        record.setOperatorDept(application.getApplicantDept());
        record.setUnitPrice(goods.getUnitPrice() != null ? goods.getUnitPrice() : BigDecimal.ZERO);
        record.setTotalPrice(goods.getUnitPrice().multiply(new BigDecimal(actualDistributeQuantity)));

        record.setOperationTime(LocalDateTime.now());
        record.setRemark("申领出库");

        recordService.save(record);
    }


    // 查询申请单详情
    public ConsumableApplication getApplicationDetail(Integer id) {
        return applicationMapper.selectById(id);
    }

    /**
     * 获取审批历史（根据审批人ID）
     */
    @Override
    public List<ConsumableApplication> getApprovalHistory(Integer approverId) {
        return applicationMapper.selectApprovalHistory(approverId);
    }

}