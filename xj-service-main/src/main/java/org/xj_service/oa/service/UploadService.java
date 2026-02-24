package org.xj_service.oa.service;

import org.xj_service.oa.entity.Upload;

public interface UploadService {
    Upload create(Upload upload);

    Upload getById(Long id);

    /**
     * 将一组 upload id 关联到同一个 originId（例如 noticeId）
     */
    void updateOriginIds(java.util.List<Long> ids, Long originId);

    /**
     * 根据 origin 和 originId 列出 Upload 记录（例如 origin='notice', originId=123）
     */
    java.util.List<org.xj_service.oa.entity.Upload> listByOrigin(String origin, Long originId);
}
