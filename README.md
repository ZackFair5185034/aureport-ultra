# Aureport Ultra

基于 [UReport2](https://github.com/youseries/ureport)（Apache-2.0）设计思想重构的高性能 Java 中国式报表引擎。

## 项目概述

| 维度 | 数据 |
|------|------|
| 定位 | Java 报表引擎 + Vue 3 可视化设计器 |
| 技术栈 | SpringBoot 3.2 + Java 17 + Vue 3 + Vite + TypeScript |
| 后端模块 | 5 个 Maven 模块（core/web/font/pub/bean-discovery-starter）|
| Java 文件 | 524 个，约 49k 行 |
| 前端组件 | 120+ Vue 组件 |
| 数据库支持 | MySQL / Oracle / SQLServer / 达梦（仅用作数据源）|

**核心架构**：报表定义以 `.ureport.xml` 文件存储，**不是数据库存储**。数据库连接仅用于执行用户配置的 SQL 数据集查询。

## 技术继承说明

Aureport Ultra 起源于对 [UReport2](https://github.com/youseries/ureport) 的深度重构：

- **包名重命名**：`com.bstek.ureport` → `com.aureport.ultra`
- **技术栈升级**：Spring Boot 2 → SpringBoot 3，Vue 2 → Vue 3
- **架构重写**：核心引擎完全重写，移除对旧版本 Spring 的依赖
- **新增功能**：HTTP 数据源、SpringBean 自动发现、嵌套迭代聚合、进度条单元格、二维码/条形码等

**继承链**：
```
UReport2 (Apache-2.0) → luck-report (Apache-2.0) → Aureport Ultra (Apache-2.0)
```

- [UReport2](https://github.com/youseries/ureport) — 第一个基于 Apache-2.0 的中国式报表引擎
- [luck-report](https://gitee.com/LuckyPools/luck-report) — 基于 UReport2 的 SpringBoot + Vue 重构版本，Aureport Ultra 的重要参考
- Aureport Ultra — 在 luck-report 基础上进一步升级技术栈（SpringBoot 3 + Vue 3）

## 功能特性

### 核心引擎
- ✅ XML 报表定义解析（`<paper>`、`<dataset>`、`<cell>`、`<dataset-value>` 等）
- ✅ 四向单元格扩展（左/右/上/下 expand + fill）
- ✅ 数据集绑定（SQL / SpringBean / HTTP / 内建数据源）
- ✅ 聚合计算（Sum、Avg、Count、Max、Min、分组聚合、嵌套迭代）
- ✅ 父子格联动（leftParentCell、topParentCell）
- ✅ 单元格表达式解析（DSL 基于 ANTLR4）
- ✅ 条件格式（颜色/字体/边框/链接/分页等动态样式）
- ✅ 图表引擎（柱状、折线、饼、散点、气泡图）
- ✅ 二维码/条形码生成（ZXing）
- ✅ ProgressBar 进度条单元格
- ✅ Tooltip 悬浮提示
- ✅ 多数据库支持（MySQL、Oracle、SQLServer、达梦）

### 报表设计器（Vue 3 前端）
- ✅ 可视化表格设计器（基于 Handsontable 6.2.2）
- ✅ 工具栏（50+ 工具按钮：对齐、字体、边框、图表、导入导出等）
- ✅ 右侧属性面板（单元格属性、数据集配置、条件格式）
- ✅ 数据源管理（SQL 数据集、Bean 数据集、HTTP 数据集、内建数据源）
- ✅ 搜索表单设计器（拖拽式表单项布局）
- ✅ 报表预览（HTML 预览、打印）
- ✅ 多格式导出（Excel、PDF、Word、图片）

### 待完成
- ❌ 交叉表（Pivot Table）
- ❌ 报表版本管理（历史记录、回滚）
- ❌ 权限管理（用户/角色/报表级别权限）

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
│   ├── aureport-ultra-core/    # 核心引擎（纯 Java，无 Spring 依赖）
│   ├── aureport-ultra-web/     # SpringBoot Web 层
│   ├── aureport-ultra-font/     # 字体资源
│   ├── aureport-ultra-pub/      # 启动入口
│   └── aureport-ultra-bean-discovery-starter/  # Bean 自动发现
├── aureport-ultra-ui/          # Vue 3 前端（Vite 8）
├── reports/                    # 报表定义文件存储目录
├── docs/                       # 项目文档
├── LICENSE                      # Apache-2.0
└── NOTICE                       # 继承关系声明
```

## 技术栈

### 后端
- Java 17
- SpringBoot 3.2.5
- Maven（多模块）
- Apache POI 5.x（Excel 导出）
- iText 5.x（PDF 导出）
- ANTLR4（表达式解析）
- ZXing（二维码/条形码）
- Chart.js（图表）
- Caffeine（缓存）
- SqlParser（SQL 解析/方言转换）

### 前端
- Vue 3.5 + Composition API
- TypeScript 5.8
- Vite 8
- Pinia 2（状态管理）
- Vue Router 4
- Vue I18n 10
- Handsontable 6.2.2（表格编辑器）
- Chart.js 4（图表）
- UnoCSS + SCSS

## License

Apache License 2.0 - 详见 [LICENSE](./LICENSE)

## NOTICE

Aureport Ultra includes software derived from [UReport2](https://github.com/youseries/ureport)，originally licensed under Apache-2.0.

UReport2 is developed by [youseries](https://github.com/youseries) and licensed under Apache License 2.0.

Aureport Ultra 对 UReport2 的架构和核心设计思想进行了深度重构，包名从 `com.bstek.ureport` 改为 `com.aureport.ultra`，技术栈从 Spring Boot 2 升级到 Spring Boot 3，前端从 Vue 2 升级到 Vue 3。

Copyright 2017 Bstek. All rights reserved.