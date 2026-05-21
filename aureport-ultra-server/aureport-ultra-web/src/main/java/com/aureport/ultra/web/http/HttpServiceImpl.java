package com.aureport.ultra.web.http;

import com.aureport.ultra.core.definition.dataset.HttpParameter;
import com.aureport.ultra.core.definition.datasource.HttpRequestConfig;
import com.aureport.ultra.core.definition.datasource.HttpService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * HTTP 数据源执行服务实现。
 * <p>
 * 使用 RestTemplate 执行 HTTP 请求，
 * 支持标准协议和三方协议的请求构建与响应解析。
 * 当 Nacos 服务发现可用时，自动使用 @LoadBalanced RestTemplate 解析服务名。
 * </p>
 */
@Service("aureport.httpService")
public class HttpServiceImpl implements HttpService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private RestTemplate discoveryRestTemplate;

    public HttpServiceImpl(@Qualifier("aureportRestTemplate") RestTemplate restTemplate,
                           ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Autowired(required = false)
    @Qualifier("aureportDiscoveryRestTemplate")
    public void setDiscoveryRestTemplate(RestTemplate discoveryRestTemplate) {
        this.discoveryRestTemplate = discoveryRestTemplate;
    }

    @Override
    public List<Map<String, Object>> executeStandard(HttpRequestConfig config,
                                                     String datasourceName,
                                                     String datasetName,
                                                     Map<String, Object> parameters) {
        // 构建标准协议请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("datasourceName", datasourceName);
        requestBody.put("datasetName", datasetName);
        requestBody.put("parameters", parameters != null ? parameters : Map.of());

        String url = config.getUrl();
        String method = config.getMethod() != null ? config.getMethod().toUpperCase() : "POST";

        // 构建请求头
        org.springframework.http.HttpHeaders headers = buildHeaders(config.getHeaders());
        if (headers.getContentType() == null) {
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        }

        // 构造带 query params 的 URL
        String fullUrl = appendQueryParams(url, config.getRequestParameters());

        log.info("HTTP standard protocol request: {} {}, body={}, headers={}", method, fullUrl, requestBody, headers);

        org.springframework.http.HttpEntity<Map<String, Object>> requestEntity =
            new org.springframework.http.HttpEntity<>(requestBody, headers);

        RestTemplate rt = resolveRestTemplate(config);

        String response;
        if ("POST".equals(method)) {
            response = rt.postForObject(fullUrl, requestEntity, String.class);
        } else if ("PUT".equals(method)) {
            rt.put(fullUrl, requestEntity);
            response = null;
        } else if ("DELETE".equals(method)) {
            rt.delete(fullUrl);
            response = null;
        } else {
            // GET - 使用 getForObject
            response = rt.getForObject(fullUrl, String.class);
        }

        // 解析响应：期望 {code: 200, data: [...], message: "..."}
        return parseStandardResponse(response, datasetName);
    }

    @Override
    public List<Map<String, Object>> executeThirdParty(HttpRequestConfig config,
                                                       Map<String, Object> parameters) {
        String resolvedUrl = resolveTemplate(config.getUrl(), parameters);
        String method = config.getMethod() != null ? config.getMethod().toUpperCase() : "GET";
        String resolvedBody = config.getBody() != null
            ? resolveTemplate(config.getBody(), parameters) : null;

        // 构造带 query params 的 URL
        String fullUrl = appendQueryParams(resolvedUrl, config.getRequestParameters());

        // 构建请求头
        org.springframework.http.HttpHeaders headers = buildHeaders(config.getHeaders());

        log.info("HTTP third-party protocol request: {} {}, body={}", method, fullUrl, resolvedBody);

        org.springframework.http.HttpEntity<String> requestEntity =
            new org.springframework.http.HttpEntity<>(resolvedBody, headers);

        String response;

        if ("POST".equals(method)) {
            response = restTemplate.postForObject(fullUrl, requestEntity, String.class);
        } else if ("PUT".equals(method)) {
            restTemplate.put(fullUrl, requestEntity);
            response = null;
        } else if ("DELETE".equals(method)) {
            restTemplate.delete(fullUrl);
            response = null;
        } else {
            response = restTemplate.getForObject(fullUrl, String.class);
        }

        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }

        return parseByJsonPath(response, config.getResponsePath());
    }

    /**
     * 根据配置选择合适的 RestTemplate。
     * 当启用服务发现且 discoveryRestTemplate 可用时，使用 @LoadBalanced 版本。
     */
    private RestTemplate resolveRestTemplate(HttpRequestConfig config) {
        if (config.isDiscoveryEnabled() && discoveryRestTemplate != null) {
            return discoveryRestTemplate;
        }
        return restTemplate;
    }

    /**
     * 将请求参数追加到 URL 上
     */
    private String appendQueryParams(String url, List<HttpParameter> requestParameters) {
        if (requestParameters == null || requestParameters.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        boolean hasQuery = url.contains("?");
        for (HttpParameter param : requestParameters) {
            if (param.getName() == null || param.getName().isBlank()) continue;
            sb.append(hasQuery ? '&' : '?');
            sb.append(param.getName()).append('=');
            if (param.getValue() != null) {
                sb.append(param.getValue());
            }
            hasQuery = true;
        }
        return sb.toString();
    }

    /**
     * 从 JSON 字符串解析请求头
     */
    private org.springframework.http.HttpHeaders buildHeaders(String headersJson) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (headersJson != null && !headersJson.isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> headerMap = objectMapper.readValue(headersJson, Map.class);
                headerMap.forEach(headers::set);
            } catch (Exception e) {
                log.warn("Failed to parse HTTP headers JSON: {}", headersJson, e);
            }
        }
        return headers;
    }

    /**
     * 解析标准协议响应
     */
    private List<Map<String, Object>> parseStandardResponse(String response, String datasetName) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }
        try {
            @SuppressWarnings("rawtypes")
            Map result = objectMapper.readValue(response, Map.class);

            Object code = result.get("code");
            if (code instanceof Number && ((Number) code).intValue() != 200) {
                String msg = result.getOrDefault("message", "unknown error").toString();
                throw new RuntimeException("HTTP standard protocol error: code=" + code + ", message=" + msg);
            }

            Object data = result.get("data");
            if (data == null) {
                return Collections.emptyList();
            }

            return objectMapper.convertValue(data, new TypeReference<List<Map<String, Object>>>() {});
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse standard protocol response for dataset [" + datasetName + "]", e);
        }
    }

    /**
     * 通过 JSONPath 解析响应
     */
    private List<Map<String, Object>> parseByJsonPath(String response, String responsePath) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Object document = JsonPath.parse(response);
            Object result;

            if (responsePath != null && !responsePath.isBlank()) {
                result = JsonPath.read(document, responsePath);
            } else {
                result = objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                if (result instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> list = (List<Map<String, Object>>) result;
                    return list;
                }
                return Collections.emptyList();
            }

            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) result;
                return list;
            } else if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> single = (Map<String, Object>) result;
                return Collections.singletonList(single);
            } else {
                return Collections.emptyList();
            }
        } catch (PathNotFoundException e) {
            log.warn("JSONPath not found: {}", responsePath);
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse third-party protocol response with JSONPath [" + responsePath + "]", e);
        }
    }

    @Override
    public String executeProxyGet(String url, Map<String, String> headers) {
        log.info("HTTP standard proxy GET: {}", url);
        org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(httpHeaders);
        return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class).getBody();
    }

    /**
     * 替换字符串中的 {{paramName}} 模板变量
     */
    private String resolveTemplate(String template, Map<String, Object> parameters) {
        if (template == null || parameters == null || parameters.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
