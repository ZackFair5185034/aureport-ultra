package com.aureport.ultra.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * InvokeController 单元测试。
 * 覆盖标准协议远程 Bean 方法调用的各种场景。
 */
@ExtendWith(MockitoExtension.class)
class InvokeControllerTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private InvokeController controller;

    private InvokeController.InvokeRequest request;

    @BeforeEach
    void setUp() {
        request = new InvokeController.InvokeRequest();
        request.setBeanId("userService");
        request.setMethod("queryUsers");
        Map<String, Object> params = new HashMap<>();
        params.put("datasourceName", "ds1");
        params.put("datasetName", "users");
        request.setParameters(params);
    }

    @Test
    void testInvoke_ListOfMapResult() throws Exception {
        // 模拟 Service Bean 和方法
        UserService mockService = mock(UserService.class);
        when(applicationContext.getBean("userService")).thenReturn(mockService);

        List<Map<String, Object>> expectedData = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", 1);
        row1.put("name", "张三");
        expectedData.add(row1);

        when(mockService.queryUsers(anyString(), anyString(), any())).thenReturn(expectedData);

        Map<String, Object> result = controller.invoke(request);

        assertEquals(200, result.get("code"));
        assertEquals("", result.get("message"));
        List<?> data = (List<?>) result.get("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals(1, ((Map<?, ?>) data.get(0)).get("id"));
        assertEquals("张三", ((Map<?, ?>) data.get(0)).get("name"));

        verify(applicationContext).getBean("userService");
        verify(mockService).queryUsers("ds1", "users", request.getParameters());
    }

    @Test
    void testInvoke_SinglePojoResult() throws Exception {
        UserPojo mockService = mock(UserPojo.class);
        when(applicationContext.getBean("userService")).thenReturn(mockService);

        // 返回单个 POJO
        User user = new User(1, "张三", "zhangsan@test.com");
        when(mockService.queryUsers(anyString(), anyString(), any())).thenReturn(user);

        Map<String, Object> result = controller.invoke(request);

        assertEquals(200, result.get("code"));
        List<?> data = (List<?>) result.get("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        Map<?, ?> userMap = (Map<?, ?>) data.get(0);
        assertEquals(1, userMap.get("id"));
        assertEquals("张三", userMap.get("name"));
        assertEquals("zhangsan@test.com", userMap.get("email"));
    }

    @Test
    void testInvoke_SingleMapResult() throws Exception {
        SingleMapService mockService = mock(SingleMapService.class);
        when(applicationContext.getBean("userService")).thenReturn(mockService);

        Map<String, Object> singleMap = new HashMap<>();
        singleMap.put("id", 1);
        singleMap.put("name", "李四");
        when(mockService.queryUsers(anyString(), anyString(), any())).thenReturn(singleMap);

        Map<String, Object> result = controller.invoke(request);

        assertEquals(200, result.get("code"));
        List<?> data = (List<?>) result.get("data");
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals(1, ((Map<?, ?>) data.get(0)).get("id"));
        assertEquals("李四", ((Map<?, ?>) data.get(0)).get("name"));
    }

    @Test
    void testInvoke_NullResult() throws Exception {
        NullService mockService = mock(NullService.class);
        when(applicationContext.getBean("userService")).thenReturn(mockService);
        when(mockService.queryUsers(anyString(), anyString(), any())).thenReturn(null);

        Map<String, Object> result = controller.invoke(request);

        assertEquals(200, result.get("code"));
        List<?> data = (List<?>) result.get("data");
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    void testInvoke_BeanNotFound() {
        when(applicationContext.getBean("userService"))
            .thenThrow(new RuntimeException("No bean named 'userService' available"));

        Map<String, Object> result = controller.invoke(request);

        assertEquals(500, result.get("code"));
        assertTrue(((String) result.get("message")).contains("No bean named 'userService'"));
    }

    @Test
    void testInvoke_MethodNotFound() {
        UnknownMethodService mockService = mock(UnknownMethodService.class);
        when(applicationContext.getBean("userService")).thenReturn(mockService);

        // 方法名不存在（UnknownMethodService 只有 queryOrders，没有 queryUsers）
        Map<String, Object> result = controller.invoke(request);

        assertEquals(500, result.get("code"));
        // 应该在错误消息中包含方法名
        String message = (String) result.get("message");
        assertNotNull(message);
        assertTrue(message.contains("queryUsers") || message.contains("method"));
    }

    @Test
    void testInvoke_NullParameters() {
        // 无参数的请求
        InvokeController.InvokeRequest noParamsReq = new InvokeController.InvokeRequest();
        noParamsReq.setBeanId("userService");
        noParamsReq.setMethod("queryUsers");
        noParamsReq.setParameters(null);

        UserService mockService = mock(UserService.class);
        when(applicationContext.getBean("userService")).thenReturn(mockService);

        List<Map<String, Object>> emptyList = Collections.emptyList();
        when(mockService.queryUsers(eq(""), eq(""), any())).thenReturn(emptyList);

        // 当 parameters 为 null 时，datasourceName 和 datasetName 默认为 ""
        Map<String, Object> result = controller.invoke(noParamsReq);

        assertEquals(200, result.get("code"));
        verify(mockService).queryUsers(eq(""), eq(""), any());
    }

    @Test
    void testInvoke_MethodNameMismatch() throws Exception {
        MethodMismatchService mockService = mock(MethodMismatchService.class);
        when(applicationContext.getBean("userService")).thenReturn(mockService);

        // 方法有 3 个参数但类型不匹配（String, Integer, Map 而非 String, String, Map）
        Map<String, Object> result = controller.invoke(request);

        assertEquals(500, result.get("code"));
        String message = (String) result.get("message");
        assertNotNull(message);
        assertTrue(message.contains("queryUsers") || message.contains("No matching method"));
    }

    @Test
    void testInvoke_MethodWithException() {
        ExceptionService mockService = mock(ExceptionService.class);
        when(applicationContext.getBean("userService")).thenReturn(mockService);

        when(mockService.queryUsers(anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("数据库连接失败"));

        Map<String, Object> result = controller.invoke(request);

        assertEquals(500, result.get("code"));
        assertTrue(((String) result.get("message")).contains("数据库连接失败"));
    }

    // ===== 辅助测试类 =====

    /** 标准服务 — 返回 List<Map> */
    public interface UserService {
        List<Map<String, Object>> queryUsers(String dsName, String datasetName, Map<String, Object> params);
    }

    /** 返回单个 Map */
    public interface SingleMapService {
        Map<String, Object> queryUsers(String dsName, String datasetName, Map<String, Object> params);
    }

    /** 返回 null */
    public interface NullService {
        List<Map<String, Object>> queryUsers(String dsName, String datasetName, Map<String, Object> params);
    }

    /** 返回 POJO */
    public interface UserPojo {
        User queryUsers(String dsName, String datasetName, Map<String, Object> params);
    }

    /** 没有匹配的方法名 */
    public interface UnknownMethodService {
        List<Map<String, Object>> queryOrders(String dsName, String datasetName, Map<String, Object> params);
    }

    /** 参数类型不匹配 */
    public interface MethodMismatchService {
        List<Map<String, Object>> queryUsers(String dsName, Integer count, Map<String, Object> params);
    }

    /** 调用时抛异常 */
    public interface ExceptionService {
        List<Map<String, Object>> queryUsers(String dsName, String datasetName, Map<String, Object> params);
    }

    /** 测试用 POJO */
    public static class User {
        private int id;
        private String name;
        private String email;

        public User(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}
