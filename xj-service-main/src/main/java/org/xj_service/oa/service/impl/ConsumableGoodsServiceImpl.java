package org.xj_service.oa.service.impl;


import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xj_service.oa.entity.ConsumableGoods;
import org.xj_service.oa.entity.ConsumableInOutRecord;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.entity.ConsumableStockInApproval;
import org.xj_service.oa.mapper.ConsumableCategoryMapper;
import org.xj_service.oa.mapper.ConsumableGoodsMapper;
import org.xj_service.oa.mapper.ConsumableNoticeMapper;
import org.xj_service.oa.service.ConsumableInOutRecordService;
import org.xj_service.oa.service.IConsumableStockInApprovalService;
import org.xj_service.oa.service.IUserService;
import org.xj_service.oa.service.IConsumableGoodsService;
import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsumableGoodsServiceImpl
        extends ServiceImpl<ConsumableGoodsMapper, ConsumableGoods>
        implements IConsumableGoodsService {
    @Autowired
    private ConsumableNoticeMapper noticeMapper;
    @Autowired
    private ConsumableInOutRecordService recordService;

    @Autowired
    private IUserService userService;  // 注入IUserService

    @Autowired
    private ConsumableCategoryMapper categoryMapper;

    @Autowired
    @Lazy
    private IConsumableStockInApprovalService stockInApprovalService;

    @Override
    @Transactional
    public boolean saveOrUpdateWithRecord(ConsumableGoods goods) {
        // ========== 1. 获取当前登录用户ID ==========
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取当前请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new RuntimeException("请求未携带Token");
        }

        String token = authHeader.replaceFirst("^Bearer\\s+", "");

        Integer operatorId;
        try {
            String userIdStr = JWT.decode(token).getAudience().get(0);
            operatorId = Integer.parseInt(userIdStr);
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败，无法获取用户ID");
        }

        // ========== 2. 查询用户详细信息 ==========
        String operatorName = "未知用户";
        String operatorDept = "未分配部门";
        String nickName = "未知用户";

        try {
            // 使用 getUserWithDepartment 获取完整用户和部门信息
            User operatorUser = userService.getUserWithDepartment(operatorId);
            if (operatorUser != null) {
                if (operatorUser.getUsername() != null) {
                    operatorName = operatorUser.getUsername();
                }
                if (operatorUser.getNickname() != null){
                    nickName = operatorUser.getNickname();
                }
                // 假设 operatorUser.getDepartmentName() 能拿到部门名
                if (operatorUser.getDepartmentName() != null) {
                    operatorDept = operatorUser.getDepartmentName();
                } else if (operatorUser.getDepartmentId() != null) {
                    operatorDept = "部门ID:" + operatorUser.getDepartmentId();
                }
            }
        } catch (Exception e) {
            System.err.println("警告：查询操作员信息失败，operatorId=" + operatorId + ", 错误：" + e.getMessage());
        }

        // ========== 2. 判断是否需要走审批流程 ==========
        // 条件1：needApproval为true（IT中心用户在前端会设置这个值）
        // 条件2：logisticsApproverId不为空（有指定的后保部审批人）
        if (goods.getNeedApproval() != null && goods.getNeedApproval()
                && goods.getLogisticsApproverId() != null) {

            // ========== 需要审批：提交入库申请，不直接入库 ==========
            System.out.println("检测到需要审批的入库申请，转向审批流程...");

            // 获取审批人信息
            User approverUser = userService.getById(goods.getLogisticsApproverId());
            if (approverUser == null) {
                throw new RuntimeException("指定的审批人不存在，ID: " + goods.getLogisticsApproverId());
            }

            // 验证物品名称不为空
            if (goods.getItemName() == null || goods.getItemName().trim().isEmpty()) {
                throw new RuntimeException("物品名称不能为空");
            }

            // 验证数量
            if (goods.getQuantity() == null || goods.getQuantity() <= 0) {
                throw new RuntimeException("物品数量必须大于0");
            }

            // 创建入库审批申请
            ConsumableStockInApproval approval = new ConsumableStockInApproval();

            // 设置审批信息
            approval.setApproverId(goods.getLogisticsApproverId());
            approval.setApproverName(approverUser.getUsername());
            approval.setRemark(goods.getRemark());

            // 设置申请人信息
            approval.setApplicantId(operatorId);
            approval.setApplicantName(operatorName);
            approval.setApplicantDept(operatorDept);

            // 设置物品信息
            approval.setItemName(goods.getItemName());
            approval.setCategoryId(goods.getCategoryId());
            approval.setSpec(goods.getSpec());
            approval.setQuantity(goods.getQuantity());
            approval.setUnit(goods.getUnit() != null ? goods.getUnit() : "个");
            approval.setBrand(goods.getBrand());
            approval.setSupplier(goods.getSupplier());
            approval.setUnitPrice(goods.getUnitPrice());
            approval.setPurchaseDate(goods.getPurchaseDate());
            approval.setExpireDate(goods.getExpireDate());
            approval.setWarningValue(goods.getWarningValue());

            // 如果分类ID不为空，获取分类名称
            if (goods.getCategoryId() != null) {
                String categoryName = categoryMapper.selectById(goods.getCategoryId()) != null
                        ? categoryMapper.selectById(goods.getCategoryId()).getCategoryName()
                        : null;
                approval.setCategoryName(categoryName);
            }

            // 提交审批申请
            try {
                boolean approvalSuccess = stockInApprovalService.submitStockIn(approval);
                if (approvalSuccess) {
                    System.out.println("入库申请已提交，申请ID：" + approval.getId() +
                            "，审批人：" + approverUser.getUsername());
                    return true; // 申请已提交成功，返回true
                } else {
                    throw new RuntimeException("提交入库申请失败");
                }
            } catch (Exception e) {
                throw new RuntimeException("提交入库申请时发生错误: " + e.getMessage());
            }

        } else {
            // ========== 不需要审批：走原有逻辑直接入库 ==========
            System.out.println("直接入库，无需审批");

            // 原有业务逻辑：保存物品并记录出入库
            int beforeQty = 0;
            if (goods.getId() != null) {
                ConsumableGoods old = getById(goods.getId());
                if (old != null) {
                    beforeQty = old.getQuantity();
                }
            }

            // 新增时自动填充创建时间
            if (goods.getId() == null) {
                goods.setCreatedAt(LocalDateTime.now());
            }

            // 状态默认值（如果未传递，设为"1"代表在用）
            if (goods.getStatus() == null || goods.getStatus().trim().isEmpty()) {
                goods.setStatus("1");
            }

            boolean success = this.saveOrUpdate(goods);
            if (!success) return false;

        int afterQty = goods.getQuantity();
        int change = afterQty - beforeQty;

        if (change != 0) {
            // ========== 4. 设置完整的出入库记录 ==========
            ConsumableInOutRecord record = new ConsumableInOutRecord();
            record.setGoodsId(goods.getId());
            record.setItemName(goods.getItemName());
            // 分类信息，从category表查
            if (goods.getCategoryId() != null) {
                String categoryName = categoryMapper.selectById(goods.getCategoryId()) != null
                        ? categoryMapper.selectById(goods.getCategoryId()).getCategoryName()
                        : null;
                record.setCategoryId(goods.getCategoryId());
                record.setCategoryName(categoryName);
            }

                record.setOperationType(change > 0 ? "IN" : "OUT");
                record.setQuantityBefore(beforeQty);
                record.setQuantityChange(change);
                record.setQuantityAfter(afterQty);

                String supplier = goods.getSupplier() != null ? goods.getSupplier() : "未知供应商";
                record.setSupplier(supplier);

                BigDecimal unitPrice = goods.getUnitPrice() != null ? goods.getUnitPrice() : BigDecimal.ZERO;
                record.setUnitPrice(unitPrice);

                // 总价计算
                BigDecimal totalPrice = goods.getUnitPrice() != null
                        ? goods.getUnitPrice().multiply(new BigDecimal(Math.abs(change)))
                        : BigDecimal.ZERO;
                record.setTotalPrice(totalPrice);

                record.setRemark(change > 0 ? "新增入库" : "库存出库");

            // 设置操作员信息
            record.setOperatorId(operatorId);           // 用户ID
            record.setOperatorName(operatorName);       // 用户名
            record.setOperatorDept(operatorDept);       // 部门信息

            recordService.save(record);
            // ========== 5. 如果是入库操作，添加入库公示信息 ==========
            if (change > 0) {
                try {
                    // 创建入库公示
                    org.xj_service.oa.entity.ConsumableNotice notice = new org.xj_service.oa.entity.ConsumableNotice();
                    notice.setNoticeType("IN"); // 入库公示
                    notice.setTitle("耗材入库公示");

                    // 构建公示内容JSON
                    java.util.Map<String, Object> content = new java.util.HashMap<>();
                    content.put("物品名称", goods.getItemName());
                    content.put("分类", record.getCategoryName());
                    content.put("入库数量", Math.abs(change));
                    content.put("单位", goods.getUnit() != null ? goods.getUnit() : "个");
                    content.put("供货商", supplier);
                    content.put("单价", unitPrice);
                    content.put("总价", totalPrice);
                    content.put("操作用户", nickName);
                    content.put("入库时间", LocalDateTime.now());

                    notice.setContent(com.alibaba.fastjson.JSON.toJSONString(content));
                    notice.setNoticeTime(LocalDateTime.now());
                    notice.setDeptName(operatorDept);
                    // 设置过期时间为30天后
                    notice.setExpireTime(LocalDateTime.now().plusDays(30));
                    notice.setCreatorId(operatorId);
                    notice.setCreatorName(nickName);
                    notice.setStatus("UNAPPROVED"); // 入库记录也得后保部确定是否公示

                    // 插入公示信息
                    noticeMapper.insert(notice);

                } catch (Exception e) {
                    // 公示创建失败不应影响主要业务流程，记录错误日志即可
                    System.err.println("创建入库公示失败: " + e.getMessage());
                }
            }
        }

            return true;
        }
    }

    public boolean saveOrUpdateWithRecordBatch(List<ConsumableGoods> goodsList) {
        boolean allSuccess = true;
        for (ConsumableGoods goods : goodsList) {
            boolean success = saveOrUpdateWithRecord(goods); // 调用已有逻辑
            if (!success) allSuccess = false;
        }
        return allSuccess;
    }

}
