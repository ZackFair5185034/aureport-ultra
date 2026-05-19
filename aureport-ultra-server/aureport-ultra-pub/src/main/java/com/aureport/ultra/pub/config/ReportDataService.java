package com.aureport.ultra.pub.config;

import com.aureport.ultra.pub.config.model.Department;
import com.aureport.ultra.pub.config.model.Employee;
import com.aureport.ultra.pub.config.model.SalesRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Bean 数据源示例
 *
 * 前端配置：
 *   beanId      → "reportDataService"
 *   方法        → "queryEmployeeList" / "queryDepartmentList" / "querySalesByDate"
 *   返回类      → 对应 POJO 的全限定名，如 "com.aureport.ultra.pub.config.model.Employee"
 *                 引擎通过反射识别 getter 自动获取字段列表
 *
 * 方法签名固定为 (String datasourceName, String datasetName, Map<String, Object> parameters)
 * 返回 List<T>，引擎自动将每项作为一行数据
 */
@Service("reportDataService")
public class ReportDataService {

    /**
     * 查询员工列表
     *
     * @param datasourceName 前端配置的数据源名称
     * @param datasetName    前端配置的数据集名称
     * @param parameters     报表参数，如 { deptId: "技术部" }
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
     *
     * @param parameters 如 { startDate: "2026-01-01", endDate: "2026-03-31" }
     */
    public List<SalesRecord> querySalesByDate(String datasourceName, String datasetName, Map<String, Object> parameters) {
        return List.of(
            new SalesRecord("1月", 45000.0, 120),
            new SalesRecord("2月", 52000.0, 135),
            new SalesRecord("3月", 48000.0, 128)
        );
    }
}
