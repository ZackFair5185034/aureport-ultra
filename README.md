# Aureport Ultra

基于 [UReport2](https://github.com/youseries/ureport)（Apache-2.0）设计思想重构的高性能 Java 中国式报表引擎，继承自 [luck-report](https://gitee.com/LuckyPools/luck-report)。

## 项目概述

| 维度 | 数据 |
|------|------|
| 定位 | Java 报表引擎 + Vue 3 可视化设计器 |
| 技术栈 | SpringBoot 3.2 + Java 17 + Vue 3 + Vite 8 + TypeScript |
| 后端模块 | 6 个 Maven 模块（core/web/font/pub/bean-discovery-starter/cloud-starter）|
| Java 文件 | 524 个，约 49k 行 |
| Vue 组件 | 143 个 |
| 后端 Controller | 12 个 |
| 数据库支持 | MySQL / Oracle / SQLServer / 达梦（仅用作数据源）|

**核心架构**：报表定义以 `.ureport.xml` 文件存储，**不是数据库存储**。数据库连接仅用于执行用户配置的 SQL 数据集查询。

## 技术继承说明

**继承链**：
```
UReport2 (Apache-2.0) → luck-report (Apache-2.0) → Aureport Ultra (Apache-2.0)
```

- [UReport2](https://github.com/youseries/ureport) — 第一个基于 Apache-2.0 的中国式报表引擎（已停止维护约 5 年）
- [luck-report](https://gitee.com/LuckyPools/luck-report) — 基于 UReport2 的 SpringBoot + Vue 重构版本，Aureport Ultra 的重要参考
- Aureport Ultra — 在 luck-report 基础上进一步升级技术栈（SpringBoot 3 + Vue 3），大量新功能

### 技术栈升级对比

| 维度 | UReport2 | luck-report | Aureport Ultra |
|------|----------|-------------|----------------|
| 后端框架 | Spring 4 | SpringBoot 2 | SpringBoot 3.2.5 |
| 前端框架 | 无（后端渲染） | Vue 2 | Vue 3 + Composition API |
| 构建工具 | Maven | Maven | Maven + Vite 8 |
| 表达式引擎 | ANTLR3 | ANTLR3 | ANTLR4 |
| Java 版本 | 8 | 8/11 | 17 |
| 表格编辑器 | jQuery 插件 | Handsontable | Handsontable 6.2.2 |

## 核心功能

### 核心引擎亮点

- ✅ **ANTLR4 表达式引擎**：比 UReport2 的 ANTLR3 语法解析更强大
- ✅ **嵌套迭代聚合（IterateAggregate）**：解决一对多主子表场景，UReport2/luck-report 均不支持
- ✅ **HTTP 数据源**：标准协议（类 SpringBean 远程调用）+ 三方协议（JSONPath 解析）
- ✅ **SpringBean 自动发现**：通过 `@ReportBean` 注解 + Starter 自动暴露 REST 端点
- ✅ **进度条单元格（ProgressBarValue）**：内置百分比进度条展示
- ✅ **Tooltip 悬浮提示**：单元格鼠标悬停显示提示
- ✅ **GroupStatAggregate 分组聚合**：从父格 BindData 读取统计字段
- ✅ **父子格循环引用修复**：解决 StackOverflowError 问题
- ✅ **四向单元格扩展**（左/右/上/下 expand + fill）
- ✅ **多数据库支持**：MySQL、Oracle、SQLServer、达梦
- ✅ **多格式导出**：Excel（POI 5.x）、PDF（iText 5.x）、Word、图片、HTML
- ✅ **图表引擎**：Chart.js 柱状/折线/饼/散点/气泡图
- ✅ **二维码/条形码**：ZXing 生成
- ✅ **条件格式**：颜色/字体/边框/链接/分页等动态样式
- ✅ **SQL 方言转换**：自动适配不同数据库语法

### 报表设计器（Vue 3 前端）

- ✅ **Handsontable 表格编辑器**：类 Excel 交互体验，支持 50+ 工具栏按钮
- ✅ **属性面板**：单元格属性、数据集配置、条件格式、条件样式
- ✅ **数据源管理**：SQL 数据集、Bean 数据集、HTTP 数据集（标准/三方协议）、内建数据源
- ✅ **搜索表单设计器**：拖拽式表单项布局
- ✅ **报表预览**：HTML 预览、打印、分页
- ✅ **Web Component 独立部署**：可嵌入任意前端项目
- ✅ **SpringBean 嵌套字段树**：递归展开 + `@FieldDesc` 中文描述
- ✅ **HTTP 数据源树**：协议标签内联显示，字段递归展开/折叠
- ✅ **TypeScript 全面类型安全**：严格模式零错误
- ⚠️ **交叉表（Pivot Table）**：待开发

### 企业功能规划

| 版本 | 功能 | 状态 |
|------|------|------|
| v1.0.0 | UReport2 基线功能恢复 | ✅ 已完成 |
| v1.1.0 | 嵌套迭代聚合 + 搜索表单修复 | ✅ 已完成 |
| v1.2.0 | HTTP 数据源 + SpringBean 增强 + WebComponent | ✅ 已完成 |
| v1.3.0 | 用户权限 + 定时任务 + 报表订阅 | 🔄 规划中 |
| v2.0.0 | 集群部署 + 插件机制 + REST API 全面开放 | 🔄 规划中 |

## 快速启动

### 后端启动

```bash
cd aureport-ultra-server/aureport-ultra-pub
mvn spring-boot:run

# 验证
curl http://localhost:8050/report/datasource/loadBuildinDatasources
```

### 前端启动

```bash
cd aureport-ultra-ui
npx vite --port 3000 --host
```

### 访问

- 报表设计器：http://localhost:3000/report/designer
- 报表预览：http://localhost:3000/report/preview?reportPath=file:报表名.ureport.xml

## 项目结构

```
aureport-ultra/
├── aureport-ultra-server/     # Java 后端（Maven 多模块）
│   ├── aureport-ultra-core/           # 核心引擎（纯 Java，无 Spring 依赖）
│   │   ├── build/                      # 报表构建器（aggregate/cell/compute/paging）
│   │   ├── chart/                      # 图表引擎
│   │   ├── dsl/                        # ANTLR4 语法定义
│   │   ├── expression/                 # 表达式解析
│   │   ├── model/                      # 数据模型
│   │   ├── parser/                     # 报表 XML 解析
│   │   ├── provider/                   # 报表提供者（classpath/file/spring）
│   │   ├── cache/                      # 缓存接口
│   │   └── export/                     # 导出（Excel/PDF/Word/HTML）
│   ├── aureport-ultra-web/            # SpringBoot Web 层（12 个 Controller）
│   │   ├── controller/                 # 设计器/预览/导出/图表等 API
│   │   ├── config/                     # Spring 配置（数据源、SQL 方言）
│   │   └── sql/                        # 数据库方言（MySQL/Oracle/SQLServer/达梦）
│   ├── aureport-ultra-font/           # 字体资源模块
│   ├── aureport-ultra-pub/            # 启动入口，打包为可执行 jar
│   ├── aureport-ultra-bean-discovery-starter/  # Bean 自动发现（@ReportBean）
│   └── aureport-ultra-cloud-starter/   # 云服务支持（Nacos 服务发现）
├── aureport-ultra-ui/        # Vue 3 前端（Vite 8）
│   ├── src/
│   │   ├── api/                       # TypeScript API（Axios）
│   │   ├── components/                # Vue 3 组件（全局自动导入）
│   │   ├── stores/                    # Pinia Store（report/designer）
│   │   ├── views/report/
│   │   │   ├── designer/              # 报表设计器（edit-table/tool-bar/resource-panel）
│   │   │   └── preview/               # 报表预览
│   │   └── lib/                       # Web Component（LuckDesigner/LuckPreview）
├── reports/                   # 报表定义文件存储目录（运行时创建）
├── docs/                      # 项目文档
│   ├── 调研/                   # 竞品分析、技术选型
│   ├── 开发计划/               # 功能路线图
│   ├── 技术方案/               # 架构设计、ADR、HTTP 数据源方案
│   ├── 开发进度/               # CHANGELOG
│   └── 用户手册/               # 设计器指南、运维部署
├── LICENSE                    # Apache-2.0
└── NOTICE                     # 继承关系声明
```

## 技术栈

### 后端

| 类别 | 技术 |
|------|------|
| Java | 17 |
| 框架 | SpringBoot 3.2.5 |
| 构建 | Maven（6 个模块） |
| 表达式解析 | ANTLR4 |
| Excel 导出 | Apache POI 5.x |
| PDF 导出 | iText 5.x |
| 二维码/条形码 | ZXing |
| 图表 | Chart.js |
| 缓存 | Caffeine |
| SQL 解析 | SqlParser |
| API 文档 | springdoc-openapi |
| 服务发现 | Nacos（可选） |

### 前端

| 类别 | 技术 |
|------|------|
| 框架 | Vue 3.5 + Composition API |
| 语言 | TypeScript 5.8（严格模式） |
| 构建 | Vite 8 |
| 状态管理 | Pinia 2 |
| 路由 | Vue Router 4 |
| 国际化 | Vue I18n 10 |
| 表格编辑器 | Handsontable 6.2.2 |
| 图表 | Chart.js 4 |
| 样式 | UnoCSS + SCSS |

## 与竞品对比

| 维度 | FineReport | 润乾 | UReport2 | Aureport Ultra |
|------|-----------|------|----------|----------------|
| License | 商业付费 | 商业付费 | Apache 2.0 | Apache 2.0 |
| 源码开放 | ❌ | ❌ | ✅ | ✅ |
| Vue 3 前端 | ❌ | ❌ | ❌ | ✅ |
| 嵌套迭代聚合 | ⚠️ | ⚠️ | ❌ | ✅ |
| HTTP 数据源 | ✅ | ✅ | ❌ | ✅ |
| SpringBean 自动发现 | ✅ | ✅ | ❌ | ✅ |
| ANTLR4 | ❌ | ❌ | ❌ | ✅ |
| 交叉表 | ✅ | ✅ | ❌ | ❌ |
| 集群部署 | ✅ | ✅ | ❌ | ❌ (v2.0 规划) |

## License

Apache License 2.0 - 详见 [LICENSE](./LICENSE)

## NOTICE

Aureport Ultra includes software derived from [UReport2](https://github.com/youseries/ureport) and references [luck-report](https://gitee.com/LuckyPools/luck-report), both originally licensed under Apache-2.0.

继承链：UReport2 → luck-report → Aureport Ultra

- **UReport2**：https://github.com/youseries/ureport | Apache-2.0 | Copyright 2017 Bstek
- **luck-report**：https://gitee.com/LuckyPools/luck-report | Apache-2.0 | 基于 UReport2 的 SpringBoot + Vue 重构

Aureport Ultra 对 UReport2 的架构和核心设计思想进行了深度重构，包名从 `com.bstek.ureport` 改为 `com.aureport.ultra`，技术栈从 Spring Boot 2 升级到 Spring Boot 3，前端从 Vue 2 升级到 Vue 3。