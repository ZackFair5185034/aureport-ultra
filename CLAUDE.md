# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Aureport Ultra 是一款基于开源项目 UReport2 重构的 Java 高性能报表引擎，基于 SpringBoot 3.2.5 + Vue 3 构建，支持复杂中国式报表设计。

**项目规模**：490 个 Java 文件，120+ Vue 组件，19 个后端 Controller。

## 项目结构

```
aureport-ultra/
├── aureport-ultra-server/     # Java 后端（SpringBoot 3.2.5）
│   ├── aureport-ultra-core/      # 核心引擎（纯 Java，无 Spring 依赖）
│   ├── aureport-ultra-web/       # SpringBoot Web 层
│   ├── aureport-ultra-font/      # 字体资源模块
│   └── aureport-ultra-pub/       # 启动入口，打包为可执行 jar
└── aureport-ultra-ui/         # Vue 3 前端（Vite 8）
```

## 后端构建与运行

```bash
# 编译整个项目
cd aureport-ultra-server && mvn clean package -DskipTests

# 运行（入口模块，端口 8050）
cd aureport-ultra-server/aureport-ultra-pub && mvn spring-boot:run

# 单独编译某个模块
mvn clean install -pl aureport-ultra-core -am -DskipTests
```

**运行时端口**：
- `application.yml` 默认端口：`8050`
- 开发环境（`--server.port=18080` 启动参数覆盖）：`18080`

**前置要求**：JDK >= 17（推荐 21），Maven 构建。

## 前端构建与运行

```bash
cd aureport-ultra-ui

# 开发（注意：需用 npx vite 代替 pnpm dev，绕过 @parcel/watcher postinstall 脚本阻塞）
npx vite --port 3000

# 构建生产版本
pnpm build        # vue-tsc 类型检查 + vite build

# 类型检查
pnpm typecheck    # vue-tsc --noEmit

# 预览构建产物
pnpm preview
```

**已知问题**：`@parcel/watcher` 的 postinstall 脚本会被 pnpm 拦截拒绝，导致 `pnpm dev` 失败。临时解决方案是用 `npx vite --port 3000` 直接启动。

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
│   │   │   ├── edit-table/       # 表格编辑区（单元格/图表/交叉表）
│   │   │   ├── tool-bar/         # 工具栏（50+ 工具按钮：对齐/字体/边框/图表/导入导出等）
│   │   │   ├── resource-panel/   # 右侧资源面板
│   │   │   │   ├── datasource-panel/    # 数据源配置（SQL/Bean/内建数据源）
│   │   │   │   └── property-panel/       # 属性面板（图表/单元格值/条件格式）
│   │   │   └── search-form/      # 查询表单设计器
│   │   └── preview/          # 报表预览页面
├── lib/                      # Web Component（LuckDesigner/LuckPreview）
└── types/modules.d.ts        # .js 模块类型声明
```

## 后端模块架构

```
aureport-ultra-server/
├── aureport-ultra-core/        # 核心引擎（无 Spring 依赖，纯 Java）
│   ├── build/               # 报表构建器
│   │   ├── aggregate/       # 聚合计算（Sum/Avg/Count/Max/Min/自定义分组）
│   │   ├── cell/            # 单元格构建（上下左右扩展填充）
│   │   ├── compute/         # 数据计算
│   │   └── paging/          # 分页处理
│   ├── chart/               # 图表引擎（柱状/折线/饼/散点/气泡图）
│   ├── cache/               # 报表定义缓存接口
│   ├── dsl/                 # ANTLR4 语法定义（ReportLexer.g4, ReportParser.g4）
│   ├── expression/          # 表达式解析
│   ├── model/               # 数据模型
│   ├── parser/              # 报表 XML 解析
│   └── export/               # 导出（Excel/PDF/Word/HTML）
│
├── aureport-ultra-web/         # Spring Boot Web 层
│   ├── controller/          # 19 个控制器
│   │   ├── designer/        # 设计器 + 数据源管理
│   │   ├── excel/           # Excel 97/2007 导出
│   │   ├── pdf/             # PDF 导出
│   │   ├── word/            # Word 导出
│   │   ├── html/            # HTML 预览
│   │   ├── image/           # 图片导出
│   │   ├── chart/           # 图表 API
│   │   ├── importexcel/     # Excel 导入
│   │   └── res/             # 资源加载
│   ├── config/              # Spring 配置（数据源、SQL 方言）
│   ├── sql/                 # 数据库方言（MySQL/Oracle/SQLServer/达梦）
│   └── cache/               # HttpSession 缓存实现
├── aureport-ultra-font/         # 报表字体资源模块
└── aureport-ultra-pub/          # Spring Boot 启动入口，打包为可执行 jar
```

**核心依赖关系**：`aureport-ultra-core` <- `aureport-ultra-web` <- `aureport-ultra-pub`

## 核心设计

- **DSL 引擎**：基于 ANTLR4 定义的报表语法（`ReportLexer.g4` / `ReportParser.g4`），解析报表单元格表达式
- **单元格渲染**：通过 `CellRenderer` 接口将报表单元格渲染为 Excel/PDF/HTML
- **图表引擎**：图表配置由 `Chart` 类管理，数据绑定通过 `ChartData` 处理，支持多种图表类型
- **数据绑定**：`BindData` -> `Dataset` -> `DatasetUtils` 链路完成数据集解析与数据填充
- **聚合计算**：`aggregate/` 包下实现 SUM/AVG/COUNT/MAX/MIN/CUSTOM GROUP 等聚合
- **单元格扩展**：左/右/上/下四向单元格扩展填充，支持重复/空白单元格策略
- **缓存机制**：`ReportDefinitionCache` 接口定义报表定义缓存，默认使用内存缓存
- **多格式导出**：Excel（Apache POI 5.2.5）、PDF（iText 5.5.13.3）、Word、HTML
- **二维码支持**：ZXing 生成二维码/条形码
- **条件格式**：动态绑定单元格样式（颜色/字体/边框/链接/分页等条件）
- **多数据库支持**：MySQL / Oracle / SQLServer / 达梦，通过 `DialectFactory` 选择对应 SQL 方言

## 关键配置

- `AureportUltraMainConfig`（aureport-ultra-web）：Spring 主配置，扫描 `com.aureport.ultra` 包
- `application.yml`（aureport-ultra-pub）：数据库连接等运行时配置
- `DialectFactory`：根据数据库类型选择对应 SQL 方言
- `vite.config.ts`：Vite 构建配置，包含 `@babel/polyfill` shim（解决 handsontable@6.2.2 引用废弃包问题）
- `.env.development` / `.env.production`：前端环境变量（API 地址、端口、公共路径）
- 数据库初始化脚本：`aureport-ultra-server/aureport-ultra-pub/src/main/sql/v1.0.0.sql`（数据库名 `luck_product`）

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
- **2026.05**：core/build 包 62 个文件从 luck-report 参考项目恢复并强制提交（包名 `com.aureport.ultra.core.build`，原 UReport2 历史中不存在）

<!-- rtk-instructions v2 -->
# RTK (Rust Token Killer) - Token-Optimized Commands

## Golden Rule

**Always prefix commands with `rtk`**. If RTK has a dedicated filter, it uses it. If not, it passes through unchanged. This means RTK is always safe to use.

**Important**: Even in command chains with `&&`, use `rtk`:
```bash
# ❌ Wrong
git add . && git commit -m "msg" && git push

# ✅ Correct
rtk git add . && rtk git commit -m "msg" && rtk git push
```

## RTK Commands by Workflow

### Build & Compile (80-90% savings)
```bash
rtk cargo build         # Cargo build output
rtk cargo check         # Cargo check output
rtk cargo clippy        # Clippy warnings grouped by file (80%)
rtk tsc                 # TypeScript errors grouped by file/code (83%)
rtk lint                # ESLint/Biome violations grouped (84%)
rtk prettier --check    # Files needing format only (70%)
rtk next build          # Next.js build with route metrics (87%)
```

### Test (60-99% savings)
```bash
rtk cargo test          # Cargo test failures only (90%)
rtk go test             # Go test failures only (90%)
rtk jest                # Jest failures only (99.5%)
rtk vitest              # Vitest failures only (99.5%)
rtk playwright test     # Playwright failures only (94%)
rtk pytest              # Python test failures only (90%)
rtk rake test           # Ruby test failures only (90%)
rtk rspec               # RSpec test failures only (60%)
rtk test <cmd>          # Generic test wrapper - failures only
```

### Git (59-80% savings)
```bash
rtk git status          # Compact status
rtk git log             # Compact log (works with all git flags)
rtk git diff            # Compact diff (80%)
rtk git show            # Compact show (80%)
rtk git add             # Ultra-compact confirmations (59%)
rtk git commit          # Ultra-compact confirmations (59%)
rtk git push            # Ultra-compact confirmations
rtk git pull            # Ultra-compact confirmations
rtk git branch          # Compact branch list
rtk git fetch           # Compact fetch
rtk git stash           # Compact stash
rtk git worktree        # Compact worktree
```

Note: Git passthrough works for ALL subcommands, even those not explicitly listed.

### GitHub (26-87% savings)
```bash
rtk gh pr view <num>    # Compact PR view (87%)
rtk gh pr checks        # Compact PR checks (79%)
rtk gh run list         # Compact workflow runs (82%)
rtk gh issue list       # Compact issue list (80%)
rtk gh api              # Compact API responses (26%)
```

### JavaScript/TypeScript Tooling (70-90% savings)
```bash
rtk pnpm list           # Compact dependency tree (70%)
rtk pnpm outdated       # Compact outdated packages (80%)
rtk pnpm install        # Compact install output (90%)
rtk npm run <script>    # Compact npm script output
rtk npx <cmd>           # Compact npx command output
rtk prisma              # Prisma without ASCII art (88%)
```

### Files & Search (60-75% savings)
```bash
rtk ls <path>           # Tree format, compact (65%)
rtk read <file>         # Code reading with filtering (60%)
rtk grep <pattern>      # Search grouped by file (75%). Format flags (-c, -l, -L, -o, -Z) run raw.
rtk find <pattern>      # Find grouped by directory (70%)
```

### Analysis & Debug (70-90% savings)
```bash
rtk err <cmd>           # Filter errors only from any command
rtk log <file>          # Deduplicated logs with counts
rtk json <file>         # JSON structure without values
rtk deps                # Dependency overview
rtk env                 # Environment variables compact
rtk summary <cmd>       # Smart summary of command output
rtk diff                # Ultra-compact diffs
```

### Infrastructure (85% savings)
```bash
rtk docker ps           # Compact container list
rtk docker images       # Compact image list
rtk docker logs <c>     # Deduplicated logs
rtk kubectl get         # Compact resource list
rtk kubectl logs        # Deduplicated pod logs
```

### Network (65-70% savings)
```bash
rtk curl <url>          # Compact HTTP responses (70%)
rtk wget <url>          # Compact download output (65%)
```

### Meta Commands
```bash
rtk gain                # View token savings statistics
rtk gain --history      # View command history with savings
rtk discover            # Analyze Claude Code sessions for missed RTK usage
rtk proxy <cmd>         # Run command without filtering (for debugging)
rtk init                # Add RTK instructions to CLAUDE.md
rtk init --global       # Add RTK to ~/.claude/CLAUDE.md
```

## Token Savings Overview

| Category | Commands | Typical Savings |
|----------|----------|-----------------|
| Tests | vitest, playwright, cargo test | 90-99% |
| Build | next, tsc, lint, prettier | 70-87% |
| Git | status, log, diff, add, commit | 59-80% |
| GitHub | gh pr, gh run, gh issue | 26-87% |
| Package Managers | pnpm, npm, npx | 70-90% |
| Files | ls, read, grep, find | 60-75% |
| Infrastructure | docker, kubectl | 85% |
| Network | curl, wget | 65-70% |

Overall average: **60-90% token reduction** on common development operations.
<!-- /rtk-instructions -->