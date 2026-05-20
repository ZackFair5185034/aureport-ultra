package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 员工信息 VO（包含基本信息和嵌套的家庭成员列表）
 */
@Data
@NoArgsConstructor
public class EmployeeInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @FieldDesc("员工ID")
    private Long id;

    @FieldDesc("姓名")
    private String name;

    @FieldDesc("性别")
    private String gender;

    @FieldDesc("年龄")
    private Integer age;

    @FieldDesc("部门")
    private String department;

    @FieldDesc("职位")
    private String position;

    @FieldDesc("手机号")
    private String phone;

    @FieldDesc("邮箱")
    private String email;

    @FieldDesc("入职年份")
    private Integer entryYear;

    @FieldDesc("家庭成员")
    private List<FamilyMemberVO> familyMembers;

    public EmployeeInfoVO(Long id, String name, String gender, Integer age, String department,
                          String position, String phone, String email, Integer entryYear,
                          List<FamilyMemberVO> familyMembers) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.department = department;
        this.position = position;
        this.phone = phone;
        this.email = email;
        this.entryYear = entryYear;
        this.familyMembers = familyMembers;
    }
}
