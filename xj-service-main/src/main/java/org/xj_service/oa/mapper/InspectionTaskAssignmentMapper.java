package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xj_service.oa.entity.InspectionTaskAssignment;

import java.util.List;

@Mapper
public interface InspectionTaskAssignmentMapper extends BaseMapper<InspectionTaskAssignment> {
    // 根据任务ID查询分配列表
    List<InspectionTaskAssignment> selectByTaskId(@Param("taskId") Long taskId);

    // 根据主任务ID和当前用户ID查询子任务信息
    InspectionTaskAssignment getAssignmentByTaskIdAndUseID(@Param("inspectionTaskId") Long inspectionTaskId, @Param("inspectorId") Long inspectorId);

    @Insert({
            "<script>",
            "INSERT INTO oa_inspection_task_assignment ",
            "(inspection_task_id, inspector_id, inspector_name, task_status, receive_time, redo_count, create_time, flow_task_id) ",
            "VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.inspectionTaskId}, #{item.inspectorId}, #{item.inspectorName}, #{item.taskStatus}, #{item.receiveTime}, #{item.redoCount}, #{item.createTime}, #{item.flowTaskId})",
            "</foreach>",
            "</script>"
    })
    int batchInsert(@Param("list") List<InspectionTaskAssignment> list);

    // 根据检查员ID和状态查询任务
    List<InspectionTaskAssignment> selectByFlowTaskId(@Param("inspectorId") Long inspectorId, @Param("flowTaskId") String flowTaskId);
}