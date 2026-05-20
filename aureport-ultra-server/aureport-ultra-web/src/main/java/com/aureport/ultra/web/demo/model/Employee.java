package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工（主表）
 */
@Data
@NoArgsConstructor
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    @FieldDesc("姓名")
    private String name;

    @FieldDesc("部门")
    private String dept;

    @FieldDesc("职位")
    private String position;

    @FieldDesc("薪资")
    private Double salary;

    @FieldDesc("邮箱")
    private String email;

    @FieldDesc("年龄")
    private Integer age;

    @FieldDesc("学历")
    private String education;

    @FieldDesc("手机号")
    private String phone;

    @FieldDesc("爱好")
    private String hobbies;

    @FieldDesc("身份证号")
    private String idNumber;

    @FieldDesc("家庭成员")
    private List<FamilyMember> familyMembers = new ArrayList<>();

    public Employee(Integer id, String name, String dept, String position, Double salary, String email,
                    Integer age, String education, String phone, String hobbies, String idNumber) {
        this.id = id;
        this.name = name;
        this.dept = dept;
        this.position = position;
        this.salary = salary;
        this.email = email;
        this.age = age;
        this.education = education;
        this.phone = phone;
        this.hobbies = hobbies;
        this.idNumber = idNumber;
    }

    public void addFamilyMember(FamilyMember member) {
        this.familyMembers.add(member);
    }

    // ========== 展平的家庭成员 List 属性（供 UReport 主子表展开使用） ==========
    public List<String> getFmNames() {
        return familyMembers.stream().map(FamilyMember::getName).collect(Collectors.toList());
    }

    public List<String> getFmRelations() {
        return familyMembers.stream().map(FamilyMember::getRelation).collect(Collectors.toList());
    }

    public List<String> getFmOccupations() {
        return familyMembers.stream().map(FamilyMember::getOccupation).collect(Collectors.toList());
    }

    public List<String> getFmPhones() {
        return familyMembers.stream().map(FamilyMember::getPhone).collect(Collectors.toList());
    }

    public List<Integer> getFmAges() {
        return familyMembers.stream().map(FamilyMember::getAge).collect(Collectors.toList());
    }
}
