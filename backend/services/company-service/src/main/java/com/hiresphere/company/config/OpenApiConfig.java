package com.hiresphere.company.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI companyServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("HireSphere — Company Service API")
                .version("1.0.0")
                .description("Company profiles, members and verification."));
    }
}
