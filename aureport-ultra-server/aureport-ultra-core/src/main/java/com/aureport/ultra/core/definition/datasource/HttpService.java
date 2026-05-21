package com.aureport.ultra.core.definition.datasource;

import java.util.List;
import java.util.Map;

/**
 * HTTP 数据源执行服务接口。
 * <p>
 * 标准协议（ReportBean）：POST host/path，body 为 {datasourceName, datasetName, parameters}，
 * 期望响应 {code: 200, data: [...], message: "success"}。
 * </p>
 * <p>
 * RESTFUL 协议：按数据集独立配置 URL/Method/Headers/Body/JSONPath，
 * 响应通过 JSONPath 提取数据数组。
 * </p>
 */
public interface HttpService {

    /**
     * 执行标准协议请求
     * @param config 请求配置（URL、方法、头、请求参数）
     * @param datasourceName 数据源名称
     * @param datasetName 数据集名称
     * @param parameters 查询参数
     * @return 数据行列表
     */
    List<Map<String, Object>> executeStandard(HttpRequestConfig config,
                                              String datasourceName,
                                              String datasetName,
                                              Map<String, Object> parameters);

    /**
     * 执行三方协议请求
     * @param config 三方协议请求配置（URL、方法、头、请求体、JSONPath）
     * @param parameters 查询参数
     * @return 数据行列表
     */
    List<Map<String, Object>> executeThirdParty(HttpRequestConfig config,
                                                Map<String, Object> parameters);

    /**
     * 执行标准协议代理 GET 请求
     * @param url 完整请求 URL（含查询参数）
     * @param headers 需要转发的请求头
     * @return 原始响应字符串
     */
    String executeProxyGet(String url, Map<String, String> headers);
}
