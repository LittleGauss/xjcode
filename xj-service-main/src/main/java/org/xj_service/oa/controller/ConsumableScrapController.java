package org.xj_service.oa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.dto.ScrapApproveRequest;
import org.xj_service.oa.dto.ScrapReviewRequest;
import org.xj_service.oa.entity.ConsumableScrap;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.service.IConsumableScrapService;
import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;



@RestController
@RequestMapping("/consumable/scrap")
public class ConsumableScrapController {

    @Autowired
    private IConsumableScrapService scrapService;
    @Autowired
    private ObjectMapper objectMapper;

    // 提交报废申请
    @PostMapping("/submit")
    public ConsumableScrap submitScrap(@RequestBody ConsumableScrap scrap) {
        scrapService.submitScrap(scrap);
        return scrap;
    }

    // 获取我的报废列表（后端自动获取当前用户）
    @GetMapping("/my-list")
    public List<ConsumableScrap> getMyScrapList() {
        return scrapService.getMyScrapList();
    }

    // 获取待我审批的报废列表（后端自动获取当前用户）
    @GetMapping("/to-approve-list")
    public List<ConsumableScrap> getToApproveScrapList(@RequestParam(required = false) String status) {
        return scrapService.getToApproveScrapList(status);
    }

    // 撤回报废申请
    @PostMapping("/withdraw")
    public boolean withdrawScrap(@RequestParam Integer scrapId) {
        return scrapService.withdrawScrap(scrapId);
    }

    // 后保部审核
    @PostMapping("/review")
    public boolean reviewScrap(@RequestBody ScrapReviewRequest request) {
        try {
            // 直接打印请求对象（Spring已自动转换好）
            System.out.println("审核请求对象: " + request);
            System.out.println("nextApproverId: " + request.getNextApproverId());
            System.out.println("nextApproverName: " + request.getNextApproverName());

            // 调用服务层逻辑
            return scrapService.reviewScrap(request);

        } catch (Exception e) {
            System.err.println("审核报废申请失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("审核失败: " + e.getMessage());
        }
    }

    // 分管领导审批
    @PostMapping("/approve")
    public boolean approveScrap(@RequestBody ScrapApproveRequest request) {
        return scrapService.approveScrap(request);
    }

    // 获取报废详情
    @GetMapping("/{scrapId}")
    public ConsumableScrap getScrapDetail(@PathVariable Integer scrapId) {
        return scrapService.getScrapDetail(scrapId);
    }
    @GetMapping("/vice-leaders")
    public List<User> listViceLeaderUsers() {
        return scrapService.listViceLeaderUsers();
    }
}
