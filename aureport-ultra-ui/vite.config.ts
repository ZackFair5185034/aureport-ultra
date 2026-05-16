import { defineConfig } from 'vite'
import { resolve } from 'path'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import VueDevTools from 'vite-plugin-vue-devtools'
import Components from 'unplugin-vue-components/vite'
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
        resolve(__dirname, 'src/views/report/designer/edit-table'),
        resolve(__dirname, 'src/views/report/designer/resource-panel'),
        resolve(__dirname, 'src/views/report/designer/search-form'),
      ],
      extensions: ['vue'],
      deep: true,
      dts: resolve(__dirname, 'src/types/components.d.ts'),
    }),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    host: '0.0.0.0',
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
