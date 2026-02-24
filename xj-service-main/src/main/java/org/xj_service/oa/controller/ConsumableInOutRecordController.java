package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.Constants;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.ConsumableInOutRecord;
import org.xj_service.oa.entity.ConsumableGoods;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.service.ConsumableInOutRecordService;
import org.xj_service.oa.service.IConsumableGoodsService;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.IUserService;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/consumable-goods/in-out-records")
@Api(tags = "耗材出入库记录管理")
public class ConsumableInOutRecordController {

    @Autowired
    private ConsumableInOutRecordService recordService;

    @Autowired
    private IConsumableGoodsService goodsService;

    @Autowired
    private IUserService userService;

    /**
     * 获取出入库记录列表（分页+筛选）
     */
    @GetMapping("/list")
    @ApiOperation("获取出入库记录列表")
    public Result getInOutRecords(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String itemName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("请求参数: timeRange={}, startDate={}, endDate={}, supplier={}, operationType={}, itemName={}, pageNum={}, pageSize={}",
                timeRange, startDate, endDate, supplier, operationType, itemName, pageNum, pageSize);
        try {
            // 构建查询条件
            QueryWrapper<ConsumableInOutRecord> queryWrapper = new QueryWrapper<>();
            log.info("初始化查询条件完成");
            // 处理时间范围
            // 时间范围筛选（统一逻辑）
            if (startDate != null && endDate != null) {
                queryWrapper.ge("operation_time", startDate.atStartOfDay());
                queryWrapper.le("operation_time", endDate.atTime(LocalTime.MAX));
            } else if (timeRange != null && !timeRange.isEmpty()) {
                LocalDate now = LocalDate.now();
                LocalDate rangeStart = null;
                LocalDate rangeEnd = now;

                switch (timeRange) {
                    case "month":
                        rangeStart = now.withDayOfMonth(1);
                        break;
                    case "quarter":
                        int month = now.getMonthValue();
                        int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
                        rangeStart = LocalDate.of(now.getYear(), quarterStartMonth, 1);
                        break;
                    case "year":
                        rangeStart = LocalDate.of(now.getYear(), 1, 1);
                        break;
                }

                if (rangeStart != null) {
                    queryWrapper.ge("operation_time", rangeStart.atStartOfDay());
                    queryWrapper.le("operation_time", rangeEnd.atTime(LocalTime.MAX));
                }
            }

            // 供货商筛选
            if (supplier != null && !supplier.isEmpty()) {
                queryWrapper.eq("supplier", supplier);
                log.info("添加供货商条件: supplier = {}", supplier);
            }

            // 操作类型筛选
            if ("入库".equals(operationType)) {
                operationType = "IN";
            } else if ("出库".equals(operationType)) {
                operationType = "OUT";
            }
            if (operationType != null && !operationType.isEmpty()) {
                // 仅当筛选出库时，包含OUT和SCRAP；其他情况保持原有eq逻辑
                if ("OUT".equals(operationType)) {
                    queryWrapper.in("operation_type", "OUT", "SCRAP");
                } else {
                    // 入库/其他类型：保持原有等值筛选，不改动
                    queryWrapper.eq("operation_type", operationType);
                }
            }

            // 物品名称模糊查询
            if (itemName != null && !itemName.isEmpty()) {
                queryWrapper.like("item_name", itemName);
            }

            // 排序：按操作时间倒序
            queryWrapper.orderByDesc("operation_time");

            // 执行分页查询
            Page<ConsumableInOutRecord> page = new Page<>(pageNum, pageSize);
            Page<ConsumableInOutRecord> resultPage = recordService.page(page, queryWrapper);

            // 新增：为查询到的记录设置操作人昵称
            List<ConsumableInOutRecord> records = resultPage.getRecords();
            for (ConsumableInOutRecord record : records) {
                if (record.getOperatorId() != null) {
                    // 查询用户信息
                    User user = userService.getById(record.getOperatorId());
                    if (user != null) {
                        record.setOperatorNickname(user.getNickname());
                    }
                }
            }

            // 重新设置records到分页对象
            resultPage.setRecords(records);

            Map<String, Object> result = new HashMap<>();
            result.put("records", resultPage.getRecords());
            result.put("total", resultPage.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", resultPage.getPages());

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取出入库记录列表失败", e);
            return Result.error(Constants.CODE_500, "获取出入库记录列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取出入库记录统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取出入库记录统计")
    public Result getInOutStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            QueryWrapper<ConsumableInOutRecord> queryWrapper = new QueryWrapper<>();

            // 时间范围筛选
            if (startDate != null && endDate != null) {
                queryWrapper.ge("operation_time", startDate.atStartOfDay());
                queryWrapper.le("operation_time", endDate.atTime(LocalTime.MAX));
            }

            // 获取所有记录进行统计
            List<ConsumableInOutRecord> records = recordService.list(queryWrapper);


            Map<String, Object> statistics = new HashMap<>();
            statistics.put("records", records);
            // 计算入库总量
            int totalIn = records.stream()
                    .filter(r -> "IN".equals(r.getOperationType()))
                    .mapToInt(ConsumableInOutRecord::getQuantityChange)
                    .sum();
            statistics.put("totalIn", totalIn);

            // 计算出库总量（取绝对值）
            int totalOut = records.stream()
                    .filter(r -> "OUT".equals(r.getOperationType()) || "SCRAP".equals(r.getOperationType()))
                    .mapToInt(r -> Math.abs(r.getQuantityChange()))
                    .sum();
            statistics.put("totalOut", totalOut);

            // 供货商数量
            long supplierCount = records.stream()
                    .map(ConsumableInOutRecord::getSupplier)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            statistics.put("supplierCount", supplierCount);

            // 操作人员数量
            long operatorCount = records.stream()
                    .map(ConsumableInOutRecord::getOperatorName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            statistics.put("operatorCount", operatorCount);

            // 涉及物品数量
            long itemCount = records.stream()
                    .map(ConsumableInOutRecord::getItemName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            statistics.put("itemCount", itemCount);

            return Result.success(statistics);

        } catch (Exception e) {
            log.error("获取出入库统计失败", e);
            return Result.error(Constants.CODE_500, "获取统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取供货商列表（用于下拉框）
     */
    @GetMapping("/suppliers")
    @ApiOperation("获取供货商列表")
    public Result getSuppliers() {
        log.info("开始调用获取供货商列表接口");
        try {
            // 从物品表获取供货商列表
            QueryWrapper<ConsumableGoods> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("DISTINCT supplier");
            queryWrapper.isNotNull("supplier");
            queryWrapper.ne("supplier", "");

            List<Map<String, Object>> suppliers = goodsService.listMaps(queryWrapper);
            List<String> supplierList = suppliers.stream()
                    .map(map -> (String) map.get("supplier"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return Result.success(supplierList);

        } catch (Exception e) {
            log.error("获取供货商列表失败", e);
            return Result.error(Constants.CODE_500, "获取供货商列表失败：" + e.getMessage());
        }
    }


    /**
     * 导出出入库记录到Excel（直接下载）
     */
    @GetMapping("/export")
    @ApiOperation("导出出入库记录到Excel")
    public void exportInOutRecords(
            HttpServletResponse response,
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String itemName) {

        try {
            // 构建查询条件（同列表查询）
            QueryWrapper<ConsumableInOutRecord> queryWrapper = buildExportQueryWrapper(
                    timeRange, startDate, endDate, supplier, operationType, itemName);

            // 获取所有符合条件的数据（不分页）
            List<ConsumableInOutRecord> records = recordService.list(queryWrapper);
            // 新增：为每条记录设置操作人昵称
            for (ConsumableInOutRecord record : records) {
                if (record.getOperatorId() != null) {
                    User user = userService.getById(record.getOperatorId());
                    if (user != null) {
                        record.setOperatorNickname(user.getNickname());
                    }
                }
            }

            // 生成Excel文件
            Workbook workbook = generateExportExcel(records);

            // 设置响应头
            String fileName = URLEncoder.encode(
                    "出入库记录_" + LocalDate.now() + ".xlsx", "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 写入响应流
            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            log.error("导出出入库记录失败", e);
            try {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
        }
    }

   

    /**
     * 构建导出查询条件
     */
    private QueryWrapper<ConsumableInOutRecord> buildExportQueryWrapper(
            String timeRange, LocalDate startDate, LocalDate endDate,
            String supplier, String operationType, String itemName) {

        QueryWrapper<ConsumableInOutRecord> queryWrapper = new QueryWrapper<>();

        // 处理时间范围
        if (timeRange != null && !timeRange.isEmpty()) {
            LocalDate now = LocalDate.now();
            LocalDate rangeStart = null;
            LocalDate rangeEnd = now;

            switch (timeRange) {
                case "month": // 本月
                    rangeStart = now.withDayOfMonth(1);
                    break;
                case "quarter": // 本季度
                    int month = now.getMonthValue();
                    int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
                    rangeStart = LocalDate.of(now.getYear(), quarterStartMonth, 1);
                    break;
                case "year": // 本年
                    rangeStart = LocalDate.of(now.getYear(), 1, 1);
                    break;
                default:
                    // 自定义日期范围
                    if (startDate != null && endDate != null) {
                        rangeStart = startDate;
                        rangeEnd = endDate;
                    }
            }

            if (rangeStart != null) {
                queryWrapper.ge("operation_time", rangeStart.atStartOfDay());
                queryWrapper.le("operation_time", rangeEnd.atTime(LocalTime.MAX));
            }
        } else if (startDate != null && endDate != null) {
            // 自定义日期范围
            queryWrapper.ge("operation_time", startDate.atStartOfDay());
            queryWrapper.le("operation_time", endDate.atTime(LocalTime.MAX));
        }

        // 供货商筛选
        if (supplier != null && !supplier.isEmpty()) {
            queryWrapper.eq("supplier", supplier);
        }

        // 操作类型筛选
        if (operationType != null && !operationType.isEmpty()) {
            queryWrapper.eq("operation_type", operationType);
        }

        // 物品名称模糊查询
        if (itemName != null && !itemName.isEmpty()) {
            queryWrapper.like("item_name", itemName);
        }

        // 排序
        queryWrapper.orderByDesc("operation_time");

        return queryWrapper;
    }

    /**
     * 生成导出Excel文件
     */
    private Workbook generateExportExcel(List<ConsumableInOutRecord> records) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("出入库记录");

        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "操作时间", "操作类型", "物品名称", "分类",
                "数量变化", "操作前库存", "操作后库存",
                "操作人","操作人昵称", "操作人部门", "供货商", "单价", "总价",
                "备注"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 日期格式
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        // 填充数据
        int rowNum = 1;
        for (ConsumableInOutRecord record : records) {
            Row row = sheet.createRow(rowNum++);

            // 操作时间
            Cell operationTimeCell = row.createCell(0);
            operationTimeCell.setCellValue(record.getOperationTime());
            operationTimeCell.setCellStyle(dateStyle);

            // 操作类型
            String operationTypeText = "入库";
            if ("OUT".equals(record.getOperationType())) {
                operationTypeText = "出库";
            }
            row.createCell(1).setCellValue(operationTypeText);

            // 物品信息
            row.createCell(2).setCellValue(record.getItemName());
            row.createCell(3).setCellValue(record.getCategoryName());

            // 数量变化（正数显示+，负数显示-）
            String quantityChange = record.getQuantityChange() >= 0 ?
                    "+" + record.getQuantityChange() :
                    String.valueOf(record.getQuantityChange());
            row.createCell(4).setCellValue(quantityChange);
            row.createCell(5).setCellValue(record.getQuantityBefore());
            row.createCell(6).setCellValue(record.getQuantityAfter());

            // 操作人（出库人）信息
            row.createCell(7).setCellValue(record.getOperatorId());
            row.createCell(8).setCellValue(record.getOperatorName());
            row.createCell(9).setCellValue(record.getOperatorNickname() != null ? record.getOperatorNickname() : ""); //
            row.createCell(10).setCellValue(record.getOperatorDept() != null ? record.getOperatorDept() : "");

            // 供货商和价格
            row.createCell(11).setCellValue(record.getSupplier() != null ? record.getSupplier() : "");

            if (record.getUnitPrice() != null) {
                row.createCell(12).setCellValue(record.getUnitPrice().doubleValue());
            } else {
                row.createCell(12).setCellValue("");
            }

            if (record.getTotalPrice() != null) {
                row.createCell(13).setCellValue(record.getTotalPrice().doubleValue());
            } else {
                row.createCell(13).setCellValue("");
            }

            row.createCell(14).setCellValue(
                    record.getRemark() != null ? record.getRemark() : ""
            );

        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}