package com.aureport.ultra.pub.config;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Bean 数据源示例
 *
 * 在前端配置 Spring Bean 数据源时：
 *   beanId → "reportDataService"
 *   数据集方法 → "queryEmployeeList" 或 "queryDepartmentList"
 *   返回类 → "com.aureport.ultra.pub.config.ReportDataService.Employee"
 *                  （返回类的全限定名，引擎反射识别属性作为字段）
 *
 * 方法签名固定为 (String datasourceName, String datasetName, Map<String, Object> parameters)
 * 返回 List<实际类型>，引擎自动将每项作为一行数据
 */
@Service("reportDataService")
public class ReportDataService {

    // ====== 数据模型 ======

    /**
     * 员工
     * getter/setter 由引擎通过反射识别为字段名
     */
    public static class Employee {
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

    /** 部门 */
    public static class Department {
        private String deptId;
        private String deptName;
        private String manager;

        public Department() {}

        public Department(String deptId, String deptName, String manager) {
            this.deptId = deptId;
            this.deptName = deptName;
            this.manager = manager;
        }

        public String getDeptId() { return deptId; }
        public void setDeptId(String deptId) { this.deptId = deptId; }
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }
    }

    /** 销售记录 */
    public static class SalesRecord {
        private String month;
        private Double amount;
        private Integer count;

        public SalesRecord() {}

        public SalesRecord(String month, Double amount, Integer count) {
            this.month = month;
            this.amount = amount;
            this.count = count;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    // ====== 数据集方法 ======

    /**
     * 查询员工列表
     *
     * datasourceName — 前端配置的数据源名称
     * datasetName    — 前端配置的数据集名称
     * parameters     — 报表参数（如查询条件）
     */
    public List<Employee> queryEmployeeList(String datasourceName, String datasetName, Map<String, Object> parameters) {
        String dept = (String) parameters.getOrDefault("deptId", "");

        List<Employee> list = new ArrayList<>();
        list.add(new Employee(1L, "张三", dept.isEmpty() ? "技术部" : dept, 15000.0));
        list.add(new Employee(2L, "李四", dept.isEmpty() ? "技术部" : dept, 18000.0));
        list.add(new Employee(3L, "王五", dept.isEmpty() ? "市场部" : dept, 12000.0));
        return list;
    }

    /**
     * 查询部门列表
     */
    public List<Department> queryDepartmentList(String datasourceName, String datasetName, Map<String, Object> parameters) {
        return List.of(
            new Department("D001", "技术部", "赵六"),
            new Department("D002", "市场部", "钱七"),
            new Department("D003", "财务部", "孙八")
        );
    }

    /**
     * 按日期范围查询销售数据
     * parameters 示例: { startDate: "2026-01-01", endDate: "2026-03-31" }
     */
    public List<SalesRecord> querySalesByDate(String datasourceName, String datasetName, Map<String, Object> parameters) {
        return List.of(
            new SalesRecord("1月", 45000.0, 120),
            new SalesRecord("2月", 52000.0, 135),
            new SalesRecord("3月", 48000.0, 128)
        );
    }
}
