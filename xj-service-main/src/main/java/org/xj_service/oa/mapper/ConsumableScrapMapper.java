package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.xj_service.oa.entity.ConsumableScrap;

import java.util.List;

public interface ConsumableScrapMapper extends BaseMapper<ConsumableScrap> {
    /**
     * 获取我的报废申请列表（修正为Integer，和Entity一致）
     */
    List<ConsumableScrap> getMyScrapList(@Param("applyUserId") Integer applyUserId);

    /**
     * 获取待我审批的报废申请列表（修正为Integer）
     */
    List<ConsumableScrap> getToApproveScrapList(@Param("assigneeId") Integer assigneeId, @Param("status") String status);
}