package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.ContractAttachment;
import org.xj_service.oa.mapper.ContractAttachmentMapper;
import org.xj_service.oa.service.IContractAttachmentService;

import java.util.List;

@Service
public class ContractAttachmentServiceImpl extends ServiceImpl<ContractAttachmentMapper, ContractAttachment> implements IContractAttachmentService {

    @Override
    public List<ContractAttachment> listByContract(Integer contractId) {
        return this.list(new LambdaQueryWrapper<ContractAttachment>().eq(ContractAttachment::getContractId, contractId));
    }

    @Override
    public void deleteByContract(Integer contractId) {
        this.remove(new LambdaQueryWrapper<ContractAttachment>().eq(ContractAttachment::getContractId, contractId));
    }
}
