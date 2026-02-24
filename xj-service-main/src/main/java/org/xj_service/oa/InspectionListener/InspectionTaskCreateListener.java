package org.xj_service.oa.InspectionListener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xj_service.oa.entity.InspectionTaskAssignment;
import org.xj_service.oa.mapper.InspectionTaskAssignmentMapper;

@Component("inspectionTaskCreateListener")
public class InspectionTaskCreateListener implements TaskListener {

    @Autowired
    private InspectionTaskAssignmentMapper assignmentMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        // 获取流程变量
        Long inspectionTaskId = (Long) delegateTask.getVariable("inspectionTaskId");
        Long inspectorId = Long.valueOf(delegateTask.getAssignee());

        // 更新分配表的Flowable任务ID
        QueryWrapper<InspectionTaskAssignment> wrapper = new QueryWrapper<>();
        wrapper.eq("inspection_task_id", inspectionTaskId)
                .eq("inspector_id", inspectorId);
        InspectionTaskAssignment assignment = assignmentMapper.selectOne(wrapper);
        if (assignment != null) {
            assignment.setFlowTaskId(delegateTask.getId());
            assignment.setTaskStatus("PENDING");//收到创建为
            assignmentMapper.updateById(assignment);
        }
    }
}