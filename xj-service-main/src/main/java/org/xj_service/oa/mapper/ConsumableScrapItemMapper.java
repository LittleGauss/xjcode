package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xj_service.oa.entity.ConsumableScrapItem;

import java.util.List;

@Mapper
public interface ConsumableScrapItemMapper extends BaseMapper<ConsumableScrapItem> {

    /**
     * 根据报废单ID查询报废明细
     */
    List<ConsumableScrapItem> selectByScrapId(@Param("scrapId") Integer scrapId);


}