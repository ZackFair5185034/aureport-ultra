# CHANGELOG

Aureport Ultra 报表引擎变更记录。

格式基于 [Keep a Changelog](https://keepachangelog.com/)。

## [1.2.0] - 开发中

### 新增

#### 核心引擎
- **ProgressBarValue 进度条单元格**：新增 `progressbar` 值类型，支持百分比进度条展示（value/max 属性）。
- **Tooltip 悬浮提示**：单元格新增 `tooltip` 属性，鼠标悬停显示提示内容。
- **GroupStatAggregate 分组聚合**：新增 `groupstat` 聚合类型，支持从父格 BindData 读取统计字段。

#### 前端设计器
- **ProgressBarValueEditor**：进度条单元格值编辑器，支持配置 value/max 属性。
- **Tooltip 配置**：单元格属性面板新增 tooltip 配置项。

### 修复

- **RowDefinition row-number 属性解析**：`RowParser` 正确解析 `row-number` 属性并设置到 `rowNumber` 字段。
- **ColumnDefinition col-number 属性解析**：`ColumnParser` 正确解析 `col-number` 属性并设置到 `columnNumber` 字段。
- **ReportDefinition.newReport NPE**：`rowMap.get()` 可能返回 null 的问题（待完整修复）。
- **设置弹窗样式修复**：`.u-dialog-wrap` 添加 `color: #333` 解决白字白底看不见的问题。
- **设置弹窗关闭按钮**：修复 `update:visible` 事件传播链，X 按钮和取消按钮已可正常关闭。

#### 前端设计器
- **vue-simple-suggest 兼容性修复**：将 Vue 2 不兼容的 `vue-simple-suggest` v1.11.2 替换为 Vue 3 原生 fork `@ffrosch/vue-simple-suggest` v2.0.7，解决 `this.constructor` 不存在于 Vue 3 `PublicInstanceProxyHandlers` 导致的崩溃。
- **UCheckbox 注入警告修复**：为 9 个组件中独立使用的 `<u-checkbox>` 包裹 `<u-checkbox-group>`，消除 `[Vue warn]: injection 'checkboxGroupContext' not found` 警告，涉及数据集配置和全部 8 个条件属性配置（值、颜色、字体、对齐、边框、尺寸、分页、链接）。
- **父子格引用修复**：插入/删除行列时更新受影响单元格的 `leftParentCellName`/`topParentCellName` 字段（P0-1）；右键清除内容时重置父格引用（P0-4）。

#### 核心引擎
- **ReportParser rowSpan/colSpan 自减 Bug 修复**：后缀 `--` 改为前缀 `--`，确保合并单元格的行/列范围计算正确（P0-2）。
- **BlankCellApply 父子格重定向修复**：`DownBlankCellApply` 和 `RightBlankCellApply` 同时重定向 `leftParentCell` 和 `topParentCell`，修复同时持有两种父格引用的单元格在一侧填充后引用失效的问题（P0-3）。

---

## [1.1.0] - 2026-05-15

### 新增

#### 核心引擎
- **IterateAggregate 嵌套迭代聚合**：新增 `iterate` 聚合类型，支持对父行 Bean 的嵌套数组属性（如 `familyMembers[]`）做迭代展开，生成多行明细。适用于一对多主子表场景。
- **Utils.getProperty() 数组下标支持**：`Utils.java` 重写属性路径解析，新增 `[index]` 数组下标语法（如 `familyMembers[0].name`）。

#### 前端设计器
- **嵌套迭代聚合 UI**：数据集值编辑器新增"嵌套迭代"聚合选项 + "嵌套属性"输入框。
- **工具栏组件自动导入修复**：修复 `unplugin-vue-components` 无法扫描 tool-bar 目录的问题，工具栏按钮恢复正常显示。

### 修复

- **搜索表单设计器白屏**：4 个根因全部修复（vuedraggable 版本冲突、u-* 组件未注册、render.jsx Options API 问题、draggable-item 双 script 块混用）
- **ClasspathReportProvider 前缀**：返回 `"classpath:"` 而非 `"classpath"`（缺少冒号）
- **Vite proxy 目标端口**：硬编码为 `http://localhost:18080`
- **paper 标签 paging-mode NPE**：所有 `<paper>` 标签必须指定 `paging-mode` 属性

---

## [1.0.0] - 2026-05-14

### 新增

#### 核心引擎
- XML 报表定义解析（`<paper>`、`<dataset>`、`<cell>`、`<dataset-value>` 等）
- 四向单元格扩展（左/右/上/下 expand + fill）
- 数据集绑定（SQL 数据源、Bean 数据源、内建数据源）
- 聚合计算（Sum、Avg、Count、Max、Min、分组聚合）
- 父子格联动（leftParentCellName、topParentCellName）
- 单元格表达式解析（DSL 基于 ANTLR4）
- 条件格式（颜色/字体/边框/链接/分页等动态样式）
- 图表引擎（柱状、折线、饼、散点、气泡图）
- 二维码/条形码生成（ZXing）
- 多数据库支持（MySQL、Oracle、SQLServer、达梦）
- 多格式导出（Excel、PDF、Word、图片、HTML）
- 报表定义缓存（内存缓存）

#### 报表设计器（Vue 3 前端）
- 可视化表格设计器（基于 Handsontable 6.2.2）
- 工具栏（50+ 工具按钮：对齐、字体、边框、图表、导入导出等）
- 右侧属性面板（单元格属性、数据集配置、条件格式）
- 数据源管理（SQL 数据集、Bean 数据集、内建数据源）
- 搜索表单设计器（拖拽式表单项布局）
- 报表预览（HTML 预览、打印）
- 多格式导出（Excel、PDF、Word、图片）

#### 项目基础设施
- Spring Boot 3.2.5 后端（Maven 多模块）
- Vue 3 + Vite + TypeScript 前端
- Vue Router 4 路由
- Pinia 2 状态管理
- Vue I18n 10 国际化
- UnoCSS 原子化 CSS

### 修复

- （基线版本，无历史修复记录）