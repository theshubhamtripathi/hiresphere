package com.hiresphere.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false"
        })
class ConfigServerApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    void servesServiceConfigFromNativeRepo() {
        String body = RestClient.create()
                .get()
                .uri("http://localhost:{port}/user-service/default", port)
                .retrieve()
                .body(String.class);

        assertThat(body).contains("user_db");
    }
}
