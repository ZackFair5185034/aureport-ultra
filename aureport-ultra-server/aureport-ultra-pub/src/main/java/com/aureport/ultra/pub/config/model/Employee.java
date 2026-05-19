package com.aureport.ultra.pub.config.model;

public class Employee {
    private Long id;
    private String name;
    private String dept;
    private Double salary;

    public Employee() {}

    public Employee(Long id, String name, String dept, Double salary) {
        this.id = id;
        this.name = name;
        this.dept = dept;
        this.salary = salary;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
}
