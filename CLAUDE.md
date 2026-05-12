# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Aureport Ultra 是一款基于开源项目 UReport2 重构的 Java 高性能报表引擎，基于 SpringBoot 3.2.5 + Vue 构建，支持复杂中国式报表设计。

## 构建与运行

```bash
# 编译整个项目
cd aureport-ultra-server && mvn clean package -DskipTests

# 运行（入口模块）
cd aureport-ultra-server/aureport-ultra-pub && mvn spring-boot:run

# 单独编译某个模块
mvn clean install -pl aureport-ultra-core -am -DskipTests
```

JDK >= 17，Maven 构建。

## 模块架构

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
