package org.xj_service.oa.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.DispatchAttachment;
import org.xj_service.oa.mapper.DispatchAttachmentMapper;
import org.xj_service.oa.service.DispatchAttachmentService;

import java.time.LocalDateTime;

@Service
public class DispatchAttachmentServiceImpl implements DispatchAttachmentService {

    @Autowired
    private DispatchAttachmentMapper mapper;

    @Override
    public void create(DispatchAttachment attach) {
        if (attach.getCreatedAt() == null) {
            attach.setCreatedAt(LocalDateTime.now());
        }
        mapper.insert(attach);
    }

    @Override
    public void deleteByDispatchId(Long dispatchId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DispatchAttachment> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("dispatch_id", dispatchId);
        mapper.delete(qw);
    }
}
