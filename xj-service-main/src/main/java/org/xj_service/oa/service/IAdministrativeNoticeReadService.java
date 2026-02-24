package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xj_service.oa.entity.AdministrativeNoticeRead;
import org.xj_service.oa.entity.User;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface IAdministrativeNoticeReadService extends IService<AdministrativeNoticeRead> {
    boolean markRead(Integer noticeId, Integer userId);

    int countUnreadForUser(Integer userId);

    List<User> getUnreadUsersByNoticeId(Integer noticeId);

    ByteArrayOutputStream exportUnreadToExcel(Integer noticeId) throws Exception;

    List<User> getReadUsersByNoticeId(Integer noticeId);

    /**
     * 获取某条公告的已读数量
     */
    int getReadCountByNoticeId(Integer noticeId);
}
