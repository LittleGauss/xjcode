package org.xj_service.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 合同附件实体
 */
@Getter
@Setter
@TableName("oa_contract_attachment")
public class ContractAttachment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer contractId;
    private String name;
    private String type; // word/pdf/other
    private Long size;   // 字节大小
    private LocalDateTime uploadTime;
    private String storePath; // 可选：实际保存路径或对象存储key
}
