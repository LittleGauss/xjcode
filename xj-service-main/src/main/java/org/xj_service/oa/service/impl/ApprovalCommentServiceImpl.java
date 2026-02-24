package org.xj_service.oa.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.ApprovalComment;
import org.xj_service.oa.mapper.ApprovalCommentMapper;
import org.xj_service.oa.service.IApprovalCommentService;
import java.util.List;

@Service
public class ApprovalCommentServiceImpl extends ServiceImpl<ApprovalCommentMapper, ApprovalComment>
        implements IApprovalCommentService {

    @Override
    public boolean saveComment(ApprovalComment comment) {
        comment.setCreatedAt(java.time.LocalDateTime.now());
        return save(comment);
    }

    @Override
    public List<ApprovalComment> getByBusiness(Integer businessId, String businessType) {
        return baseMapper.selectByBusiness(businessId, businessType);
    }

    @Override
    public List<ApprovalComment> getByProcessInstanceId(String processInstanceId) {
        return baseMapper.selectByProcessInstanceId(processInstanceId);
    }
}