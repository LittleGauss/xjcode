package org.xj_service.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xj_service.oa.entity.AdministrativeNoticeRead;
import org.xj_service.oa.entity.User;

import java.util.List;

@Mapper
public interface AdministrativeNoticeReadMapper extends BaseMapper<AdministrativeNoticeRead> {

    @Select("SELECT COUNT(*) FROM oa_administrative_notice_read WHERE notice_id = #{noticeId}")
    int countByNoticeId(@Param("noticeId") Integer noticeId);

    @Select("SELECT u.* FROM sys_user u WHERE u.id NOT IN (SELECT user_id FROM oa_administrative_notice_read r WHERE r.notice_id = #{noticeId})")
    List<User> selectUnreadUsersByNoticeId(@Param("noticeId") Integer noticeId);

    @Select("SELECT u.* FROM sys_user u WHERE u.id IN (SELECT user_id FROM oa_administrative_notice_read r WHERE r.notice_id = #{noticeId})")
    List<User> selectReadUsersByNoticeId(@Param("noticeId") Integer noticeId);

    @Select("SELECT COUNT(*) FROM oa_administrative_notice WHERE status = 'active' AND id NOT IN (SELECT notice_id FROM oa_administrative_notice_read r WHERE r.user_id = #{userId})")
    int countUnreadNoticesForUser(@Param("userId") Integer userId);
}
