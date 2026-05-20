package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;

import java.io.Serializable;

/**
 * 组织（顶层模型，嵌套 OfficeInfo）
 */
public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String orgCode;
    private String industry;
    private Integer employeeCount;
    private OfficeInfo officeInfo;

    public Organization() {}

    public Organization(String name, String orgCode, String industry, Integer employeeCount) {
        this.name = name;
        this.orgCode = orgCode;
        this.industry = industry;
        this.employeeCount = employeeCount;
    }

    @FieldDesc("组织名称")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @FieldDesc("组织编码")
    public String getOrgCode() { return orgCode; }
    public void setOrgCode(String orgCode) { this.orgCode = orgCode; }

    @FieldDesc("所属行业")
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    @FieldDesc("员工数量")
    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }

    @FieldDesc("办公信息")
    public OfficeInfo getOfficeInfo() { return officeInfo; }
    public void setOfficeInfo(OfficeInfo officeInfo) { this.officeInfo = officeInfo; }
}
