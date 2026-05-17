import type { App } from 'vue'
import { createPinia } from 'pinia'
import { createApp, h } from 'vue'
import { createI18n } from 'vue-i18n'
import en from '@/locales/lang/en'
import zh from '@/locales/lang/zh'
import DesignerComponent from '@/views/report/designer/index.vue'

import 'handsontable/dist/handsontable.min.css'
import 'codemirror/lib/codemirror.css'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/lint/lint.css'
import '@/assets/css/designer/tree.css'
import '@/assets/css/iconfont/iconfont.css'
import '@/assets/css/common/index.css'

class LuckDesignerElement extends HTMLElement {
  private _app: App<Element> | null = null
  private _vm: any = null
  private _props: Record<string, any> = {}

  static get observedAttributes() {
    return ['report-path', 'locale']
  }

  connectedCallback() {
    this._mount()
  }

  disconnectedCallback() {
    if (this._app) {
      this._app.unmount()
      this._app = null
      this._vm = null
    }
  }

  attributeChangedCallback(name: string, oldValue: string | null, newValue: string | null) {
    if (oldValue !== newValue && this._vm) {
      const propName = this._camelize(name)
      this._props[propName] = this._parseValue(newValue)

      if (name === 'report-path') {
        this._vm.internalReportPath = this._props.reportPath
      }
    }
  }

  set reportPath(val: string) {
    this._props.reportPath = val
    if (this._vm) {
      this._vm.internalReportPath = val
    }
  }

  get reportPath() {
    return this._props.reportPath
  }

  private _mount() {
    const container = document.createElement('div')
    container.className = 'luck-designer-container'
    container.style.width = '100%'
    container.style.height = '100%'
    this.append(container)

    const pinia = createPinia()
    const locale = this.getAttribute('locale') || 'zh'
    const i18n = createI18n({
      locale,
      fallbackLocale: 'en',
      legacy: false,
      messages: { zh, en },
    })

    const reportPath = this.getAttribute('report-path') || ''

    const Wrapper = {
      name: 'LuckDesignerWrapper',
      props: { reportPath: String },
      data() {
        return { internalReportPath: (this as any).reportPath || reportPath }
      },
      watch: {
        reportPath: {
          immediate: true,
          handler(val: string) {
            if (val !== undefined)
              (this as any).internalReportPath = val
          },
        },
      },
      methods: {
        _emit(eventName: string, detail?: any) {
          ;(this as any).$el.dispatchEvent(
            new CustomEvent(eventName, { detail, bubbles: true, composed: true }),
          )
        },
        _handleNavigate(data: any) {
          if (data.target === 'Designer' && data.params && data.params.reportPath) {
            ;(this as any).internalReportPath = data.params.reportPath
            if ((this as any).$el && (this as any).$el.parentElement) {
              ;(this as any).$el.parentElement.setAttribute('report-path', data.params.reportPath)
            }
          }

          ;(this as any)._emit('navigate', data)
        },
        getReportData() {
          return (this as any).$refs?.designerComponent?.getReportData?.()
        },
        saveReport() {
          return (this as any).$refs?.designerComponent?.saveReport?.()
        },
        navigateTo(target: string, params: any, openInNewTab = true) {
          ;(this as any)._handleNavigate({ target, params, openInNewTab })
        },
      },
      render() {
        return h(DesignerComponent as any, {
          ref: 'designerComponent',
          reportPath: (this as any).internalReportPath,
          onNavigate: (data: any) => (this as any)._handleNavigate(data),
          onSave: (data: any) => (this as any)._emit('save', data),
          onError: (err: any) => (this as any)._emit('error', err),
        })
      },
    }

    this._app = createApp(Wrapper)
    this._app.use(pinia)
    this._app.use(i18n)
    this._vm = this._app.mount(container)
  }

  private _camelize(str: string) {
    return str.replaceAll(/-(\w)/g, (_, c) => (c ? c.toUpperCase() : ''))
  }

  private _parseValue(value: string | null) {
    if (value === null || value === undefined)
      return value
    if (value === 'true')
      return true
    if (value === 'false')
      return false
    try {
      return JSON.parse(value)
    }
    catch {
      return value
    }
  }

  getReportData() {
    return this._vm?.getReportData?.()
  }

  saveReport() {
    return this._vm?.saveReport?.()
  }

  navigateTo(target: string, params: any, openInNewTab = true) {
    return this._vm?.navigateTo?.(target, params, openInNewTab)
  }

  setReportPath(path: string) {
    this.setAttribute('report-path', path)
  }

  setLocale(locale: string) {
    this.setAttribute('locale', locale)
  }
}

export default LuckDesignerElement
