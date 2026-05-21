package com.aureport.ultra.bean;

import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 扫描 Spring 容器中可作为报表数据源使用的 Bean。
 * <p>
 * 支持三种发现机制（按优先级）：
 * <ol>
 *   <li>实现了 {@link ReportBeanMarker} 接口的 Bean</li>
 *   <li>标注了 {@link ReportBean} 注解的 Bean</li>
 *   <li>通过 {@link ReportBeanProperties} 配置文件声明的 Bean</li>
 * </ol>
 * </p>
 */
public class ReportBeanService {

    public List<ReportBeanInfo> listReportBeans(ApplicationContext ctx) {
        Set<String> seen = new LinkedHashSet<>();
        List<ReportBeanInfo> result = new ArrayList<>();

        // 1. ReportBeanMarker 接口
        Map<String, ReportBeanMarker> markerBeans = ctx.getBeansOfType(ReportBeanMarker.class);
        for (Map.Entry<String, ReportBeanMarker> entry : markerBeans.entrySet()) {
            String beanId = entry.getKey();
            ReportBeanMarker bean = entry.getValue();
            String name = bean.name();
            result.add(new ReportBeanInfo(
                beanId,
                name != null ? name : beanId,
                bean.getClass().getName()
            ));
            seen.add(beanId);
        }

        // 2. @ReportBean 注解
        Map<String, Object> annotatedBeans = ctx.getBeansWithAnnotation(ReportBean.class);
        for (Map.Entry<String, Object> entry : annotatedBeans.entrySet()) {
            String beanId = entry.getKey();
            if (seen.contains(beanId)) continue;
            Object bean = entry.getValue();
            ReportBean ann = ctx.findAnnotationOnBean(beanId, ReportBean.class);
            String name = (ann != null && !ann.name().isBlank()) ? ann.name() : beanId;
            result.add(new ReportBeanInfo(beanId, name, bean.getClass().getName()));
            seen.add(beanId);
        }

        // 3. 配置文件声明
        // ReportBeanProperties 中的 beans 在 AutoConfig 中注册
        return result;
    }

    public List<ReportBeanInfo> listConfiguredBeans(ReportBeanProperties properties) {
        if (properties == null || properties.getReportBeans() == null) {
            return List.of();
        }
        return properties.getReportBeans().stream()
            .map(cfg -> new ReportBeanInfo(cfg.getBeanId(), cfg.getName(), ""))
            .collect(Collectors.toList());
    }
}
