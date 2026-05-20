# Aureport Ultra 报表设计器 - 宿主项目接入指南

Aureport Ultra 报表设计器可作为前端组件库接入到任何 Vue 3 项目中，提供完整的报表设计和预览能力。

---

## 目录

- [构建产物](#构建产物)
- [宿主项目依赖](#宿主项目依赖)
- [快速接入](#快速接入)
- [使用方式](#使用方式)
- [API 参考](#api-参考)
- [事件参考](#事件参考)
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
| `aureport-ultra.es.js` | ~1.2 MB | 报表设计器核心（ESM） |
| `aureport-ultra.css` | ~215 KB | 样式（含 codemirror/handsontable 主题） |

> 如果宿主项目已安装 `handsontable` 和 `codemirror`，可以在 `vite.lib.config.ts` 的 `externalDeps` 中添加对应 CSS 路径进一步减少体积。

---

## 宿主项目依赖

宿主项目必须安装以下 peer dependencies：

```bash
# Vue 生态
pnpm add vue vue-router pinia vue-i18n

# HTTP
pnpm add axios

# 图表
pnpm add chart.js

# 报表编辑器
pnpm add handsontable codemirror
```

> **注意**：版本要求参考 `aureport-ultra-ui/package.json` 中的 `dependencies` 字段。建议使用相同或兼容的版本。

---

## 快速接入

### 方式一：CDN 引入（适合快速预览）

```html
<!DOCTYPE html>
<html>
<head>
  <!-- 样式 -->
  <link rel="stylesheet" href="https://your-cdn.com/aureport-ultra.css" />
</head>
<body>
  <aureport-designer
    report-path="/reports/sales.ureport.xml"
    locale="zh"
    @save="onSave"
    @navigate="onNavigate"
    @error="onError">
  </aureport-designer>

  <!-- Vue 3 -->
  <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
  <!-- 业务依赖 -->
  <script src="https://unpkg.com/handsontable/dist/handsontable.min.js"></script>
  <script src="https://unpkg.com/codemirror/lib/codemirror.js"></script>
  <!-- 报表设计器 -->
  <script type="module" src="https://your-cdn.com/aureport-ultra.es.js"></script>
</body>
</html>
```

### 方式二：ESM 模块引入（推荐）

#### 1. 发布到私有 npm

将 `dist-lib/` 目录发布到私有 npm 源，或直接在宿主项目中通过 `npm link` / `pnpm add file:` 引用：

```bash
# 在 aureport-ultra-ui 目录
pnpm build:lib

# 在宿主项目
pnpm add file:/path/to/aureport-ultra-ui/dist-lib
```

#### 2. 配置 Vite

如果使用私有源或 file 协议，需要配置 `vite.config.ts` 将 codemirror/handsontable CSS 设为 external：

```ts
// vite.config.ts
export default defineConfig({
  resolve: {
    alias: {
      // codemirror 和 handsontable 的 CSS 需要 external，避免重复打包
      'codemirror/lib/codemirror.css': 'codemirror/lib/codemirror.css',
      'handsontable/dist/handsontable.min.css': 'handsontable/dist/handsontable.min.css',
    },
  },
  build: {
    rollupOptions: {
      external: [
        'vue',
        'vue-router',
        'pinia',
        'vue-i18n',
        'axios',
        'chart.js',
        'handsontable',
        'codemirror',
        'handsontable/dist/handsontable.min.css',
        'codemirror/lib/codemirror.css',
        'codemirror/addon/hint/show-hint.css',
        'codemirror/addon/lint/lint.css',
      ],
    },
  },
})
```

---

## 使用方式

### 基础用法

```vue
<template>
  <aureport-designer
    report-path="/reports/sales.ureport.xml"
    locale="zh"
    @save="handleSave"
    @navigate="handleNavigate"
    @error="handleError" />
</template>

<script setup>
import 'aureport-ultra-ui' // 自动注册 Web Component

const handleSave = (data) => {
  console.log('报表保存', data)
}

const handleNavigate = ({ target, params }) => {
  console.log('导航到', target, params)
  if (target === 'Preview') {
    // 跳转到预览页面
  }
}

const handleError = (err) => {
  console.error('报表错误', err)
}
</script>
```

### React 项目中使用

```tsx
import { useEffect, useRef } from 'react'

function ReportDesigner({ reportPath, onSave }) {
  const ref = useRef(null)

  useEffect(() => {
    // 等待 Web Component 注册完成
    const init = () => {
      const el = ref.current
      if (!el) return

      el.addEventListener('save', (e) => onSave(e.detail))
      el.addEventListener('navigate', (e) => console.log(e.detail))
    }

    // 确保 aureport-ultra 已加载
    if (!customElements.get('aureport-designer')) {
      import('aureport-ultra-ui').then(init)
    } else {
      init()
    }
  }, [])

  return (
    <div ref={ref}>
      <aureport-designer report-path={reportPath} locale="zh" />
    </div>
  )
}
```

### 手动注册（不自动 install）

```js
import AureportUltra from 'aureport-ultra-ui'
const { install, AureportDesignerElement } = AureportUltra

// 手动控制注册时机
install()

// 或只注册特定组件
if (!customElements.get('aureport-designer')) {
  customElements.define('aureport-designer', AureportDesignerElement)
}
```

---

## API 参考

### `<aureport-designer>` 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `report-path` | `string` | `''` | 报表文件路径，如 `/reports/sales.ureport.xml` |
| `locale` | `string` | `'zh'` | 语言，支持 `zh` / `en` |

### `<aureport-preview>` 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `report-path` | `string` | `''` | 报表文件路径 |
| `locale` | `string` | `'zh'` | 语言 |

### 方法

```js
const designer = document.querySelector('aureport-designer')

// 获取报表数据（JSON 格式）
const reportData = designer.getReportData()

// 保存报表
designer.saveReport()

// 跳转到目标（Designer / Preview）
designer.navigateTo('Preview', { reportPath: '/reports/test.ureport.xml' })

// 动态修改报表路径
designer.setReportPath('/reports/new.ureport.xml')

// 切换语言
designer.setLocale('en')
```

### 导出对象

```js
import AureportUltra from 'aureport-ultra-ui'

// 完整导出
const {
  install,                    // 注册 Web Component
  AureportDesignerElement,     // 设计器类（可用于 customElements.define）
  AureportPreviewElement,      // 预览类
  requestAdapter,              // HTTP 请求适配器
  navigationAdapter,           // 导航适配器
  setLibMode,                  // 设置库模式
  getLibMode,                  // 获取库模式
} = AureportUltra
```

---

## 事件参考

### `save`

报表保存时触发。

```js
document.querySelector('aureport-designer').addEventListener('save', (e) => {
  console.log(e.detail) // { reportPath, xmlContent, ... }
})
```

### `navigate`

报表内部导航时触发（如点击预览按钮）。

```js
document.querySelector('aureport-designer').addEventListener('navigate', (e) => {
  const { target, params } = e.detail
  // target: 'Designer' | 'Preview'
  // params: { reportPath, ... }
})
```

### `error`

报表渲染出错时触发。

```js
document.querySelector('aureport-designer').addEventListener('error', (e) => {
  console.error('报表错误:', e.detail)
})
```

---

## 宿主要求

- **Vue 3**（必须，版本 >= 3.5）
- **浏览器**：现代浏览器（Chrome、Firefox、Edge、Safari）
- **Node.js**：>= 16（开发阶段）
- ** Handsontable 版本兼容性**：推荐 `6.2.2` 或兼容版本
- **CodMirror 版本兼容性**：推荐 `5.65.x`
