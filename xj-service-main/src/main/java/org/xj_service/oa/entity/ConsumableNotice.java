package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oa_consumable_notice")
public class ConsumableNotice {
    /**
     * 公示ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 公示类型：IN-入库公示，SCRAP-报废公示，STAT-领用统计公示
     */
    private String noticeType;

    /**
     * 公示标题
     */
    private String title;

    /**
     * 公示内容（JSON格式存储，如入库单号、耗材名称、数量等）
     */
    private String content;

    /**
     * 所属部门（统计公示用）
     */
    private String deptName;

    /**
     * 公示时间
     */
    private LocalDateTime noticeTime;

    /**
     * 过期时间（公示30天后过期）
     */
    private LocalDateTime expireTime;

    /**
     * 创建人ID
     */
    private Integer creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 公示状态：UNAPPROVED-待审批，APPROVED-已批准，EXPIRED-已过期
     */
    private String status;
}
