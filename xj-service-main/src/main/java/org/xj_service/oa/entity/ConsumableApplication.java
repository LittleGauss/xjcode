package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_consumable_application")
public class ConsumableApplication {

    /**
     * 申请ID（自增主键）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关联耗材ID（外键，关联库存表）
     */
    private Integer itemId;

    /**
     * 耗材名称（冗余存储）
     */
    private String itemName;
    /**
     * 供货商
     */
    private String supplier;

    /**
     * 申请人ID（关联sys_user.id）
     */
    private Integer applicantId;

    /**
     * 申请人姓名（冗余存储）
     */
    private String applicantName;
    /**
     * 申请人昵称（关联查询，非数据库字段）
     */
    @TableField(exist = false)
    private String applicantNickname;


    /**
     * 申请人部门
     */
    private String applicantDept;

    /**
     * 耗材类型：electronic/experimental/general
     */
    private String consumableType;

    /**
     * 申请数量
     */
    private Integer quantity;

    /**
     * 申请用途
     */
    private String purpose;

    /**
     * 申请提交时间
     */
    private LocalDateTime applyTime;

    /**
     * 流程实例ID（关联工作流引擎）
     */
    private String processInstanceId;

    /**
     * 当前状态： * pending - 待提交
     *  * pending_first_approval - 等待一级审批
     *  * pending_final_approval - 等待二级审批
     *  * first_approved - 一级审批通过
     *  * first_rejected - 一级审批驳回
     *  * final_rejected - 二级审批驳回
     *  * issued - 已发放
     */
    private String finalStatus;

    /**
     * 审核人人ID（关联sys_user.id）
     */
    private Integer assigneeId;

    /**
     * 审核人姓名（冗余存储）
     */
    private String assigneeName;

    /**
     * 审核人昵称（关联查询，非数据库字段）
     */
    @TableField(exist = false)
    private String assigneeNickname;

    /**
     *  领用数量
     */
    private Integer actualDistributeQuantity;
}