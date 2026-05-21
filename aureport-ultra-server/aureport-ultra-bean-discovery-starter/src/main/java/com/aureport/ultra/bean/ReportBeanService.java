package com.aureport.ultra.bean;

import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 扫描 Spring 容器中可作为报表数据源使用的 Bean。
 * <p>
 * 支持两种发现机制（按优先级）：
 * <ol>
 *   <li>标注了 {@link ReportBean} 注解的 Bean</li>
 *   <li>通过 {@link ReportBeanProperties} 配置文件声明的 Bean</li>
 * </ol>
 * </p>
 */
public class ReportBeanService {

    public List<ReportBeanInfo> listReportBeans(ApplicationContext ctx) {
        List<ReportBeanInfo> result = new ArrayList<>();

        // @ReportBean 注解
        Map<String, Object> annotatedBeans = ctx.getBeansWithAnnotation(ReportBean.class);
        for (Map.Entry<String, Object> entry : annotatedBeans.entrySet()) {
            String beanId = entry.getKey();
            Object bean = entry.getValue();
            ReportBean ann = ctx.findAnnotationOnBean(beanId, ReportBean.class);
            String name = (ann != null && !ann.name().isBlank()) ? ann.name() : beanId;
            result.add(new ReportBeanInfo(beanId, name, bean.getClass().getName()));
        }

        return result;
    }

    public List<ReportBeanInfo> listConfiguredBeans(ReportBeanProperties properties) {
        if (properties == null || properties.getReportBeans() == null) {
            return List.of();
        }
        return properties.getReportBeans().stream()
            .map(cfg -> new ReportBeanInfo(cfg.getBeanId(), cfg.getName(), ""))
            .toList();
    }
}
