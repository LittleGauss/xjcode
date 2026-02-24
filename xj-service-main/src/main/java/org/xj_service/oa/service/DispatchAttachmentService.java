package org.xj_service.oa.service;

import org.xj_service.oa.entity.DispatchAttachment;

public interface DispatchAttachmentService {
    void create(DispatchAttachment attach);

    void deleteByDispatchId(Long dispatchId);
}
