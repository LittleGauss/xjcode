package org.xj_service.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.SignRecord;
import org.xj_service.oa.service.ISignRecordService;
import org.xj_service.oa.service.FileStorageService;
import org.xj_service.oa.service.IUserService;

@RestController
@RequestMapping("/sign-record")
@Tag(name = "签名管理", description = "电子签名记录管理接口")
public class SignRecordController {

    @Resource
    private ISignRecordService signRecordService;

    @Resource
    private IUserService userService;

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 上传签名
     */
    @PostMapping("/upload")
    @Operation(summary = "上传签名", description = "上传用户签名图片")
    public Result uploadSignature(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "ext1", required = false) String ext1,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            // 保存文件到MinIO
            String filePath = fileStorageService.upload(file, "signature", "oa-signature");

            // 创建签名记录
            SignRecord signRecord = new SignRecord();
            signRecord.setUserId(userId);
            signRecord.setSignUrl(filePath);
            signRecord.setCreateTime(LocalDateTime.now());
            signRecord.setExt1(ext1);

            // 保存记录
            signRecordService.save(signRecord);

            return Result.success("签名上传成功");
        } catch (Exception e) {
            return Result.error("500", "签名上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除签名
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除签名", description = "根据ID删除签名记录")
    public Result deleteSignature(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        SignRecord signRecord = signRecordService.getById(id);
        if (signRecord == null) {
            return Result.error("404", "签名记录不存在");
        }

        if (!signRecord.getUserId().equals(userId)) {
            return Result.error("403", "无权删除他人的签名");
        }

        // 删除MinIO中的文件
        fileStorageService.delete(signRecord.getSignUrl());

        // 删除数据库记录
        signRecordService.removeById(id);

        return Result.success("签名删除成功");
    }

    /**
     * 分页查询所有签名记录
     */
    /**
     * 分页查询所有签名记录（适配前端分页查询）
     */
    @GetMapping("/listAll")
    @Operation(summary = "分页查询所有签名", description = "分页查询用户的签名记录")
    public Result listAllSignatures(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId) {
        Page<SignRecord> result = signRecordService.listSignRecords(pageNum, pageSize, userId);
        return Result.success(result);
    }

    /**
     * 获取当前用户的最新签名
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新签名", description = "获取当前用户的最新签名")
    public Result getLatestSignature(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        QueryWrapper<SignRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");

        SignRecord latestSign = signRecordService.getOne(queryWrapper);
        return Result.success(latestSign);
    }

    /**
     * 获取图片预签名URL
     */
    @GetMapping("/getImageUrl")
    @Operation(summary = "获取图片预签名URL", description = "根据文件路径生成图片的预签名访问URL")
    public Result getImageUrl(@RequestParam String filePath) {
        try {
            // 通过 fileStorageService 生成预签名URL
            String presignedUrl = fileStorageService.getTempUrl(filePath,3600);
            return Result.success(presignedUrl);
        } catch (Exception e) {
            return Result.error("500", "获取图片URL失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取当前用户ID（需要根据实际认证方式调整） 公共方法，请求头里面有用户id的token的
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                String userIdStr = com.auth0.jwt.JWT.decode(token).getAudience().get(0);
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        return Long.valueOf(userIdStr);
                    } catch (Exception ignore) {
                        return null;
                    }
                }
            }
        } catch (Exception ignore) {
            return null;
        }
        return null;
    }
}
