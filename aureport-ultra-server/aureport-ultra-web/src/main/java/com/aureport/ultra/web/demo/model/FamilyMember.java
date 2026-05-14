package com.aureport.ultra.web.demo.model;

import java.io.Serializable;

/**
 * 家庭成员
 */
public class FamilyMember implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String relation;  // 关系：配偶、子女、父母
    private String phone;
    private Integer age;
    private String matchingName;  // 用于与父格员工姓名匹配

    public FamilyMember() {}

    public FamilyMember(String name, String relation, String phone, Integer age) {
        this.name = name;
        this.relation = relation;
        this.phone = phone;
        this.age = age;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getMatchingName() { return matchingName; }
    public void setMatchingName(String matchingName) { this.matchingName = matchingName; }
}
