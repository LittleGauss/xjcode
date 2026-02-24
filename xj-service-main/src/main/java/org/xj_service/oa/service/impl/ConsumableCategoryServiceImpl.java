package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.ConsumableCategory;
import org.xj_service.oa.mapper.ConsumableCategoryMapper;
import org.xj_service.oa.service.ConsumableCategoryService;

@Service
public class ConsumableCategoryServiceImpl extends ServiceImpl<ConsumableCategoryMapper, ConsumableCategory> implements ConsumableCategoryService {
}
