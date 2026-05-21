package com.aureport.ultra.web.demo.service;

import com.aureport.ultra.bean.ReportBean;
import com.aureport.ultra.web.demo.model.AddressInfo;
import com.aureport.ultra.web.demo.model.OfficeInfo;
import com.aureport.ultra.web.demo.model.Organization;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 嵌套属性演示服务 - 测试多层嵌套 Bean 在 Spring 树中的展开显示
 * 模型层级: Organization → OfficeInfo → AddressInfo
 * 方法签名必须为: List<T> methodName(String datasourceName, String datasetName, Map<String, Object> parameters)
 */
@Service("nestedDemoService")
@ReportBean(name = "嵌套属性演示（3层嵌套）")
public class NestedDemoService {

    /**
     * 查询组织列表，每个组织包含嵌套的 OfficeInfo 和 AddressInfo
     */
    public List<Organization> queryOrganizations(String datasourceName, String datasetName, Map<String, Object> parameters) {
        List<Organization> list = new ArrayList<>();

        // 组织1
        Organization org1 = new Organization("星辰科技有限公司", "ORG-001", "信息技术", 500);
        OfficeInfo office1 = new OfficeInfo();
        office1.setBuildingName("腾飞大厦");
        office1.setFloor("18层");
        office1.setRoomNumber("1801");
        office1.setArea("1200");
        AddressInfo addr1 = new AddressInfo();
        addr1.setProvince("广东省");
        addr1.setCity("深圳市");
        addr1.setDistrict("南山区");
        addr1.setStreet("科技南路 100 号");
        addr1.setZipCode("518057");
        office1.setAddressInfo(addr1);
        org1.setOfficeInfo(office1);
        list.add(org1);

        // 组织2
        Organization org2 = new Organization("智慧云教育集团", "ORG-002", "教育培训", 1200);
        OfficeInfo office2 = new OfficeInfo();
        office2.setBuildingName("创新中心 B 座");
        office2.setFloor("22层");
        office2.setRoomNumber("2205-2210");
        office2.setArea("2500");
        AddressInfo addr2 = new AddressInfo();
        addr2.setProvince("北京市");
        addr2.setCity("北京市");
        addr2.setDistrict("海淀区");
        addr2.setStreet("中关村大街 88 号");
        addr2.setZipCode("100080");
        office2.setAddressInfo(addr2);
        org2.setOfficeInfo(office2);
        list.add(org2);

        // 组织3
        Organization org3 = new Organization("天际物流有限公司", "ORG-003", "物流运输", 800);
        OfficeInfo office3 = new OfficeInfo();
        office3.setBuildingName("绿地中心");
        office3.setFloor("6层");
        office3.setRoomNumber("603");
        office3.setArea("800");
        AddressInfo addr3 = new AddressInfo();
        addr3.setProvince("上海市");
        addr3.setCity("上海市");
        addr3.setDistrict("浦东新区");
        addr3.setStreet("世纪大道 200 号");
        addr3.setZipCode("200120");
        office3.setAddressInfo(addr3);
        org3.setOfficeInfo(office3);
        list.add(org3);

        return list;
    }
}
