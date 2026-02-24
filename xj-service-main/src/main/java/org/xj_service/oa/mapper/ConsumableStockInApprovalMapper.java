package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xj_service.oa.entity.ConsumableStockInApproval;

import java.util.List;

@Mapper
public interface ConsumableStockInApprovalMapper extends BaseMapper<ConsumableStockInApproval> {
    List<ConsumableStockInApproval> selectPendingApprovalsWithNickname(@Param("approverId") Integer approverId);
}