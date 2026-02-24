package org.xj_service.oa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.ConsumableCategory;
import org.xj_service.oa.service.ConsumableCategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class ConsumableCategoryController {

    @Autowired
    private ConsumableCategoryService categoryService;

    /**
     * 查询所有分类（按排序号升序）
     */
    @GetMapping("/list")
    public Result getAllCategories() { // 返回类型改为 Result
        List<ConsumableCategory> list = categoryService.lambdaQuery()
                .orderByAsc(ConsumableCategory::getSort)
                .list();
        return Result.success(list); // 用 Result 包装，自动包含 code=200 和 data=list
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    public ConsumableCategory getCategoryById(@PathVariable Integer id) {
        return categoryService.getById(id);
    }

    /**
     * 新增分类
     */
    @PostMapping("/add")
    public Result addCategory(@RequestBody ConsumableCategory category) {
        // 检查分类名称是否已存在
        boolean exists = categoryService.lambdaQuery()
                .eq(ConsumableCategory::getCategoryName, category.getCategoryName())
                .exists();

        if (exists) {
            return Result.error("500", "分类名称已存在");
        }

        boolean success = categoryService.save(category);
        if (success) {
            return Result.success("分类添加成功");
        } else {
            return Result.error("500", "分类添加失败");
        }
    }

    /**
     * 更新分类
     */
    @PutMapping
    public boolean updateCategory(@RequestBody ConsumableCategory category) {
        return categoryService.updateById(category);
    }

    /**
     * 删除分类
     /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteCategory(@PathVariable Integer id) {
        try {
            boolean success = categoryService.removeById(id);
            if (success) {
                return Result.success("分类删除成功");
            } else {
                return Result.error("500", "分类删除失败");
            }
        } catch (Exception e) {
            return Result.error("500", "删除分类时发生错误: " + e.getMessage());
        }
    }

}