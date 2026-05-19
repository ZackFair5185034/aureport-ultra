package com.aureport.ultra.pub.config.model;

import com.aureport.ultra.core.annotation.FieldDesc;

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

    @FieldDesc("部门编号")
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    @FieldDesc("部门名称")
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    @FieldDesc("负责人")
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
}
