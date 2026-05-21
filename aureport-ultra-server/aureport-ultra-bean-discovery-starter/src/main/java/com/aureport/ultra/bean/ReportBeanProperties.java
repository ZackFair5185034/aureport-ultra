package com.aureport.ultra.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件方式声明的报表数据源 Bean。
 * <p>
 * 在 application.yml 中配置：
 * <pre>{@code
 * aureport-ultra:
 *   report-beans:
 *     - name: 员工服务
 *       bean-id: employeeService
 *     - name: 订单服务
 *       bean-id: orderService
 * }</pre>
 * </p>
 */
@ConfigurationProperties(prefix = "aureport-ultra")
public class ReportBeanProperties {

    private List<ReportBeanConfig> reportBeans = new ArrayList<>();

    public List<ReportBeanConfig> getReportBeans() { return reportBeans; }
    public void setReportBeans(List<ReportBeanConfig> reportBeans) { this.reportBeans = reportBeans; }

    public static class ReportBeanConfig {
        private String name;
        private String beanId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getBeanId() { return beanId; }
        public void setBeanId(String beanId) { this.beanId = beanId; }
    }
}
