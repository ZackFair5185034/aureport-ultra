package com.aureport.ultra.web.demo;

import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.definition.dataset.BeanDatasetDefinition;
import com.aureport.ultra.core.build.Dataset;
import com.aureport.ultra.web.demo.model.Employee;
import com.aureport.ultra.web.demo.model.FamilyMember;

import java.util.*;

/**
 * 手动测试 Bean 数据集
 */
public class BeanDatasetTest {

    public static void main(String[] args) throws Exception {
        // 1. 模拟 XML 解析出的 BeanDatasetDefinition
        BeanDatasetDefinition beanDef = new BeanDatasetDefinition();
        beanDef.setName("employees");
        beanDef.setMethod("queryEmployees");
        beanDef.setClazz("com.aureport.ultra.web.demo.model.Employee");

        // 2. 模拟 EmployeeService 的 queryEmployees 方法
        List<Employee> employeeList = buildTestData();
        System.out.println("构建了 " + employeeList.size() + " 个员工");

        // 3. 直接调用 buildDataset 逻辑（模拟）
        Map<String, Object> params = new HashMap<>();

        // 手动构建 Dataset
        Dataset ds = new Dataset("employees", (List<Object>) (List<?>) employeeList);
        List<?> data = ds.getData();
        System.out.println("数据集大小: " + data.size());

        Object firstEmployee = data.get(0);

        // 4. 基础属性
        System.out.println("\n=== 基础属性 ===");
        System.out.println("name = " + Utils.getProperty(firstEmployee, "name"));
        System.out.println("dept = " + Utils.getProperty(firstEmployee, "dept"));
        System.out.println("position = " + Utils.getProperty(firstEmployee, "position"));

        // 5. 嵌套属性（点号）
        System.out.println("\n=== 嵌套属性（点号）===");
        System.out.println("familyMembers (List) = " + Utils.getProperty(firstEmployee, "familyMembers"));
        // familyMembers.relation 会被 Apache BeanUtils 解释为 getFamilyMembers().getRelation()，
        // 而 Employee.relation 字段是 String（非 List），所以会失败
        // 但这里 Employee 没有 getRelation 方法，会报错
        // 正确的做法是用 familyMembers[0].name 这种带索引的路径

        // 6. 带数组下标的嵌套属性（验证新功能）
        System.out.println("\n=== 带数组下标的嵌套属性 ===");
        System.out.println("familyMembers[0].name = " + Utils.getProperty(firstEmployee, "familyMembers[0].name"));
        System.out.println("familyMembers[0].relation = " + Utils.getProperty(firstEmployee, "familyMembers[0].relation"));
        System.out.println("familyMembers[0].phone = " + Utils.getProperty(firstEmployee, "familyMembers[0].phone"));
        System.out.println("familyMembers[1].name = " + Utils.getProperty(firstEmployee, "familyMembers[1].name"));

        // 7. 多级索引
        System.out.println("\n=== 多级索引 ===");
        Object secondEmployee = data.get(1);
        System.out.println("员工2: " + Utils.getProperty(secondEmployee, "name"));
        System.out.println("familyMembers[2].name = " + Utils.getProperty(secondEmployee, "familyMembers[2].name"));
        System.out.println("familyMembers[2].relation = " + Utils.getProperty(secondEmployee, "familyMembers[2].relation"));

        System.out.println("\n✅ Utils.getProperty() 数组下标支持验证完成");
    }

    private static List<Employee> buildTestData() {
        List<Employee> list = new ArrayList<>();

        Employee e1 = new Employee(1, "张三", "技术部", "高级工程师", 25000.0, "zhangsan@company.com");
        e1.addFamilyMember(new FamilyMember("李红", "配偶", "13800001111", 30));
        e1.addFamilyMember(new FamilyMember("张明", "子女", "13800001112", 8));

        Employee e2 = new Employee(2, "李四", "产品部", "产品经理", 22000.0, "lisi@company.com");
        e2.addFamilyMember(new FamilyMember("王芳", "配偶", "13800002221", 28));
        e2.addFamilyMember(new FamilyMember("李华", "子女", "13800002222", 5));
        e2.addFamilyMember(new FamilyMember("李强", "父亲", "13800002223", 65));

        list.add(e1);
        list.add(e2);
        return list;
    }
}
