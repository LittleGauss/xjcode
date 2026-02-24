package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestParam;
import org.xj_service.oa.entity.ConsumableNotice;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsumableNoticeService extends IService<ConsumableNotice> {
    /**
     * 获取3条最新的有效的公示，展示在主页的，不足3条是多少就获取多少
     *
     * @return 公示列表
     */
    List<ConsumableNotice> getValidNotices();

    /**
     * 根据类型获取有效公示
     *
     * @param noticeType 公示类型
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 公示列表
     */
    List<ConsumableNotice> getValidNoticesWithConditions(String noticeType,
                                                         String title,
                                                         String status,
                                                         int pageNum,
                                                         int pageSize);


    /**
     * 创建入库公示
     *
     * @param title      标题
     * @param content    内容
     * @param creatorId  创建人ID
     * @param creatorName 创建人姓名
     */
    void createInboundNotice(String title, String content, Integer creatorId, String creatorName);

    /**
     * 创建报废公示
     *
     * @param title      标题
     * @param content    内容
     * @param creatorId  创建人ID
     * @param creatorName 创建人姓名
     */
    void createScrapNotice(String title, String content, Integer creatorId, String creatorName);

    /**
     * 创建领用统计公示
     *
     * @param title      标题
     * @param deptName   部门名称
     * @param creatorId  创建人ID
     * @param creatorName 创建人姓名
     */
    boolean createStatisticalNotice(String title,
                                 String deptName, Integer creatorId,
                                 String creatorName,
                                 LocalDateTime startDate,
                                 LocalDateTime endDate);

    /**
     * 删除过期公示
     *
     * @return 删除的记录数
     */
    Long deleteExpiredNotices();

    /**
     * 批准公示
     *
     * @param id 公示ID
     * @return 是否成功
     */
    boolean approveNoticeWithNewTitle(Integer id, String newTitle);

    /**
     * 删除单个公示
     *
     * @param id 公示ID
     * @return 是否成功
     */
    boolean deleteNotice(Integer id);

    /**
     * 批量删除公示
     *
     * @param ids 公示ID列表
     * @return 删除的数量
     */
    int batchDeleteNotices(List<Integer> ids);

    /**
     * 将过期的公示状态更新为EXPIRED
     * @return 更新的记录数量
     */
    Long updateExpiredNoticesStatus();

}
