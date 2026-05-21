package com.aureport.ultra.web.demo.service;

import com.aureport.ultra.bean.ReportBean;
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
@ReportBean(name = "员工服务（主子表演示）")
public class EmployeeService {

    /**
     * 查询员工列表（主表）
     */
    public List<Employee> queryEmployees(String datasourceName, String datasetName, Map<String, Object> parameters) {
        List<Employee> list = new ArrayList<>();

        Employee e1 = new Employee(1, "张三", "技术部", "高级工程师", 25000.0, "zhangsan@company.com",
                28, "本科", "13800001110", "篮球、编程、摄影", "110101199801011234");
        e1.addFamilyMember(new FamilyMember("李红", "配偶", "13800001111", 30, "教师"));
        e1.addFamilyMember(new FamilyMember("张明", "子女", "13800001112", 8, "学生"));

        Employee e2 = new Employee(2, "李四", "产品部", "产品经理", 22000.0, "lisi@company.com",
                32, "硕士", "13900002220", "游泳、阅读、旅行", "110101199201012345");
        e2.addFamilyMember(new FamilyMember("王芳", "配偶", "13800002221", 28, "医生"));
        e2.addFamilyMember(new FamilyMember("李华", "子女", "13800002222", 5, "学生"));
        e2.addFamilyMember(new FamilyMember("李强", "父亲", "13800002223", 65, "退休"));

        Employee e3 = new Employee(3, "王五", "市场部", "市场总监", 28000.0, "wangwu@company.com",
                35, "MBA", "13700003330", "滑雪、摄影、茶道", "110101199003014567");
        e3.addFamilyMember(new FamilyMember("赵敏", "配偶", "13800003331", 35, "律师"));

        Employee e4 = new Employee(4, "赵六", "财务部", "财务主管", 20000.0, "zhaoliu@company.com",
                30, "本科", "13600004440", "瑜伽、烘焙", "110101199204016789");
        e4.addFamilyMember(new FamilyMember("钱七", "配偶", "13800004441", 32, "护士"));
        e4.addFamilyMember(new FamilyMember("赵雪", "母亲", "13800004442", 60, "退休教师"));

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
        list.add(createMember("李红", "配偶", "13800001111", 30, "教师", "张三"));
        list.add(createMember("张明", "子女", "13800001112", 8, "学生", "张三"));
        // 李四的家属
        list.add(createMember("王芳", "配偶", "13800002221", 28, "医生", "李四"));
        list.add(createMember("李华", "子女", "13800002222", 5, "学生", "李四"));
        list.add(createMember("李强", "父亲", "13800002223", 65, "退休", "李四"));
        // 王五的家属
        list.add(createMember("赵敏", "配偶", "13800003331", 35, "律师", "王五"));
        // 赵六的家属
        list.add(createMember("钱七", "配偶", "13800004441", 32, "护士", "赵六"));
        list.add(createMember("赵雪", "母亲", "13800004442", 60, "退休教师", "赵六"));
        return list;
    }

    private FamilyMember createMember(String name, String relation, String phone, int age, String occupation, String matchingName) {
        FamilyMember m = new FamilyMember(name, relation, phone, age, occupation);
        m.setMatchingName(matchingName);
        return m;
    }

    /**
     * 扁平化主子表数据 - 员工+家属一行展示
     * 每行 = 员工信息 + 家属信息，适用于 iterate 聚合不支持的场景
     */
    public List<Map<String, Object>> queryEmployeeFamilyFlat(String datasourceName, String datasetName, Map<String, Object> parameters) {
        List<Employee> employees = queryEmployees(datasourceName, datasetName, parameters);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Employee emp : employees) {
            for (FamilyMember fm : emp.getFamilyMembers()) {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                // 员工信息
                row.put("emp_name", emp.getName());
                row.put("emp_age", emp.getAge());
                row.put("emp_education", emp.getEducation());
                row.put("emp_phone", emp.getPhone());
                row.put("emp_hobbies", emp.getHobbies());
                row.put("emp_id_number", emp.getIdNumber());
                row.put("emp_dept", emp.getDept());
                row.put("emp_position", emp.getPosition());
                // 家属信息
                row.put("fm_name", fm.getName());
                row.put("fm_relation", fm.getRelation());
                row.put("fm_occupation", fm.getOccupation());
                row.put("fm_phone", fm.getPhone());
                row.put("fm_age", fm.getAge());
                result.add(row);
            }
        }
        return result;
    }
}
