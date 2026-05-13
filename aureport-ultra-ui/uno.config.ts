import { defineConfig, presetUno, presetAttributify, presetIcons } from 'unocss'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons({
      scale: 1.2,
      warn: true,
    }),
  ],
  shortcuts: {
    'btn-primary': 'px-4 py-2 bg-primary text-white rounded hover:bg-primary/80 disabled:opacity-50 disabled:cursor-not-allowed',
    'btn-default': 'px-4 py-2 bg-white text-gray-700 rounded border border-gray-300 hover:bg-gray-50 disabled:opacity-50',
    'btn-danger': 'px-4 py-2 bg-danger text-white rounded hover:bg-danger/80',
    'card': 'p-4 bg-white rounded-lg shadow-sm border border-gray-200',
    'card-header': 'text-base font-bold text-gray-800 mb-3',
    'form-label': 'block text-sm font-medium text-gray-700 mb-1',
    'form-input': 'w-full px-3 py-2 border border-gray-300 rounded text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary',
    'form-select': 'w-full px-3 py-2 border border-gray-300 rounded text-sm bg-white focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary',
    'dialog-overlay': 'fixed inset-0 bg-black/50 z-50 flex items-center justify-center',
    'dialog-panel': 'bg-white rounded-lg shadow-xl max-w-lg w-full mx-4 max-h-[90vh] overflow-y-auto',
    'toolbar-btn': 'flex items-center gap-1 px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded cursor-pointer',
    'menu-item': 'flex items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 cursor-pointer',
    'tab-header': 'px-4 py-2 text-sm cursor-pointer border-b-2 border-transparent hover:text-primary hover:border-primary/30',
    'tab-header-active': 'px-4 py-2 text-sm cursor-pointer border-b-2 border-primary text-primary font-medium',
  },
  theme: {
    colors: {
      primary: '#409eff',
      success: '#67c23a',
      warning: '#e6a23c',
      danger: '#f56c6c',
      info: '#909399',
    },
  },
  rules: [],
  preflights: [
    {
      getCSS: () => `
        html { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; }
        body { margin: 0; padding: 0; }
      `,
    },
  ],
})
