package org.xj_service.oa.service;

import org.xj_service.oa.entity.ConsumableGoods;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 杜泽淇
 * @since 2025-08-13
 */
public interface IConsumableGoodsService extends IService<ConsumableGoods> {
    // 新增一个方法，用于新增或更新库存并写入出入库记录
    boolean saveOrUpdateWithRecord(ConsumableGoods goods);

    boolean saveOrUpdateWithRecordBatch(@Valid List<@Valid ConsumableGoods> goodsList);
}
