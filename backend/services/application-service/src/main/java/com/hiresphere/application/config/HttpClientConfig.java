package com.hiresphere.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public OpenAPI applicationServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("HireSphere — Application Service API")
                .version("1.0.0")
                .description("Job applications, notes and status workflow."));
    }
}
