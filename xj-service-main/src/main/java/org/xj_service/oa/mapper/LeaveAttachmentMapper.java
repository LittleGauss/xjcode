package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xj_service.oa.entity.LeaveAttachment;

import java.util.List;

/**
 * 附件Mapper接口（操作数据库）
 */
@Mapper
public interface LeaveAttachmentMapper extends BaseMapper<LeaveAttachment> {

    /**
     * 根据请假单ID查询所有附件
     */
    default List<LeaveAttachment> selectByLeaveId(Long leaveId) {
        // 利用MyBatis-Plus的QueryWrapper构建查询条件
        return selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LeaveAttachment>()
                        .eq("leave_id", leaveId)
                        .orderByDesc("created_at")
        );
    }
}