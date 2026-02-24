package org.xj_service.oa.dto;

import lombok.Data;
import org.xj_service.oa.entity.LeaveProcess;

@Data
public class LeaveProcessRequest {
    
    // 请假单实体（包含天数 duration 等信息）
    private LeaveProcess leaveProcess;
    
    // 流程发起人 (对应 BPMN: starter)
    private String starter;

    // --- 审批人字段 ---

    // 部门主管 (对应 BPMN: deptManager)
    // 原名 firstApprover，建议保留原名以免改前端，或者做好映射
    private String firstApprover; 

    // 分管领导 (对应 BPMN: viceLeader)
    // 场景：请假 > 0.5 天
    private String secondApprover;

    // [新增] 主要领导 (对应 BPMN: mainLeader)
    // 场景：请假 > 3 天
    private String mainLeader;

    // [新增] 行政办人员 (对应 BPMN: adminUser)
    // 如果系统行政人员不固定，建议加上；如果固定，可不加，在后端写死
    private String adminUser;

    // [新增] 附件ID列表
    private java.util.List<Long> attachmentIds;
}