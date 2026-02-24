package org.xj_service.oa.dto;

import lombok.Data;

@Data
public class ScrapApproveRequest {

    private Integer scrapId;
    /**
     * APPROVED / REJECTED
     */
    private String status; // APPROVED / REJECTED

    private String remark;
}
