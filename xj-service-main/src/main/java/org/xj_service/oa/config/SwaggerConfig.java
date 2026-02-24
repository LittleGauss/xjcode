package org.xj_service.oa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Swagger3的接口配置
 *
 * @author du
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()  // 创建文档信息对象
                        .title("Spring Boot 中使用 Swagger UI 构建 RESTful API")   // 设置文档标题
                        .version("1.0.0")    // 设置API版本号
                        .description("纤监平台接口文档")  // 添加详细描述
                        .contact(new Contact()  // 设置联系人信息
                                .name("纤监")     // 联系人姓名
                                .email("666@example.com"))  // 联系人邮箱
                        .license(new License()  // 设置许可证信息
                                .name("Apache 2.0")  // 许可证名称
                                .url("https://springdoc.org")));  // 许可证详情URL
    }

}

