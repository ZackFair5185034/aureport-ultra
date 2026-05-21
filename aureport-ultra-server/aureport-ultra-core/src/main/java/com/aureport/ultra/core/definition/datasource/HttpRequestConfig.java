package com.aureport.ultra.core.definition.datasource;

import com.aureport.ultra.core.definition.dataset.HttpParameter;

import java.util.List;
import java.util.Map;

/**
 * HTTP 请求配置。
 * <p>
 * 标准协议: url=host+path, method, headers, requestParameters
 * RESTFUL: url, method, headers, body, responsePath, requestParameters
 * </p>
 */
public class HttpRequestConfig {

    /** 请求 URL（标准协议为 host+path，RESTFUL 为完整 URL） */
    private String url;

    /** HTTP 方法：GET / POST / PUT / DELETE */
    private String method = "GET";

    /** 请求头 JSON */
    private String headers;

    /** 请求体模板（RESTFUL POST/PUT 时使用，支持 {{paramName}} 参数模板） */
    private String body;

    /** 响应 JSONPath（RESTFUL），用于从响应中提取数据数组 */
    private String responsePath;

    /** 请求参数（URL query params） */
    private List<HttpParameter> requestParameters;

    /** 是否启用服务发现（标准协议 + discovery 模式） */
    private boolean discoveryEnabled;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getResponsePath() { return responsePath; }
    public void setResponsePath(String responsePath) { this.responsePath = responsePath; }

    public List<HttpParameter> getRequestParameters() { return requestParameters; }
    public void setRequestParameters(List<HttpParameter> requestParameters) { this.requestParameters = requestParameters; }

    public boolean isDiscoveryEnabled() { return discoveryEnabled; }
    public void setDiscoveryEnabled(boolean discoveryEnabled) { this.discoveryEnabled = discoveryEnabled; }
}
