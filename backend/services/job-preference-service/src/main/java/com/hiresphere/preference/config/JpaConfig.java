package com.hiresphere.preference.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestClient;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public OpenAPI preferenceServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("HireSphere — Job Preference Service API")
                .version("1.0.0")
                .description("Saved jobs, job preferences and smart alerts."));
    }
}
