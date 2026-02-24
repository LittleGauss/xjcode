package org.xj_service.oa.dto;

import org.xj_service.oa.entity.ContractReview;
import lombok.Data; // 如果你用了 Lombok

@Data
public class ContractProcessRequest {
    private ContractReview contractReview;
    private String starter;
    private String gmUser;
    private String legalUser;
    
    // 【必须新增这个字段】
    private String adminUser; 
}