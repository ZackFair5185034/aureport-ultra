# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Aureport Ultra 是一款基于开源项目 UReport2 重构的 Java 高性能报表引擎，基于 SpringBoot 3.2.5 + Vue 3 构建，支持复杂中国式报表设计。

## 项目结构

```
aureport-ultra/
├── aureport-ultra-server/     # Java 后端（SpringBoot 3.2.5）
└── aureport-ultra-ui/         # Vue 3 前端（Vite 8）
```

## 后端构建与运行

```bash
# 编译整个项目
cd aureport-ultra-server && mvn clean package -DskipTests

# 运行（入口模块）
cd aureport-ultra-server/aureport-ultra-pub && mvn spring-boot:run

# 单独编译某个模块
mvn clean install -pl aureport-ultra-core -am -DskipTests
```

JDK >= 17，Maven 构建。

## 前端构建与运行

```bash
cd aureport-ultra-ui

# 开发
pnpm dev          # 启动 Vite dev server（默认 8080 端口）

# 构建生产版本
pnpm build        # vue-tsc 类型检查 + vite build

# 类型检查
pnpm typecheck    # vue-tsc --noEmit

# 预览构建产物
pnpm preview
```

## 前端技术栈

| 类别 | 技术 |
|---|---|
| 框架 | Vue 3.5.x（`<script setup lang="ts">`） |
| 构建工具 | Vite 8（Rollydown） |
| 语言 | TypeScript 5.8（严格模式） |
| 路由 | Vue Router 4.x |
| 状态管理 | Pinia 2.x |
| 国际化 | Vue I18n 10.x |
| UI 组件 | OverSnail UI（自动导入） |
| 样式 | UnoCSS + SCSS |
| 图表 | Chart.js 4.x |
| 表格编辑器 | Handsontable 6.2.2 |
| 代码编辑器 | CodeMirror 5.x |

## 前端架构

```
aureport-ultra-ui/src/
├── main.ts                    # Vue 3 入口（createApp）
├── App.vue
├── router/index.ts            # Vue Router 4
├── locales/index.ts           # Vue I18n 10 Composition API
├── stores/                   # Pinia Store
│   ├── report.ts             # 报表上下文状态
│   └── designer.ts           # 设计器状态
├── api/                      # TypeScript API（Axios）
├── components/               # Vue 3 组件（全局自动导入，无需手动 import）
├── utils/                    # TypeScript 工具函数
├── views/
│   ├── report/
│   │   ├── designer/         # 报表设计器页面
│   │   └── preview/          # 报表预览页面
├── lib/                      # Web Component（LuckDesigner/LuckPreview）
└── types/modules.d.ts        # .js 模块类型声明
```

## 后端模块架构

```
aureport-ultra-server/
├── aureport-ultra-core/        # 核心引擎（无 Spring 依赖，纯 Java）
│   ├── build/               # 报表构建器（单元格、聚合、分页）
│   ├── chart/               # 图表引擎（柱状图、折线图、饼图等）
│   ├── cache/               # 报表定义缓存接口
│   ├── dsl/                 # ANTLR4 语法定义（ReportLexer.g4, ReportParser.g4）
│   └── engine/              # 报表解析与渲染引擎
├── aureport-ultra-web/         # Spring Boot Web 层
│   ├── controller/          # 控制器（设计器、导出、预览）
│   ├── config/              # Spring 配置（数据源、SQL 方言）
│   ├── sql/                 # 数据库方言（MySQL/Oracle/SQLServer/达梦）
│   └── cache/               # HttpSession 缓存实现
├── aureport-ultra-font/         # 报表字体资源模块
└── aureport-ultra-pub/          # Spring Boot 启动入口，打包为可执行 jar
```

**核心依赖关系**：`aureport-ultra-core` <- `aureport-ultra-web` <- `aureport-ultra-pub`，前端资源（Vue）在 aureport-ultra-font 模块中管理。

## 核心设计

- **DSL 引擎**：基于 ANTLR4 定义的报表语法（`ReportLexer.g4` / `ReportParser.g4`），解析报表单元格表达式
- **单元格渲染**：通过 `CellRenderer` 接口将报表单元格渲染为 Excel/PDF/HTML
- **图表引擎**：图表配置由 `Chart` 类管理，数据绑定通过 `ChartData` 处理，支持多种图表类型
- **数据绑定**：`BindData` -> `Dataset` -> `DatasetUtils` 链路完成数据集解析与数据填充
- **聚合计算**：`aggregate/` 包下实现 SUM/AVG/COUNT/MAX/MIN/CUSTOM GROUP 等聚合
- **缓存机制**：`ReportDefinitionCache` 接口定义报表定义缓存，默认使用内存缓存
- **导出支持**：Excel（POI）、PDF（iText）、Word 多格式导出

## 关键配置

- `AureportUltraMainConfig`（aureport-ultra-web）：Spring 主配置，扫描 `com.aureport.ultra` 包
- `application.yml`（aureport-ultra-pub）：数据库连接等运行时配置
- `DialectFactory`：根据数据库类型选择对应 SQL 方言，支持 MySQL/Oracle/SQLServer/达梦
- `vite.config.ts`：Vite 构建配置，包含 `@babel/polyfill` shim（解决 handsontable@6.2.2 引用废弃包问题）
- `.env.development` / `.env.production`：前端环境变量（API 地址、端口、公共路径）

## 前端开发规范

### 组件

- 使用 `<script setup lang="ts">` 风格
- 组件放置在 `src/components/` 目录下，每个组件一个目录
- 组件自动导入，无需手动 `import`
- Props 使用 `defineProps`，Emits 使用 `defineEmits`
- 需要暴露方法给父组件时使用 `defineExpose`

### Pinia Store

- 放置在 `src/stores/` 目录
- 使用 `defineStore` + Composition API 风格
- 不要混用 Vuex 模式

### API

- 放置在 `src/api/` 目录
- 使用 TypeScript，返回类型明确
- 统一使用 Axios

### 动态组件渲染

- `renderTemplateToComponent` 使用 `createApp` 而非 `new Vue`
- 旧的 Vue 2 `Vue.extend` / `new Vue({render}).$mount()` 模式已废弃

## 迁移记录

- **2026.05**：完成 Vue 2 + Vue CLI → Vue 3 + Vite 8 全量迁移，详见 `aureport-ultra-ui/MIGRATION.md`
