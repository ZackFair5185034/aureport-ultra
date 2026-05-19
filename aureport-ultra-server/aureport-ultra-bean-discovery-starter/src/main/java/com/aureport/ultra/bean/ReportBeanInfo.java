package com.aureport.ultra.bean;

/**
 * 报表 Bean 信息 DTO
 */
public class ReportBeanInfo {
    private String beanId;
    private String name;
    private String className;

    public ReportBeanInfo() {
    }

    public ReportBeanInfo(String beanId, String name, String className) {
        this.beanId = beanId;
        this.name = name;
        this.className = className;
    }

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
