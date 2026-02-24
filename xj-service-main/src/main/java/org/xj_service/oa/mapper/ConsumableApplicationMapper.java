package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xj_service.oa.entity.ConsumableApplication;

import java.util.List;
@Mapper
public interface ConsumableApplicationMapper extends BaseMapper<ConsumableApplication> {
    /**
     * 根据申请人ID查询申请记录
     * @param applicantId 申请人ID
     * @return 申请列表
     */
    List<ConsumableApplication> selectByApplicantId(@Param("applicantId") Integer applicantId);

    /**
     * 根据流程实例ID查询申请
     * @param processInstanceId 流程实例ID
     * @return 申请实体
     */
    ConsumableApplication selectByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 根据状态和耗材类型查询申请
     * @param finalStatus 状态
     * @param consumableType 耗材类型
     * @return 申请列表
     */
    List<ConsumableApplication> selectByStatusAndType(
            @Param("finalStatus") String finalStatus,
            @Param("consumableType") String consumableType
    );

    /**
     * 查询所有领用申请
     * @return 所有申请列表
     */
    List<ConsumableApplication> selectAllApplications();

    /**
     * 根据状态筛选申请
     * @param status 状态值
     * @return 申请列表
     */
    List<ConsumableApplication> selectByStatus(@Param("status") String status);

    /**
     * 获取审批历史（根据审批人ID）
     * @param approverId 审批人ID
     * @return 审批历史列表
     */
    List<ConsumableApplication> selectApprovalHistory(@Param("approverId") Integer approverId);
}
