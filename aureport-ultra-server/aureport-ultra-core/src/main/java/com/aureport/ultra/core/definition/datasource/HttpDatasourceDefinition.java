package com.aureport.ultra.core.definition.datasource;

import com.aureport.ultra.core.build.Dataset;
import com.aureport.ultra.core.definition.dataset.DatasetDefinition;
import com.aureport.ultra.core.definition.dataset.HttpDatasetDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP 数据源定义。
 * <p>
 * 支持两种协议模式：
 * <ul>
 *   <li><b>standard</b>（标准协议 / ReportBean）— 引擎按固定协议格式请求，解析规范响应 {code, data, message}</li>
 *   <li><b>thirdParty</b>（三方协议）— 每个数据集独立配置 HTTP 请求，引擎按配置执行并通过 JSONPath 解析</li>
 * </ul>
 * </p>
 */
public class HttpDatasourceDefinition implements DatasourceDefinition {

    private String name;

    /** 协议类型：standard / thirdParty */
    private String protocolType = "standard";

    /** 三方协议完整请求地址 */
    private String baseUrl;

    /** 标准协议：主机方式 manual/discovery */
    private String hostType;

    /** 标准协议：主机地址（手动模式） */
    private String host;

    /** 标准协议：服务发现服务名 */
    private String serviceName;

    /** 数据源级请求头 JSON（与数据集级请求头合并，数据集级优先级更高） */
    private String headers;

    private List<DatasetDefinition> datasets;

    public List<Dataset> buildDatasets(HttpService httpService, Map<String, Object> parameters) {
        List<Dataset> list = new ArrayList<>();
        if ("thirdParty".equals(protocolType)) {
            // 三方协议：每个数据集独立配置请求
            for (DatasetDefinition dsDef : datasets) {
                HttpDatasetDefinition httpDsDef = (HttpDatasetDefinition) dsDef;
                HttpRequestConfig config = new HttpRequestConfig();
                config.setUrl(httpDsDef.getUrl());
                config.setMethod(httpDsDef.getMethod());
                config.setHeaders(mergeHeaders(this.headers, httpDsDef.getHeaders()));
                config.setBody(httpDsDef.getBody());
                config.setResponsePath(httpDsDef.getResponsePath());
                config.setRequestParameters(httpDsDef.getRequestParameters());

                List<Map<String, Object>> data = httpService.executeThirdParty(config, parameters);
                list.add(new Dataset(httpDsDef.getName(), data));
            }
        } else {
            // 标准协议：host + urlPath，按数据集名区分
            for (DatasetDefinition dsDef : datasets) {
                HttpDatasetDefinition httpDsDef = (HttpDatasetDefinition) dsDef;
                HttpRequestConfig config = new HttpRequestConfig();
                String requestUrl = buildRequestUrl(httpDsDef.getUrl());
                config.setUrl(requestUrl);
                if ("discovery".equals(hostType)) {
                    config.setDiscoveryEnabled(true);
                }
                config.setMethod(httpDsDef.getMethod());
                config.setHeaders(mergeHeaders(this.headers, httpDsDef.getHeaders()));
                config.setRequestParameters(httpDsDef.getRequestParameters());

                List<Map<String, Object>> data = httpService.executeStandard(
                    config, name, httpDsDef.getName(), parameters);
                list.add(new Dataset(httpDsDef.getName(), data));
            }
        }
        return list;
    }

    private String buildRequestUrl(String urlPath) {
        String base;
        if ("discovery".equals(hostType) && serviceName != null && !serviceName.isBlank()) {
            base = "http://" + serviceName;
        } else if (host != null && !host.isBlank()) {
            base = host.replaceAll("/+$", "");
        } else {
            base = "";
        }
        String path = (urlPath != null) ? urlPath : "";
        if (!path.startsWith("/") && !base.isEmpty()) {
            path = "/" + path;
        }
        return base + path;
    }

    @Override
    public DatasourceType getType() { return DatasourceType.http; }

    @Override
    public List<DatasetDefinition> getDatasets() { return datasets; }
    public void setDatasets(List<DatasetDefinition> datasets) { this.datasets = datasets; }

    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProtocolType() { return protocolType; }
    public void setProtocolType(String protocolType) { this.protocolType = protocolType; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getHostType() { return hostType; }
    public void setHostType(String hostType) { this.hostType = hostType; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }

    /**
     * 合并数据源级请求头与数据集级请求头。
     * 数据集级优先级更高，同名 key 会覆盖数据源级的值。
     */
    private String mergeHeaders(String dsHeaders, String datasetHeaders) {
        if ((dsHeaders == null || dsHeaders.isBlank()) && (datasetHeaders == null || datasetHeaders.isBlank())) {
            return null;
        }
        if (dsHeaders == null || dsHeaders.isBlank()) {
            return datasetHeaders;
        }
        if (datasetHeaders == null || datasetHeaders.isBlank()) {
            return dsHeaders;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> merged = new java.util.HashMap<>();
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> dsMap = mapper.readValue(dsHeaders, java.util.Map.class);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> dtMap = mapper.readValue(datasetHeaders, java.util.Map.class);
            merged.putAll(dsMap);
            merged.putAll(dtMap);
            return mapper.writeValueAsString(merged);
        } catch (Exception e) {
            return datasetHeaders;
        }
    }
}
