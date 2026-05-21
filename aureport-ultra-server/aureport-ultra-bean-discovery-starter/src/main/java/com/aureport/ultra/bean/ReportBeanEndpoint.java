package com.aureport.ultra.bean;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST 端点：枚举所有可作为报表数据源的 Spring Bean。
 * <p>
 * 支持三种发现机制：
 * <ul>
 *   <li>实现 {@link ReportBeanMarker} 接口</li>
 *   <li>标注 {@link ReportBean} 注解</li>
 *   <li>通过 {@link ReportBeanProperties} 配置文件声明</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "${aureport-ultra.servletPrefix}/report-beans")
@Tag(name = "报表Bean发现", description = "枚举可用 Spring Bean 数据源")
public class ReportBeanEndpoint {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ReportBeanService reportBeanService;

    @Autowired(required = false)
    private ReportBeanProperties reportBeanProperties;

    @Operation(summary = "获取所有标记为报表数据源的 Spring Bean 列表")
    @GetMapping
    public List<ReportBeanInfo> listReportBeans() {
        List<ReportBeanInfo> result = new ArrayList<>();
        // 1+2: 接口 + 注解 发现的 Bean
        result.addAll(reportBeanService.listReportBeans(applicationContext));
        // 3: 配置文件声明的 Bean
        result.addAll(reportBeanService.listConfiguredBeans(reportBeanProperties));
        return result;
    }
}
