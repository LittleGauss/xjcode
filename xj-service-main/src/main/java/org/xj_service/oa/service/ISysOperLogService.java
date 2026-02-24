package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.SysOperLog;

public interface ISysOperLogService extends IService<SysOperLog> {
    public void saveLog(SysOperLog log);

    /**
     * 获取用户登录总次数作为 userIndex
     */
    public Integer getCurrentUserIndex();
}
