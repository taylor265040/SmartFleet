package com.studyback.smartfleet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc/Swagger 配置类
 * <p>自定义 API 文档信息</p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * 自定义 OpenAPI 信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartFleet API")
                        .description("智能车辆租赁管理平台 API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SmartFleet Team")
                                .email("admin@smartfleet.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
