package com.aureport.ultra.web.http;

import com.aureport.ultra.core.definition.datasource.HttpRequestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HttpServiceImpl 单元测试 —— 验证 Phase2 beanId+method 请求体构造。
 */
@ExtendWith(MockitoExtension.class)
class HttpServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<HttpEntity<Map<String, Object>>> httpEntityCaptor;

    private HttpServiceImpl httpService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        httpService = new HttpServiceImpl(restTemplate, objectMapper);
    }

    @Test
    void testExecuteStandard_phase2WithBeanIdAndMethod() throws Exception {
        // 模拟返回标准响应
        String mockResponse = "{\"code\":200,\"data\":[{\"id\":1,\"name\":\"张三\"}],\"message\":\"\"}";
        when(restTemplate.postForObject(
            eq("http://localhost:8080/report/datasource/invoke"),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(mockResponse);

        HttpRequestConfig config = new HttpRequestConfig();
        config.setUrl("http://localhost:8080/report/datasource/invoke");
        config.setMethod("POST");

        List<Map<String, Object>> result = httpService.executeStandard(
            config, "dsName", "datasetName",
            "userService", "queryUsers", Map.of("param1", "value1"));

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).get("id"));
        assertEquals("张三", result.get(0).get("name"));

        // 验证请求体中包含 beanId + method（Phase2 模式）
        verify(restTemplate).postForObject(
            eq("http://localhost:8080/report/datasource/invoke"),
            httpEntityCaptor.capture(),
            eq(String.class));

        HttpEntity<Map<String, Object>> captured = httpEntityCaptor.getValue();
        Map<String, Object> body = captured.getBody();
        assertNotNull(body);
        assertEquals("userService", body.get("beanId"));
        assertEquals("queryUsers", body.get("method"));
        assertNotNull(body.get("parameters"));
        assertEquals("value1", ((Map<String, Object>) body.get("parameters")).get("param1"));

        // 验证 Phase2 模式下没有 datasourceName/datasetName
        assertFalse(body.containsKey("datasourceName"));
        assertFalse(body.containsKey("datasetName"));
    }

    @Test
    void testExecuteStandard_legacyModeWithoutBeanInfo() throws Exception {
        String mockResponse = "{\"code\":200,\"data\":[{\"id\":1}],\"message\":\"\"}";
        when(restTemplate.postForObject(
            anyString(), any(HttpEntity.class), eq(String.class)
        )).thenReturn(mockResponse);

        HttpRequestConfig config = new HttpRequestConfig();
        config.setUrl("http://localhost:8080/legacy");
        config.setMethod("POST");

        // beanId 和 beanMethod 为空 → 使用向后兼容模式
        List<Map<String, Object>> result = httpService.executeStandard(
            config, "dsName", "datasetName",
            null, null, Map.of());

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(restTemplate).postForObject(
            eq("http://localhost:8080/legacy"),
            httpEntityCaptor.capture(),
            eq(String.class));

        Map<String, Object> body = httpEntityCaptor.getValue().getBody();
        assertNotNull(body);
        assertEquals("dsName", body.get("datasourceName"));
        assertEquals("datasetName", body.get("datasetName"));
        // legacy 模式下没有 beanId
        assertNull(body.get("beanId"));
    }

    @Test
    void testExecuteStandard_phase2WithEmptyParameters() throws Exception {
        String mockResponse = "{\"code\":200,\"data\":[],\"message\":\"\"}";
        when(restTemplate.postForObject(
            anyString(), any(HttpEntity.class), eq(String.class)
        )).thenReturn(mockResponse);

        HttpRequestConfig config = new HttpRequestConfig();
        config.setUrl("http://localhost:8080/invoke");
        config.setMethod("POST");

        // parameters 为 null
        List<Map<String, Object>> result = httpService.executeStandard(
            config, "ds", "dsName",
            "testBean", "testMethod", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(restTemplate).postForObject(
            eq("http://localhost:8080/invoke"),
            httpEntityCaptor.capture(),
            eq(String.class));

        Map<String, Object> body = httpEntityCaptor.getValue().getBody();
        assertNotNull(body);
        assertEquals("testBean", body.get("beanId"));
        assertEquals("testMethod", body.get("method"));
        // parameters 为 null 时应该转为空 Map
        assertNotNull(body.get("parameters"));
        assertTrue(((Map<?, ?>) body.get("parameters")).isEmpty());
    }

    @Test
    void testExecuteStandard_phase2BlankBeanMethodUsesLegacy() throws Exception {
        String mockResponse = "{\"code\":200,\"data\":[{\"x\":1}],\"message\":\"\"}";
        when(restTemplate.postForObject(
            anyString(), any(HttpEntity.class), eq(String.class)
        )).thenReturn(mockResponse);

        HttpRequestConfig config = new HttpRequestConfig();
        config.setUrl("http://localhost:8080/legacy");
        config.setMethod("POST");

        // beanId 有值但 beanMethod 为空字符串 → 应使用 legacy 模式
        List<Map<String, Object>> result = httpService.executeStandard(
            config, "dsName", "datasetName",
            "testBean", "", Map.of());

        assertNotNull(result);

        verify(restTemplate).postForObject(
            eq("http://localhost:8080/legacy"),
            httpEntityCaptor.capture(),
            eq(String.class));

        Map<String, Object> body = httpEntityCaptor.getValue().getBody();
        assertNotNull(body);
        // 应使用 legacy 模式
        assertTrue(body.containsKey("datasourceName"));
        assertFalse(body.containsKey("beanId"));
    }

    @Test
    void testExecuteStandard_parseStandardResponse() throws Exception {
        // 错误响应 code ≠ 200
        String errorResponse = "{\"code\":500,\"data\":[],\"message\":\"服务器异常\"}";
        when(restTemplate.postForObject(
            anyString(), any(HttpEntity.class), eq(String.class)
        )).thenReturn(errorResponse);

        HttpRequestConfig config = new HttpRequestConfig();
        config.setUrl("http://localhost:8080/invoke");
        config.setMethod("POST");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            httpService.executeStandard(config, "ds", "dsName",
                "bean", "method", Map.of()));

        assertTrue(ex.getMessage().contains("500"));
        assertTrue(ex.getMessage().contains("服务器异常"));
    }

    @Test
    void testExecuteStandard_GETMethod() {
        // GET 请求时 body 不应为 null
        when(restTemplate.getForObject(
            anyString(), eq(String.class)
        )).thenReturn("{\"code\":200,\"data\":[],\"message\":\"\"}");

        HttpRequestConfig config = new HttpRequestConfig();
        config.setUrl("http://localhost:8080/invoke");
        config.setMethod("GET");

        List<Map<String, Object>> result = httpService.executeStandard(
            config, "ds", "dsName",
            "testBean", "testMethod", Map.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(restTemplate).getForObject("http://localhost:8080/invoke", String.class);
    }
}
