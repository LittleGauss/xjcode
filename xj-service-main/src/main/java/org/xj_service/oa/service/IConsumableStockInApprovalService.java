package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.ConsumableStockInApproval;

import java.util.List;

public interface IConsumableStockInApprovalService extends IService<ConsumableStockInApproval> {

    // 提交入库申请
    boolean submitStockIn(ConsumableStockInApproval approval);

    // 审批入库申请
    boolean approveStockIn(Integer approvalId, String remark);

    // 驳回入库申请
    boolean rejectStockIn(Integer approvalId, String rejectReason);

    // 获取我的入库申请列表
    List<ConsumableStockInApproval> getMyStockInApplications();

    // 获取待我审批的入库申请列表
    List<ConsumableStockInApproval> getPendingStockInApprovals();

}