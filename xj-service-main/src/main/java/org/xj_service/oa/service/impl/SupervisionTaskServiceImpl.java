package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.SupervisionTask;
import org.xj_service.oa.mapper.SupervisionTaskMapper;
import org.xj_service.oa.service.ISupervisionTaskService;

@Service
public class SupervisionTaskServiceImpl extends ServiceImpl<SupervisionTaskMapper, SupervisionTask>
        implements ISupervisionTaskService {

}
