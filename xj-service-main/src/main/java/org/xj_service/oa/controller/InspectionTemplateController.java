package org.xj_service.oa.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.InspectionTemplate;
import org.xj_service.oa.service.InspectionTemplateService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 检查模板控制器（统一返回Result类型）
 */
@Slf4j
@RestController
@RequestMapping("/inspection/template")
public class InspectionTemplateController {

    @Autowired
    private InspectionTemplateService inspectionTemplateService;

    /**
     * 批量上传模板文件（你提供的原始方法，已符合规范）
     */
    @PostMapping("/upload")
    public Result uploadTemplate(
            @RequestParam("templateFile") MultipartFile[] templateFiles,
            @RequestParam("uploaderId") Long uploaderId,
            @RequestParam("uploaderName") String uploaderName) {

        try {
            if (templateFiles == null || templateFiles.length == 0) {
                return Result.error("请选择要上传的文件");
            }
            List<InspectionTemplate> templateList = inspectionTemplateService.batchUploadTemplate(templateFiles, uploaderId, uploaderName);
            return Result.success(templateList);
        } catch (Exception e) {
            log.error("批量上传模板失败", e);
            return Result.error("批量上传失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有模板列表（改造为统一返回Result）
     */
    @GetMapping("/list")
    public Result listTemplates() {
        try {
            List<InspectionTemplate> templateList = inspectionTemplateService.getTemplateList();
            return Result.success(templateList);
        } catch (Exception e) {
            log.error("查询模板列表失败", e);
            return Result.error("查询模板列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询模板详情（改造为统一返回Result）
     */
    @GetMapping("/{id}")
    public Result getTemplateById(@PathVariable("id") Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("模板ID不能为空且必须为正数");
            }
            InspectionTemplate template = inspectionTemplateService.getTemplateById(id);
            if (template == null) {
                return Result.error("未找到ID为" + id + "的模板");
            }
            return Result.success(template);
        } catch (Exception e) {
            log.error("查询模板详情失败，ID：{}", id, e);
            return Result.error("查询模板详情失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID删除模板（改造为统一返回Result）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteTemplate(@PathVariable("id") Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("模板ID不能为空且必须为正数");
            }
            boolean deleteSuccess = inspectionTemplateService.deleteTemplate(id);
            if (deleteSuccess) {
                return Result.success("模板删除成功");
            } else {
                return Result.error("模板删除失败，可能模板不存在");
            }
        } catch (Exception e) {
            log.error("删除模板失败，ID：{}", id, e);
            return Result.error("删除模板失败：" + e.getMessage());
        }
    }

    /**
     * 下载模板文件
     * @param templateId 模板ID
     */
    @GetMapping("/download/{templateId}")
    public Result  downloadTemplate(@PathVariable("templateId") Long templateId) {
        try {
            // 参数校验
            if (templateId == null || templateId <= 0) {
                return Result.error("未找到文件");
            }
            // 调用服务层下载方法
            return Result.success(inspectionTemplateService.downloadTemplate(templateId));
        } catch (RuntimeException e) {
            return Result.error("文件下载失败");
        } catch (Exception e) {
            return Result.error("文件下载异常");
        }
    }

    @GetMapping("/downloadUserTemplate")
    public Result  downloadTemplate() {
        try {
            // 调用服务层下载方法
            return Result.success(inspectionTemplateService.downloadUserTemplate());//固定user-template.xlsx文件名
        } catch (RuntimeException e) {
            return Result.error("文件下载失败");
        } catch (Exception e) {
            return Result.error("文件下载异常");
        }
    }

    /**
     * 下载模板文件
     * @param request 模板地址
     */
    @PostMapping("/downloadwith")
    public Result  downloadTemplate(@RequestBody Map<String, String> request) {
        try {
            String inspectionFormUrl = request.get("inspectionFormUrl");
            System.out.println("待下载路径"+inspectionFormUrl);
            // 参数校验
            if (inspectionFormUrl == null || inspectionFormUrl.length() <= 0) {
                return Result.error("未找到文件");
            }

            // 调用服务层下载方法
            return Result.success(inspectionTemplateService.downloadwith(inspectionFormUrl));
        } catch (RuntimeException e) {
            return Result.error("文件下载失败");
        } catch (Exception e) {
            return Result.error("文件下载异常");
        }
    }

}