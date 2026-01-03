package com.vgerbot.app.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger/OpenAPI 配置类
 * 
 * 配置 API 文档的基本信息和安全认证方式（JWT）
 * 
 * 访问地址（注意：项目配置了 context-path: /api）：
 * - Swagger UI: http://localhost:8082/swagger-ui/index.html
 * - OpenAPI JSON: http://localhost:8082/v3/api-docs
 */
@Configuration
class SwaggerConfiguration {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Spring Boot Ktorm API Documentation")
                    .description("Spring Boot + Ktorm 项目 API 文档")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("API Support")
                            .email("support@example.com")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                    )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearer-jwt",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .`in`(SecurityScheme.In.HEADER)
                            .name("Authorization")
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList("bearer-jwt")
            )
    }
}

