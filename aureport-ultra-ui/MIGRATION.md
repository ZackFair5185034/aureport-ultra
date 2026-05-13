# Aureport Ultra 前端 Vue 2 → Vue 3 + Vite 8 迁移文档

## 概述

本文档记录 aureport-ultra-ui 前端项目从 Vue 2 + Vue CLI 迁移到 Vue 3 + Vite 8 的完整过程。

**迁移范围**：整个前端项目（`aureport-ultra-ui/`）

**迁移前后对比**：

| 维度 | 迁移前 | 迁移后 |
|---|---|---|
| 构建工具 | Vue CLI (webpack) | Vite 8 |
| Vue 版本 | Vue 2.x | Vue 3.5.x (Composition API) |
| 路由 | Vue Router 3.x | Vue Router 4.x |
| 状态管理 | Vuex 4.x | Pinia 2.x |
| 国际化 | Vue I18n 8.x | Vue I18n 10.x |
| TypeScript | 部分使用 | 全面启用 |
| 组件风格 | Options API | `<script setup lang="ts">` |

---

## 迁移详情

### Phase 1：Vite 脚手架搭建

- 创建 `vite.config.ts`，配置 Vue 插件、组件自动导入、UnoCSS
- 创建 `tsconfig.json`，配置严格类型检查
- 创建 `src/main.ts`，Vue 3 `createApp` 入口
- 创建 `src/router/index.ts`，Vue Router 4 路由
- 配置 `.env.development` / `.env.production` 环境变量

### Phase 2：核心基础设施

- `src/stores/` — Pinia Store 替代 Vuex：`report.ts`（报表上下文）、`designer.ts`（设计器状态）
- `src/locales/index.ts` — Vue I18n 10 Composition API 替代旧 Options API
- `src/types/modules.d.ts` — 大量 `.js` 模块的类型声明
- `src/utils/url.ts`、`src/utils/comnon.ts` 等工具函数迁移

### Phase 3：组件库 + 全局自动导入

- 完整迁移 `src/components/` 下的所有组件为 Vue 3 + TypeScript
- 配置 `unplugin-vue-components` 自动导入，移除所有手动 `import`
- 组件文件结构扁平化：每个组件一个目录，包含 `index.vue`

### Phase 4：API 层和工具函数

- `src/api/` — 全面迁移为 TypeScript，函数式 API
- `src/utils/` — 表格操作、URL、上下文操作等工具函数迁移

### Phase 5：报表设计器核心页面

- `designer/index.vue` — 设计器主页，`<script setup lang="ts">` + Pinia
- `resource-panel/property-panel/` — 属性面板，支持单元格属性编辑
- `resource-panel/datasource-panel/` — 数据源面板，支持数据集、字段管理
- `tool-bar/` — 设计器工具栏，打印线、分页等
- `edit-table/` — 表格编辑器，封装 handsontable
- `print-line/` — 打印线组件
- `search-form/` — 搜索表单设计器

### Phase 6：Preview + SearchForm 预览页面

- `preview/index.vue` — 报表预览主页，图表渲染、分页支持
- `preview/tool-box/` — 打印/导出工具栏
- `preview/pdf-print-dialog/` — PDF 打印对话框
- `preview/search-box/` — 动态表单渲染
- `preview/utils/chart.ts` — Chart.js 图表构建
- `preview/utils/render.ts` — 动态组件渲染（`createApp`）

### Phase 7：Vue 2 基础设施迁移

#### 7.1 入口和路由

| 迁移前 | 迁移后 |
|---|---|
| `src/main.js` (Vue 2 `new Vue()`) | `src/main.ts` (`createApp()`) |
| `src/router/index.js` (Vue Router 3) | `src/router/index.ts` (Vue Router 4) |
| `src/locales/index.js` (Vue I18n 8) | `src/locales/index.ts` (Vue I18n 10) |

#### 7.2 组件实例服务

| 文件 | 迁移模式 |
|---|---|
| `components/loading/instance.ts` | `createApp` + reactive props 替代 Vue 2 指令 |
| `components/message/instance.ts` | `createApp` 替代 `new Vue({render})` |
| `components/messagebox/instance.ts` | `createApp` + Promise API 替代 `Vue.extend` |

#### 7.3 Edit-table 对话框类

- `edit-table/chart-widget/class.ts` — 图表组件，`createApp` 替代 `new Vue({render}).$mount()`
- `edit-table/cross-tab-widget/class.ts` — 交叉表组件，`defineExpose` 替代 `$children[0]`
- `edit-table/row-col-number-dialog/class.ts` — 行数列数对话框
- `edit-table/row-col-width-height-dialog/class.ts` — 行高列宽对话框

#### 7.4 Lib 自定义元素

- `lib/components/LuckDesigner.ts` — 设计器 Web Component，Pinia 替代 Vuex
- `lib/components/LuckPreview.ts` — 预览器 Web Component，Pinia 替代 Vuex
- `lib/index.ts` — 库入口，Vite 构建兼容

### Phase 8：构建配置完善

#### 8.1 废弃文件清理

删除的 Vue 2 遗留文件：

- `vue.config.js` — Vue CLI 配置
- `babel.config.js` — Vue CLI 专属
- `jsconfig.json` — 已由 tsconfig.json 覆盖
- `.env.dev`、`.env.prod` — Vue 2 旧环境变量
- `src/main.js`、`src/router/index.js` 等旧入口

#### 8.2 依赖清理

移除未引用的依赖：

- `stream-browserify`
- `core-js`

#### 8.3 `@babel/polyfill` shim

- **问题**：`handsontable@6.2.2` 内部引用了废弃的 `@babel/polyfill`
- **解决**：Vite 插件拦截 `@babel/polyfill` 导入，返回空模块

```typescript
// vite.config.ts
const babelPolyfillShim = () => ({
  name: 'babel-polyfill-shim',
  resolveId(id: string) {
    if (id === '@babel/polyfill' || id.startsWith('@babel/polyfill/')) {
      return '\0babel-polyfill-empty'
    }
    return null
  },
  load(id: string) {
    if (id === '\0babel-polyfill-empty') {
      return 'export default {}'
    }
    return null
  },
})
```

#### 8.4 `.jsx` 扩展名修复

- `search-form/utils/render.js` → `render.jsx`（包含 JSX 语法）
- Vite 配置添加 `@vitejs/plugin-vue-jsx` 插件处理 Vue JSX

### Phase 9：Vite 6 → Vite 8 升级

#### 升级的依赖

| 依赖 | 升级前 | 升级后 |
|---|---|---|
| `vite` | 6.4.2 | 8.0.12 |
| `@vitejs/plugin-vue` | 5.2.0 | 6.0.6 |
| `@vitejs/plugin-vue-jsx` | — | 5.1.5（新增） |
| `vite-plugin-vue-devtools` | 7.7.0 | 8.1.2 |
| `esbuild` | 内置 | 需单独安装为 devDependencies |

#### Vite 8 破坏性变化

1. **esbuild 不再内置**：需显式安装 `esbuild`
2. **Rolldown 替换 Rollup**：Vite 8 默认使用 Rolldown 作为打包器，行为更严格
3. **`@vitejs/plugin-vue-jsx` 需显式配置**：Rolldown 默认将 `.jsx` 当作 React JSX，需 Vue JSX 插件处理
4. **`@vitejs/plugin-vue` 需升级到 v6**：兼容 Vite 8 + Rolldown

#### vite.config.ts 最终配置

```typescript
import { defineConfig } from 'vite'
import { resolve } from 'path'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import VueDevTools from 'vite-plugin-vue-devtools'
import Components from 'unplugin-vue-components/vite'
import UnoCSS from 'unocss/vite'

const babelPolyfillShim = () => ({ ... })

export default defineConfig({
  plugins: [
    babelPolyfillShim(),
    vue(),
    vueJsx(),
    VueDevTools(),
    UnoCSS(),
    Components({
      dirs: [resolve(__dirname, 'src/components')],
      extensions: ['vue'],
      deep: true,
      dts: resolve(__dirname, 'src/types/components.d.ts'),
    }),
  ],
  resolve: {
    alias: { '@': resolve(__dirname, 'src') },
  },
  server: {
    port: Number(process.env.VITE_DEV_PORT) || 8080,
    proxy: { '/api': { target: '...', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '/report') } },
  },
  build: { target: 'esnext', outDir: 'dist', assetsDir: 'assets', sourcemap: false },
})
```

---

## 构建验证

```bash
# 类型检查
pnpm typecheck  # vue-tsc --noEmit

# 构建
pnpm build     # vue-tsc --noEmit && vite build

# 开发
pnpm dev       # vite
```

**构建结果**：669 模块转换成功，1.73s 完成

---

## 遗留问题与后续工作

### 需调研

- [ ] **handsontable@6.2.2 升级评估**：当前使用 v6.2.2（开源免费），最新为 v17.0.1（商业 License）。从 6.x 到 7+ 有较大 API 变化，需评估工作量
- [ ] **`vue-i18n@10` 升级到 `v11`**：`v9/v10` 已停止维护，v11 有破坏性变化
- [ ] **代码分割优化**：当前 `designer` chunk 达 1.4MB，建议按需加载优化

### 非阻塞性警告（暂不处理）

- `>>>` CSS 深度选择器弃用 → 计划中逐步替换为 `:deep()`
- `eval` 使用 → 已有代码（chart.ts、js.js），非本次迁移引入
- chunk 大小超限 → 需专项优化

---

## 目录结构（迁移后）

```
src/
├── main.ts                  # Vue 3 入口
├── App.vue
├── router/index.ts          # Vue Router 4
├── locales/index.ts         # Vue I18n 10
├── stores/                  # Pinia Store
│   ├── report.ts
│   └── designer.ts
├── api/                     # TypeScript API
├── components/              # Vue 3 组件（自动导入）
├── utils/                  # TypeScript 工具
├── views/
│   ├── report/
│   │   ├── designer/       # 设计器页面
│   │   └── preview/        # 预览页面
├── lib/                    # Web Component（Pinia）
└── types/modules.d.ts      # 类型声明
```
