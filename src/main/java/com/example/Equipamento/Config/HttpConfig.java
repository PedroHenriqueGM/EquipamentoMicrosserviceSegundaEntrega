package com.example.Equipamento.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpConfig {
    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
