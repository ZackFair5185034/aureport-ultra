package com.aureport.ultra.bean;

import java.lang.annotation.*;

/**
 * 声明一个 Spring Bean 可作为报表数据源使用。
 * <p>
 * 替代 {@link ReportBeanMarker} 接口方式，通过注解声明：
 * <pre>{@code
 * @ReportBean(name = "员工服务")
 * @Service
 * public class EmployeeService {
 *     public List<Employee> queryEmployees(String dsName, String datasetName, Map<String, Object> params) { ... }
 * }
 * }</pre>
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReportBean {

    /** 前端显示名称，默认使用 Spring Bean 名称 */
    String name() default "";

    /** 描述信息 */
    String description() default "";
}
