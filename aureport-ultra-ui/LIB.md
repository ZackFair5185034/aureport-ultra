# Aureport Ultra 报表设计器 — 宿主项目集成指南

Aureport Ultra 报表设计器以 Web Component 形式发布，可接入任何 Vue 3 项目，提供完整的报表设计和预览能力。

---

## 目录

- [构建产物](#构建产物)
- [宿主项目依赖](#宿主项目依赖)
- [快速接入](#快速接入)
- [路由与页面](#路由与页面)
- [开发代理配置](#开发代理配置)
- [API 参考](#api-参考)
  - [组件属性](#组件属性)
  - [方法](#方法)
  - [导出对象](#导出对象)
  - [事件](#事件)
- [宿主要求](#宿主要求)

---

## 构建产物

```bash
cd aureport-ultra-ui
pnpm build:lib
```

产出目录 `dist-lib/`：

| 文件 | 体积 | 说明 |
|------|------|------|
| `aureport-ultra.es.js` | ~1.2 MB | 报表引擎核心（ESM） |
| `aureport-ultra.css` | ~240 KB | 全部样式（含 handsontable / codemirror） |

宿主只需引入 `aureport-ultra-ui/style.css`，无需额外导入 handsontable 或 codemirror 样式。

---

## 宿主项目依赖

```bash
# 核心
pnpm add vue@^3.5 vue-router@^5 pinia@^3 vue-i18n@^11

# HTTP
pnpm add axios

# 图表 & 编辑器
pnpm add chart.js handsontable@~6.2.2 codemirror@^5.65
```

版本需兼容 Vue 3.5+ 生态。建议锁定 handsontable 6.2.x（上游依赖）。

---

## 快速接入

### 安装

```bash
# 方式一：本地路径引用
cd aureport-ultra-ui && pnpm build:lib
cd /path/to/host-project
pnpm add file:../aureport-ultra-ui

# 方式二：私有 npm 仓库发布后（略）
```

### 配置 Vite 自定义元素

```ts
// vite.config.ts
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          isCustomElement: tag => tag.startsWith('aureport-'),
        },
      },
    }),
  ],
})
```

### 入口初始化

```ts
// src/main.ts
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')
```

```vue
<!-- src/App.vue -->
<script setup lang="ts">
// 1. 注册 Web Component（aureport-designer / aureport-preview）
import 'aureport-ultra-ui'
// 2. 导入样式
import 'aureport-ultra-ui/style.css'
</script>

<template>
  <router-view />
</template>
```

---

## 路由与页面

推荐两个独立页面，宿主控制路由：

### 路由配置

```ts
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',       name: 'Designer', component: () => import('@/views/DesignerPage.vue') },
    { path: '/preview', name: 'Preview',  component: () => import('@/views/PreviewPage.vue') },
  ],
})
```

建议 `/` 的路径名为 `/index`，方便从后端接口调转到设计页。

### 设计器页面

```vue
<!-- src/views/DesignerPage.vue -->
<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 从 URL query 读取报表路径，支持 /index?reportPath=xxx
const reportPath = (route.query.reportPath as string) || ''

function onWindowNavigate(e: Event) {
  const { target, params } = (e as CustomEvent).detail
  if (target === 'Preview' && params?.reportPath) {
    router.push({ path: '/preview', query: { reportPath: params.reportPath } })
  }
}

function onSave(e: Event) {
  console.log('报表保存:', (e as CustomEvent).detail)
}

onMounted(() => window.addEventListener('aureport-navigate', onWindowNavigate))
onBeforeUnmount(() => window.removeEventListener('aureport-navigate', onWindowNavigate))
</script>

<template>
  <aureport-designer
    :report-path="reportPath"
    locale="zh"
    @save="onSave"
    @error="(e: Event) => console.error(e)"
  />
</template>

<style scoped>
aureport-designer { display: block; width: 100vw; height: 100vh; }
</style>
```

### 预览页面

```vue
<!-- src/views/PreviewPage.vue -->
<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const reportPath = (route.query.reportPath as string) || ''
</script>

<template>
  <div style="display:flex;flex-direction:column;height:100vh">
    <div style="padding:8px 16px;background:#f5f5f5;border-bottom:1px solid #ddd">
      <button @click="router.push('/')">← 返回设计器</button>
    </div>
    <aureport-preview
      :report-path="reportPath"
      locale="zh"
      mode="preview"
      style="flex:1"
    />
  </div>
</template>
```

### 导航数据流

```
用户点击"预览"按钮
  → designer 内部 navigator 派发 window CustomEvent('aureport-navigate')
  → DesignerPage 监听事件 → router.push('/preview?reportPath=xxx')
  → PreviewPage 从 route.query 读取 → 传给 <aureport-preview>
```

---

## 开发代理配置

前端所有 API 请求默认以 `/api` 为前缀（如 `/api/designer/loadReport`），后端实际路径为 `/{servletPrefix}/designer/loadReport`，通过 Vite proxy 转发：

### 后端路径结构

```yaml
# application.yml 中的 servletPrefix
aureport-ultra:
  servletPrefix: report    # → 后端 API 前缀 /report
```

### Vite Proxy 配置

```ts
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8050',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '/report'),
      },
    },
  },
})
```

请求转发链路：

```
浏览器                        Vite dev server (5173)             后端 (8050)
/api/designer/loadReport  →  proxy /api → /report            →  /report/designer/loadReport
/api/datasource/...       →  proxy /api → /report            →  /report/datasource/...
```

### 后端本地 profile

确保后端使用正确的文件存储路径。推荐激活 `local` profile：

```yaml
# application.yml
spring:
  profiles:
    active: dev,local
```

```yaml
# application-local.yml
aureport-ultra:
  fileStoreDir: E:/code_source/aiCode/aureport-ultra/reports   # Windows 路径
```

`dev` 提供数据源等配置，`local` 覆盖本地文件路径，互不冲突。

---

## API 参考

### 组件属性

#### `<aureport-designer>`

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `report-path` | `string` | `''` | 报表文件路径 |
| `locale` | `'zh' \| 'en'` | `'zh'` | 语言 |

#### `<aureport-preview>`

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `report-path` | `string` | `''` | 报表文件路径 |
| `locale` | `'zh' \| 'en'` | `'zh'` | 语言 |
| `params` | `string` (JSON) | `'{}'` | 额外查询参数 |
| `mode` | `string` | `''` | 预览模式 |
| `page-index` | `string` | `''` | 初始页码 |
| `tools-info` | `string` | `''` | 工具栏配置 |

### 方法

```js
const designer = document.querySelector('aureport-designer')

designer.getReportData()         // 获取报表 JSON
designer.saveReport()            // 触发保存
designer.navigateTo(target, params, openInNewTab)
designer.setReportPath(path)     // 动态切换报表
designer.setLocale('en')         // 切换语言

const preview = document.querySelector('aureport-preview')
preview.refresh()                // 刷新预览
preview.handlePageChange(index)  // 翻页
preview.setReportPath(path)
preview.setParams({ key: 'val' })
preview.setLocale('en')
```

### 导出对象

```js
import { requestAdapter } from 'aureport-ultra-ui'

// 配置 API 前缀（默认 /api）
requestAdapter.setBaseURL('/api')

// 设置全局请求头
requestAdapter.setDefaultHeaders({ 'X-Custom': 'value' })
```

> `requestAdapter.setBaseURL` 通常不需要调用，默认值为 `/api`，配合 Vite proxy 即可。

其他导出：

```js
import AureportUltra from 'aureport-ultra-ui'

const {
  install,                     // 手动注册 Web Component
  setLibMode, getLibMode,      // 库模式标志
  AureportDesignerElement,     // Web Component 类
  AureportPreviewElement,      // Web Component 类
} = AureportUltra
```

### 事件

组件派发标准 DOM CustomEvent，宿主通过 `@save` / `addEventListener` 监听。

| 事件 | 触发时机 | `e.detail` 结构 |
|------|----------|-----------------|
| `save` | 报表保存 | `{ reportPath, data }` |
| `navigate` | 内部导航 | `{ target, params, openInNewTab }` |
| `error` | 渲染/保存出错 | `{ message }` |

窗口自定义事件（由 designer 内部工具栏触发）：

| 事件 | 说明 | `e.detail` 结构 |
|------|------|-----------------|
| `aureport-navigate` | 点击"预览"等按钮 | `{ target: 'Preview', params: { reportPath, mode } }` |

---

## 宿主要求

| 项目 | 要求 |
|------|------|
| Vue | `^3.5` |
| vue-router | `^5.0` |
| pinia | `^3.0` |
| vue-i18n | `^11.0` |
| handsontable | `~6.2.2` |
| codemirror | `^5.65` |
| 浏览器 | Chrome / Firefox / Edge / Safari 最新版 |
| 开发 Node.js | `>= 18` |

---

## 集成清单

- [ ] 宿主安装 peer dependencies
- [ ] `pnpm build:lib` 构建库产物
- [ ] `vite.config.ts` 配置 `isCustomElement` 和 proxy
- [ ] 创建两个页面：DesignerPage + PreviewPage
- [ ] 配置路由 `/` → 设计页, `/preview` → 预览页
- [ ] `App.vue` 中 `import 'aureport-ultra-ui'` 注册组件
- [ ] 启动后端 `mvn spring-boot:run -Dspring-boot.run.profiles=dev,local`
- [ ] 验证 `http://localhost:5173/index?reportPath=file:test.ureport.xml`
