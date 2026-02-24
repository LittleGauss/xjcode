package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService; // 1. 必须引入这个
import org.xj_service.oa.entity.ContractReview;
import org.flowable.task.api.Task;
import java.util.List;

/**
 * 合同工作流服务接口
 */
public interface IContractProcessService extends IService<ContractReview> { // 2. 必须继承 IService

    /**
     * 启动流程
     * 对应实现类中的: startContractReview
     * 增加 adminUser 参数以支持流程中指定的行政办终审人
     */
    String startContractReview(ContractReview review, String starter, String gmUser, String legalUser, String adminUser);

    /**
     * 获取用户待办任务
     */
    List<Task> getTasksForUser(String userId);

    /**
     * 审批任务
     * 对应实现类中的: completeTask
     * 第三个参数为 nextApprover（可用于动态指定下一节点的审批人），
     * 如果不需要可传 null。
     */
    void completeTask(String taskId, boolean approved, String nextApprover);
}