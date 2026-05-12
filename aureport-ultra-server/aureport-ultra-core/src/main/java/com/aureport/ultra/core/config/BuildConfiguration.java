package com.aureport.ultra.core.config;

import com.aureport.ultra.core.build.HideRowColumnBuilder;
import com.aureport.ultra.core.build.ReportBuilder;
import com.aureport.ultra.core.parser.ReportParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BuildConfiguration {

    @Bean
    public ReportBuilder reportBuilder(HideRowColumnBuilder hideRowColumnBuilder) {
        ReportBuilder reportBuilder = new ReportBuilder();
        reportBuilder.setHideRowColumnBuilder(hideRowColumnBuilder);
        return reportBuilder;
    }

    @Bean
    public HideRowColumnBuilder hideRowColumnBuilder() {
        return new HideRowColumnBuilder();
    }

    @Bean
    public ReportParser reportParser() {
        return new ReportParser();
    }
}
