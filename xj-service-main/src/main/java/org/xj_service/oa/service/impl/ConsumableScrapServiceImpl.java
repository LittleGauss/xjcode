package org.xj_service.oa.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xj_service.oa.dto.ScrapApproveRequest;
import org.xj_service.oa.dto.ScrapReviewRequest;
import org.xj_service.oa.entity.*;
import org.xj_service.oa.mapper.*;
import org.xj_service.oa.service.IConsumableScrapService;
import org.xj_service.oa.service.IUserService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsumableScrapServiceImpl
        extends ServiceImpl<ConsumableScrapMapper, ConsumableScrap>
        implements IConsumableScrapService {

    @Autowired
    private ConsumableScrapItemMapper scrapItemMapper;
    @Autowired
    private ConsumableGoodsMapper consumableGoodsMapper;
    @Autowired
    private ConsumableInOutRecordMapper inOutRecordMapper;
    @Autowired
    private IUserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsumableNoticeMapper noticeMapper;
    @Autowired
    private ConsumableCategoryMapper categoryMapper;

    /* ================= 公共方法：获取当前登录用户 ================= */
    private User getCurrentUser() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) throw new RuntimeException("无法获取请求上下文");

        HttpServletRequest request = attr.getRequest();
        String auth = request.getHeader("Authorization");
        if (StringUtils.isBlank(auth)) throw new RuntimeException("未携带Token");

        String token = auth.replaceFirst("^Bearer\\s+", "");
        Integer userId = Integer.parseInt(com.auth0.jwt.JWT.decode(token).getAudience().get(0));
        User user = userService.getUserWithDepartment(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        return user;
    }

    /* ================= 提交报废申请 ================= */
    @Override
    @Transactional
    public void submitScrap(ConsumableScrap scrap) {
        scrap.setScrapId("SCRAP-" + System.currentTimeMillis());
        scrap.setStatus(StringUtils.isBlank(scrap.getStatus()) ? "REVIEW" : scrap.getStatus());
        scrap.setApplyTime(LocalDateTime.now());
        scrap.setCreatedAt(LocalDateTime.now());
        scrap.setUpdatedAt(LocalDateTime.now());
        this.save(scrap);

        Integer scrapId = scrap.getId();
        if (scrapId == null) throw new RuntimeException("保存报废单失败");

        for (ConsumableScrapItem item : scrap.getItems()) {
            item.setScrapId(scrapId);
            item.setCreatedAt(LocalDateTime.now());
            if (item.getUnitPrice() != null && item.getQuantity() != null) {
                item.setTotalPrice(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
            }
            if (item.getGoodsId() != null) {
                ConsumableGoods goods = consumableGoodsMapper.selectById(item.getGoodsId());
                if (goods != null) {
                    item.setGoodsName(goods.getItemName());

                    // 关键：这里设置分类信息到 item 表中
                    if (goods.getCategoryId() != null) {
                        item.setCategoryId(goods.getCategoryId());

                        // 从分类表获取分类名称
                        ConsumableCategory category = categoryMapper.selectById(goods.getCategoryId());
                        if (category != null) {
                            item.setCategoryName(category.getCategoryName());
                        }
                    }
                }
            }
            scrapItemMapper.insert(item);
        }
    }

    /* ================= 获取我的报废列表 ================= */
    @Override
    public List<ConsumableScrap> getMyScrapList() {
        User currentUser = getCurrentUser();
        List<ConsumableScrap> scraps = baseMapper.getMyScrapList(currentUser.getId());

        // 为每个报废单加载物品明细
        for (ConsumableScrap scrap : scraps) {
            List<ConsumableScrapItem> items = scrapItemMapper.selectByScrapId(scrap.getId());
            scrap.setItems(items);
            for (ConsumableScrapItem item : items) {
                System.out.println("商品ID: " + item.getGoodsId());
                System.out.println("分类ID: " + item.getCategoryId());
                System.out.println("分类名称: " + item.getCategoryName());
            }

            scrap.setItems(items);
        }

        return scraps;
    }

    /* ================= 获取待我审批的报废列表 ================= */
    @Override
    public List<ConsumableScrap> getToApproveScrapList(String status) {
        User currentUser = getCurrentUser();
        List<ConsumableScrap> scraps = baseMapper.getToApproveScrapList(currentUser.getId(), status);

        // 为每个报废单加载物品明细
        if (scraps != null) {
            for (ConsumableScrap scrap : scraps) {
                List<ConsumableScrapItem> items = scrapItemMapper.selectByScrapId(scrap.getId());
                scrap.setItems(items);
            }
        }

        return scraps;
    }

    /* ================= 撤回报废申请 ================= */
    @Override
    @Transactional
    public boolean withdrawScrap(Integer scrapId) {
        ConsumableScrap scrap = getById(scrapId);
        if (scrap == null) throw new RuntimeException("报废单不存在");

        User currentUser = getCurrentUser();

        // 验证权限：只能撤回自己的申请
        if (!scrap.getApplyUserId().equals(currentUser.getId()))
            throw new RuntimeException("只能撤回自己的申请");

        // 验证状态：只有REVIEW状态才能撤回
        if (!"REVIEW".equals(scrap.getStatus()))
            throw new RuntimeException("只有待审核状态才能撤回");

        // 修改状态为DRAFT（或者直接删除，根据业务需求）
        scrap.setStatus("DRAFT");
        scrap.setUpdatedAt(LocalDateTime.now());
        return updateById(scrap);
    }

    /* ================= 后保部审核 ================= */
    @Override
    @Transactional
    public boolean reviewScrap(ScrapReviewRequest request) {
        System.out.println("=== 开始审核报废单 ===");
        System.out.println("请求参数详细: scrapId=" + request.getScrapId() +
                ", status=" + request.getStatus() +
                ", remark=" + request.getRemark() +
                ", nextApproverId=" + request.getNextApproverId() +
                ", nextApproverName=" + request.getNextApproverName());

        ConsumableScrap scrap = getById(request.getScrapId());
        if (scrap == null) throw new RuntimeException("报废单不存在");

        User currentUser = getCurrentUser();
        System.out.println("当前用户名: " + currentUser.getUsername() + ", ID: " + currentUser.getId());
        System.out.println("报废单状态: " + scrap.getStatus());
        System.out.println("报废单assigneeId: " + scrap.getAssigneeId());
        System.out.println("当前用户是否匹配assigneeId: " + currentUser.getId().equals(scrap.getAssigneeId()));

        // 验证：当前用户是否是assigneeId指定的后保部审核人
        if (!currentUser.getId().equals(scrap.getAssigneeId())) {
            throw new RuntimeException("您不是指定的后保部审核人，无权审核");
        }

        // 验证状态：必须是REVIEW状态
        if (!"REVIEW".equals(scrap.getStatus())) {
            throw new RuntimeException("当前状态不允许审核");
        }

        // 验证是否已审核过
        if (scrap.getReviewTime() != null) {
            throw new RuntimeException("已审核，禁止重复操作");
        }

        if ("REVIEWED".equals(request.getStatus())) {
            System.out.println("审核状态为通过，检查nextApproverId: " + request.getNextApproverId());

            // 验证并设置下一级审批人（分管领导）
            if (request.getNextApproverId() == null) {
                System.out.println("错误：nextApproverId为null！");
                throw new RuntimeException("审核通过必须指定下一级审批人");
            }

            System.out.println("nextApproverId不为null，值为: " + request.getNextApproverId());

            User nextApprover = userService.getById(request.getNextApproverId());
            if (nextApprover == null) {
                System.out.println("错误：找不到ID为 " + request.getNextApproverId() + " 的用户");
                throw new RuntimeException("指定的审批人不存在");
            }

            System.out.println("找到审批人: " + nextApprover.getUsername());

            scrap.setStatus("REVIEWED");
            scrap.setReviewTime(LocalDateTime.now());
            scrap.setReviewRemark(request.getRemark());

            scrap.setNextApproverId(request.getNextApproverId());
            scrap.setNextApproverName(request.getNextApproverName() != null ?
                    request.getNextApproverName() : nextApprover.getUsername());

        } else if ("REJECTED".equals(request.getStatus())) {
            scrap.setStatus("REJECTED");
            scrap.setReviewTime(LocalDateTime.now());
            scrap.setReviewRemark(request.getRemark());
        } else {
            throw new RuntimeException("非法审核状态");
        }

        scrap.setUpdatedAt(LocalDateTime.now());
        return updateById(scrap);
    }

    /* ================= 分管领导审批 ================= 报废审批，通过后就真的报废了*/
    @Override
    @Transactional
    public boolean approveScrap(ScrapApproveRequest request) {
        ConsumableScrap scrap = getById(request.getScrapId());
        if (scrap == null) throw new RuntimeException("报废单不存在");

        User currentUser = getCurrentUser();

        // 验证：当前用户是否是nextApproverId指定的分管领导
        if (!currentUser.getId().equals(scrap.getNextApproverId())) {
            throw new RuntimeException("您不是指定的分管领导，无权审批");
        }

        // 验证状态：必须是REVIEWED状态
        if (!"REVIEWED".equals(scrap.getStatus())) {
            throw new RuntimeException("当前状态不允许审批");
        }

        // 验证是否已审批过
        if (scrap.getApproveTime() != null) {
            throw new RuntimeException("已审批，禁止重复操作");
        }

        if ("APPROVED".equals(request.getStatus())) {
            scrap.setStatus("APPROVED");
            scrap.setApproveTime(LocalDateTime.now());
            scrap.setApproveRemark(request.getRemark());

            // 扣库存和生成出入库记录
            List<ConsumableScrapItem> items = scrapItemMapper.selectByScrapId(scrap.getId());
            for (ConsumableScrapItem item : items) {
                ConsumableGoods goods = consumableGoodsMapper.selectById(item.getGoodsId());
                int before = goods.getQuantity();
                goods.setQuantity(before - item.getQuantity());
                consumableGoodsMapper.updateById(goods);



                ConsumableInOutRecord record = new ConsumableInOutRecord();
                record.setGoodsId(goods.getId());
                record.setItemName(goods.getItemName());
                // 1. 补充分类信息（参考goods服务的逻辑）
                if (goods.getCategoryId() != null) {
                    // 注意：需要注入ConsumableCategoryMapper
                    String categoryName = categoryMapper.selectById(goods.getCategoryId()) != null
                            ? categoryMapper.selectById(goods.getCategoryId()).getCategoryName()
                            : null;
                    record.setCategoryId(goods.getCategoryId());
                    record.setCategoryName(categoryName);
                }
                record.setOperationType("SCRAP");
                record.setQuantityBefore(before);
                record.setQuantityChange(-item.getQuantity());
                record.setQuantityAfter(goods.getQuantity());
                // 2. 补充供应商信息
                String supplier = goods.getSupplier() != null ? goods.getSupplier() : "未知供应商";
                record.setSupplier(supplier);

                // 3. 补充单价和总价
                BigDecimal unitPrice = goods.getUnitPrice() != null ? goods.getUnitPrice() : BigDecimal.ZERO;
                record.setUnitPrice(unitPrice);
                // 总价 = 单价 × 报废数量（绝对值）
                BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(item.getQuantity()));
                record.setTotalPrice(totalPrice);

                record.setOperatorId(currentUser.getId());
                record.setOperatorName(currentUser.getUsername());
                record.setOperatorDept(scrap.getApplyDept());
                record.setOperationTime(LocalDateTime.now());
                record.setRemark("报废出库：" + scrap.getScrapId());
                inOutRecordMapper.insert(record);
            }
            try {
                // 审批通过直接添加公示信息
                ConsumableNotice notice = new ConsumableNotice();
                notice.setNoticeType("SCRAP"); // 报废公示
                notice.setTitle("耗材报废公示");

                // 构建公示内容JSON
                Map<String, Object> content = new HashMap<>();
                content.put("记录ID", scrap.getId());
                content.put("报废ID", scrap.getScrapId());
                content.put("所属部门", scrap.getApplyDept());
                content.put("提交时间", scrap.getApproveTime());
                content.put("报废条目", items.stream()
                        .map(item -> {
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("物品名", item.getGoodsName());
                            itemMap.put("类型", item.getCategoryName());
                            itemMap.put("数量", item.getQuantity());
                            itemMap.put("总价", item.getTotalPrice());
                            itemMap.put("报废原因", scrap.getReason());
                            return itemMap;
                        })
                        .collect(Collectors.toList()));

                notice.setContent(JSON.toJSONString(content));
                notice.setDeptName(scrap.getApplyDept());
                notice.setNoticeTime(LocalDateTime.now());
                // 设置过期时间为30天后
                notice.setExpireTime(LocalDateTime.now().plusDays(30));
                notice.setCreatorId(currentUser.getId());
                notice.setCreatorName(currentUser.getNickname());
                notice.setStatus("UNAPPROVED"); // 报废审批通过后直接设为待公示状态，需要后保部点一下公示即可

                // 插入公示信息
                noticeMapper.insert(notice);
            } catch (Exception e) {
                // 记录详细的异常信息
                System.err.println("创建报废公示信息时发生异常:");
                e.printStackTrace();
                throw e; // 重新抛出异常，确保事务能够回滚
            }


        } else if ("REJECTED".equals(request.getStatus())) {
            scrap.setStatus("FINAL_REJECTED");
            scrap.setApproveTime(LocalDateTime.now());
            scrap.setApproveRemark(request.getRemark());
        } else {
            throw new RuntimeException("非法审批状态");
        }

        scrap.setUpdatedAt(LocalDateTime.now());
        return updateById(scrap);
    }

    /* ================= 获取报废详情 ================= */
    @Override
    public ConsumableScrap getScrapDetail(Integer scrapId) {
        ConsumableScrap scrap = getById(scrapId);
        if (scrap != null) {
            scrap.setItems(scrapItemMapper.selectByScrapId(scrapId));
        }
        return scrap;
    }
    /* ================= 查询分管领导列表 ================= */
    @Override
    public List<User> listViceLeaderUsers() {
        // 直接调用UserMapper中定义的无参方法：查询所有ROLE_VICE_LEADER角色的分管领导
        return userMapper.getViceLeaderUsers();
    }
}
