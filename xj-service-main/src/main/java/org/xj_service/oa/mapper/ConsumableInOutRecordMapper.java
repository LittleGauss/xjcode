package org.xj_service.oa.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xj_service.oa.entity.ConsumableInOutRecord;
import java.util.List;
import java.time.LocalDateTime;


@Mapper
public interface ConsumableInOutRecordMapper
        extends BaseMapper<ConsumableInOutRecord> {

    List<ConsumableInOutRecord> selectRecordList(
            @Param("operationType") String operationType,
            @Param("supplier") String supplier,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
