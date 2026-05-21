package com.aureport.ultra.web.controller.designer;

import com.aureport.ultra.core.annotation.FieldDesc;
import com.aureport.ultra.core.exception.ReportServiceException;
import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.definition.dataset.Field;
import com.aureport.ultra.core.definition.datasource.BuildinDatasource;
import com.aureport.ultra.core.definition.datasource.DataType;
import com.aureport.ultra.core.expression.ExpressionUtils;
import com.aureport.ultra.core.expression.model.Expression;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectExpressionData;
import com.aureport.ultra.core.utils.ProcedureUtils;
import com.aureport.ultra.web.exception.ReportDesignException;
import com.aureport.ultra.web.sql.enums.DbType;
import com.aureport.ultra.web.sql.DialectFactory;
import com.aureport.ultra.web.sql.IPageDialect;
import com.aureport.ultra.web.utils.ResponseUtils;
import com.aureport.ultra.core.definition.datasource.HttpRequestConfig;
import com.aureport.ultra.web.http.HttpServiceImpl;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.util.ClassUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据源控制器
 */
@RestController("bean.datasourceController")
@RequestMapping(value = "${aureport-ultra.servletPrefix}/datasource")
@Tag(name = "数据源", description = "数据源管理、数据库连接与数据预览")
public class DatasourceController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private HttpServiceImpl httpService;

    @Value("${aureport-ultra.servletPrefix}")
    private String servletPrefix;

    /**
     * 加载内置数据源
     */
    @Operation(
        summary = "加载内置数据源",
        responses = {
            @ApiResponse(responseCode = "200", description = "内置数据源列表JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @GetMapping("/loadBuildinDatasources")
    public void loadBuildinDatasources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> datasources = new ArrayList<>();
        for (BuildinDatasource datasource : Utils.getBuildinDatasources()) {
            datasources.add(datasource.name());
        }
        ResponseUtils.writeObjectToJson(resp, datasources);
    }

    /**
     * 加载Bean方法
     */
    @Operation(
        summary = "加载Bean方法",
        parameters = {
            @Parameter(name = "beanId", description = "Spring Bean ID", required = true, example = "userService")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "方法列表JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @GetMapping("/loadMethods")
    public void loadMethods(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String beanId = req.getParameter("beanId");
        Object obj = applicationContext.getBean(beanId);
        Class<?> clazz = obj.getClass();
        // 如果 Spring 使用了 CGLIB 代理，取父类获取原始方法的泛型信息
        Class<?> userClass = ClassUtils.getUserClass(clazz);
        Method[] methods = clazz.getMethods();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Method method : methods) {
            Class<?>[] types = method.getParameterTypes();
            if (types.length != 3) {
                continue;
            }
            if (!String.class.isAssignableFrom(types[0])) {
                continue;
            }
            if (!String.class.isAssignableFrom(types[1])) {
                continue;
            }
            if (!Map.class.isAssignableFrom(types[2])) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("method", method.getName());
            item.put("returnClass", resolveReturnClass(userClass, method));
            result.add(item);
        }
        ResponseUtils.writeObjectToJson(resp, result);
    }

    /**
     * 解析方法返回值中的泛型类型。支持以下场景：
     * <ul>
     *   <li>返回 {@code List<X>} → 提取 X 的全限定类名</li>
     *   <li>返回普通 POJO → 直接使用返回类型</li>
     *   <li>无法解析 → null</li>
     * </ul>
     */
    private String resolveReturnClass(Class<?> userClass, Method proxyMethod) {
        try {
            // 在原始类（非 CGLIB 代理）上找到对应方法以保留泛型签名
            Method userMethod = userClass.getMethod(proxyMethod.getName(), proxyMethod.getParameterTypes());
            Type returnType = userMethod.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) returnType;
                Type[] args = pt.getActualTypeArguments();
                if (args.length == 1 && args[0] instanceof Class) {
                    return ((Class<?>) args[0]).getName();
                }
            } else if (returnType instanceof Class) {
                Class<?> retClass = (Class<?>) returnType;
                if (retClass != List.class && retClass != Collection.class && !retClass.isArray()) {
                    return retClass.getName();
                }
            }
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    /**
     * 构建类字段
     */
    @Operation(
        summary = "构建类字段",
        parameters = {
            @Parameter(name = "clazz", description = "类的全限定名", required = true, example = "com.example.User")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "字段列表JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @GetMapping("/buildClass")
    public void buildClass(HttpServletRequest req, HttpServletResponse resp) {
        String clazz = req.getParameter("clazz");
        try {
            Class<?> targetClass = resolveClass(clazz);
            List<Field> result = buildFieldsForClass(targetClass, 0, new HashSet<>());
            ResponseUtils.writeObjectToJson(resp, result);
        } catch (Exception ex) {
            throw new ReportDesignException(ex);
        }
    }

    /**
     * 递归构建类字段，支持 @FieldDesc 注解和嵌套 Bean
     */
    private List<Field> buildFieldsForClass(Class<?> targetClass, int depth, Set<Class<?>> visited) {
        List<Field> result = new ArrayList<>();
        if (targetClass == null || depth > 3 || visited.contains(targetClass)) {
            return result;
        }
        visited.add(targetClass);

        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(targetClass);
        for (PropertyDescriptor pd : propertyDescriptors) {
            String name = pd.getName();
            if ("class".equals(name)) {
                continue;
            }

            Class<?> propertyType = pd.getPropertyType();
            if (propertyType == null) {
                continue;
            }

            String label = resolveFieldDesc(pd, targetClass);
            String typeName = propertyType.getName();
            List<Field> children = null;

            if (isComplexType(propertyType)) {
                children = buildFieldsForClass(propertyType, depth + 1, visited);
            } else if (Iterable.class.isAssignableFrom(propertyType) || propertyType.isArray()) {
                Type genericType = pd.getReadMethod() != null ? pd.getReadMethod().getGenericReturnType() : null;
                Class<?> elementClass = resolveElementClass(genericType);
                if (elementClass != null && isComplexType(elementClass)) {
                    children = buildFieldsForClass(elementClass, depth + 1, visited);
                }
            }

            Field field = new Field(name, label, typeName, children);
            result.add(field);
        }

        visited.remove(targetClass);
        return result;
    }

    /**
     * 解析 @FieldDesc 注解：优先 getter 方法，其次字段
     */
    private String resolveFieldDesc(PropertyDescriptor pd, Class<?> targetClass) {
        Method readMethod = pd.getReadMethod();
        if (readMethod != null) {
            FieldDesc desc = readMethod.getAnnotation(FieldDesc.class);
            if (desc != null) {
                return desc.value();
            }
        }
        try {
            java.lang.reflect.Field field = targetClass.getDeclaredField(pd.getName());
            FieldDesc desc = field.getAnnotation(FieldDesc.class);
            if (desc != null) {
                return desc.value();
            }
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }

    /**
     * 从泛型返回类型中解析集合元素类型
     * e.g. {@code List<FamilyMemberVO>} → {@code FamilyMemberVO.class}
     */
    private Class<?> resolveElementClass(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class) {
                return (Class<?>) args[0];
            }
        }
        return null;
    }

    /**
     * 判断是否为需要递归展开的复杂类型
     */
    private boolean isComplexType(Class<?> type) {
        if (type.isPrimitive()) return false;
        if (type.isArray()) return false;
        if (type.isEnum()) return false;
        // JDK 标准类型不展开
        String name = type.getName();
        if (name.startsWith("java.")) return false;
        if (name.startsWith("javax.")) return false;
        // 常用简单类型
        if (String.class.isAssignableFrom(type)) return false;
        if (Number.class.isAssignableFrom(type)) return false;
        if (Boolean.class.isAssignableFrom(type)) return false;
        if (Date.class.isAssignableFrom(type)) return false;
        if (Iterable.class.isAssignableFrom(type)) return false;
        if (Map.class.isAssignableFrom(type)) return false;
        return true;
    }

    /**
     * 解析类名，支持内部类（. → $）
     */
    private Class<?> resolveClass(String clazz) throws ClassNotFoundException {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            int dot = clazz.lastIndexOf('.');
            int dollar = clazz.lastIndexOf('$');
            if (dot > dollar) {
                return Class.forName(
                    clazz.substring(0, dot) + "$" + clazz.substring(dot + 1)
                );
            }
            throw e;
        }
    }

    /**
     * 构建数据库表
     */
    @Operation(
        summary = "构建数据库表",
        parameters = {
            @Parameter(name = "type", description = "数据源类型(jdbc或内置数据源名称)", required = true, example = "jdbc"),
            @Parameter(name = "username", description = "数据库用户名(当type=jdbc时)", required = false, example = "root"),
            @Parameter(name = "password", description = "数据库密码(当type=jdbc时)", required = false, example = "password"),
            @Parameter(name = "driver", description = "数据库驱动(当type=jdbc时)", required = false, example = "com.mysql.cj.jdbc.Driver"),
            @Parameter(name = "url", description = "数据库连接URL(当type=jdbc时)", required = false, example = "jdbc:mysql://localhost:3306/test"),
            @Parameter(name = "name", description = "内置数据源名称(当type非jdbc时)", required = false, example = "mysql")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "数据库表列表JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @GetMapping("/buildDatabaseTables")
    public void buildDatabaseTables(HttpServletRequest req, HttpServletResponse resp) throws ReportServiceException {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = buildConnection(req);
            DatabaseMetaData metaData = conn.getMetaData();
            String url = metaData.getURL();
            String schema = null;
            if (url.toLowerCase().contains("oracle")) {
                schema = metaData.getUserName();
            }
            List<Map<String, String>> tables = new ArrayList<>();
            rs = metaData.getTables(null, schema, "%", new String[]{"TABLE", "VIEW"});
            while (rs.next()) {
                Map<String, String> table = new HashMap<>();
                table.put("name", rs.getString("TABLE_NAME"));
                table.put("type", rs.getString("TABLE_TYPE"));
                tables.add(table);
            }
            ResponseUtils.writeObjectToJson(resp, tables);
        } catch (Exception ex) {
            throw new ReportServiceException(ex);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeConnection(conn);
        }
    }

    /**
     * 构建字段
     */
    @Operation(
        summary = "构建字段",
        parameters = {
            @Parameter(name = "sql", description = "SQL查询语句", required = true, example = "SELECT * FROM users"),
            @Parameter(name = "parameters", description = "SQL参数JSON数组", required = false, example = "[{\"name\":\"id\",\"type\":\"Integer\",\"defaultValue\":\"1\"}]"),
            @Parameter(name = "type", description = "数据源类型(jdbc或内置数据源名称)", required = true, example = "jdbc"),
            @Parameter(name = "username", description = "数据库用户名(当type=jdbc时)", required = false, example = "root"),
            @Parameter(name = "password", description = "数据库密码(当type=jdbc时)", required = false, example = "password"),
            @Parameter(name = "driver", description = "数据库驱动(当type=jdbc时)", required = false, example = "com.mysql.cj.jdbc.Driver"),
            @Parameter(name = "url", description = "数据库连接URL(当type=jdbc时)", required = false, example = "jdbc:mysql://localhost:3306/test"),
            @Parameter(name = "name", description = "内置数据源名称(当type非jdbc时)", required = false, example = "mysql")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "字段列表JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @GetMapping("/buildFields")
    public void buildFields(HttpServletRequest req, HttpServletResponse resp) {
        String sql = req.getParameter("sql");
        String parameters = req.getParameter("parameters");
        Connection conn = null;
        final List<Field> fields = new ArrayList<>();
        try {
            conn = buildConnection(req);
            Map<String, Object> map = buildParameters(parameters);
            sql = parseSql(sql, map);
            if (ProcedureUtils.isProcedure(sql)) {
                List<Field> fieldsList = ProcedureUtils.procedureColumnsQuery(sql, map, conn);
                fields.addAll(fieldsList);
            } else {
                DataSource dataSource = new SingleConnectionDataSource(conn, false);
                NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
                PreparedStatementCreator statementCreator = getPreparedStatementCreator(sql, new MapSqlParameterSource(map));
                jdbc.getJdbcOperations().execute(statementCreator, new PreparedStatementCallback<Object>() {
                    @Override
                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        ResultSet rs = null;
                        try {
                            rs = ps.executeQuery();
                            ResultSetMetaData metadata = rs.getMetaData();
                            int columnCount = metadata.getColumnCount();
                            for (int i = 0; i < columnCount; i++) {
                                String columnName = metadata.getColumnLabel(i + 1);
                                fields.add(new Field(columnName));
                            }
                            return null;
                        } finally {
                            JdbcUtils.closeResultSet(rs);
                        }
                    }
                });
            }
            ResponseUtils.writeObjectToJson(resp, fields);
        } catch (Exception ex) {
            throw new ReportDesignException(ex);
        } finally {
            JdbcUtils.closeConnection(conn);
        }
    }

    /**
     * 测试 HTTP 数据源连接
     */
    @Operation(
        summary = "测试 HTTP 数据源连接",
        parameters = {
            @Parameter(name = "url", description = "HTTP 请求 URL", required = true),
            @Parameter(name = "method", description = "HTTP 方法(GET/POST)", required = false)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "连接测试结果"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @PostMapping("/testHttpConnection")
    public void testHttpConnection(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getParameter("url");
        String method = req.getParameter("method");
        if (method == null || method.isBlank()) method = "GET";

        Map<String, Object> result = new HashMap<>();
        try {
            HttpRequestConfig config = new HttpRequestConfig();
            config.setUrl(url);
            config.setMethod(method);
            List<Map<String, Object>> data = httpService.executeThirdParty(config, Map.of());
            result.put("result", true);
            result.put("message", "Connection successful, got " + (data != null ? data.size() : 0) + " rows");
        } catch (Exception e) {
            log.warn("HTTP connection test failed: url={}", url, e);
            result.put("result", false);
            result.put("message", e.getMessage());
        }
        ResponseUtils.writeObjectToJson(resp, result);
    }

    /**
     * HTTP 数据源预览数据
     */
    @Operation(
        summary = "HTTP 数据源预览数据",
        parameters = {
            @Parameter(name = "protocol", description = "协议类型(standard/thirdParty)", required = true),
            @Parameter(name = "hostType", description = "标准协议主机方式(manual/discovery)", required = false),
            @Parameter(name = "host", description = "标准协议 host（手动模式）", required = false),
            @Parameter(name = "serviceName", description = "标准协议服务名（服务发现模式）", required = false),
            @Parameter(name = "url", description = "URL 路径（标准）/ 完整 URL（三方协议）", required = false),
            @Parameter(name = "datasourceName", description = "数据源名称", required = false),
            @Parameter(name = "datasetName", description = "数据集名称", required = true),
            @Parameter(name = "method", description = "HTTP 方法", required = false),
            @Parameter(name = "headers", description = "请求头 JSON", required = false),
            @Parameter(name = "body", description = "三方协议请求体", required = false),
            @Parameter(name = "responsePath", description = "三方协议响应 JSONPath", required = false)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "数据预览结果"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @GetMapping("/httpPreviewData")
    public void httpPreviewData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String protocol = req.getParameter("protocol");
        String datasetName = req.getParameter("datasetName");

        List<Map<String, Object>> data;
        try {
            if ("thirdParty".equals(protocol)) {
                HttpRequestConfig config = new HttpRequestConfig();
                config.setUrl(req.getParameter("url"));
                config.setMethod(req.getParameter("method"));
                config.setHeaders(req.getParameter("headers"));
                config.setBody(req.getParameter("body"));
                config.setResponsePath(req.getParameter("responsePath"));
                data = httpService.executeThirdParty(config, Map.of());
            } else {
                HttpRequestConfig config = new HttpRequestConfig();
                String hostType = req.getParameter("hostType");
                if ("discovery".equals(hostType)) {
                    String serviceName = req.getParameter("serviceName");
                    String urlPath = req.getParameter("url");
                    String base = "http://" + (serviceName != null ? serviceName : "");
                    String path = (urlPath != null) ? urlPath : "";
                    if (!path.startsWith("/")) path = "/" + path;
                    config.setUrl(base + path);
                    config.setDiscoveryEnabled(true);
                } else {
                    String host = req.getParameter("host");
                    String urlPath = req.getParameter("url");
                    config.setUrl((host != null ? host : "") + (urlPath != null ? urlPath : ""));
                }
                config.setMethod(req.getParameter("method"));
                config.setHeaders(req.getParameter("headers"));
                String dsName = req.getParameter("datasourceName");
                String beanId = req.getParameter("beanId");
                String beanMethod = req.getParameter("beanMethod");
                data = httpService.executeStandard(config, dsName, datasetName, beanId, beanMethod, Map.of());
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            ResponseUtils.writeObjectToJson(resp, error);
            return;
        }

        // 构建与 SQL previewData 相同格式的响应
        DataResult result = new DataResult();
        List<String> fields = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            Map<String, Object> first = data.get(0);
            fields.addAll(first.keySet());
        }
        result.setFields(fields);
        result.setCurrentTotal(data != null ? data.size() : 0);
        result.setData(data != null ? data : List.of());
        result.setTotal(data != null ? data.size() : 0);
        ResponseUtils.writeObjectToJson(resp, result);
    }

    /**
     * 标准协议 HTTP 代理请求
     * <p>
     * 将前端请求代理转发到标准协议的目标服务。
     * 根据 datasource 的 host/serviceName 配置组装目标 URL，
     * 执行 GET 请求并从标准响应 {code, data, message} 中提取 data 返回。
     * </p>
     */
    @Operation(
        summary = "标准协议 HTTP 代理请求",
        parameters = {
            @Parameter(name = "datasourceName", description = "数据源名称", required = true),
            @Parameter(name = "endpoint", description = "目标 API 路径（如 report-beans、loadMethods）", required = true),
            @Parameter(name = "host", description = "主机地址（手动模式）", required = false),
            @Parameter(name = "hostType", description = "主机方式 manual/discovery", required = false),
            @Parameter(name = "serviceName", description = "服务名（服务发现模式）", required = false)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "代理响应结果"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @GetMapping("/httpStandardProxy")
    public void httpStandardProxy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String datasourceName = req.getParameter("datasourceName");
        String endpoint = req.getParameter("endpoint");
        String host = req.getParameter("host");
        String hostType = req.getParameter("hostType");
        String serviceName = req.getParameter("serviceName");

        // 构建基础 URL
        StringBuilder urlBuilder = new StringBuilder();
        if ("discovery".equals(hostType) && serviceName != null && !serviceName.isBlank()) {
            // 通过 DiscoveryClient 手动解析服务名（避免依赖 @LoadBalanced 拦截器）
            String resolved = resolveServiceHost(serviceName);
            if (resolved == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Service discovery failed: no instances found for service '" + serviceName
                    + "'. Check Nacos console to verify the service is registered.");
                ResponseUtils.writeObjectToJson(resp, error);
                return;
            }
            urlBuilder.append(resolved);
        } else if (host != null && !host.isBlank()) {
            urlBuilder.append(host.replaceAll("/+$", ""));
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No host or service name configured for datasource: " + datasourceName);
            ResponseUtils.writeObjectToJson(resp, error);
            return;
        }

        // 构造完整 URL：{base}/{servletPrefix}/{endpoint}
        urlBuilder.append('/').append(servletPrefix).append('/').append(endpoint);

        // 转发剩余查询参数
        String separator = "?";
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            if ("datasourceName".equals(name) || "endpoint".equals(name)
                || "host".equals(name) || "hostType".equals(name) || "serviceName".equals(name)) {
                continue;
            }
            String value = req.getParameter(name);
            if (value != null && !value.isEmpty()) {
                urlBuilder.append(separator).append(name).append('=').append(value);
                separator = "&";
            }
        }

        String url = urlBuilder.toString();

        // 转发原始请求头（排除 hop-by-hop 头）
        Map<String, String> forwardedHeaders = new HashMap<>();
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (isHopByHopHeader(headerName)) continue;
            forwardedHeaders.put(headerName, req.getHeader(headerName));
        }

        try {
            // executeProxyGet 始终使用普通 RestTemplate（URL 已在上游解析好）
            String result = httpService.executeProxyGet(url, forwardedHeaders);
            if (result == null || result.isBlank()) {
                ResponseUtils.writeObjectToJson(resp, List.of());
                return;
            }

            // 检测响应类型：JSON 数组 → 直接返回（metadata 接口如 report-beans）
            String trimmed = result.trim();
            if (trimmed.startsWith("[")) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(result);
                return;
            }

            // 尝试解析标准协议响应 {code, data, message} 并提取 data
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(result, Map.class);
            if (parsed.containsKey("code")) {
                Object code = parsed.get("code");
                if (code instanceof Number && ((Number) code).intValue() != 200) {
                    Map<String, Object> errResult = new HashMap<>();
                    errResult.put("error", parsed.getOrDefault("message", "Unknown error from standard protocol service"));
                    ResponseUtils.writeObjectToJson(resp, errResult);
                    return;
                }
                ResponseUtils.writeObjectToJson(resp, parsed.get("data"));
                return;
            }
            // 非标准协议格式，直接返回
            ResponseUtils.writeObjectToJson(resp, parsed);
        } catch (Exception e) {
            String causeMsg = e.getCause() != null ? e.getCause().getMessage() : null;
            log.warn("HTTP standard proxy request failed: datasourceName={}, endpoint={}, url={}, cause={}",
                datasourceName, endpoint, url, causeMsg, e);
            Map<String, Object> errResult = new HashMap<>();
            errResult.put("error", e.getMessage());
            if (causeMsg != null) {
                errResult.put("cause", causeMsg);
            }
            errResult.put("proxyUrl", url);
            ResponseUtils.writeObjectToJson(resp, errResult);
        }
    }

    /**
     * 判断是否为 hop-by-hop 头，这些头不应转发到目标服务
     */
    private static boolean isHopByHopHeader(String name) {
        if (name == null) return true;
        String lower = name.toLowerCase();
        return lower.equals("host")
            || lower.equals("connection")
            || lower.equals("content-length")
            || lower.equals("transfer-encoding")
            || lower.equals("upgrade")
            || lower.equals("proxy-authorization")
            || lower.equals("proxy-authenticate")
            || lower.equals("te")
            || lower.equals("trailer");
    }

    /**
     * 通过 DiscoveryClient 解析服务名到实际的 http://host:port 地址。
     * 使用反射调用以规避编译期对 spring-cloud-commons 的依赖。
     */
    @SuppressWarnings("unchecked")
    private String resolveServiceHost(String serviceName) {
        try {
            Class<?> dcClass = Class.forName("org.springframework.cloud.client.discovery.DiscoveryClient");
            Object discoveryClient = applicationContext.getBean(dcClass);
            Method getInstances = dcClass.getMethod("getInstances", String.class);
            List<Object> instances = (List<Object>) getInstances.invoke(discoveryClient, serviceName);
            if (instances != null && !instances.isEmpty()) {
                Object instance = instances.get(0);
                Class<?> siClass = instance.getClass();
                String hostName = (String) siClass.getMethod("getHost").invoke(instance);
                int port = (int) siClass.getMethod("getPort").invoke(instance);
                boolean secure = (boolean) siClass.getMethod("isSecure").invoke(instance);
                String scheme = secure ? "https" : "http";
                String resolved = scheme + "://" + hostName + ":" + port;
                log.info("Resolved service '{}' → {}", serviceName, resolved);
                return resolved;
            }
            log.warn("No instances found for service '{}' via DiscoveryClient", serviceName);
        } catch (ClassNotFoundException e) {
            log.warn("DiscoveryClient not available on classpath, cannot resolve service '{}'", serviceName);
        } catch (Exception e) {
            log.warn("Failed to resolve service '{}' via DiscoveryClient: {}", serviceName, e.getMessage());
        }
        return null;
    }

    /**
     * 预览数据
     */
    @Operation(
        summary = "预览数据",
        parameters = {
            @Parameter(name = "sql", description = "SQL查询语句", required = true, example = "SELECT * FROM users"),
            @Parameter(name = "parameters", description = "SQL参数JSON数组", required = false, example = "[{\"name\":\"id\",\"type\":\"Integer\",\"defaultValue\":\"1\"}]"),
            @Parameter(name = "type", description = "数据源类型(jdbc或内置数据源名称)", required = true, example = "jdbc"),
            @Parameter(name = "username", description = "数据库用户名(当type=jdbc时)", required = false, example = "root"),
            @Parameter(name = "password", description = "数据库密码(当type=jdbc时)", required = false, example = "password"),
            @Parameter(name = "driver", description = "数据库驱动(当type=jdbc时)", required = false, example = "com.mysql.cj.jdbc.Driver"),
            @Parameter(name = "url", description = "数据库连接URL(当type=jdbc时)", required = false, example = "jdbc:mysql://localhost:3306/test"),
            @Parameter(name = "name", description = "内置数据源名称(当type非jdbc时)", required = false, example = "mysql")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "数据预览结果JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @GetMapping("/previewData")
    public void previewData(HttpServletRequest req, HttpServletResponse resp) throws ReportServiceException, IOException {
        String sql = req.getParameter("sql");
        String parameters = req.getParameter("parameters");
        Map<String, Object> map = buildParameters(parameters);
        sql = parseSql(sql, map);
        Connection conn = null;
        try {
            conn = buildConnection(req);
            DatabaseMetaData metaData = conn.getMetaData();
            String dbProductName = metaData.getDatabaseProductName();
            DbType dbType = DbType.getDbType(dbProductName);
            IPageDialect dialect = DialectFactory.getDialect(dbType);
            long offset = 0;
            long limit = 15;
            if (dialect != null) {
                sql = dialect.buildPaginationSql(sql, offset, limit);
            }
            List<Map<String, Object>> list = null;
            if (ProcedureUtils.isProcedure(sql)) {
                list = ProcedureUtils.procedureQuery(sql, map, conn);
            } else {
                DataSource dataSource = new SingleConnectionDataSource(conn, false);
                NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
                list = jdbc.queryForList(sql, map);
            }
            int size = list.size();
            int currentTotal = size;
            if (currentTotal > 500) {
                currentTotal = 500;
            }
            List<Map<String, Object>> ls = new ArrayList<>();
            for (int i = 0; i < currentTotal; i++) {
                ls.add(list.get(i));
            }
            DataResult result = new DataResult();
            List<String> fields = new ArrayList<>();
            if (size > 0) {
                Map<String, Object> item = list.get(0);
                for (String name : item.keySet()) {
                    fields.add(name);
                }
            }
            result.setFields(fields);
            result.setCurrentTotal(currentTotal);
            result.setData(ls);
            result.setTotal(size);
            ResponseUtils.writeObjectToJson(resp, result);
        } catch (Exception ex) {
            throw new ReportServiceException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试数据库连接
     */
    @Operation(
        summary = "测试数据库连接",
        parameters = {
            @Parameter(name = "username", description = "数据库用户名", required = true, example = "root"),
            @Parameter(name = "password", description = "数据库密码", required = true, example = "password"),
            @Parameter(name = "driver", description = "数据库驱动", required = true, example = "com.mysql.cj.jdbc.Driver"),
            @Parameter(name = "url", description = "数据库连接URL", required = true, example = "jdbc:mysql://localhost:3306/test")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "连接测试结果JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @PostMapping("/testConnection")
    public void testConnection(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException, ClassNotFoundException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String driver = req.getParameter("driver");
        String url = req.getParameter("url");
        Connection conn = null;
        Map<String, Object> map = new HashMap<>();
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
            map.put("result", true);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("连接异常",e);
                }
            }
        }
        ResponseUtils.writeObjectToJson(resp, map);
    }

    // 辅助方法
    protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
        ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
        String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
        Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
        List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, declaredParameters);
        return pscf.newPreparedStatementCreator(params);
    }

    private String parseSql(String sql, Map<String, Object> parameters) {
        sql = sql.trim();
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        Context context = new Context(applicationContext, parameters);
        if (sql.startsWith(ExpressionUtils.EXPR_PREFIX) && sql.endsWith(ExpressionUtils.EXPR_SUFFIX)) {
            sql = sql.substring(2, sql.length() - 1);
            Expression expr = ExpressionUtils.parseExpression(sql);
            sql = executeSqlExpr(expr, context);
            return sql;
        } else {
            String sqlForUse = sql;
            Pattern pattern = Pattern.compile("\\$\\{.*?\\}");
            Matcher matcher = pattern.matcher(sqlForUse);
            while (matcher.find()) {
                String substr = matcher.group();
                String sqlExpr = substr.substring(2, substr.length() - 1);
                Expression expr = ExpressionUtils.parseExpression(sqlExpr);
                String result = executeSqlExpr(expr, context);
                sqlForUse = sqlForUse.replace(substr, result);
            }
            Utils.logToConsole("DESIGN SQL:" + sqlForUse);
            return sqlForUse;
        }
    }

    private String executeSqlExpr(Expression sqlExpr, Context context) {
        String sqlForUse = null;
        ExpressionData<?> exprData = sqlExpr.execute(null, null, context);
        if (exprData instanceof ObjectExpressionData) {
            ObjectExpressionData data = (ObjectExpressionData) exprData;
            Object obj = data.getData();
            if (obj != null) {
                String s = obj.toString();
                s = s.replaceAll("\\\\", "");
                sqlForUse = s;
            }
        }
        return sqlForUse;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildParameters(String parameters) throws IOException {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(parameters)) {
            return map;
        }
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> list = mapper.readValue(parameters, ArrayList.class);
        for (Map<String, Object> param : list) {
            String name = param.get("name").toString();
            DataType type = DataType.valueOf(param.get("type").toString());
            String defaultValue = (String) param.get("defaultValue");
            if (defaultValue == null || defaultValue.equals("")) {
                switch (type) {
                    case Boolean:
                        map.put(name, false);
                        break;
                    case Date:
                        map.put(name, new Date());
                        break;
                    case Float:
                        map.put(name, 0f);
                        break;
                    case Integer:
                        map.put(name, 0);
                        break;
                    case String:
                        if (defaultValue != null && defaultValue.equals("")) {
                            map.put(name, "");
                        } else {
                            map.put(name, "null");
                        }
                        break;
                    case List:
                        map.put(name, new ArrayList<Object>());
                        break;
                }
            } else {
                map.put(name, type.parse(defaultValue));
            }
        }
        return map;
    }

    private Connection buildConnection(HttpServletRequest req) throws Exception {
        String type = req.getParameter("type");
        if (type.equals("jdbc")) {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String driver = req.getParameter("driver");
            String url = req.getParameter("url");

            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } else {
            String name = req.getParameter("name");
            Connection conn = Utils.getBuildinConnection(name);
            if (conn == null) {
                throw new ReportDesignException("Buildin datasource [" + name + "] not exist.");
            }
            return conn;
        }
    }

}
