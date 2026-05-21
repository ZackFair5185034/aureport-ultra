package com.aureport.ultra.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 服务发现端点：查询 Nacos 注册的所有可用服务。
 * <p>
 * 前端配置标准协议数据源时，选择服务发现模式后，
 * 通过此接口获取服务名下拉列表。
 * </p>
 */
@RestController
@RequestMapping(value = "${aureport-ultra.servletPrefix}/services")
public class ServiceDiscoveryEndpoint {

    @Autowired(required = false)
    private DiscoveryClient discoveryClient;

    @GetMapping
    public List<String> listServices() {
        if (discoveryClient == null) {
            return List.of();
        }
        return discoveryClient.getServices();
    }
}
