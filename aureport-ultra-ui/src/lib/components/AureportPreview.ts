import type { App } from 'vue'
import { Chart, registerables } from 'chart.js'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import { createPinia } from 'pinia'
import { createApp } from 'vue'
import { createI18n } from 'vue-i18n'
import en from '@/locales/lang/en'

import zh from '@/locales/lang/zh'
import { updateUrlParams } from '@/utils/url'
import PreviewComponent from '@/views/report/preview/index.vue'
import { registerUComponents } from '@/lib/registerUComponents'
import '@/assets/css/iconfont/iconfont.css'
import '@/assets/css/common/index.css'

Chart.register(...registerables, ChartDataLabels)

class AureportPreviewElement extends HTMLElement {
  private _app: App<Element> | null = null
  private _vm: any = null
  private _i18n: any = null

  static get observedAttributes() {
    return ['report-path', 'params', 'locale', 'mode', 'page-index', 'tools-info']
  }

  connectedCallback() {
    this._mount()
  }

  disconnectedCallback() {
    if (this._app) {
      this._app.unmount()
      this._app = null
      this._vm = null
      this._i18n = null
    }
  }

  attributeChangedCallback(name: string, oldValue: string | null, newValue: string | null) {
    if (oldValue !== newValue) {
      if (name === 'locale') {
        if (this._i18n) {
          this._i18n.global.locale = newValue || 'zh'
        }

        return
      }

      this._syncUrlFromAttributes()
      this._refreshPreview(true)
    }
  }

  private _refreshPreview(reload = false) {
    if (this._vm) {
      ;(this._vm as any).parseParamsFromUrl()
      if (reload) {
        ;(this._vm as any).initReport()
      }
    }
  }

  set reportPath(val: string) {
    updateUrlParams({ reportPath: val })
    this._refreshPreview(true)
  }

  get reportPath() {
    return this.getAttribute('report-path') || ''
  }

  set params(val: Record<string, any>) {
    if (val && typeof val === 'object') {
      updateUrlParams(val)
    }

    this._refreshPreview(true)
  }

  get params() {
    try {
      return JSON.parse(this.getAttribute('params') || '{}')
    }
    catch {
      return {}
    }
  }

  set mode(val: string) {
    updateUrlParams({ mode: val })
    this._refreshPreview(false)
  }

  get mode() {
    return this.getAttribute('mode') || ''
  }

  set pageIndex(val: string | number) {
    updateUrlParams({ _i: val })
    this._refreshPreview(false)
  }

  get pageIndex() {
    return this.getAttribute('page-index') || ''
  }

  set toolsInfo(val: string) {
    updateUrlParams({ _t: val })
    this._refreshPreview(false)
  }

  get toolsInfo() {
    return this.getAttribute('tools-info') || ''
  }

  private _mount() {
    this._syncUrlFromAttributes()
    this.style.display = this.style.display || 'block'
    this.style.width = this.style.width || '100%'
    this.style.height = this.style.height || '100%'

    const container = document.createElement('div')
    container.className = 'aureport-preview-container'
    container.style.width = '100%'
    container.style.height = '100%'
    this.append(container)

    const pinia = createPinia()
    const locale = this.getAttribute('locale') || 'zh'
    this._i18n = createI18n({
      locale,
      fallbackLocale: 'en',
      legacy: false,
      messages: { zh, en },
    })

    this._app = createApp(PreviewComponent)
    registerUComponents(this._app)
    this._app.use(pinia)
    this._app.use(this._i18n)
    this._vm = this._app.mount(container)
  }

  private _syncUrlFromAttributes() {
    const params: Record<string, any> = {}

    const paramsStr = this.getAttribute('params')
    if (paramsStr) {
      try {
        const extraParams = JSON.parse(paramsStr)
        for (const key of Object.keys(extraParams)) {
          params[key] = extraParams[key]
        }
      }
      catch {
        // ignore invalid JSON
      }
    }

    const reportPath = this.getAttribute('report-path')
    const mode = this.getAttribute('mode')
    const pageIndex = this.getAttribute('page-index')
    const toolsInfo = this.getAttribute('tools-info')

    if (reportPath != null)
      params.reportPath = reportPath || null
    if (mode != null)
      params.mode = mode || null
    if (pageIndex != null)
      params._i = pageIndex || null
    if (toolsInfo != null)
      params._t = toolsInfo || null

    updateUrlParams(params)
  }

  refresh() {
    return this._vm?.refresh?.()
  }

  handlePageChange(pageIndex: number) {
    return this._vm?.handlePageChange?.(pageIndex)
  }

  setReportPath(path: string) {
    this.setAttribute('report-path', path)
  }

  setParams(params: Record<string, any>) {
    this.setAttribute('params', JSON.stringify(params))
  }

  setLocale(locale: string) {
    this.setAttribute('locale', locale)
  }
}

export default AureportPreviewElement
