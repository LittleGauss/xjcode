package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.ContractAttachment;

import java.util.List;

public interface IContractAttachmentService extends IService<ContractAttachment> {
    List<ContractAttachment> listByContract(Integer contractId);
    void deleteByContract(Integer contractId);
}
