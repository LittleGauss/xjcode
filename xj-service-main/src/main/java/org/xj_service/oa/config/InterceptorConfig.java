package org.xj_service.oa.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.xj_service.oa.config.interceptor.JwtInterceptor;
import org.xj_service.oa.config.interceptor.PermissionInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigInteger;
import java.util.List;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT认证拦截器
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login","/user/register","/**/export","/**/import","/captcha/**","/department/list")
                .excludePathPatterns("/**/swagger-ui.html", "/**/v3/api-docs/**", "/**/swagger-ui/**");
        // 角色权限拦截器
        registry.addInterceptor(permissionInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login","/user/register","/**/export","/**/import","/captcha/**")
                        .excludePathPatterns("/**/swagger-ui.html", "/**/v3/api-docs/**", "/**/swagger-ui/**");
    }

    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }

    @Bean
    public PermissionInterceptor permissionInterceptor() {
        return new PermissionInterceptor();
    }

//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
//        SimpleModule simpleModule = new SimpleModule();
//        //主要是由于后端long类型传递到前端js后 精度丢失，后二位会自动变为0
//        // 核心代码：将 Long 和 long 类型在序列化时，转换为字符串
//        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
//        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
//
//        // 可选：如果使用了 BigInteger 也需要转换
//        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
//
//        jacksonConverter.getObjectMapper().registerModule(simpleModule);
//
//        // 确保你的自定义转换器位于默认转换器之前或替换它
//        converters.add(0, jacksonConverter);
//    }
}
