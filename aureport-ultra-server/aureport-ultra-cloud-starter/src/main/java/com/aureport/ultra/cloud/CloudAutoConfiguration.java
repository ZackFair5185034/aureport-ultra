package com.aureport.ultra.cloud;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring Cloud 自动配置。
 * <p>
 * 当 classpath 中存在 Nacos 服务发现时，提供 @LoadBalanced RestTemplate
 * 供 HttpServiceImpl 使用，实现 http://serviceName 自动解析到实际地址。
 * </p>
 * <p>
 * 引入此模块即启用服务发现能力，不引入则降级为直连。
 * </p>
 */
@Configuration
public class CloudAutoConfiguration {

    @Bean
    @LoadBalanced
    public RestTemplate aureportDiscoveryRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler());
        return restTemplate;
    }
}
