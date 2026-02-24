package org.xj_service.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.entity.InspectionTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 检查任务模板服务接口
 */
public interface InspectionTemplateService extends IService<InspectionTemplate> {

    /**
     * 上传模板文件
     * @param file 上传的文件
     * @param uploaderId 上传人ID
     * @param uploaderName 上传人名称
     * @return 模板信息
     */
    public List<InspectionTemplate> batchUploadTemplate(MultipartFile[] file, Long uploaderId, String uploaderName);

    /**
     * 下载模板文件
     * @param templateId 模板ID
     */
    String downloadTemplate(Long templateId) throws IOException;

    /**
     * 下载固定模板文件
     */
    String downloadUserTemplate() throws IOException;

    /**
     * 下载模板文件 根据路径下载
     */
    String downloadwith(String inspectionFormUrl) throws IOException;
    /**
     * 删除模板（同时删除MinIO文件）
     * @param templateId 模板ID
     */
    boolean deleteTemplate(Long templateId);

    /**
     * 获取模板列表
     * @return 模板列表
     */
    List<InspectionTemplate> getTemplateList();

    /**
     * 根据ID获取模板详情
     * @param templateId 模板ID
     * @return 模板信息
     */
    InspectionTemplate getTemplateById(Long templateId);
}