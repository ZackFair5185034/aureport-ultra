package com.aureport.ultra.bean;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot 自动配置：注册 ReportBeanService 和 ReportBeanEndpoint
 */
@AutoConfiguration
public class ReportBeanDiscoveryAutoConfig {

    @Bean
    public ReportBeanService reportBeanService() {
        return new ReportBeanService();
    }
}
