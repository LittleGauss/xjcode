package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.dto.ScrapApproveRequest;
import org.xj_service.oa.dto.ScrapReviewRequest;
import org.xj_service.oa.entity.ConsumableScrap;
import org.xj_service.oa.entity.User;

import java.util.List;

public interface IConsumableScrapService extends IService<ConsumableScrap> {

    void submitScrap(ConsumableScrap scrap);

    // 不再需要传用户ID，后端自动获取当前登录用户
    List<ConsumableScrap> getMyScrapList();

    List<ConsumableScrap> getToApproveScrapList(String status);

    boolean withdrawScrap(Integer scrapId);

    boolean reviewScrap(ScrapReviewRequest request);

    boolean approveScrap(ScrapApproveRequest request);

    ConsumableScrap getScrapDetail(Integer scrapId);
    /**
     * 查询所有分管领导（ROLE_VICE_LEADER）
     * 用于审核时选择下一级审批人
     */
    List<User> listViceLeaderUsers();


}
