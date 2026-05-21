# HTTP 数据源方案

> 文档路径：`docs/技术方案/HTTP数据源方案.md`

---

## 1. 背景与目标

报表引擎需要对接外部 HTTP API 作为数据源，支持两种场景：

- **标准协议（ReportBean）**：类似 SpringBean 的远程 HTTP 版。后端集成 `aureport-ultra-bean-discovery-starter` 并添加 `@ReportBean`，Starter 自动暴露标准接口。引擎通过服务发现（Nacos）找到服务地址，按固定协议调用
- **三方协议**：对接任意 HTTP 接口，返回任意格式，通过 JSONPath 做规则解析

### 核心区别

| 角度 | 标准协议（ReportBean） | 三方协议 |
|------|----------------------|---------|
| 集成方式 | 集成 Starter + 添加 @ReportBean | 无依赖，直接调用 |
| 接口定义 | Starter 自动暴露标准接口 | 用户完全自定义 |
| 响应格式 | 固定 `{code:200, data:[...]}` | 任意格式，JSONPath 解析 |
| 数据集配置 | 仅需数据集名称 | URL/方法/头/体/JSONPath 全配置 |
| 类似本地模式 | SpringBean 本地调 Bean | — |

---

## 2. 协议类型

| 协议 | 标识 | 说明 |
|------|------|------|
| 标准协议（ReportBean） | `standard` | 类似 SpringBean 的远程调用 |
| 三方协议 | `thirdParty` | 任意 HTTP 接口，规则解析 |

---

## 3. 数据模型

### 3.1 Datasource（数据源）

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | string | 数据源名称 |
| `type` | `'http'` | 数据源类型 |
| `protocolType` | `'standard' \| 'thirdParty'` | 协议类型 |
| `host` | string | 标准协议：主机地址（手动录入） |
| `hostType` | `'manual' \| 'discovery'` | 标准协议：主机方式 |
| `serviceName` | string | 标准协议：服务发现服务名 |
| `baseUrl` | string | 三方协议：完整请求地址 |
| `datasets` | Dataset[] | 数据集列表 |

### 3.2 Dataset（数据集）

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | string | 数据集名称 |
| `type` | `'http'` | 数据集类型 |
| `url` | string | 三方协议：完整请求 URL |
| `method` | string | 三方协议：HTTP 方法 |
| `headers` | string | 三方协议：请求头 JSON |
| `body` | string | 三方协议：请求体模板 |
| `responsePath` | string | 三方协议：响应 JSONPath |
| `requestParameters` | HttpParameter[] | 三方协议：请求参数 |
| `fields` | Field[] | 返回字段定义（运行时自动发现） |

> 标准协议数据集仅需名称，不配置 URL/方法/头/体/JSONPath。

### 3.3 HttpParameter（请求参数）

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | string | 参数名 |
| `value` | string | 参数值 |

---

## 4. 请求地址组成

### 4.1 标准协议

完整 URL 由引擎根据主机方式自动构建：

```
手动模式:  {host}                         → http://192.168.1.100:8080
服务发现:  http://{serviceName}           → http://order-service
```

引擎向该地址 POST 标准请求体，无需 URL 路径（Starter 暴露的接口由协议定义）。

### 4.2 三方协议

三方协议的 `url` 字段直接作为完整请求 URL：

```
完整 URL = {url}                          → https://api.weather.com/v1/current
```

### 4.3 请求参数追加

三方协议的请求参数（`requestParameters`）自动追加到 URL：

```
GET http://host/api/data?param1=value1&param2=value2
```

---

## 5. 标准协议（ReportBean）

### 5.1 工作原理

类似 SpringBean 的远程 HTTP 版：
1. 后端服务集成 `aureport-ultra-bean-discovery-starter`，在 Bean 上添加 `@ReportBean`
2. Starter 自动暴露标准 HTTP 接口
3. 引擎通过服务发现（Nacos）或手动配置的主机地址找到服务
4. 按固定协议格式调用

### 5.2 请求格式

引擎向目标地址发送 POST 请求，请求体为 JSON：

```json
{
  "datasourceName": "数据源名称",
  "datasetName": "数据集名称",
  "parameters": {
    "参数名": "参数值"
  }
}
```

### 5.3 响应格式

服务端必须返回标准格式：

```json
{
  "code": 200,
  "data": [
    { "field1": "value1", "field2": "value2" }
  ],
  "message": "success"
}
```

- `code != 200` 时抛出异常，`message` 为错误描述
- `data` 为数据行数组（字段由引擎自动发现）

### 5.4 数据集配置

标准协议数据集仅需配置**数据集名称**，无需 URL/方法/头/请求体等。数据集的字段由引擎运行时从响应数据自动发现。

---

## 6. 三方协议

### 6.1 请求配置

| 配置项 | 必填 | 说明 |
|--------|------|------|
| URL | ✅ | 完整请求 URL |
| 请求方法 | ✅ | GET/POST/PUT/DELETE |
| 请求头 | ❌ | Key:Value 或 JSON 格式 |
| 请求参数 | ❌ | URL query parameters |
| 请求体 | POST/PUT 时 | 支持 {{paramName}} 模板 |
| 响应 JSONPath | ❌ | 从响应提取数据数组 |

### 6.2 请求参数模板

URL 和 Body 支持 `{{paramName}}` 模板变量替换，运行时由报表参数值填充：

```
url:  http://api.example.com/users/{{userId}}
body: {"name": "{{userName}}"}
```

### 6.3 响应解析

通过 JSONPath 从响应中提取数据数组：

| responsePath | 说明 |
|---|---|
| 空 | 直接解析响应为 JSON 数组 |
| `$.data.list` | 从嵌套结构中提取数组 |
| `$.data` | 单层结构提取 |

---

## 7. XML 序列化格式

### 7.1 数据源

```xml
<!-- 标准协议 -->
<datasource name="数据源名" type="http"
            protocol="standard"
            hostType="manual"
            host="http://192.168.1.100:8080"/>

<!-- 标准协议（服务发现） -->
<datasource name="数据源名" type="http"
            protocol="standard"
            hostType="discovery"
            serviceName="order-service"/>

<!-- 三方协议 -->
<datasource name="数据源名" type="http"
            protocol="thirdParty"
            baseUrl="https://api.example.com"/>
```

### 7.2 数据集

```xml
<!-- 标准协议数据集：仅名称 -->
<dataset name="用户列表" type="http"/>

<!-- 三方协议数据集：全配置 -->
<dataset name="天气数据" type="http"
         url="https://api.weather.com/current?city={{city}}"
         method="GET"
         headers='{"X-API-Key":"abc123"}'
         responsePath="$.data">
  <requestParameter name="page" value="1"/>
  <requestParameter name="size" value="20"/>
</dataset>
```

---

## 8. 前端界面

### 8.1 数据源配置对话框

| 配置项 | 标准协议 | 三方协议 |
|--------|----------|----------|
| 数据源名称 | ✅ | ✅ |
| 协议类型选择 | ✅ | ✅ |
| 主机方式（手动/服务发现） | ✅ | ❌ |
| 主机地址（手动录入） | ✅ | ❌ |
| 服务名（服务发现） | ✅ | ❌ |
| 请求地址 | ❌ | ✅ |

### 8.2 数据集配置对话框

| 配置项 | 标准协议 | 三方协议 |
|--------|----------|----------|
| 数据集名称 | ✅ | ✅ |
| 请求 URL | ❌（协议固定） | ✅ |
| 请求方法 | ❌ | ✅ |
| 请求头 | ❌ | ✅ |
| 请求参数 | ❌ | ✅ |
| 请求体（POST/PUT） | ❌ | ✅ |
| 响应 JSONPath | ❌ | ✅ |

树组件展示协议标签：`标准` / `三方`。

---

## 9. 后端架构

### 9.1 模块依赖

```
HttpService (core 接口)
  └── HttpServiceImpl (web 实现)
        └── RestTemplate (HTTP 客户端)

HttpDatasourceDefinition (core 模型)
  └── buildDatasets(HttpService) → List<Dataset>

DatasourceParser (core 解析)
  └── parse() → HttpDatasourceDefinition
```

### 9.2 请求执行流程

```
ReportBuilder.buildDatasets()
  → HttpDatasourceDefinition.buildDatasets(httpService, parameters)
    → 标准协议:
        → URL = host (manual) 或 http://serviceName (discovery)
        → httpService.executeStandard(config, dsName, datasetName, params)
          → POST {url} with body {datasourceName, datasetName, parameters}
          → parseStandardResponse()
          → 字段自动从响应 data 键名发现
    → 三方协议:
        → httpService.executeThirdParty(config, params)
          → resolveTemplate() (替换 {{paramName}})
          → appendQueryParams() (追加请求参数到 URL)
          → buildHeaders() (解析请求头 JSON)
          → HTTP 请求执行 (GET/POST/PUT/DELETE)
          → parseByJsonPath() → List<Map<String, Object>>
```

---

## 10. 配置示例

### 10.1 标准协议（手动）

```
数据源名称: 用户服务
协议类型:   标准协议（ReportBean）
主机方式:   手动录入
主机地址:   http://192.168.1.100:8080

数据集名称: 用户列表
（无需额外配置）
```

### 10.2 标准协议（服务发现）

```
数据源名称: 订单服务
协议类型:   标准协议（ReportBean）
主机方式:   服务发现
服务名:     order-service

数据集名称: 订单列表
（无需额外配置）
```

### 10.3 三方协议

```
数据源名称: 天气API
协议类型:   三方协议
请求地址:   https://api.weather.com/v1

数据集名称: 实时天气
请求URL:    /current?city={{city}}
请求方法:   GET
请求头:     {"X-API-Key": "abc123"}
响应JSONPath: $.data.weather
```

---

## 11. 文件清单

### 新增文件

| 文件 | 说明 |
|------|------|
| `core/.../dataset/HttpParameter.java` | 请求参数键值对模型 |
| `core/.../datasource/HttpService.java` | HTTP 执行服务接口 |
| `core/.../datasource/HttpRequestConfig.java` | 请求配置 DTO |
| `core/.../datasource/HttpDatasourceDefinition.java` | HTTP 数据源定义 |
| `core/.../dataset/HttpDatasetDefinition.java` | HTTP 数据集定义 |
| `web/.../http/HttpServiceImpl.java` | HTTP 服务实现 |
| `web/.../http/HttpConfig.java` | RestTemplate Bean 配置 |
| `ui/.../http-dialog/index.vue` | HTTP 数据源配置对话框 |
| `ui/.../http-tree/index.vue` | HTTP 数据源树组件 |
| `ui/.../http-dataset-dialog/index.vue` | HTTP 三方协议数据集配置对话框 |
| `ui/.../http-standard-dataset-dialog/index.vue` | HTTP 标准协议数据集配置对话框（类 SpringBean） |

### 修改文件

| 文件 | 变更 |
|------|------|
| `core/.../DatasourceType.java` | 新增 `http` 枚举 |
| `core/.../DatasourceParser.java` | 解析 HTTP XML 节点 |
| `core/.../ReportBuilder.java` | 集成 HttpDatasourceDefinition |
| `web/.../DatasourceController.java` | 新增测试/预览/代理端点；请求头转发；服务发现解析 |
| `web/.../http/HttpServiceImpl.java` | 新增 `executeProxyGet` 实现，支持请求头透传 |
| `ui/src/types/index.ts` | 新增协议类型、主机方式等字段 |
| `ui/.../table.ts` | HTTP XML 序列化 |
| `ui/.../datasource-panel/index.vue` | 集成 HTTP 按钮和树组件 |
| `ui/src/locales/lang/zh.js` | HTTP 相关中文文案 |
| `ui/src/locales/lang/en.js` | HTTP 相关英文文案 |

---

## 12. Proxy 代理机制

### 12.1 原理

标准协议的 metadata 接口（`report-beans`、`loadMethods`、`buildClass`）不直接由前端调用远程服务，而是通过报表服务端代理转发：

```
前端 → /api/datasource/httpStandardProxy
  → DatasourceController.httpStandardProxy()
    → 根据数据源配置构建目标 URL
    → 转发原始请求头（排除了 hop-by-hop 头）
    → httpService.executeProxyGet(url, headers)
      → RestTemplate.exchange() 发起 GET 请求
    → 解析标准响应 {code, data, message} 提取 data
  ← 返回结果给前端
```

### 12.2 地址构建

```
手动模式:  {host}/{servletPrefix}/{endpoint}?params
           → http://192.168.1.100:8080/report/report-beans

服务发现:  {resolvedHost}/{servletPrefix}/{endpoint}?params
           → http://10.0.1.5:8080/report/datasource/loadMethods?beanId=xxx
```

### 12.3 请求头转发

`httpStandardProxy` 自动从 `HttpServletRequest` 提取所有 header，过滤掉 hop-by-hop 头（host、connection、content-length、transfer-encoding、upgrade 等 9 种），其余全部透传到目标服务。确保认证头（Authorization、Cookie 等）在代理链路中不丢失。

### 12.4 响应处理

| 响应格式 | 处理方式 |
|----------|----------|
| JSON 数组 `[...]` | 直接返回（metadata 接口如 `report-beans`） |
| 标准协议 `{code, data, message}` | `code==200` 时提取 `data` 返回，否则抛异常 |
| 其他 JSON | 直接返回 |

---

## 13. 三方协议测试与字段发现（设计阶段，待实现）

### 13.1 背景

三方协议目前仅支持手动配置 URL/方法/头/体/JSONPath，用户无法在配置时查看实际接口返回的字段结构，类比 JDBC 数据源的「测试连接→自动发现字段」流程，需要在三方协议数据集配置中增加实时测试与字段自动发现能力。

### 13.2 交互流程

```
数据集配置面板填好
  → 检测 {{xxx}} 变量 → 动态生成测试参数输入框
  → 用户填写测试参数值
  → 点击 [测试并获取字段]
    → 前端校验：{{xxx}} 是否有未填的 → 提示
    → 请求后端 httpTestAndDiscover 端点
  → 后端调实际接口
    → 拼接完整 URL（baseUrl + 数据集 url）
    → 替换 {{xxx}} 为测试值
    → 继承数据源请求头
    → 发起 HTTP 请求
    → JSONPath 提取数据数组
    → 从 data[0] 键名推断字段列表 + 类型
    → 返回字段列表 + 原始响应
  → 前端展示
    → Status 码 + 耗时
    → 字段树（同 http-tree 递归 children 展开）
    → 原始响应（可折叠）
    → 错误信息内联显示（不弹对话框）
  → 用户保存 → 字段自动写入数据集定义
```

### 13.3 后端 API

```
POST /api/datasource/httpTestAndDiscover
```

请求体：

| 字段 | 来源 | 说明 |
|------|------|------|
| `baseUrl` | 数据源配置 | 数据源的 baseUrl |
| `headers` | 数据源配置 | 数据源的请求头，数据集继承复用 |
| `url` | 数据集配置 | 请求相对路径，支持 `{{paramName}}` |
| `method` | 数据集配置 | HTTP 方法 |
| `body` | 数据集配置 | 请求体模板（POST/PUT 时） |
| `responsePath` | 数据集配置 | 响应 JSONPath |
| `requestParameters` | 数据集配置 | URL query 参数键值对 |
| `parameters` | 测试参数 | `{{xxx}}` 替换值，含 requestParameters 值 |

响应：

```json
{
  "success": true,
  "status": 200,
  "elapsed": 235,
  "fields": [
    { "name": "orderId",   "label": "orderId",   "type": "Integer" },
    { "name": "totalAmount", "label": "totalAmount", "type": "BigDecimal" },
    { "name": "status",     "label": "status",     "type": "String" }
  ],
  "rawResponse": "{\"code\":0,\"data\":[{\"orderId\":1001,...}]}",
  "error": null
}
```

### 13.4 字段类型推断规则

| JSON 类型 | 映射类型 |
|-----------|---------|
| 整数（无小数） | `Integer` / `Long` |
| 浮点数（有小数） | `BigDecimal` / `Double` |
| 字符串 | `String` |
| 布尔 | `Boolean` |
| 数组 | `Array`（递归展开 children） |
| 对象 | `Object`（递归展开 children） |

### 13.5 请求头继承规则

数据集**不再单独配置请求头**，完全继承数据源的 `headers` 配置。数据源层级负责认证鉴权（API Key、Bearer Token 等），数据集层级只关注请求路径、参数和响应解析。

### 13.6 URL 地址拼接

```
完整 URL = baseUrl + dataset.url

示例:
  baseUrl:    https://api.example.com
  dataset:    /v1/users/{{userId}}/orders
  结果:       https://api.example.com/v1/users/{{userId}}/orders

测试时 {{userId}} 由测试参数替换
```

### 13.7 响应 JSONPath 为空处理

| 条件 | 行为 |
|------|------|
| 响应为 JSON 数组 | 直接解析为数据行 |
| 响应为 JSON 对象（非数组） | 提示用户"请配置 JSONPath" |
| `responsePath` 已配置 | 按 JSONPath 提取 |

### 13.8 面板布局

```
┌─────────────────────────────────────────┐
│  数据集名称:  [__________________]       │
│                                         │
│  请求路径:    [/v1/users/{{userId}}/orders]
│  请求方式:    [GET ▾]                   │
│                                         │
│  ── 请求参数 ──                          │
│  [参数名] [参数值]          [ + ]        │
│  [page  ] [1        ]       [ − ]        │
│                                         │
│  ── 请求体 (POST/PUT) ──                │
│  [___________________________________]  │
│                                         │
│  响应JSONPath: [$.data.list      ]       │
│                                         │
│  ── 测试参数 ──                          │
│  ⇢ 检测到模板变量:                       │
│  userId: [__________]   ← {{userId}}     │
│                                         │
│  [🚀 测试并获取字段]                      │
│                                         │
│  ── 测试结果 ──                          │
│  ✅ 200 OK · 235ms                      │
│                                         │
│  字段列表:                               │
│  ├─ orderId        Integer              │
│  ├─ totalAmount    BigDecimal           │
│  ├─ status         String               │
│  └─ items          Array                │
│      ├─ name       String               │
│      └─ price      BigDecimal           │
│                                         │
│  ▶ 原始响应（点击展开）                   │
│                                         │
│  ⚠ 错误信息内联显示（如有）              │
├─────────────────────────────────────────┤
│         [取消]          [保存]           │
└─────────────────────────────────────────┘
```

### 13.9 涉及改动文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `core/.../datasource/HttpService.java` | 新增方法 | `testAndDiscover()` 接口方法 |
| `core/.../datasource/HttpRequestConfig.java` | 扩展 | 添加 `parameters` 字段用于模板替换 |
| `web/.../http/HttpServiceImpl.java` | 新增方法 | 实现：拼接 URL → 替换模板 → 继承头 → 请求 → JSONPath → 字段推断 |
| `web/.../controller/designer/DatasourceController.java` | 新增端点 | `httpTestAndDiscover` |
| `ui/.../http-dataset-dialog/index.vue` | 重写 | 集成测试按钮 + 动态参数输入 + 字段树展示 + 原始响应折叠 |
| `ui/.../http-tree/index.vue` | 引用 | 复用字段树渲染逻辑（展开/折叠/右键菜单） |
