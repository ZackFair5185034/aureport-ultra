package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 组织（顶层模型，嵌套 OfficeInfo）
 */
@Data
@NoArgsConstructor
public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;

    @FieldDesc("组织名称")
    private String name;

    @FieldDesc("组织编码")
    private String orgCode;

    @FieldDesc("所属行业")
    private String industry;

    @FieldDesc("员工数量")
    private Integer employeeCount;

    @FieldDesc("办公信息")
    private OfficeInfo officeInfo;

    public Organization(String name, String orgCode, String industry, Integer employeeCount) {
        this.name = name;
        this.orgCode = orgCode;
        this.industry = industry;
        this.employeeCount = employeeCount;
    }
}
