package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.SignRecord;

public interface ISignRecordService extends IService<SignRecord> {
    /**
     * 分页查询签名记录
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID，可选
     * @return 分页结果
     */
    Page<SignRecord> listSignRecords(Integer pageNum, Integer pageSize, Long userId);
}
