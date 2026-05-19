package com.aureport.ultra.bean;

import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 扫描所有实现了 ReportBeanMarker 接口的 Spring Bean
 */
public class ReportBeanService {

    public List<ReportBeanInfo> listReportBeans(ApplicationContext ctx) {
        Map<String, ReportBeanMarker> beans = ctx.getBeansOfType(ReportBeanMarker.class);
        return beans.entrySet().stream().map(entry -> {
            ReportBeanMarker bean = entry.getValue();
            String name = bean.name();
            return new ReportBeanInfo(
                entry.getKey(),
                name != null ? name : entry.getKey(),
                bean.getClass().getName()
            );
        }).collect(Collectors.toList());
    }
}
