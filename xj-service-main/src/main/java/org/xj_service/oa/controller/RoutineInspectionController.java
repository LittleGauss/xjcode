package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import org.xj_service.oa.common.Result;


import org.xj_service.oa.service.IRoutineInspectionService;
import org.xj_service.oa.entity.RoutineInspection;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 杜泽淇
 * @since 2025-08-13
 */
@RestController
@RequestMapping("/routine-inspection")
public class RoutineInspectionController {

    @Resource
    private IRoutineInspectionService routineInspectionService;

    //新增 修改
    @PostMapping
    public Result save(@RequestBody RoutineInspection routineInspection){
    //新增或更新
        return Result.success( routineInspectionService.saveOrUpdate(routineInspection));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(routineInspectionService.removeById(id));   //  routineInspection ==user
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> userIds){
        return Result.success(routineInspectionService.removeBatchByIds(userIds));
    }

    //查询所有
    @GetMapping
    public Result findAll() {
        return Result.success(routineInspectionService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(routineInspectionService.getById(id));
    }

    //分页
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {
        QueryWrapper<RoutineInspection> queryWrapper = new QueryWrapper<>();

        queryWrapper.orderByDesc("id");
        return Result.success(routineInspectionService.page(new Page<>(pageNum, pageSize),queryWrapper));

    }

}

