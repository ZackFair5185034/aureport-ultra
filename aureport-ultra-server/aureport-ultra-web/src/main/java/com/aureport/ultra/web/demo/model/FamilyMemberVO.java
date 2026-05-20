package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 家庭成员信息（嵌套在 EmployeeInfoVO 中）
 */
@Data
@NoArgsConstructor
public class FamilyMemberVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @FieldDesc("姓名")
    private String name;

    @FieldDesc("关系")
    private String relation;

    @FieldDesc("年龄")
    private Integer age;

    @FieldDesc("职业")
    private String occupation;

    @FieldDesc("电话")
    private String phone;

    public FamilyMemberVO(String name, String relation, Integer age, String occupation, String phone) {
        this.name = name;
        this.relation = relation;
        this.age = age;
        this.occupation = occupation;
        this.phone = phone;
    }
}
