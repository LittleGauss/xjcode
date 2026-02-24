package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xj_service.oa.entity.AdministrativeNotice;
import org.xj_service.oa.entity.AdministrativeNoticeRead;
import org.xj_service.oa.entity.User;
import org.xj_service.oa.mapper.AdministrativeNoticeMapper;
import org.xj_service.oa.mapper.AdministrativeNoticeReadMapper;
import org.xj_service.oa.service.IAdministrativeNoticeReadService;

import java.io.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdministrativeNoticeReadServiceImpl
        extends ServiceImpl<AdministrativeNoticeReadMapper, AdministrativeNoticeRead>
        implements IAdministrativeNoticeReadService {

    @Autowired
    private AdministrativeNoticeReadMapper readMapper;

    // noticeMapper not needed currently

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markRead(Integer noticeId, Integer userId) {
        if (noticeId == null || userId == null)
            return false;
        QueryWrapper<AdministrativeNoticeRead> qw = new QueryWrapper<>();
        qw.eq("notice_id", noticeId).eq("user_id", userId);
        Long cnt = readMapper.selectCount(qw);
        if (cnt != null && cnt > 0)
            return true; // already marked

        AdministrativeNoticeRead r = new AdministrativeNoticeRead();
        r.setNoticeId(noticeId);
        r.setUserId(userId);
        r.setReadAt(LocalDateTime.now());
        int inserted = readMapper.insert(r);
        return inserted > 0;
    }

    @Override
    public int countUnreadForUser(Integer userId) {
        if (userId == null)
            return 0;
        return readMapper.countUnreadNoticesForUser(userId);
    }

    @Override
    public List<User> getUnreadUsersByNoticeId(Integer noticeId) {
        if (noticeId == null)
            return java.util.Collections.emptyList();
        return readMapper.selectUnreadUsersByNoticeId(noticeId);
    }

    @Override
    public ByteArrayOutputStream exportUnreadToExcel(Integer noticeId) throws Exception {
        // 使用 Apache POI 生成 XLSX 文件并返回为 ByteArrayOutputStream
        List<User> list = getUnreadUsersByNoticeId(noticeId);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("未读用户");

        // header
        XSSFRow headerRow = sheet.createRow(0);
        String[] headers = new String[] { "id", "username", "nickname", "email", "phone" };
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // data rows
        int rowIdx = 1;
        for (User u : list) {
            XSSFRow row = sheet.createRow(rowIdx++);
            if (u.getId() != null) {
                row.createCell(0).setCellValue(u.getId().doubleValue());
            } else {
                row.createCell(0).setCellValue("");
            }
            row.createCell(1).setCellValue(safe(u.getUsername()));
            row.createCell(2).setCellValue(safe(u.getNickname()));
            row.createCell(3).setCellValue(safe(u.getEmail()));
            row.createCell(4).setCellValue(safe(u.getPhone()));
        }

        // auto-size columns
        for (int i = 0; i < headers.length; i++) {
            try {
                sheet.autoSizeColumn(i);
            } catch (Exception ignored) {
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out;
    }

    @Override
    public int getReadCountByNoticeId(Integer noticeId) {
        if (noticeId == null)
            return 0;
        return readMapper.countByNoticeId(noticeId);
    }

    @Override
    public List<User> getReadUsersByNoticeId(Integer noticeId) {
        if (noticeId == null)
            return java.util.Collections.emptyList();
        return readMapper.selectReadUsersByNoticeId(noticeId);
    }

    private String safe(String s) {
        return s == null ? "" : s.replaceAll(",", " ");
    }
}
