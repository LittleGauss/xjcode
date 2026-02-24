// 1. Service接口
// src/main/java/org/xj_service/oa/service/IApprovalCommentService.java
package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.ApprovalComment;
import java.util.List;

public interface IApprovalCommentService extends IService<ApprovalComment> {
    // 保存审批意见
    boolean saveComment(ApprovalComment comment);

    // 根据业务ID和类型查询
    List<ApprovalComment> getByBusiness(Integer businessId, String businessType);

    // 根据流程实例ID查询
    List<ApprovalComment> getByProcessInstanceId(String processInstanceId);
}
