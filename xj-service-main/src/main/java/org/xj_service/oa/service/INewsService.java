package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.News;

import java.util.List;

public interface INewsService extends IService<News> {
    List<News> getHomePageNews(); // 获取主页固定2条新闻

}
