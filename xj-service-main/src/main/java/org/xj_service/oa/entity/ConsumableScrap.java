package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("oa_consumable_scrap")
public class ConsumableScrap {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String scrapId;

    // 申请人信息
    private Integer applyUserId;
    private String applyUserName;
    @TableField(exist = false)
    private String applyUserNickname;
    private String applyDept;

    // 审核人（后保部）
    private Integer assigneeId;    // 对应数据库 assignee_id
    private String assigneeName;
    @TableField(exist = false)  // 这个字段不在数据库，而是关联查询的结果
    private String assigneeNickName;  // 新增：审核人昵称

    private LocalDateTime applyTime;
    private String reason; // 报废原因

    /**
     * 处理方式：
     * RECYCLE - 回收
     * DESTROY - 销毁
     */
    private String processMethod;

    /**
     * 状态枚举（补全所有状态）：
     * DRAFT       - 已撤回
     * REVIEW      - 后保部审核中
     * REVIEWED    - 后保部审核通过（待分管领导审批）
     * APPROVED    - 分管领导审批通过（已出库）
     * REJECTED    - 后保部审核驳回
     * FINAL_REJECTED - 分管领导审批驳回
     */
    private String status;

    // 后保部审核信息

    private LocalDateTime reviewTime;
    private String reviewRemark;

    // 分管领导审批人（后保部审核通过时指定
    private Integer nextApproverId;    // 下一级审批人ID
    private String nextApproverName;   // 下一级审批人姓名

    // 分管领导审批信息

    private LocalDateTime approveTime;
    private String approveRemark;


    @TableField("created_at")  // 添加这个注解
    private LocalDateTime createdAt;

    @TableField("updated_at")  // 同样处理 updatedAt
    private LocalDateTime updatedAt;

    // ========= 报废明细（非数据库字段）=========
    @TableField(exist = false)
    private List<ConsumableScrapItem> items;
}