package org.xj_service.oa.controller;

import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.Constants;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.ConsumableStockInApproval;
import org.xj_service.oa.service.IConsumableStockInApprovalService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/consumable/stock-in-approval")
public class ConsumableStockInApprovalController {

    @Resource
    private IConsumableStockInApprovalService stockInApprovalService;

    // 提交入库申请
    @PostMapping("/submit")
    public Result submitStockIn(@RequestBody ConsumableStockInApproval approval) {
        try {
            boolean success = stockInApprovalService.submitStockIn(approval);
            if (success) {
                return Result.success("入库申请已提交，等待审批");
            } else {
                return Result.error(Constants.CODE_500, "提交失败");
            }
        } catch (Exception e) {
            return Result.error(Constants.CODE_500, e.getMessage());
        }
    }

    // 审批入库申请
    @PostMapping("/approve")
    public Result approveStockIn(@RequestParam Integer approvalId,
                                 @RequestParam String remark) {
        try {
            boolean success = stockInApprovalService.approveStockIn(approvalId, remark);
            if (success) {
                return Result.success("审批通过，物品已入库");
            } else {
                return Result.error(Constants.CODE_500, "审批失败");
            }
        } catch (Exception e) {
            return Result.error(Constants.CODE_500, e.getMessage());
        }
    }

    // 驳回入库申请
    @PostMapping("/reject")
    public Result rejectStockIn(@RequestParam Integer approvalId,
                                @RequestParam String rejectReason) {
        try {
            boolean success = stockInApprovalService.rejectStockIn(approvalId, rejectReason);
            if (success) {
                return Result.success("已驳回申请");
            } else {
                return Result.error(Constants.CODE_500, "驳回失败");
            }
        } catch (Exception e) {
            return Result.error(Constants.CODE_500, e.getMessage());
        }
    }

    // 获取我的入库申请列表
    @GetMapping("/my-list")
    public Result getMyStockInApplications() {
        try {
            List<ConsumableStockInApproval> list = stockInApprovalService.getMyStockInApplications();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(Constants.CODE_500, e.getMessage());
        }
    }

    // 获取待我审批的入库申请列表
    @GetMapping("/pending-list")
    public Result getPendingStockInApprovals() {
        try {
            List<ConsumableStockInApproval> list = stockInApprovalService.getPendingStockInApprovals();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(Constants.CODE_500, e.getMessage());
        }
    }
}