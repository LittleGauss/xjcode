package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xj_service.oa.entity.ApprovalComment;
import java.util.List;
@Mapper
public interface ApprovalCommentMapper extends BaseMapper<ApprovalComment> {

    // 根据业务ID和业务类型查询审批意见
    default List<ApprovalComment> selectByBusiness(Integer businessId, String businessType) {
        return selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ApprovalComment>()
                        .eq("business_id", businessId)
                        .eq("business_type", businessType)
                        .orderByAsc("approved_time")
        );
    }

    // 根据流程实例ID查询审批意见
    default List<ApprovalComment> selectByProcessInstanceId(String processInstanceId) {
        return selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ApprovalComment>()
                        .eq("process_instance_id", processInstanceId)
                        .orderByAsc("approved_time")
        );
    }
}