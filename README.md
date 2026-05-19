# Aureport Ultra 报表引擎

基于开源项目 **UReport2** 重构的高性能中国式报表引擎。

## 项目概述

| 维度 | 数据 |
|------|------|
| 定位 | Java 报表引擎 + Vue 3 可视化设计器 |
| 技术栈 | SpringBoot 3.2.5 + Vue 3 + Vite + TypeScript |
| 后端模块 | 4 个 Maven 模块（core/web/font/pub） |
| Java 文件 | 490 个 |
| 数据库 | MySQL / Oracle / SQLServer / 达梦（仅用作数据源） |
| 前端端口 | 3000（Vite 开发服务器） |
| 后端端口 | 8050（Spring Boot） |

**核心架构**：报表定义以 `.ureport.xml` 文件存储在 `fileStoreDir` 目录，**不是数据库存储**。数据库连接只用来执行用户配置的 SQL 数据集查询。

## 快速启动

### 1. 后端启动

```bash
cd /home/coding/aureport-ultra/aureport-ultra-server/aureport-ultra-pub

# 方式一：Maven 启动（推荐开发用，自动监听文件变化）
mvn spring-boot:run

# 方式二：JAR 启动（需要先 mvn package）
java -jar target/aureport-ultra-pub.jar
```

验证后端启动成功：
```bash
curl http://localhost:8050/report/datasource/loadBuildinDatasources
```

### 2. 前端启动

```bash
cd /home/coding/aureport-ultra/aureport-ultra-ui

# ⚠️ 必须用 npx vite，不能用 pnpm dev（@parcel/watcher postinstall 问题）
npx vite --port 3000 --host

# 验证前端访问
curl -s http://localhost:3000 | head -5
```

**⚠️ `--host` 参数必须加**，否则其他机器无法访问（Vite 默认只监听 127.0.0.1）。

### 3. 访问报表设计器

- 报表设计器：http://localhost:3000/report/designer
- 报表预览：http://localhost:3000/report/preview?reportPath=file:报表名.ureport.xml

## 项目结构

```
aureport-ultra/
├── aureport-ultra-server/     # Java 后端（Maven 多模块）
│   ├── aureport-ultra-core/    # 核心引擎（纯 Java，无 Spring）
│   ├── aureport-ultra-web/     # SpringBoot Web 层
│   ├── aureport-ultra-font/     # 字体资源
│   └── aureport-ultra-pub/      # 启动入口
├── aureport-ultra-ui/          # Vue 3 前端（Vite）
├── reports/                    # 报表定义文件存储目录（运行时创建）
│   └── *.ureport.xml           # 报表模板文件（热生效，保存即用）
└── docs/                       # 项目文档
    ├── 调研/                    # 调研文档（竞品分析、技术选型等）
    ├── 开发计划/                # 功能规划、迭代计划
    ├── 技术方案/                # 架构设计、接口文档、技术决策
    ├── 开发进度/                # 已完成功能、版本记录
    └── 用户手册/                # 使用指南、运维手册
```

## 已实现功能

### 核心引擎
- ✅ XML 报表定义解析（`<paper>`、`<dataset>`、`<cell>`、`<dataset-value>` 等）
- ✅ 四向单元格扩展（左/右/上/下 expand + fill）
- ✅ 数据集绑定（Bean 数据源、SQL 数据源、内建数据源）
- ✅ 聚合计算（Sum、Avg、Count、Max、Min、分组聚合）
- ✅ 嵌套迭代聚合（IterateAggregate，支持一对多嵌套展开）
- ✅ 父子格联动（leftParentCell、topParentCell）
- ✅ 单元格表达式解析（DSL 基于 ANTLR4）
- ✅ 条件格式（颜色/字体/边框/链接/分页等动态样式）
- ✅ 图表引擎（柱状、折线、饼、散点、气泡图）
- ✅ 二维码/条形码生成（ZXing）
- ✅ 多数据库支持（MySQL、Oracle、SQLServer、达梦）
- ✅ Utils.getProperty() 数组下标支持（如 `familyMembers[0].name`）

### 报表设计器（Vue 3 前端）
- ✅ 可视化表格设计器（基于 Handsontable 6.2.2）
- ✅ 工具栏（50+ 工具按钮：对齐、字体、边框、图表、导入导出等）
- ✅ 右侧属性面板（单元格属性、数据集配置、条件格式）
- ✅ 数据源管理（SQL 数据集、Bean 数据集、内建数据源）
- ✅ 搜索表单设计器（拖拽式表单项布局）
- ✅ 报表预览（HTML 预览、打印）
- ✅ 多格式导出（Excel、PDF、Word、图片）

### 已知限制 / 待完成
- ❌ 交叉表（Pivot Table）未实现
- ❌ 报表版本管理（历史记录、回滚）未实现
- ❌ 权限管理（用户/角色/报表级别权限）未实现
- ❌ 集群部署（多节点报表缓存同步）未实现

## 报表模板示例

### 加载报表
```bash
# 列出可用报表
ls /home/coding/aureport-ultra/reports/

# 预览报表（HTTP 方式）
curl "http://localhost:8050/report/html/loadHtml?reportPath=file:sales_report.ureport.xml"

# 通过前端预览（浏览器打开）
http://localhost:3000/report/preview?reportPath=file:sales_report.ureport.xml
```

### 新建报表
1. 打开 http://localhost:3000/report/designer
2. 点击工具栏「新建报表」
3. 配置数据源（右侧面板 → 数据源管理）
4. 在表格中拖拽单元格，输入 `${dataset.field}` 绑定数据
5. 点击「保存」，输入报表名称

## 常见问题

### 1. `paper` 标签缺少 `paging-mode` 属性导致 500
```xml
<!-- ❌ 错误：缺少 paging-mode -->
<paper type="A4" orientation="portrait"></paper>

<!-- ✅ 正确 -->
<paper type="A4" orientation="portrait" paging-mode="fitpage"></paper>
```

### 2. 工具栏按钮全部不显示
- 检查 `vite.config.ts` 是否配置了 tool-bar 目录扫描
- 检查 `unplugin-vue-components` 是否正确解析目录名到组件名

### 3. 预览页 iframe 空白
- 等待 1-2 秒（iframe 异步加载）
- 打开浏览器控制台检查是否有 JS 错误
- 检查 URL 参数是否正确：`reportPath` 而非 `filePath`

### 4. MySQL 连接失败（Access denied）
- 确认数据库用户名/密码正确
- 确认 Java 应用所在 IP 已授权：`CREATE USER 'urp'@'<本机IP>' ...`

## 文档目录

| 分类 | 内容 |
|------|------|
| [docs/调研/](调研/) | 竞品分析、技术选型、外部参考 |
| [docs/开发计划/](开发计划/) | 功能规划、迭代计划 |
| [docs/技术方案/](技术方案/) | 架构设计、接口文档、技术决策记录（ADR） |
| [docs/开发进度/](开发进度/) | 版本记录、已完成功能、里程碑 |
| [docs/用户手册/](用户手册/) | 管理员手册、设计器使用指南 |

## 相关资源

- [CLAUDE.md](./CLAUDE.md) — Claude Code 开发指南
- [aureport-ultra-ui/MIGRATION.md](./aureport-ultra-ui/MIGRATION.md) — Vue 2→3 迁移记录
