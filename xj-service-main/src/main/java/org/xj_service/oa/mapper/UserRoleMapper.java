package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xj_service.oa.entity.UserRole;
import java.util.List;
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    List<String> selectRoleCodesByUserId(Integer userId);
}
