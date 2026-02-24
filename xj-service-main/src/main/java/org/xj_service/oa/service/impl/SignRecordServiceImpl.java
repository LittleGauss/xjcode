package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.SignRecord;
import org.xj_service.oa.mapper.SignRecordMapper;
import org.xj_service.oa.service.ISignRecordService;

@Service
public class SignRecordServiceImpl extends ServiceImpl<SignRecordMapper, SignRecord> implements ISignRecordService {
    /**
     * 分页查询签名记录
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID，可选
     * @return 分页结果
     */
    @Override
    public Page<SignRecord> listSignRecords(Integer pageNum, Integer pageSize, Long userId) {
        Page<SignRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SignRecord> queryWrapper = new QueryWrapper<>();
        // 如果指定了userId，则查询特定用户的签名
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }

        queryWrapper.orderByDesc("create_time");
        return page(page, queryWrapper);
    }

}
