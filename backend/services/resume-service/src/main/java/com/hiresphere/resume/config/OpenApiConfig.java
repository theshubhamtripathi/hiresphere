package com.hiresphere.resume.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resumeServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("HireSphere — Resume Service API")
                .version("1.0.0")
                .description("Resume builder with sections, completion score and default resume."));
    }
}
