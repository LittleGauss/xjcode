package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xj_service.oa.entity.InspectionTask;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InspectionTaskMapper extends BaseMapper<InspectionTask> {
    // 根据发起人ID查询任务列表
    List<InspectionTask> selectByInitiatorId(@Param("initiatorId") Long initiatorId);


    @Select("SELECT * FROM oa_inspection_task WHERE deadline < #{now} " +
            "AND status = 'IN_PROGRESS' AND overdue_flag = 'NO'")
    List<InspectionTask> findOverdueTasks(@Param("now") LocalDateTime now);
}