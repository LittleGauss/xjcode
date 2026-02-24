package org.xj_service.oa.service.impl;

import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.Upload;
import org.xj_service.oa.mapper.UploadMapper;
import org.xj_service.oa.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private UploadMapper uploadMapper;

    @Override
    public Upload create(Upload upload) {
        if (upload.getCreatedAt() == null) {
            upload.setCreatedAt(LocalDateTime.now());
        }
        uploadMapper.insert(upload);
        return upload;
    }

    @Override
    public Upload getById(Long id) {
        return uploadMapper.selectById(id);
    }

    @Override
    public void updateOriginIds(java.util.List<Long> ids, Long originId) {
        if (ids == null || ids.isEmpty() || originId == null)
            return;
        uploadMapper.updateOriginIds(ids, originId);
    }

    @Override
    public java.util.List<Upload> listByOrigin(String origin, Long originId) {
        if (origin == null || originId == null)
            return java.util.Collections.emptyList();
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Upload> qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        qw.eq("origin", origin).eq("origin_id", originId);
        return uploadMapper.selectList(qw);
    }
}
