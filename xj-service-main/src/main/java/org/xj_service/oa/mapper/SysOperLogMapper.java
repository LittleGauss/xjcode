package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xj_service.oa.entity.SysOperLog;
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
    /**
     * 查询满足指定条件的日志记录总数 (即用户登录总次数)
     * * @return 记录总数
     */
    Integer countUserLogins();
}
