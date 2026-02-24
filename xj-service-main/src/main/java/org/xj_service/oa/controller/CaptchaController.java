package org.xj_service.oa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.utils.CaptchaUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/captcha")
@Tag(name = "验证码管理", description = "验证码相关接口")
public class CaptchaController {

    // 注入RedisTemplate
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private CaptchaUtil captchaUtil;

    /**
     * 获取验证码图片(返回Base64编码)
     * @param request HTTP请求对象
     * @return 包含验证码图片和key的结果
     */
    @GetMapping("/image")
    @Operation(summary = "获取验证码图片", description = "获取Base64编码的验证码图片")
    public Result getCaptchaImage(HttpServletRequest request) {
        try {
            // 生成验证码文本
            String captchaText = captchaUtil.generateCaptchaText();

            // 生成唯一标识符
            String captchaKey = UUID.randomUUID().toString();

            // 在生成验证码时使用Redis存储
            redisTemplate.opsForValue().set("captcha:" + captchaKey, captchaText, 300, TimeUnit.SECONDS);

            // 生成验证码图片
            String captchaImage = captchaUtil.generateCaptchaImage(captchaText);

            // 构造返回结果
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("captchaKey", captchaKey);
            resultData.put("captchaImage", captchaImage);

            return Result.success(resultData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "生成验证码失败");
        }
    }

    /**
     * 验证验证码
     * @param captchaKey 验证码key
     * @param captchaCode 用户输入的验证码
     * @return 验证结果
     */
    @GetMapping("/verify")
    @Operation(summary = "验证验证码", description = "验证用户输入的验证码是否正确")
    public Result verifyCaptcha(String captchaKey, String captchaCode) {
        try {
            // 在验证验证码时从Redis获取
            String redisCaptcha = redisTemplate.opsForValue().get("captcha:" + captchaKey);

            if (redisCaptcha == null) {
                return Result.error("400", "验证码已过期");
            }

            if (redisCaptcha.equalsIgnoreCase(captchaCode)) {
                // 验证成功后删除验证码
                redisTemplate.delete("captcha:" + captchaKey);
                return Result.success("验证码正确");
            } else {
                return Result.error("400", "验证码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "验证验证码失败");
        }
    }
}
