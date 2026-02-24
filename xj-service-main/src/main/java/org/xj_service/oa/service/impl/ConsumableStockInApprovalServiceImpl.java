package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xj_service.oa.entity.ConsumableGoods;
import org.xj_service.oa.entity.ConsumableInOutRecord;
import org.xj_service.oa.entity.ConsumableStockInApproval;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.mapper.ConsumableCategoryMapper;
import org.xj_service.oa.mapper.ConsumableStockInApprovalMapper;
import org.xj_service.oa.service.*;
import com.auth0.jwt.JWT;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsumableStockInApprovalServiceImpl
        extends ServiceImpl<ConsumableStockInApprovalMapper, ConsumableStockInApproval>
        implements IConsumableStockInApprovalService {

    @Autowired
    @Lazy
    private IConsumableGoodsService consumableGoodsService;

    @Autowired
    private ConsumableInOutRecordService recordService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ConsumableCategoryMapper categoryMapper;

    // 获取当前登录用户
    private User getCurrentUser() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            throw new RuntimeException("无法获取请求上下文");
        }

        HttpServletRequest request = attr.getRequest();
        String auth = request.getHeader("Authorization");
        if (StringUtils.isBlank(auth)) {
            throw new RuntimeException("未携带Token");
        }

        String token = auth.replaceFirst("^Bearer\\s+", "");
        Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
        User user = userService.getUserWithDepartment(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    @Transactional
    public boolean submitStockIn(ConsumableStockInApproval approval) {
        User currentUser = getCurrentUser();

        // 生成入库单号
        approval.setApplyNo("IN-" + System.currentTimeMillis());
        approval.setApplicantId(currentUser.getId());
        approval.setApplicantName(currentUser.getUsername());
        approval.setApplicantDept(currentUser.getDepartmentName());
        approval.setStatus("PENDING");
        approval.setApplyTime(LocalDateTime.now());
        approval.setCreatedAt(LocalDateTime.now());
        approval.setUpdatedAt(LocalDateTime.now());

        // 如果分类ID不为空，获取分类名称
        if (approval.getCategoryId() != null) {
            String categoryName = categoryMapper.selectById(approval.getCategoryId()) != null
                    ? categoryMapper.selectById(approval.getCategoryId()).getCategoryName()
                    : null;
            approval.setCategoryName(categoryName);
        }

        return this.save(approval);
    }

    @Override
    @Transactional
    public boolean approveStockIn(Integer approvalId, String remark) {
        ConsumableStockInApproval approval = this.getById(approvalId);
        if (approval == null) {
            throw new RuntimeException("入库申请不存在");
        }

        User currentUser = getCurrentUser();

        // 验证审批人权限
        if (!currentUser.getId().equals(approval.getApproverId())) {
            throw new RuntimeException("您不是指定的审批人，无权审批");
        }

        // 验证状态
        if (!"PENDING".equals(approval.getStatus())) {
            throw new RuntimeException("当前状态不允许审批");
        }

        // 更新审批状态
        approval.setStatus("APPROVED");
        approval.setApproveRemark(remark);
        approval.setApproveTime(LocalDateTime.now());
        approval.setUpdatedAt(LocalDateTime.now());

        // 执行入库操作
        executeStockIn(approval);

        return this.updateById(approval);
    }

    @Override
    @Transactional
    public boolean rejectStockIn(Integer approvalId, String rejectReason) {
        ConsumableStockInApproval approval = this.getById(approvalId);
        if (approval == null) {
            throw new RuntimeException("入库申请不存在");
        }

        User currentUser = getCurrentUser();

        // 验证审批人权限
        if (!currentUser.getId().equals(approval.getApproverId())) {
            throw new RuntimeException("您不是指定的审批人，无权审批");
        }

        // 验证状态
        if (!"PENDING".equals(approval.getStatus())) {
            throw new RuntimeException("当前状态不允许审批");
        }

        // 更新审批状态
        approval.setStatus("REJECTED");
        approval.setApproveRemark(rejectReason);
        approval.setApproveTime(LocalDateTime.now());
        approval.setUpdatedAt(LocalDateTime.now());

        return this.updateById(approval);
    }

    // 执行入库操作
    @Transactional
    void executeStockIn(ConsumableStockInApproval approval) {
        // 创建或更新库存物品
        ConsumableGoods goods = new ConsumableGoods();
        goods.setItemName(approval.getItemName());
        goods.setCategoryId(approval.getCategoryId());
        goods.setSpec(approval.getSpec());
        goods.setQuantity(approval.getQuantity());
        goods.setUnit(approval.getUnit());
        goods.setBrand(approval.getBrand());
        goods.setSupplier(approval.getSupplier());
        goods.setUnitPrice(approval.getUnitPrice());
        goods.setWarningValue(approval.getWarningValue());
        goods.setPurchaseDate(approval.getPurchaseDate());
        goods.setExpireDate(approval.getExpireDate());
        goods.setStatus("1"); // 在库状态

        // 使用现有的saveOrUpdateWithRecord方法
        consumableGoodsService.saveOrUpdateWithRecord(goods);
    }

    @Override
    public List<ConsumableStockInApproval> getMyStockInApplications() {
        User currentUser = getCurrentUser();
        return this.lambdaQuery()
                .eq(ConsumableStockInApproval::getApplicantId, currentUser.getId())
                .orderByDesc(ConsumableStockInApproval::getApplyTime)
                .list();
    }

    @Override
    public List<ConsumableStockInApproval> getPendingStockInApprovals() {
        User currentUser = getCurrentUser();
        return this.baseMapper.selectPendingApprovalsWithNickname(currentUser.getId());
    }
}