package com.aureport.ultra.bean;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot 自动配置：注册 ReportBeanService、ReportBeanEndpoint 和配置属性绑定。
 */
@AutoConfiguration
@EnableConfigurationProperties(ReportBeanProperties.class)
public class ReportBeanDiscoveryAutoConfig {

    @Bean
    public ReportBeanService reportBeanService() {
        return new ReportBeanService();
    }
}
