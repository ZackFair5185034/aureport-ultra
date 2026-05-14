import { defineConfig } from 'vite'
import { resolve } from 'path'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import VueDevTools from 'vite-plugin-vue-devtools'
import Components, { kebabCase } from 'unplugin-vue-components/vite'
import UnoCSS from 'unocss/vite'

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

// 目录名到组件名的映射（目录名与 defineOptions name 不一致时使用）
const toolBarDirResolver = () => {
  const toolBarDir = resolve(__dirname, 'src/views/report/designer/tool-bar')
  return (componentName: string) => {
    // 目录名与组件名不一致的映射
    const dirToFile: Record<string, string> = {
      AlignTopTool: 'align-tool/index.vue',
      PageSettings: 'settings-tool/settings-dialog/page/index.vue',
      HeaderFooterSettings: 'settings-tool/settings-dialog/headerFooter/index.vue',
      PagingSettings: 'settings-tool/settings-dialog/paging/index.vue',
      ColumnSettings: 'settings-tool/settings-dialog/column/index.vue',
    }
    if (dirToFile[componentName]) {
      return resolve(toolBarDir, dirToFile[componentName])
    }
    // 其他工具栏组件：目录名就是组件名（PascalCase），找 index.vue
    // unplugin-vue-components 默认行为已经处理了，这里返回 undefined 即可
    return undefined
  }
}

export default defineConfig({
  plugins: [
    babelPolyfillShim(),
    vue(),
    vueJsx(),
    VueDevTools(),
    UnoCSS(),
    Components({
      dirs: [
        resolve(__dirname, 'src/components'),
        resolve(__dirname, 'src/views/report/designer/tool-bar'),
      ],
      extensions: ['vue'],
      deep: true,
      dts: resolve(__dirname, 'src/types/components.d.ts'),
      resolvers: [
        toolBarDirResolver(),
      ],
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: Number(process.env.VITE_DEV_PORT) || 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8050',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/report'),
      },
    },
  },
  build: {
    target: 'esnext',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
  },
})
