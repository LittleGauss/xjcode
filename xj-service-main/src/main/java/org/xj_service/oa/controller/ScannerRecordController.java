package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import org.xj_service.oa.common.Result;


import org.xj_service.oa.service.IScannerRecordService;
import org.xj_service.oa.entity.ScannerRecord;

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
@RequestMapping("/scanner-record")
public class ScannerRecordController {

    @Resource
    private IScannerRecordService scannerRecordService;

    //新增 修改
    @PostMapping
    public Result save(@RequestBody ScannerRecord scannerRecord){
    //新增或更新
        return Result.success( scannerRecordService.saveOrUpdate(scannerRecord));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(scannerRecordService.removeById(id));   //  scannerRecord ==user
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> userIds){
        return Result.success(scannerRecordService.removeBatchByIds(userIds));
    }

    //查询所有
    @GetMapping
    public Result findAll() {
        return Result.success(scannerRecordService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(scannerRecordService.getById(id));
    }

    //分页
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {
        QueryWrapper<ScannerRecord> queryWrapper = new QueryWrapper<>();

        queryWrapper.orderByDesc("id");
        return Result.success(scannerRecordService.page(new Page<>(pageNum, pageSize),queryWrapper));

    }

}

