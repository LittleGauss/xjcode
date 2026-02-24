package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.SysOperLog;
import org.xj_service.oa.mapper.SysOperLogMapper;
import org.xj_service.oa.service.ISysOperLogService;


@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {
    @Autowired
    private SysOperLogMapper opLogMapper;
    @Async // 关键：异步执行，不阻塞主线程
    @Override
    public void saveLog(SysOperLog log) {
        this.save(log);
    }

    @Override
    public Integer getCurrentUserIndex() {
        return opLogMapper.countUserLogins();
    }

}