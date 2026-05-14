package com.aureport.ultra.web.demo.service;

import com.aureport.ultra.web.demo.model.Employee;
import com.aureport.ultra.web.demo.model.FamilyMember;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 员工服务 - 提供主子表演示数据
 * 方法签名必须为: List<T> methodName(String datasourceName, String datasetName, Map<String, Object> parameters)
 */
@Service("employeeService")
public class EmployeeService {

    /**
     * 查询员工列表（主表）
     */
    public List<Employee> queryEmployees(String datasourceName, String datasetName, Map<String, Object> parameters) {
        List<Employee> list = new ArrayList<>();

        Employee e1 = new Employee(1, "张三", "技术部", "高级工程师", 25000.0, "zhangsan@company.com");
        e1.addFamilyMember(new FamilyMember("李红", "配偶", "13800001111", 30));
        e1.addFamilyMember(new FamilyMember("张明", "子女", "13800001112", 8));

        Employee e2 = new Employee(2, "李四", "产品部", "产品经理", 22000.0, "lisi@company.com");
        e2.addFamilyMember(new FamilyMember("王芳", "配偶", "13800002221", 28));
        e2.addFamilyMember(new FamilyMember("李华", "子女", "13800002222", 5));
        e2.addFamilyMember(new FamilyMember("李强", "父亲", "13800002223", 65));

        Employee e3 = new Employee(3, "王五", "市场部", "市场总监", 28000.0, "wangwu@company.com");
        e3.addFamilyMember(new FamilyMember("赵敏", "配偶", "13800003331", 35));

        Employee e4 = new Employee(4, "赵六", "财务部", "财务主管", 20000.0, "zhaoliu@company.com");
        e4.addFamilyMember(new FamilyMember("钱七", "配偶", "13800004441", 32));
        e4.addFamilyMember(new FamilyMember("赵雪", "母亲", "13800004442", 60));

        list.add(e1);
        list.add(e2);
        list.add(e3);
        list.add(e4);

        return list;
    }

    /**
     * 查询家庭成员列表（子表）- 通过员工姓名关联
     */
    public List<FamilyMember> queryFamilyMembers(String datasourceName, String datasetName, Map<String, Object> parameters) {
        // 注：这里返回所有家庭成员，引擎通过 leftParentCell 过滤
        // matching_name 字段用于与父格员工姓名匹配
        List<FamilyMember> list = new ArrayList<>();
        // 张三的家属
        list.add(createMember("李红", "配偶", "13800001111", 30, "张三"));
        list.add(createMember("张明", "子女", "13800001112", 8, "张三"));
        // 李四的家属
        list.add(createMember("王芳", "配偶", "13800002221", 28, "李四"));
        list.add(createMember("李华", "子女", "13800002222", 5, "李四"));
        list.add(createMember("李强", "父亲", "13800002223", 65, "李四"));
        // 王五的家属
        list.add(createMember("赵敏", "配偶", "13800003331", 35, "王五"));
        // 赵六的家属
        list.add(createMember("钱七", "配偶", "13800004441", 32, "赵六"));
        list.add(createMember("赵雪", "母亲", "13800004442", 60, "赵六"));
        return list;
    }

    private FamilyMember createMember(String name, String relation, String phone, int age, String matchingName) {
        FamilyMember m = new FamilyMember(name, relation, phone, age);
        m.setMatchingName(matchingName);
        return m;
    }
}
