package org.xj_service.oa.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.xj_service.oa.entity.DispatchRecord;
import org.xj_service.oa.mapper.DispatchRecordMapper;
import org.xj_service.oa.service.IDispatchRecordService;

@Service
public class DispatchRecordServiceImpl extends ServiceImpl<DispatchRecordMapper, DispatchRecord>
        implements IDispatchRecordService {

}
