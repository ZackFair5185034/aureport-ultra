package com.aureport.ultra.bean;

/**
 * 标记接口：实现此接口的 Spring Bean 可被报表引擎识别为可用数据源 Bean。
 * 前端下拉列表将展示实现了此接口的所有 Bean。
 */
public interface ReportBeanMarker {
    /**
     * 前端显示的 Bean 名称。
     * 若返回 null，默认使用 Spring Bean 的 beanId。
     */
    default String name() {
        return null;
    }
}
