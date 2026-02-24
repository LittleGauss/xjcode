package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.xj_service.oa.entity.Upload;

import java.util.List;

@Mapper
public interface UploadMapper extends BaseMapper<Upload> {

    /**
     * 批量更新 uploads.origin_id 字段，用于把已上传的文件关联到某个业务对象（如 notice）
     */
    @Update("<script>\n"
            + "UPDATE uploads SET origin_id = #{originId} WHERE id IN "
            + "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">#{id}</foreach>\n"
            + "</script>")
    int updateOriginIds(@Param("ids") List<Long> ids, @Param("originId") Long originId);

}
