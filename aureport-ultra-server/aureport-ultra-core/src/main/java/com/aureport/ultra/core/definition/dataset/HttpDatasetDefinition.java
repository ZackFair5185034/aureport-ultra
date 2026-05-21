package com.aureport.ultra.core.definition.dataset;

import java.util.List;

/**
 * HTTP 数据集定义。
 * <p>
 * 标准协议：名称、URL 路径、方法、请求头、请求参数；
 * RESTFUL：名称、URL、方法、请求头、请求体、响应 JSONPath、请求参数。
 * </p>
 */
public class HttpDatasetDefinition implements DatasetDefinition {

    private static final long serialVersionUID = 1L;

    private String name;

    /** URL（标准协议为路径，RESTFUL 为完整 URL） */
    private String url;

    /** HTTP 方法 GET/POST/PUT/DELETE */
    private String method = "GET";

    /** 请求头 JSON */
    private String headers;

    /** 请求体模板（RESTFUL 时使用） */
    private String body;

    /** 响应 JSONPath（RESTFUL 时使用） */
    private String responsePath;

    /** 请求参数 */
    private List<HttpParameter> requestParameters;

    private List<Field> fields;

    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public List<Field> getFields() { return fields; }
    public void setFields(List<Field> fields) { this.fields = fields; }

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
}
