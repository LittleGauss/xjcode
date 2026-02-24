package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.ConsumableNotice;
import org.xj_service.oa.service.ConsumableNoticeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Api(tags = "耗材公示管理")
@RestController
@RequestMapping("/consumableNotice")
public class ConsumableNoticeController {

    @Autowired
    private ConsumableNoticeService consumableNoticeService;
    /**
     * 获取指定类型的最新有效公示（最多3条）
     * @return 最新有效公示列表
     */
    @GetMapping("/validNewnotices")
    public Result getValidNewNotices() {
        try {
            List<ConsumableNotice> notices = consumableNoticeService.getValidNotices();
            return Result.success(notices);
        } catch (Exception e) {
            return Result.error("获取公示信息失败: " + e.getMessage());
        }
    }
    @ApiOperation("获取全部公示列表")
    @GetMapping("/validNotices")
    public Result getValidNotices(@RequestParam(required = false) String noticeType,
                                  @RequestParam(required = false) String title,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        try {
        // 调用一个新的服务方法，支持多条件查询
        List<ConsumableNotice> notices = consumableNoticeService.getValidNoticesWithConditions(
                noticeType, title, status, pageNum, pageSize);
        return Result.success(notices);
    } catch (Exception e) {
        return Result.error("获取公示列表失败: " + e.getMessage());
    }
    }


    @ApiOperation("创建领用统计公示")
    @PostMapping("/statistical")
    public Result createStatisticalNotice(@RequestBody Map<String, Object> params) {
        try {
            // 从请求体中获取参数
            String title = (String) params.get("title");
            String deptName = (String) params.get("deptName");
            Integer creatorId = params.get("creatorId") != null ? Integer.parseInt(params.get("creatorId").toString()) : null;
            String creatorName = (String) params.get("creatorName");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");
            // 添加参数打印语句
            System.out.println("=== 创建领用统计公示参数 ===");
            System.out.println("title: " + title);
            System.out.println("deptName: " + deptName);
            System.out.println("creatorId: " + creatorId);
            System.out.println("creatorName: " + creatorName);
            System.out.println("startDate: " + startDate);
            System.out.println("endDate: " + endDate);
            System.out.println("=========================");
            // 如果startDate和endDate为空，默认统计最近30天
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;

            if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
                endDateTime = LocalDateTime.now();
                startDateTime = endDateTime.minusDays(30);
            } else {
                // 解析日期字符串，支持多种格式
                startDateTime = parseDateTime(startDate);
                endDateTime = parseDateTime(endDate);
            }

            // 调用服务层创建统计公示
            boolean success = consumableNoticeService.createStatisticalNotice(
                    title,
                    deptName,
                    creatorId,
                    creatorName,
                    startDateTime,
                    endDateTime
            );

            if (success) {
                return Result.success("领用统计公示创建成功");
            } else {
                return Result.success("领用统计公示创建失败|无记录");
            }
        } catch (Exception e) {
            return Result.error("领用统计公示创建异常: " + e.getMessage());
        }
    }
    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // 尝试多种格式
            DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy/MM/dd")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    if (dateTimeStr.length() == 10) { // 只有日期
                        LocalDate date = LocalDate.parse(dateTimeStr,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        return date.atStartOfDay();
                    }
                    return LocalDateTime.parse(dateTimeStr, formatter);
                } catch (Exception ignored) {
                    // 继续尝试下一个格式
                }
            }
            throw new IllegalArgumentException("日期格式不支持: " + dateTimeStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式错误: " + dateTimeStr);
        }
    }

    // 修改后的后端接口
    @ApiOperation("批准公示并更新标题")
    @PostMapping("/approve")
    public Result approveNotice(@RequestBody Map<String, Object> paramMap) {
        try {
            Integer id = Integer.parseInt(paramMap.get("id").toString());
            String title = paramMap.get("title").toString();
            boolean success = consumableNoticeService.approveNoticeWithNewTitle(id, title);
            if (success) {
                return Result.success("公示批准成功");
            } else {
                return Result.error("公示批准失败，可能是公示不存在或已过期");
            }
        } catch (Exception e) {
            return Result.error("公示批准失败: " + e.getMessage());
        }
    }

    @ApiOperation("删除单个公示")
    @DeleteMapping("/{id}")
    public Result deleteNotice(@PathVariable Integer id) {
        try {
            boolean success = consumableNoticeService.deleteNotice(id);
            if (success) {
                return Result.success("公示删除成功");
            } else {
                return Result.error("公示删除失败");
            }
        } catch (Exception e) {
            return Result.error("公示删除失败: " + e.getMessage());
        }
    }

    @ApiOperation("批量删除公示")
    @PostMapping("/batchDelete")
    public Result batchDeleteNotices(@RequestBody List<Integer> ids) {
        try {
            int deletedCount = consumableNoticeService.batchDeleteNotices(ids);
            return Result.success("成功删除 " + deletedCount + " 条公示");
        } catch (Exception e) {
            return Result.error("批量删除公示失败: " + e.getMessage());
        }
    }

    @ApiOperation("删除过期公示")
    @DeleteMapping("/expired")
    public Result deleteExpiredNotices() {
        try {
            Long deletedCount = consumableNoticeService.deleteExpiredNotices();
            return Result.success("已删除过期公示" + deletedCount+ "个");
        } catch (Exception e) {
            return Result.error("删除过期公示失败: " + e.getMessage());
        }
    }
}
