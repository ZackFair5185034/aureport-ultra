package com.aureport.ultra.web.demo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工（主表）
 */
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String dept;
    private String position;
    private Double salary;
    private String email;
    private Integer age;          // 年龄
    private String education;     // 学历
    private String phone;         // 手机号
    private String hobbies;       // 爱好
    private String idNumber;      // 身份证
    private List<FamilyMember> familyMembers = new ArrayList<>();

    public Employee() {}

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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public List<FamilyMember> getFamilyMembers() { return familyMembers; }
    public void setFamilyMembers(List<FamilyMember> familyMembers) { this.familyMembers = familyMembers; }

    public void addFamilyMember(FamilyMember member) {
        this.familyMembers.add(member);
    }

    // ========== 展平的家庭成员 List 属性（供 UReport 主子表展开使用） ==========
    public List<String> getFmNames() {
        return familyMembers.stream().map(FamilyMember::getName).collect(java.util.stream.Collectors.toList());
    }
    public List<String> getFmRelations() {
        return familyMembers.stream().map(FamilyMember::getRelation).collect(java.util.stream.Collectors.toList());
    }
    public List<String> getFmOccupations() {
        return familyMembers.stream().map(FamilyMember::getOccupation).collect(java.util.stream.Collectors.toList());
    }
    public List<String> getFmPhones() {
        return familyMembers.stream().map(FamilyMember::getPhone).collect(java.util.stream.Collectors.toList());
    }
    public List<Integer> getFmAges() {
        return familyMembers.stream().map(FamilyMember::getAge).collect(java.util.stream.Collectors.toList());
    }
}
