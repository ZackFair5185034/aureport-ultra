import { createApp, type Component, type App } from 'vue'
import UCheckbox from '@/components/checkbox/index.vue'
import UCheckboxGroup from '@/components/checkbox-group/index.vue'
import USelect from '@/components/select/index.vue'
import UOption from '@/components/option/index.vue'
import URadioGroup from '@/components/radio-group/index.vue'
import URadio from '@/components/radio/index.vue'
import USwitch from '@/components/switch/index.vue'
import UInput from '@/components/input/index.vue'
import UInputNumber from '@/components/input-number/index.vue'
import UButton from '@/components/button/index.vue'
import UDialog from '@/components/dialog/index.vue'
import UFormItem from '@/components/form-item/index.vue'
import UForm from '@/components/form/index.vue'
import URow from '@/components/row/index.vue'
import UCol from '@/components/col/index.vue'
import UDatePicker from '@/components/date-picker/index.vue'
import UTree from '@/components/tree/index.vue'
import UTreeNode from '@/components/tree/tree-node.vue'
import UTabs from '@/components/tabs/index.vue'
import UTabPane from '@/components/tabs/pane.vue'
import UButtonGroup from '@/components/button-group/index.vue'
import UColorPicker from '@/components/color-picker/index.vue'
import UDivider from '@/components/divider/index.vue'
import UTag from '@/components/tag/index.vue'
import ULoading from '@/components/loading/index.vue'
import UMessage from '@/components/message/index.vue'
import UMessageBox from '@/components/messagebox/index.vue'

const componentMap: Record<string, Component> = {
  UDialog, USwitch, URadioGroup, USelect, UOption, UCheckbox,
  UCheckboxGroup, URadio, UInput, UInputNumber, UButton, UFormItem,
  UForm, URow, UCol, UDatePicker, UTree, UTreeNode, UTabs,
  UTabPane, UButtonGroup, UColorPicker, UDivider, UTag, ULoading,
  UMessage, UMessageBox,
}

export function buildLocationSearchParameters(searchFormParameters: Record<string, unknown>): string {
  let urlParameters = window.location.search
  if (urlParameters.length > 0) {
    urlParameters = urlParameters.substring(1)
  }
  const parameters: Record<string, string> = {}
  const pairs = urlParameters.split('&')
  for (let i = 0; i < pairs.length; i++) {
    const item = pairs[i]
    if (item === '') continue
    const param = item.split('=')
    parameters[param[0]] = param[1]
  }
  if (searchFormParameters) {
    for (const key in searchFormParameters) {
      const value = searchFormParameters[key]
      if (value) {
        parameters[key] = String(value)
      }
    }
  }
  let p = '?'
  for (const key in parameters) {
    if (p === '?') {
      p += key + '=' + parameters[key]
    } else {
      p += '&' + key + '=' + parameters[key]
    }
  }
  return p
}

export function renderTemplateToComponent(componentStr: string, mountNode: HTMLElement | string): Record<string, any> {
  const node = typeof mountNode === 'string'
    ? document.querySelector(mountNode) as HTMLElement | null
    : mountNode
  if (!node) {
    console.error(`找不到挂载节点: ${mountNode}`)
    return {
      $on() { /* noop */ },
      $destroy() { /* noop */ },
    }
  }

  const templateMatch = componentStr.match(/<template[^>]*>([\s\S]*?)<\/template>/)
  const scriptMatch = componentStr.match(/<script[^>]*>([\s\S]*?)<\/script>/)

  if (!templateMatch) {
    console.error('组件字符串中未找到template部分')
    return {
      $on() { /* noop */ },
      $destroy() { /* noop */ },
    }
  }

  const template = templateMatch[1].trim()

  const eventHandlers: Record<string, Function[]> = {}
  const componentOptions: Record<string, any> = {
    template,
    components: { ...componentMap },
  }

  if (scriptMatch) {
    try {
      const scriptContent = scriptMatch[1].trim()
      const cleanedScript = scriptContent
        .replace(/import\s+.*?from\s+['"].*?['"];?\s*/g, '')
        .replace(/export\s+default\s+/, '')

      const fn = new Function(
        ...Object.keys(componentMap),
        `return ${cleanedScript}`
      )
      const vals = Object.values(componentMap)
      const scriptResult = fn(...vals)

      if (scriptResult.data && typeof scriptResult.data === 'function') {
        const originalData = componentOptions.data || (() => ({}))
        componentOptions.data = function () {
          return Object.assign({}, originalData.call(this), scriptResult.data.call(this))
        }
      }

      if (scriptResult.methods) {
        componentOptions.methods = Object.assign(
          {},
          componentOptions.methods || {},
          scriptResult.methods,
        )
      }

      for (const key in scriptResult) {
        if (!['data', 'methods', 'components'].includes(key)) {
          componentOptions[key] = scriptResult[key]
        }
      }
    } catch (error) {
      console.error('解析组件script部分时出错:', error)
    }
  }

  const app: App = createApp(componentOptions)
  const vm = app.mount(node) as any

  // Intercept internal emit to capture events (Vue 3 removed $on/$off)
  const internalInstance = (app as any)._instance
  if (internalInstance) {
    const originalEmit = internalInstance.emit
    internalInstance.emit = (event: string, ...args: any[]) => {
      if (eventHandlers[event]) {
        eventHandlers[event].forEach(fn => fn(...args))
      }
      return originalEmit(event, ...args)
    }
  }

  return {
    $on(event: string, handler: Function) {
      if (!eventHandlers[event]) eventHandlers[event] = []
      eventHandlers[event].push(handler)
    },
    $destroy() {
      app.unmount()
    },
  }
}

export function simplifyObject(obj: unknown): unknown {
  if (typeof obj !== 'object' || obj === null) return obj

  if (Array.isArray(obj)) {
    return obj.map(item => simplifyObject(item))
  }

  const result: Record<string, any> = {}
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      const value = (obj as Record<string, any>)[key]
      if (typeof value === 'object' && value !== null) {
        if (Object.prototype.hasOwnProperty.call(value, 'value') && Object.keys(value).length === 1) {
          result[key] = value.value
        } else if (
          Array.isArray(value) &&
          value.length > 0 &&
          value.every(
            (item: unknown) =>
              typeof item === 'object' &&
              item !== null &&
              Object.prototype.hasOwnProperty.call(item, 'value') &&
              Object.keys(item).length === 1,
          )
        ) {
          result[key] = value.map((item: Record<string, any>) => simplifyObject(item.value))
        } else if (Array.isArray(value)) {
          result[key] = value.map((item: unknown) => simplifyObject(item))
        } else if (Object.keys(value).length === 0) {
          result[key] = ''
        } else {
          result[key] = simplifyObject(value)
        }
      } else {
        result[key] = value
      }
    }
  }
  return result
}
