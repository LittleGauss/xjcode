package org.xj_service.oa.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 */
@Configuration
public class KaptchaConfig {

    /**
     * 配置验证码生成器
     * @return DefaultKaptcha实例
     */
    @Bean
    public DefaultKaptcha producer() {
        Properties properties = new Properties();
        // 设置验证码宽度
        properties.put("kaptcha.image.width", "120");
        // 设置验证码高度
        properties.put("kaptcha.image.height", "45");
        // 设置验证码字符集
        properties.put("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 设置验证码字符长度
        properties.put("kaptcha.textproducer.char.length", "4");
        // 设置字体大小
        properties.put("kaptcha.textproducer.font.size", "30");
        // 设置字体颜色
        properties.put("kaptcha.textproducer.font.color", "black");
        // 设置字体名称
        properties.put("kaptcha.textproducer.font.names", "Arial,Courier");
        // 设置背景颜色渐变
        properties.put("kaptcha.background.clear.from", "white");
        properties.put("kaptcha.background.clear.to", "white");
        // 设置干扰线颜色
        properties.put("kaptcha.noise.color", "black");
        // 设置干扰实现类
        properties.put("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        // 设置图片样式
        properties.put("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");

        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
