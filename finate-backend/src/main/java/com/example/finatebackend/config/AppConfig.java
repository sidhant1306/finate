package com.example.finatebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {


    // we need this bean for finnhub service
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
