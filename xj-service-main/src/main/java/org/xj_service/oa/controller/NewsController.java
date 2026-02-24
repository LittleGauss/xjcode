package org.xj_service.oa.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.BusinessType;
import org.xj_service.oa.common.Log;
import org.xj_service.oa.common.RequireRole;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.News;
import org.xj_service.oa.service.INewsService;

import java.util.List;

@RestController
@RequestMapping("/news")
@Tag(name = "新闻管理", description = "系统操作新闻接口")
public class NewsController {

    @Autowired
    private INewsService newsService;

    // 新增新闻
    @Log(title = "新增新闻", businessType = BusinessType.INSERT)
    @PostMapping
    @RequireRole({"SUPER_ADMIN","ROLE_ADMIN_OFFICE"})
    public Result addNews(@RequestBody News news) {
        boolean success = newsService.save(news);
        return success ? Result.success() : Result.error("添加新闻失败");
    }

    // 删除新闻（软删除）
    @Log(title = "删除新闻", businessType = BusinessType.DELETE)
    @RequireRole({"SUPER_ADMIN","ROLE_ADMIN_OFFICE"})
    @DeleteMapping("/{id}")
    public Result deleteNews(@PathVariable Integer id) {
        boolean success = newsService.removeById(id);
        return success ? Result.success() : Result.error("删除新闻失败");
    }

    // 获取所有新闻（用于管理页面）
    @GetMapping("/list")
    public Result getAllNews() {
        List<News> newsList = newsService.list();
        return Result.success(newsList);
    }

    // 获取主页新闻（2条）
    @GetMapping("/home")
    public Result getHomePageNews() {
        List<News> newsList = newsService.getHomePageNews();
        return Result.success(newsList);
    }
}