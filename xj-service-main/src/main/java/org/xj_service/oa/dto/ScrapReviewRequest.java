package org.xj_service.oa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ScrapReviewRequest {

    private Integer scrapId;

    /** 审核结果：REVIEWED / REJECTED */
    private String status;

    /** 审核意见 */
    private String remark;
    @JsonProperty("nextApproverId")
    private Integer nextApproverId;   // 下一级审批人ID（审核通过时需要）
    @JsonProperty("nextApproverName")
    private String nextApproverName;  // 下一级审批人姓名
}
