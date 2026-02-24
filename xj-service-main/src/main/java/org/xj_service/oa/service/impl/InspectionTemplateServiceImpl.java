package org.xj_service.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.entity.InspectionTemplate;
import org.xj_service.oa.mapper.InspectionTemplateMapper;
import org.xj_service.oa.service.InspectionTemplateService;
import org.xj_service.oa.service.FileStorageService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板服务实现类
 */
@Slf4j
@Service
public class InspectionTemplateServiceImpl extends ServiceImpl<InspectionTemplateMapper, InspectionTemplate>
        implements InspectionTemplateService {

    @Resource
    private FileStorageService minioFileStorageService;

    @Autowired
    private InspectionTemplateMapper inspectionTemplateMapper;

    private String defaultTemplateBucket="oa-template-files";

    /**
     * 上传模板文件
     */
    @Override
    public List<InspectionTemplate> batchUploadTemplate(MultipartFile[] templateFiles, Long uploaderId, String uploaderName) {
        List<InspectionTemplate> templateList = new ArrayList<>();
        // 遍历批量文件，逐个上传到 MinIO 并保存数据库
        for (MultipartFile file : templateFiles) {
            if (file.isEmpty()) {
                continue; // 跳过空文件
            }
            // 1. 生成 MinIO 存储路径
            String objectName = "inspection-template/" + LocalDate.now() + "/" + file.getOriginalFilename();
            System.out.println("准备上传的文件名字objectName"+objectName);
            // 2. 上传到 MinIO
            String filePath = minioFileStorageService.upload(file, defaultTemplateBucket, "inspection-template");
            System.out.println("filePath"+filePath);
            // 3. 构建模板对象并保存
            InspectionTemplate template = new InspectionTemplate();
            template.setTemplateName(file.getOriginalFilename());
            template.setFileType( file.getContentType());
            template.setFileSize(file.getSize());
            template.setFilePath(filePath);
            template.setUploaderId(uploaderId);
            template.setUploaderName(uploaderName);
            template.setUploadTime(LocalDateTime.now());
            template.setBizType("inspection-template");
            template.setBucketName(defaultTemplateBucket);

            inspectionTemplateMapper.insert(template);
            templateList.add(template);
        }
        return templateList;
    }

    /**
     * 下载模板文件
     */
    // 服务层 downloadTemplate 方法优化：使用 try-with-resources 自动关闭流，避免内存泄漏
    @Override
    public String downloadTemplate(Long templateId) throws IOException {
        InspectionTemplate template = this.getById(templateId);
        if (template == null) {
            return "";
        }
        try {
             return  minioFileStorageService.getTempUrl(template.getFilePath(), 60 * 60 * 24);
        } catch (Exception e) {
           return "";
        }
    }

    @Override
    public String downloadUserTemplate() throws IOException {
        InspectionTemplate template = inspectionTemplateMapper.selectOne(new QueryWrapper<InspectionTemplate>().eq("template_name", "user-template.xlsx"));
        if (template == null) {
            return "";
        }
        try {
            return  minioFileStorageService.getTempUrl(template.getFilePath(), 60 * 60 * 24);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String downloadwith(String inspectionFormUrl) throws IOException {
        try {
            return  minioFileStorageService.getTempUrl(inspectionFormUrl, 60 * 60 * 24);
        } catch (Exception e) {
            return "";
        }
    }
    /**
     * 删除模板
     */
    @Override
    public boolean deleteTemplate(Long templateId) {
        // 1. 查询模板信息
        InspectionTemplate template = this.getById(templateId);
        if (template == null) {
            return false;
        }
        try {
            // 2. 删除MinIO中的文件
            minioFileStorageService.delete(template.getFilePath());
            // 3. 删除数据库记录
            return this.removeById(templateId);
        } catch (Exception e) {
            log.error("模板删除失败", e);
            return false;
        }
    }

    /**
     * 获取模板列表
     */
    @Override
    public List<InspectionTemplate> getTemplateList() {
        return this.list();
    }

    /**
     * 根据ID获取模板详情
     */
    @Override
    public InspectionTemplate getTemplateById(Long templateId) {
        return this.getById(templateId);
    }
}