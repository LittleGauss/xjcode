package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import org.apache.logging.log4j.util.Strings;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xj_service.oa.service.FileStorageService;

import org.xj_service.oa.common.Constants;
import org.xj_service.oa.common.Result;


import org.xj_service.oa.service.IConsumableGoodsService;
import org.xj_service.oa.entity.ConsumableGoods;

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
@RequestMapping("/consumable-goods")
public class ConsumableGoodsController {

    @Resource
    private IConsumableGoodsService consumableGoodsService;

    @Resource
    private FileStorageService minioFileStorageService;

    //新增 修改
//    @PostMapping()
//    public Result save(@RequestBody ConsumableGoods consumableGoods){
//    //新增或更新
//        return Result.success( consumableGoodsService.saveOrUpdate(consumableGoods));
//    }

            /**
             * 新增/更新物品（适配你的Result类）
             */
        @PostMapping("/saveOrUpdate")
        public Result saveOrUpdateGoods(
                @RequestBody @Valid ConsumableGoods consumableGoods,
                BindingResult bindingResult) {
            // 新增日志：打印接收的supplier值
            System.out.println("接收的supplier值：" + consumableGoods.getSupplier());
            // 1. 参数校验（如果有错误，返回错误信息）
            if (bindingResult.hasErrors()) {
                String errorMsg = bindingResult.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.joining("；"));
                // 调用Result.error(String code, String msg)，使用500错误码
                return Result.error(Constants.CODE_500, errorMsg);
            }

            // 2. 新增时自动填充创建时间
            if (consumableGoods.getId() == null) {
                consumableGoods.setCreatedAt(LocalDateTime.now());
            }

            // 3. 状态默认值（如果未传递，设为"1"代表在用）
            if (consumableGoods.getStatus() == null || consumableGoods.getStatus().trim().isEmpty()) {
                consumableGoods.setStatus("1");
            }

            // 调用新的方法
            boolean success = consumableGoodsService.saveOrUpdateWithRecord(consumableGoods);
            if (success) {
                // 成功时返回数据（物品ID），调用Result.success(Object data)
                return Result.success(consumableGoods.getId());
            } else {
                // 失败时返回错误信息，调用Result.error(String code, String msg)
                return Result.error(Constants.CODE_500, "新增/更新物品失败，请重试");
            }
        }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(consumableGoodsService.removeById(id));   //  consumableGoods ==user
    }

    //批量删除
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> userIds){
        return Result.success(consumableGoodsService.removeBatchByIds(userIds));
    }

    //查询所有
    // 查询所有（含预警标记）
    @GetMapping("/list")
    public Result findAll() {
        List<ConsumableGoods> list = consumableGoodsService.list();
        // 计算预警标记
        list.forEach(item -> {
            // 库存预警
            item.setStockWarning(item.getQuantity() != null && item.getWarningValue() != null
                    && item.getQuantity() <= item.getWarningValue());
            // 保质期预警（30天内到期）
            if (item.getExpireDate() != null) {
                // 直接使用 LocalDate 对象，无需解析
                long days = ChronoUnit.DAYS.between(LocalDate.now(), item.getExpireDate());
                item.setExpireWarning(days > 0 && days <= 30);
            } else {
                item.setExpireWarning(false);
            }
        });
        return Result.success(list);
    }


    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(consumableGoodsService.getById(id));
    }
    /**
     * 根据分类ID查询物品
     */
    @GetMapping("/category/{categoryId}")
    public List<ConsumableGoods> getGoodsByCategory(@PathVariable Integer categoryId) {
        return consumableGoodsService.lambdaQuery()
                .eq(ConsumableGoods::getCategoryId, categoryId)
                .list();
    }
    //分页
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {
        QueryWrapper<ConsumableGoods> queryWrapper = new QueryWrapper<>();

        queryWrapper.orderByDesc("id");
        return Result.success(consumableGoodsService.page(new Page<>(pageNum, pageSize),queryWrapper));

    }
    @PostMapping("/batch-save")
    public Result batchSave(
            @RequestBody @Valid List<@Valid ConsumableGoods> goodsList) {  // 嵌套校验，确保每个物品都符合校验规则
        // 1. 校验列表不为空
        if (goodsList == null || goodsList.isEmpty()) {
            return Result.error(Constants.CODE_500, "导入数据不能为空");
        }

        // 2. 对每个物品进行预处理（填充默认值）
        List<ConsumableGoods> processedList = goodsList.stream().map(goods -> {
            // 新增时自动填充创建时间（ID为空代表新增）
            if (goods.getId() == null) {
                goods.setCreatedAt(LocalDateTime.now());
            }
            // 状态默认值（未传递时设为"1"代表在用）
            if (goods.getStatus() == null || goods.getStatus().trim().isEmpty()) {
                goods.setStatus("1");
            }
            return goods;
        }).collect(Collectors.toList());

        // 3. 执行批量保存（MyBatis-Plus的saveBatch方法）
        boolean success = consumableGoodsService.saveOrUpdateWithRecordBatch(goodsList);
        if (success) {
            // 返回成功导入的数量
            return Result.success("批量导入成功，共导入 " + goodsList.size() + " 条数据");
        } else {
            return Result.error(Constants.CODE_500, "批量导入失败，请重试");
        }
    }

    /**
     * 导出耗材库存到Excel并上传到MinIO
     */
    @GetMapping("/export/minio")
    public Result exportToMinIO() {
        try {
            // 1. 获取耗材数据
            List<ConsumableGoods> goodsList = consumableGoodsService.list();

            // 2. 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("耗材库存");

            // 3. 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "物品ID", "物品名称", "分类ID", "型号规格", "品牌","供应商",
                "库存数量", "预警值", "单位", "单价", "状态",
                "入库日期", "过期日期", "创建时间", "库存预警", "保质期预警"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. 填充数据
            int rowNum = 1;
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

            for (ConsumableGoods goods : goodsList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(goods.getId() != null ? goods.getId() : 0);
                row.createCell(1).setCellValue(goods.getItemName() != null ? goods.getItemName() : "");
                row.createCell(2).setCellValue(goods.getCategoryId() != null ? goods.getCategoryId() : 0);
                row.createCell(3).setCellValue(goods.getSpec() != null ? goods.getSpec() : "");
                row.createCell(4).setCellValue(goods.getBrand() != null ? goods.getBrand() : "");
                row.createCell(5).setCellValue(goods.getSupplier() != null ? goods.getSupplier() : ""); // 新增供应商数据
                row.createCell(6).setCellValue(goods.getQuantity() != null ? goods.getQuantity() : 0);
                row.createCell(7).setCellValue(goods.getWarningValue() != null ? goods.getWarningValue() : 0);
                row.createCell(8).setCellValue(goods.getUnit() != null ? goods.getUnit() : "");
                row.createCell(9).setCellValue(goods.getUnitPrice() != null ? goods.getUnitPrice().doubleValue() : 0.0);
                row.createCell(10).setCellValue(goods.getStatus() != null ? goods.getStatus() : "");

                // 日期处理
                if (goods.getPurchaseDate() != null) {
                    Cell purchaseDateCell = row.createCell(11);
                    purchaseDateCell.setCellValue(goods.getPurchaseDate());
                    purchaseDateCell.setCellStyle(dateStyle);
                } else {
                    row.createCell(11).setCellValue("");
                }

                if (goods.getExpireDate() != null) {
                    Cell expireDateCell = row.createCell(12);
                    expireDateCell.setCellValue(goods.getExpireDate());
                    expireDateCell.setCellStyle(dateStyle);
                } else {
                    row.createCell(12).setCellValue("");
                }

                row.createCell(13).setCellValue(goods.getCreatedAt() != null ?
                    goods.getCreatedAt().toLocalDate().toString() : "");
                row.createCell(14).setCellValue(Boolean.TRUE.equals(goods.getStockWarning()) ? "是" : "否");
                row.createCell(15).setCellValue(Boolean.TRUE.equals(goods.getExpireWarning()) ? "是" : "否");
            }

            // 5. 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 6. 写入到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 7. 上传到MinIO
            String fileName = "耗材库存_" + LocalDate.now().toString() + ".xlsx";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            String downloadUrl = minioFileStorageService.upload(
                inputStream,
                fileName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "oa-files",
                "consumable"
            );

            return Result.success(downloadUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "导出到MinIO失败: " + e.getMessage());
        }
    }
}

