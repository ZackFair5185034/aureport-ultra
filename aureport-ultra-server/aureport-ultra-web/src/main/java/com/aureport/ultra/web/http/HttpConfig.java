package com.aureport.ultra.web.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端配置。
 * <p>
 * 提供 RestTemplate Bean 供 HttpServiceImpl 使用。
 * 服务发现能力由 aureport-ultra-cloud-starter 提供。
 * </p>
 */
@Configuration
public class HttpConfig {

    @Bean
    public RestTemplate aureportRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler());
        return restTemplate;
    }
}
