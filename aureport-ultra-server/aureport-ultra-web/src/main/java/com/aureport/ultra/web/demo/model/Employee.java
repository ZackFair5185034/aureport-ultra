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
    private List<FamilyMember> familyMembers = new ArrayList<>();

    public Employee() {}

    public Employee(Integer id, String name, String dept, String position, Double salary, String email) {
        this.id = id;
        this.name = name;
        this.dept = dept;
        this.position = position;
        this.salary = salary;
        this.email = email;
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

    public List<FamilyMember> getFamilyMembers() { return familyMembers; }
    public void setFamilyMembers(List<FamilyMember> familyMembers) { this.familyMembers = familyMembers; }

    public void addFamilyMember(FamilyMember member) {
        this.familyMembers.add(member);
    }
}
