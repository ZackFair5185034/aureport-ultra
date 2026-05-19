package com.aureport.ultra.pub.config.model;

public class Department {
    private String deptId;
    private String deptName;
    private String manager;

    public Department() {}

    public Department(String deptId, String deptName, String manager) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.manager = manager;
    }

    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
}
