package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xj_service.oa.entity.User;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<User> getUsersByRoleCode(@Param("codeNames") List<String> codeNames);
    List<User> getDepartmentUser(Integer userId);

    User getUserWithDepartment(Integer userId);
   
    List<User> getViceLeaderUsers();
}
