package com.aureport.ultra.bean;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 统一 Invoke 端点：接收前端报表引擎的标准协议请求，通过反射调用 Spring Bean 方法并返回数据。
 * <p>
 * 标准协议 POST /report/datasource/invoke
 * 请求体格式：{beanId, method, parameters}
 * 响应格式：{code: 200, data: [...], message: ""}
 * </p>
 */
@RestController
@RequestMapping(value = "${aureport-ultra.servletPrefix}/report/datasource")
@Tag(name = "报表数据源调用", description = "通过 beanId + method 反射调用 Spring Bean 方法")
public class InvokeController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Operation(summary = "反射调用 Spring Bean 方法，返回数据")
    @PostMapping("/invoke")
    public Map<String, Object> invoke(@RequestBody InvokeRequest request) {
        String beanId = request.getBeanId();
        String methodName = request.getMethod();
        Map<String, Object> parameters = request.getParameters();

        log.info("Invoke bean: beanId={}, method={}, parameters={}", beanId, methodName, parameters);

        try {
            Object bean = applicationContext.getBean(beanId);
            Class<?> clazz = bean.getClass();

            // 查找匹配的方法：参数为 (String, String, Map)
            Method targetMethod = null;
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(methodName)) {
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length == 3
                        && String.class.isAssignableFrom(paramTypes[0])
                        && String.class.isAssignableFrom(paramTypes[1])
                        && Map.class.isAssignableFrom(paramTypes[2])) {
                        targetMethod = m;
                        break;
                    }
                }
            }

            if (targetMethod == null) {
                throw new IllegalArgumentException("No matching method found: " + methodName
                    + " with parameters (String, String, Map) on bean " + beanId);
            }

            // 调用方法：传递 datasourceName, datasetName, parameters
            String datasourceName = parameters != null
                ? (String) parameters.getOrDefault("datasourceName", "") : "";
            String datasetName = parameters != null
                ? (String) parameters.getOrDefault("datasetName", "") : "";

            Object result = targetMethod.invoke(bean, datasourceName, datasetName, parameters);

            // 将结果转换为 List<Map<String, Object>>
            List<Map<String, Object>> dataList = convertToDataList(result);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", dataList != null ? dataList : Collections.emptyList());
            response.put("message", "");
            return response;
        } catch (Exception e) {
            log.error("Invoke bean failed: beanId={}, method={}", beanId, methodName, e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("data", Collections.emptyList());
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            error.put("message", message != null ? message : "Unknown error");
            return error;
        }
    }

    /**
     * 将反射调用的返回值转换为 List&lt;Map&lt;String, Object&gt;&gt;
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertToDataList(Object result) {
        if (result == null) {
            return Collections.emptyList();
        }
        if (result instanceof List) {
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Object item : (List<?>) result) {
                if (item instanceof Map) {
                    dataList.add((Map<String, Object>) item);
                } else if (item != null) {
                    // 将 POJO 转为 Map
                    dataList.add(convertBeanToMap(item));
                }
            }
            return dataList;
        }
        if (result instanceof Map) {
            return Collections.singletonList((Map<String, Object>) result);
        }
        // 单个 POJO
        return Collections.singletonList(convertBeanToMap(result));
    }

    /**
     * 将 Java Bean 通过反射转为 Map
     */
    private Map<String, Object> convertBeanToMap(Object bean) {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            Class<?> clazz = bean.getClass();
            for (java.lang.reflect.Method getter : clazz.getMethods()) {
                String name = getter.getName();
                if (name.startsWith("get") && getter.getParameterCount() == 0
                    && !name.equals("getClass")) {
                    String prop = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    map.put(prop, getter.invoke(bean));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to convert bean to map: {}", bean, e);
        }
        return map;
    }

    /**
     * 请求体 DTO
     */
    public static class InvokeRequest {
        private String beanId;
        private String method;
        private Map<String, Object> parameters;

        public String getBeanId() { return beanId; }
        public void setBeanId(String beanId) { this.beanId = beanId; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }
}
