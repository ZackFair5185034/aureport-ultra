package com.aureport.ultra.core.config;

import com.aureport.ultra.core.export.ExportManagerImpl;
import com.aureport.ultra.core.export.ReportRender;
import com.aureport.ultra.core.export.pdf.font.FontBuilder;
import com.aureport.ultra.core.parser.ReportParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExportConfiguration {

    @Bean
    public ExportManagerImpl exportManager(ReportRender reportRender) {
        ExportManagerImpl exportManager = new ExportManagerImpl();
        exportManager.setReportRender(reportRender);
        return exportManager;
    }

    @Bean
    public ReportRender reportRender(ReportParser reportParser,
                                     com.aureport.ultra.core.build.ReportBuilder reportBuilder) {
        ReportRender reportRender = new ReportRender();
        reportRender.setReportParser(reportParser);
        reportRender.setReportBuilder(reportBuilder);
        return reportRender;
    }

    @Bean
    public FontBuilder fontBuilder() {
        return new FontBuilder();
    }
}
