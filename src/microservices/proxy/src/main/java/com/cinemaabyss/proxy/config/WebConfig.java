package com.cinemaabyss.proxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for web components.
 * Configures RestTemplate with appropriate timeouts and settings.
 */
@Configuration
public class WebConfig {

    /**
     * Creates a RestTemplate bean with appropriate configuration for proxy
     * operations.
     */
    @Bean
    public RestTemplate restTemplate() {
        var clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(3_000);
        clientHttpRequestFactory.setReadTimeout(3_000);
        return new RestTemplate(clientHttpRequestFactory);
    }
}
