package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xj_service.oa.entity.News;
import org.xj_service.oa.service.INewsService;
import org.xj_service.oa.mapper.NewsMapper;
import java.util.List;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements INewsService {

    @Override
    public List<News> getHomePageNews() {
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0) // 未删除
                .orderByDesc("create_time") // 按创建时间倒序
                .last("LIMIT 6"); // 取前6条
        return baseMapper.selectList(queryWrapper);
    }
}