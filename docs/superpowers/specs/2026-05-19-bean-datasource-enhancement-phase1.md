# Phase 1 设计：SpringBean 数据源配置增强 - 本地 Bean 发现

**日期**：2026-05-19
**状态**：草稿

---

## 1. 背景与目标

当前 Spring Bean 数据源配置需要用户手动输入 `beanId`，无提示、无校验，体验差且无法支持跨服务场景。

本阶段（Phase 1）聚焦于：**本地 Spring Bean 发现 + 前端选择 UI**，建立插件化基础，后续可扩展跨服务调用和独立部署。

---

## 2. 核心接口

### 2.1 ReportBeanMarker 标记接口

所有实现了此接口的 Spring Bean 都会被 `/api/report-beans` 枚举。

```java
package com.aureport.ultra.core.bean;

/**
 * 标记接口：实现此接口的 Spring Bean 可被报表引擎识别为可用数据源 Bean。
 * 前端下拉列表将展示实现了此接口的所有 Bean。
 */
public interface ReportBeanMarker {
    /**
     * 前端显示的 Bean 名称。
     * 若未重写，默认使用 Spring Bean 的 beanId。
     */
    default String name() {
        return null; // null 表示 fallback 为 beanId
    }
}
```

**使用示例**：
```java
@Service
public class UserService implements ReportBeanMarker {
    @Override
    public String name() {
        return "用户服务"; // 前端显示此名称
    }
    // ...
}
```

### 2.2 REST 接口

```
GET /api/report-beans

响应：
[
  { "beanId": "userService", "name": "用户服务", "className": "com.example.UserService" },
  { "beanId": "orderService", "name": "订单服务", "className": "com.example.OrderService" }
]
```

- `beanId` — Spring Bean 名称（用于 lookup）
- `name` — 前端显示名称（来自 `ReportBeanMarker.name()`，若无则用 `beanId`）
- `className` — 类的完全限定名（用于调试/展示）

---

## 3. 模块结构

### 3.1 目录

```
aureport-ultra-server/
├── aureport-ultra-bean-discovery-starter/     ← 新模块
│   ├── pom.xml
│   └── src/main/java/com/aureport/ultra/bean/
│       ├── ReportBeanMarker.java              ← 标记接口
│       ├── ReportBeanEndpoint.java             ← REST 控制器
│       ├── ReportBeanService.java              ← 扫描逻辑
│       └── ReportBeanDiscoveryAutoConfig.java  ← Spring Boot 自动配置
```

### 3.2 依赖关系

- `aureport-ultra-bean-discovery-starter` 依赖 `aureport-ultra-core`（无 Spring 依赖的核心）
- `aureport-ultra-web` 依赖 `aureport-ultra-bean-discovery-starter`
- `aureport-ultra-pub`（Spring Boot 入口）通过 `aureport-ultra-web` 传递依赖

### 3.3 独立部署支持

通过 `mvn clean install` 可单独构建此 starter，输出 `aureport-ultra-bean-discovery-starter-{version}.jar`，供其他 Spring Boot 项目引入：

```xml
<dependency>
    <groupId>com.aureport.ultra</groupId>
    <artifactId>aureport-ultra-bean-discovery-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

引入后自动暴露 `/api/report-beans` 端点（通过 Spring Boot 自动配置）。

---

## 4. 前端改造

### 4.1 交互流程

```
1. 用户点击"添加 Spring 数据源"
2. 弹窗中出现 Bean 下拉选择框（替代原来的手动输入 beanId）
3. 用户选择 Bean 后 → 自动调用 /datasource/loadMethods?beanId=xxx 获取方法列表
4. 用户选择方法 → 自动填充 method + clazz + 默认 name（可修改）
5. 确认保存
```

### 4.2 改造范围

- `spring-dialog/index.vue` — `beanId` 输入框 → 下拉选择框
- `bean-method-dialog/index.vue` — 选择方法后自动填充 `clazz` 和 `name`
- `datasource-panel/index.vue` — 调用 `/api/report-beans` 获取 Bean 列表

### 4.3 新增 API

```typescript
// GET /api/report-beans
interface ReportBeanInfo {
  beanId: string
  name: string
  className: string
}

export function listReportBeans(): Promise<ReportBeanInfo[]> {
  return get('/api/report-beans')
}
```

---

## 5. 实现步骤

### Step 1：创建 Maven 模块

在 `aureport-ultra-server/` 下创建 `aureport-ultra-bean-discovery-starter` 子模块。

### Step 2：定义 ReportBeanMarker 接口

放在 `com.aureport.ultra.bean` 包下。

### Step 3：实现 ReportBeanService

```java
public class ReportBeanService {
    public List<ReportBeanInfo> listReportBeans(ApplicationContext ctx) {
        Map<String, ReportBeanMarker> beans = ctx.getBeansOfType(ReportBeanMarker.class);
        return beans.entrySet().stream().map(e -> {
            String name = e.getValue().name();
            return new ReportBeanInfo(e.getKey(), name != null ? name : e.getKey(),
                                       e.getValue().getClass().getName());
        }).collect(Collectors.toList());
    }
}
```

### Step 4：实现 ReportBeanEndpoint

`@RestController` `/api/report-beans`，注入 `ApplicationContext`。

### Step 5：Spring Boot 自动配置

`ReportBeanDiscoveryAutoConfig` 注册为 `@Configuration`，扫描 `ReportBeanMarker` 和 `ReportBeanEndpoint`。

### Step 6：前端改造

- 添加 `/api/report-beans` 接口调用
- `spring-dialog` 的 `beanId` 字段改为下拉选择
- `bean-method-dialog` 增加自动填充逻辑

---

## 6. 涉及文件清单

### 后端（新建）

| 文件 | 说明 |
|------|------|
| `aureport-ultra-bean-discovery-starter/pom.xml` | Maven 模块配置 |
| `aureport-ultra-bean-discovery-starter/.../ReportBeanMarker.java` | 标记接口 |
| `aureport-ultra-bean-discovery-starter/.../ReportBeanInfo.java` | DTO |
| `aureport-ultra-bean-discovery-starter/.../ReportBeanService.java` | 扫描服务 |
| `aureport-ultra-bean-discovery-starter/.../ReportBeanEndpoint.java` | REST 端点 |
| `aureport-ultra-bean-discovery-starter/.../ReportBeanDiscoveryAutoConfig.java` | 自动配置 |
| `aureport-ultra-server/pom.xml` | 添加 module 引用 |

### 后端（修改）

| 文件 | 说明 |
|------|------|
| `aureport-ultra-web/pom.xml` | 添加 starter 依赖 |

### 前端（修改）

| 文件 | 说明 |
|------|------|
| `src/api/designer/index.ts` | 新增 `listReportBeans()` |
| `src/views/.../spring-dialog/index.vue` | beanId 输入 → 下拉选择 |
| `src/views/.../bean-method-dialog/index.vue` | 自动填充 name + clazz |
| `src/types/modules.d.ts` | 新增 `ReportBeanInfo` 类型 |

---

## 7. Phase 1 范围外（后续扩展）

- 跨服务调用 Bean（Feign / RestTemplate）
- 服务发现方式添加第三方服务
- 独立 starter 的 Spring Boot Starter 自动配置（SPI）

---

## 8. 成功标准

1. 用户打开 Spring 数据源对话框，能看到所有实现了 `ReportBeanMarker` 的 Bean 的下拉列表
2. 选择 Bean 后方法列表自动加载，选择方法后 method + clazz + name 自动填充
3. starter 模块可单独 `mvn clean install`，被其他 Spring Boot 项目引入后能正常工作