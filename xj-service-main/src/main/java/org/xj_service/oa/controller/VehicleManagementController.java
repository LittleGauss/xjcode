package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.common.RequireRole;
import org.xj_service.oa.service.IVehicleManagementService;
import org.xj_service.oa.entity.VehicleManagement;

/**
 * 车辆管理控制器（兼容前端 /api/vehicle* 路径）
 */
@RestController
@Tag(name = "车辆管理", description = "车辆及派车相关接口")
public class VehicleManagementController {

    @Resource
    private IVehicleManagementService vehicleManagementService;

    /**
     * 获取车辆列表或分页查询
     * 支持：/api/vehicles （无 page/size 返回全部）
     * /api/vehicles?page=1&size=10 返回分页数据
     */
    @Operation(summary = "获取车辆列表", description = "获取车辆列表，支持分页和按车牌/部门模糊查询")
    @GetMapping({ "/api/vehicles", "/api/vehicle/list" })
    public Result getVehicles(@RequestParam Map<String, Object> params) {
        try {
            // 支持分页
            Integer pageNum = params.get("page") != null ? Integer.valueOf(params.get("page").toString()) : null;
            Integer pageSize = params.get("size") != null ? Integer.valueOf(params.get("size").toString()) : null;

            QueryWrapper<VehicleManagement> queryWrapper = new QueryWrapper<>();
            // 简单的模糊查询支持
            if (params.get("plateNumber") != null) {
                queryWrapper.like("plate_number", params.get("plateNumber"));
            }
            if (params.get("department") != null) {
                queryWrapper.eq("department", params.get("department"));
            }
            queryWrapper.orderByDesc("id");

            if (pageNum != null && pageSize != null) {
                Page<VehicleManagement> page = new Page<>(pageNum, pageSize);
                Page<VehicleManagement> pageRes = vehicleManagementService.page(page, queryWrapper);
                if (pageRes != null && pageRes.getRecords() != null && !pageRes.getRecords().isEmpty()) {
                    VehicleManagement sample = pageRes.getRecords().get(0);
//                    System.out.println("[getVehicles] sample -> plate=" + sample.getPlateNumber() + ", fuelType="
//                            + sample.getFuelType());
                }
                return Result.success(pageRes);
            } else {
                List<VehicleManagement> list = vehicleManagementService.list(queryWrapper);
                if (list != null && !list.isEmpty()) {
                    VehicleManagement sample = list.get(0);
//                    System.out.println("[getVehicles] sample -> plate=" + sample.getPlateNumber() + ", fuelType="
//                            + sample.getFuelType());
                }
                return Result.success(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取车辆列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个车辆详情（按 id）
     */
    @Operation(summary = "获取车辆详情", description = "根据车辆ID获取车辆详细信息")
    @GetMapping({ "/api/vehicle/{id}", "/vehicle-management/{id}" })
    public Result getVehicle(@PathVariable Long id) {
        try {
            VehicleManagement vm = vehicleManagementService.getById(id);
            if (vm == null) {
                return Result.error("404", "车辆未找到");
            }
            return Result.success(vm);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "获取车辆详情失败");
        }
    }

    /**
     * 新增或更新车辆
     */
    @Operation(summary = "新增/更新车辆", description = "新增或更新车辆信息")
    //@RequireRole({ "SUPER_ADMIN" })
    @PostMapping({ "/api/vehicle", "/vehicle-management" })
    public Result saveVehicle(@RequestBody VehicleManagement vehicleManagement) {
        try {
            // 打印接收到的数据，便于排查字段未写入的问题
            System.out.println("[saveVehicle] incoming: plateNumber=" + vehicleManagement.getPlateNumber()
                    + ", displacement=" + vehicleManagement.getDisplacement()
                    + ", fuelType=" + vehicleManagement.getFuelType()
                    + ", vehicleStatus=" + vehicleManagement.getVehicleStatus());

            boolean ok = vehicleManagementService.saveOrUpdate(vehicleManagement);
            if (!ok) {
                return Result.error("500", "保存失败");
            }

            // 查询最新持久化对象并返回，方便前端获得数据库实际存储的字段
            VehicleManagement saved = vehicleManagementService.getById(vehicleManagement.getId());
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "保存车辆失败: " + e.getMessage());
        }
    }

    /**
     * 删除车辆
     */
    @Operation(summary = "删除车辆", description = "根据ID删除车辆")
    //@RequireRole({ "SUPER_ADMIN" })
    @DeleteMapping({ "/api/vehicle/{id}", "/vehicle-management/{id}" })
    public Result deleteVehicle(@PathVariable Long id) {
        try {
            boolean ok = vehicleManagementService.removeById(id);
            return Result.success(ok);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "删除车辆失败");
        }
    }

    /**
     * 批量删除
     */
    @Operation(summary = "批量删除车辆", description = "根据ID列表批量删除车辆")
    @PostMapping({ "/api/vehicle/del/batch", "/vehicle-management/del/batch" })
    public Result deleteBatch(@RequestBody List<Long> ids) {
        try {
            boolean ok = vehicleManagementService.removeBatchByIds(ids);
            return Result.success(ok);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "批量删除失败");
        }
    }

    /**
     * 周期调整占位接口：用于前端时间维度统计表的保险费/年审费/备注保存与查询。
     * 注意：当前为占位实现，返回空列表或成功状态，避免 400 错误；
     * 后续可落库：建立 period_adjustments 表，字段包括
     * period_type、label、insurance_fee、annual_inspection_fee、remark。
     */
    @Operation(summary = "查询周期调整", description = "按时间维度查询保险费/年审费/备注调整")
    @GetMapping({ "/api/vehicle/period-adjustments" })
    public Result getPeriodAdjustments(
            @RequestParam(value = "periodType", required = false) String periodType,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "startYear", required = false) Integer startYear,
            @RequestParam(value = "endYear", required = false) Integer endYear) {
        try {
            // 占位返回空数组，避免前端 400；后续可根据 periodType/year 区间查询真实数据
            return Result.success(java.util.Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "查询周期调整失败: " + e.getMessage());
        }
    }

    @Operation(summary = "保存周期调整", description = "保存时间维度的保险费/年审费/备注")
    @PostMapping({ "/api/vehicle/period-adjustments" })
    public Result savePeriodAdjustments(@RequestBody Map<String, Object> payload) {
        try {
            // 占位：直接返回成功；payload 示例：{ periodType, label, insuranceFee, annualInspectionFee,
            // remark }
            // TODO: 后续接入持久化保存
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "保存周期调整失败: " + e.getMessage());
        }
    }

}
