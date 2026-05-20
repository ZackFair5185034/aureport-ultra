package com.aureport.ultra.web.demo.service;

import com.aureport.ultra.bean.ReportBeanMarker;
import com.aureport.ultra.web.demo.model.EmployeeInfoVO;
import com.aureport.ultra.web.demo.model.FamilyMemberVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 员工信息服务 - 返回单个员工 VO（含嵌套的家庭成员列表）
 */
@Service("employeeInfoService")
public class EmployeeInfoService implements ReportBeanMarker {

    @Override
    public String name() {
        return "员工信息（1对多嵌套演示）";
    }

    /**
     * 查询单个员工信息，返回一个 EmployeeInfoVO（包含基本信息和嵌套的家庭成员列表）
     */
    public EmployeeInfoVO queryEmployeeInfo(String datasourceName, String datasetName, Map<String, Object> parameters) {
        List<FamilyMemberVO> members = new ArrayList<>();
        members.add(new FamilyMemberVO("李红", "配偶", 30, "教师", "13800001111"));
        members.add(new FamilyMemberVO("张明", "子女", 8, "学生", "13800001112"));
        members.add(new FamilyMemberVO("张建国", "父亲", 65, "退休", "13800001113"));

        return new EmployeeInfoVO(1L, "张三", "男", 35, "技术部", "高级工程师",
                "13800001110", "zhangsan@company.com", 2018, members);
    }
}
