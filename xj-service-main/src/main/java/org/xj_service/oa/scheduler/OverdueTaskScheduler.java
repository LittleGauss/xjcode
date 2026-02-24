// OverdueTaskScheduler.java
package org.xj_service.oa.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xj_service.oa.service.ConsumableNoticeService;
import org.xj_service.oa.service.InspectionTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueTaskScheduler {

    private final InspectionTaskService inspectionTaskService;

    private final ConsumableNoticeService consumableNoticeService;

    @Value("${task.scheduling.enabled:true}")
    private boolean schedulingEnabled;
    /**
     * 表达式 0 0 1 * * ? 中：
     * ‌秒‌：0（第 0 秒）
     * ‌分钟‌：0（第 0 分钟）
     * ‌小时‌：1（凌晨 1 点）
     * ‌日（月）‌：*（每月每一天）
     * ‌月‌：*（每年每个月）
     * ‌日（星期）‌：?（不指定星期，避免与日（月）冲突）
     * 因此，该表达式表示 ‌每天凌晨 1 点整执行任务‌。‌
     * */
    @Value("${task.scheduling.overdue.cron:0 0 1 * * ?}")
    private String overdueCron;

    @Value("${task.scheduling.overdue.hourly.enabled:false}")
    private boolean hourlyCheckEnabled;

    /**
     * 初始化调度器
     */
    @PostConstruct
    public void init() {
        if (schedulingEnabled) {
            log.info("逾期任务调度器已启用");
            log.info("主检查cron表达式: {}", overdueCron);
            log.info("小时级检查: {}", hourlyCheckEnabled ? "启用" : "禁用");
        } else {
            log.warn("逾期任务调度器已禁用");
        }
    }

    /**
     * 每天凌晨1点检查逾期任务（主检查）
     */
    @Scheduled(cron = "${task.scheduling.overdue.cron:0 0 1 * * ?}")
    public void checkOverdueTasks() {
        if (!schedulingEnabled) {
            return;
        }

        try {
            log.info("开始执行逾期任务主检查... 当前时间: {}", LocalDateTime.now());
            long startTime = System.currentTimeMillis();

            inspectionTaskService.checkAndHandleOverdueTasks();
            consumableNoticeService.updateExpiredNoticesStatus();
            long endTime = System.currentTimeMillis();
            log.info("逾期任务主检查完成，耗时: {}ms", (endTime - startTime));

        } catch (Exception e) {
            log.error("逾期任务主检查失败", e);
            // 可以发送邮件或消息通知管理员
            sendErrorNotification(e);
        }
    }

    /**
     * 每小时检查一次（补充检查，防止遗漏）
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void checkOverdueTasksHourly() {
        if (!schedulingEnabled || !hourlyCheckEnabled) {
            return;
        }

        try {
            log.debug("开始执行小时级逾期任务检查... 当前时间: {}", LocalDateTime.now());
            inspectionTaskService.checkAndHandleOverdueTasks();
            log.debug("小时级逾期任务检查完成");
        } catch (Exception e) {
            log.error("小时级逾期任务检查失败", e);
        }
    }

    /**
     * 每5分钟检查一次（测试或紧急情况下使用）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkOverdueTasksFrequently() {
        // 这个可以根据需要启用或禁用
        // 通常在测试环境或紧急情况下使用
    }

    /**
     * 发送错误通知（可以集成邮件、短信、企业微信等）
     */
    private void sendErrorNotification(Exception e) {
        try {
            // 这里可以集成各种通知方式
            // 例如：发送邮件、发送企业微信消息、记录到数据库等
            log.error("需要通知管理员: 逾期任务检查失败，错误信息: {}", e.getMessage());

            // 示例：记录错误到数据库
            // errorLogService.logError("OVERDUE_TASK_CHECK", e.getMessage(), LocalDateTime.now());

        } catch (Exception notificationException) {
            log.error("发送错误通知失败", notificationException);
        }
    }
}