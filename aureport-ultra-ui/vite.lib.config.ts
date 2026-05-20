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
  /@ckpack\/vue-color/,
]

export default defineConfig({
  plugins: [
    babelPolyfillShim(),
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
        // CDN 场景：把 vue 等 peer deps 暴露为全局变量
        globals: {
          vue: 'Vue',
          'vue-router': 'VueRouter',
          pinia: 'Pinia',
          'vue-i18n': 'VueI18n',
          axios: 'axios',
          'chart.js': 'Chart',
          handsontable: 'Handsontable',
          codemirror: 'CodeMirror',
        },
        // 把 css 提取到单独文件
        assetFileNames: (assetInfo) => {
          if (assetInfo.name === 'style.css')
            return 'aureport-ultra.css'
          return assetInfo.name || 'assets/[name]-[hash][extname]'
        },
      },
    },
  },
})
