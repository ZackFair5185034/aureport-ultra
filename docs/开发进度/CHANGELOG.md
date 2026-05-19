# CHANGELOG — Aureport Ultra 开发日志

> 版本变更 + 开发过程记录合二为一。
> 格式基于 [Keep a Changelog](https://keepachangelog.com/)。

## [1.2.0] - 开发中

### 新增

#### 核心引擎
- **ProgressBarValue 进度条单元格**：新增 `progressbar` 值类型，支持百分比进度条展示（value/max 属性）
- **Tooltip 悬浮提示**：单元格新增 `tooltip` 属性，鼠标悬停显示提示内容
- **GroupStatAggregate 分组聚合**：新增 `groupstat` 聚合类型，支持从父格 BindData 读取统计字段
- **SpringBean 数据源增强**：新增 `@FieldDesc` 注解支持字段中文描述；支持嵌套 Bean 属性递归展开
- **Bean 自动发现 Starter 模块**：新增 `aureport-ultra-bean-discovery-starter` 模块，基于 Spring Boot AutoConfiguration 自动扫描 `@ReportBean` 注解 Bean 并暴露 REST 端点（`/api/report-beans`）

#### 前端设计器
- **ProgressBarValueEditor**：进度条单元格值编辑器，支持配置 value/max 属性
- **Tooltip 配置**：单元格属性面板新增 tooltip 配置项
- **SpringBean 字段展示增强**：树节点同时显示字段名和 label 描述；嵌套 Bean 子字段可展开
- **SpringBean 数据集配置增强**：Bean 方法对话框添加 label 展示、编辑后自动重建字段树、支持不指定方法（仅用于获取嵌套属性）
- **Web Component Lib 构建**：新增 Vite lib 模式构建配置，导出 `AureportDesigner`/`AureportPreview` 自定义元素，替换原 `LuckDesigner`/`LuckPreview`
- **数据集配置字段 label 统一展示**：SpringBean 树节点、数据集属性面板下拉选项（6 个条件属性配置面板 + data-mapping）同时展示字段名和 label 描述

### 修复

#### 通用
- **RowDefinition/ColumnDefinition 属性解析**：`RowParser`/`ColumnParser` 正确读取 `row-number`/`col-number` 属性并设置到对应字段
- **ReportDefinition.newReport NPE**：`rowMap.get()` 可能返回 null 的问题（已加 null 检查，待完整修复）
- **设置弹窗样式修复**：`.u-dialog-wrap` 添加 `color: #333` 解决白字白底不可见问题
- **设置弹窗关闭按钮**：修复 `update:visible` 事件传播链，X 按钮和取消按钮恢复正常

#### 前端设计器
- **vue-simple-suggest 兼容性修复**：将 Vue 2 不兼容的 `vue-simple-suggest` v1.11.2 替换为 Vue 3 原生 fork `@ffrosch/vue-simple-suggest` v2.0.7。**根因**：`this.constructor` 不存在于 Vue 3 `PublicInstanceProxyHandlers`
- **UCheckbox 注入警告修复**：为 9 处独立使用 `<u-checkbox>` 的组件包裹 `<u-checkbox-group>`，消除 `[Vue warn]: injection 'checkboxGroupContext' not found` 警告。涉及数据集配置和全部 8 个条件属性配置面板
- **父子格引用修复（P0-1~P0-4）**：
  - P0-1：插入/删除行列时更新受影响单元格的 `leftParentCellName`/`topParentCellName`
  - P0-4：右键清除内容时重置父格引用
- **右键菜单点击外部关闭**：修复 `ContextMenu` 的 `document.contains(e.target)` 恒为 `true` 导致无法点击外部自动关闭的问题，改用 `menuEl.contains(e.target)`
- **字段编辑功能**：
  - **Buildin/Database/Spring 统一**：三棵树均支持通过右键菜单编辑字段名和标签（`FieldNameDialog` 添加 `field` prop 支持编辑预填、label 输入框）
  - **字段标签显示**：数据集属性面板下拉选项、SpringBean 树节点同时展示字段名和 label
  - **BuildinTree 编辑不持久化修复**：原编辑分支缺少 `emit` 调用，导致修改仅在本地 deepCopy 副本上生效而未持久化。将所有字段变更的 `emit` 移至 if/else 外部统一执行
  - **DatabaseTree 编辑后渲染不更新**：将 `datasets` 通过独立 prop 传递并直连 watch，避免依赖 `props.ds` 的 deep watcher 无法可靠检测深层变化的问题。后续补充：
    - 子组件 `handleFieldNameSave` 中添加 `datasets.value = newDatasets` 本地 ref 更新，确保模板响应式链触发
    - 父组件 `updateSpringDatasets` 从原地修改改为元素替换（`datasources.value[index] = { ... }`），确保 computed 正确重新求值

#### 核心引擎
- **ReportParser rowSpan/colSpan 自减 Bug 修复（P0-2）**：后缀 `--` 改为前缀 `--`，确保合并单元格范围计算正确。**根因**：后缀 `--` 在表达式解析中产生歧义
- **BlankCellApply 父子格重定向修复（P0-3）**：`DownBlankCellApply` 和 `RightBlankCellApply` 同时重定向 `leftParentCell` 和 `topParentCell`，修复同时持有两种引用时一侧填充后另一侧失效的问题

---

## [1.1.0] - 2026-05-15

### 新增

#### 核心引擎
- **IterateAggregate 嵌套迭代聚合**：新增 `iterate` 聚合类型，支持对父行 Bean 的嵌套数组属性（如 `familyMembers[]`）做迭代展开。适用于一对多主子表场景
- **Utils.getProperty() 数组下标支持**：重写属性路径解析逻辑，新增 `[index]` 数组下标语法（如 `familyMembers[0].name`）

#### 前端设计器
- **嵌套迭代聚合 UI**：数据集值编辑器新增"嵌套迭代"聚合选项 + "嵌套属性"输入框
- **工具栏组件自动导入修复**：修复 `unplugin-vue-components` 无法扫描 tool-bar 目录的问题

### 修复

- **搜索表单设计器白屏**：4 个根因全部修复
  - vuedraggable 版本冲突（同时存在 vuedraggable@next 和 v4.1.0）
  - u-* 组件在动态渲染时未注册
  - render.jsx 混用 Options API 导致 Vue 3 兼容问题
  - draggable-item 双 script 块混用（Options API + Composition API）
- **ClasspathReportProvider 前缀**：`"classpath"` 缺少冒号，修复为 `"classpath:"`
- **Vite proxy 目标端口**：硬编码为 `http://localhost:18080`
- **paper 标签 paging-mode NPE**：缺省 `paging-mode` 属性时解析返回 null 导致 NPE

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
- Handsontable 可视化表格设计器
- 工具栏（50+ 工具按钮：对齐、字体、边框、图表、导入导出等）
- 属性面板（单元格属性、数据集配置、条件格式）
- 数据源管理（SQL 数据集、Bean 数据集、内建数据源）
- 搜索表单设计器（拖拽式表单项布局）
- 报表预览（HTML 预览、打印）
- 多格式导出（Excel、PDF、Word、图片）

#### 项目基础设施
- Spring Boot 3.2.5 后端（Maven 多模块：core/web/font/pub）
- Vue 3 + Vite + TypeScript 前端
- Vue Router 4 + Pinia 2 + Vue I18n 10 + UnoCSS
