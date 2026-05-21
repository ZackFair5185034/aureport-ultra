import { defineConfig } from 'vite'
import { resolve } from 'path'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'

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
      return '// @babel/polyfill is deprecated, handsontable@6.2.2 references it internally\nexport default {}'
    }
    return null
  },
})

// vue-datepicker-next locale 文件内部有 __require("vue-datepicker-next") 调用
// 在 ESM 环境下替换为 mock 对象避免运行时错误
const vueDatepickerNextShim = () => ({
  name: 'vue-datepicker-next-shim',
  renderChunk(code: string, chunk: any) {
    if (chunk.fileName === 'aureport-ultra.es.js') {
      // locale 文件 (require_zh_cn 等) 内部有 __require("vue-datepicker-next") 调用
      // 这些 locale 是 CJS 格式，__require 在 ESM 环境会失败
      // 替换策略：把 __require("vue-datepicker-next") 替换为一个空的 shim 对象
      const shimmed = code.replace(
        /__require\("vue-datepicker-next"\)/g,
        '({ default: { install:()=>{}, lang:{ zhCn:{ formatLocale:{ months:[], week:null, t:()=>\'\' } } } } })'
      )
      return shimmed
    }
    return null
  },
})

// 库模式需要 external 的依赖（宿主环境必须提供）
const externalDeps = [
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
  'raphael',
  'sortablejs',
  'vuedraggable',
  'js-beautify',
  'sql-formatter',
  'uuid',
  'clipboard',
  'save-svg-as-png',
  'async-validator',
  'chartjs-plugin-datalabels',
  'undo-manager',
  'vue-datepicker-next',
  'vue-datepicker-next/index.css',
  'vue-datepicker-next/locale/zh-cn',
  'vue-datepicker-next/locale/en',
  /@ckpack\/vue-color/,
]

export default defineConfig({
  plugins: [
    babelPolyfillShim(),
    vueDatepickerNextShim(),
    vue(),
    vueJsx(),
    Components({
      dirs: [
        resolve(__dirname, 'src/components'),
        resolve(__dirname, 'src/views/report/designer/tool-bar'),
        resolve(__dirname, 'src/views/report/designer/edit-table'),
        resolve(__dirname, 'src/views/report/designer/resource-panel'),
        resolve(__dirname, 'src/views/report/designer/search-form'),
      ],
      extensions: ['vue'],
      deep: true,
    }),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  build: {
    lib: {
      entry: resolve(__dirname, 'src/lib/index.ts'),
      name: 'AureportUltra',
      formats: ['es'],
      fileName: (format) => `aureport-ultra.${format}.js`,
    },
    outDir: 'dist-lib',
    copyPublicDir: false,
    sourcemap: false,
    rollupOptions: {
      external: externalDeps,
      output: {
        exports: 'named',
        // 把 css 提取到单独文件，统一命名
        assetFileNames: (assetInfo) => {
          if (assetInfo.name?.endsWith('.css'))
            return 'aureport-ultra.css'
          return assetInfo.name || 'assets/[name]-[hash][extname]'
        },
      },
    },
  },
})
