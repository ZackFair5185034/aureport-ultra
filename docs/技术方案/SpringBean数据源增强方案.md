# SpringBean 数据源增强方案

> 支持嵌套 Bean 属性递归展开 + `@FieldDesc` 注解提供字段中文描述

## 1. 背景

### 1.1 问题现状

SpringBean 数据源存在两个限制：

1. **无字段描述**：当前 `Field.java` 只有一个 `name` 属性，前端数据集配置时只能看到英文/驼峰字段名（如 `userName`），无法显示中文含义。
2. **不支持嵌套 Bean**：如果 Bean 属性是一个复杂对象（如 `Address address`），无法展开查看子属性（如 `address.city`、`address.street`）。

### 1.2 用户需求

- 在 Java Bean 上添加注解标注字段中文描述，前端同时显示 `字段值` 和 `描述文字`
- 递归展开嵌套 Bean 的子属性，支持多层嵌套
- 不影响现有 XML 序列化格式的向后兼容

## 2. 方案描述

### 2.1 注解定义

```java
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldDesc {
    String value();
}
```

用户在自己的 Bean 上使用：

```java
public class User {
    @FieldDesc("用户姓名")
    private String name;

    @FieldDesc("年龄")
    private Integer age;

    @FieldDesc("地址信息")
    private Address address;
}

public class Address {
    @FieldDesc("城市")
    private String city;

    @FieldDesc("街道")
    private String street;
}
```

### 2.2 字段模型增强

`com.aureport.ultra.core.definition.dataset.Field` 新增属性：

```java
public class Field {
    private String name;
    private String label;          // 来自 @FieldDesc 的描述文字
    private String type;           // 字段类型的全限定类名
    private List<Field> children;  // 嵌套 Bean 的子字段

    public Field(String name) {
        this(name, null, null, null);
    }

    public Field(String name, String label, String type, List<Field> children) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.children = children;
    }
}
```

### 2.3 后端改造

#### DatasourceController.buildClass()

核心逻辑变更：

1. 使用 `PropertyUtils.getPropertyDescriptors()` 遍历属性
2. 对每个属性，获取 `pd.getPropertyType()` 确定类型
3. 通过反射检查 getter 方法上的 `@FieldDesc` 注解（优先），再检查字段上的 `@FieldDesc`
4. 如果是复杂类型（非原始、非 `java.lang` 包、非 `java.util` 常用类型），递归构建子字段
5. 设置递归深度限制（默认 3 层），防止循环引用

返回结构示例：

```json
[
  {"name": "name", "label": "用户姓名", "type": "java.lang.String"},
  {"name": "age", "label": "年龄", "type": "java.lang.Integer"},
  {"name": "address", "label": "地址信息", "type": "com.example.Address",
    "children": [
      {"name": "city", "label": "城市", "type": "java.lang.String"},
      {"name": "street", "label": "街道", "type": "java.lang.String"}
    ]
  }
]
```

#### 用户 Bean 的 `@FieldDesc` 扫描

```java
// 优先从 getter 方法上读
FieldDesc desc = pd.getReadMethod().getAnnotation(FieldDesc.class);
if (desc == null) {
    // 回退到字段上读
    try {
        Field field = targetClass.getDeclaredField(pd.getName());
        desc = field.getAnnotation(FieldDesc.class);
    } catch (NoSuchFieldException ignored) {}
}
```

#### DatasourceParser XML 序列化

Parser 序列化/反序列化时增加 `label` 属性：

```xml
<!-- 旧格式（向后兼容） -->
<field name="userName"/>

<!-- 新格式 -->
<field name="userName" label="用户姓名"/>
```

Parser 读取时：`label` 属性不存在时保持为 `null`，兼容旧版报表文件。

### 2.4 复杂类型判定

判断一个类型是否应该递归展开：

| 判定 | 规则 |
|------|------|
| 不展开 | `String`、`Number` 子类、`Boolean`、`Date`、`Enum`、数组、`Collection`、`Map` |
| 展开 | 其他 POJO 类型（自定义类） |

实现方式：白名单 + 黑名单组合。黑名单过滤掉 `java.*`、`javax.*` 等标准库类型，白名单仅对自定义类型展开。

### 2.5 安全保护

| 保护机制 | 实现 |
|----------|------|
| 递归深度限制 | `maxDepth = 3`，超过深度停止展开 |
| 循环引用检测 | 使用 `Set<Class<?>> visited` 记录已展开类型，防止 A→B→A 循环 |
| 大数据类型跳过 | `java.lang.Iterable`、`Map` 类型不展开内容 |

### 2.6 前端改造

#### API 类型定义

```typescript
export interface FieldInfo {
  name: string
  label?: string
  type?: string
  children?: FieldInfo[]
}
```

`buildClass()` 返回类型由 `Promise<unknown>` 改为 `Promise<FieldInfo[]>`。

#### spring-tree 展示

```
数据源名称
├── 数据集1
│   ├── name (用户姓名)
│   ├── age (年龄)
│   └── address (地址信息)
│       ├── city (城市)
│       └── street (街道)
```

- 有 `children` 的字段显示为可展开的树节点
- 字段显示格式：`字段名 (label)`，有 children 时加小箭头
- 双击叶子节点（无 children）插入单元格值表达式 `数据集名.字段名`

#### field-name-dialog

增加 label 输入框，允许手动添加字段时同时填写描述。

## 3. 权衡分析

### 方案对比

| 方案 | 优点 | 缺点 |
|------|------|------|
| **A. @FieldDesc 注解**（选中的方案） | 侵入性低、编译期安全、IDE 友好 | 需要用户在自己的 Bean 上添加注解 |
| **B. @JsonProperty + @Schema** | 复用已有 Jackson 注解生态 | 描述信息分散、非统一标准 |
| **C. 单独 properties 文件映射** | 不解耦 Java 代码 | 维护成本高、不同步、运行时额外 IO |
| **D. 字段名自动转换（驼峰→中文）** | 零配置 | 语义不可控、无法覆盖复杂命名 |

### 选择 A 的原因

- 注解直接标注在字段上，与代码同源，不会不同步
- 编译期可见、IDE 自动补全支持
- 不引入外部依赖
- 对现有代码影响最小

## 4. 依赖影响

| 模块 | 影响 |
|------|------|
| `aureport-ultra-core` | `Field.java` 新增字段 + 新注解 + 注解扫描逻辑（无 Spring 依赖）|
| `DatasourceController.java` | `buildClass()` 方法增强 |
| `DatasourceParser.java` | 新增 `label` 属性读写 |
| `前端 spring-tree` | 嵌套展示 + label 显示 |
| `前端 field-name-dialog` | 增加 label 输入 |
| `前端 api/designer/index.ts` | FieldInfo 类型更新 |
| `报表 XML 格式` | 新增可选 `label` 属性 |
| XML 序列化 | `label` 属性可选读取，向后兼容旧版 |

## 5. 实施计划

### 步骤 1：后端核心模型
- 创建 `@FieldDesc` 注解
- 增强 `Field.java`（label, type, children）
- 实现注解扫描工具方法

### 步骤 2：后端端点改造
- 重写 `DatasourceController.buildClass()`：支持递归 + 注解扫描
- 实现复杂类型判定 + 递归深度限制 + 循环引用检测

### 步骤 3：Parser 序列化
- `DatasourceParser.parseFields()` 增加 `label` 属性读取
- `Field` → XML 序列化时输出 `label`

### 步骤 4：前端类型与 API
- 更新 `FieldInfo` 接口定义
- 更新 `spring-tree` 嵌套展示和 label 显示
- 更新 `field-name-dialog` 增加 label 输入

## 6. 涉及文件清单

### 后端
- `aureport-ultra-core/.../definition/dataset/Field.java` — 增强
- `aureport-ultra-core/.../annotation/FieldDesc.java` — 新建
- `aureport-ultra-core/.../parser/impl/DatasourceParser.java` — 增强
- `aureport-ultra-web/.../DatasourceController.java` — buildClass 重写

### 前端
- `src/api/designer/index.ts` — FieldInfo 类型更新
- `src/views/.../spring-tree/index.vue` — 嵌套展示 + label
- `src/views/.../field-name-dialog/index.vue` — label 输入

### 文档
- `docs/技术方案/SpringBean数据源增强方案.md` — 本文
- `docs/开发计划/功能路线图.md` — 更新任务项
