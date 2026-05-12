package com.aureport.ultra.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 报表核心配置
 * @author 24731
 */
@Configuration
@Import({
        ExportConfiguration.class,
        ProviderConfiguration.class,
        BuildConfiguration.class,
        FormParserConfiguration.class,
        UtilsConfiguration.class,
        FunctionConfiguration.class
})
@ComponentScan(basePackages = "com.aureport.ultra.core")
public class ReportCoreConfiguration {
}
