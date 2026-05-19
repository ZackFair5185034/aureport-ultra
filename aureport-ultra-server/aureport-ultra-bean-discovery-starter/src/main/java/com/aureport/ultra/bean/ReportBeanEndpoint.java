package com.aureport.ultra.bean;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST 端点：枚举所有实现了 ReportBeanMarker 的 Spring Bean
 */
@RestController
@RequestMapping(value = "${aureport-ultra.servletPrefix}/report-beans")
@Tag(name = "报表Bean发现", description = "枚举可用 Spring Bean 数据源")
public class ReportBeanEndpoint {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ReportBeanService reportBeanService;

    @Operation(summary = "获取所有标记为报表数据源的 Spring Bean 列表")
    @GetMapping
    public List<ReportBeanInfo> listReportBeans() {
        return reportBeanService.listReportBeans(applicationContext);
    }
}
