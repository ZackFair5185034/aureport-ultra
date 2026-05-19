import type { App } from 'vue'
import { createApp } from 'vue'
import UInput from '@/components/input/index.vue'
import UButton from '@/components/button/index.vue'
import MessageBoxTemplate from './index.vue'

function registerUIComponents(app: App) {
  app.component('UInput', UInput)
  app.component('UButton', UButton)
}

function MessageBox(params: Record<string, any>) {
  const app = createApp(MessageBoxTemplate, {
    visible: true,
    ...params,
  })
  registerUIComponents(app)
  const vm = app.mount(document.createElement('div'))
  document.body.append(vm.$el as HTMLElement)

  return { app, vm }
}

let msgboxInstance: any = null

MessageBox.alert = function (message: string, title?: string, options?: Record<string, any>) {
  const params = { type: 'alert' as const, message, title, ...options }
  msgboxInstance = msgboxInstance || MessageBox(params)
  return new Promise<void>((resolve) => {
    msgboxInstance.vm.$el.addEventListener('click', () => {
      msgboxInstance.app.unmount()
      msgboxInstance = null
      resolve()
    })
  })
}

MessageBox.confirm = function (message: string, title?: string, options?: Record<string, any>) {
  const params = { type: 'confirm' as const, message, title, ...options }
  msgboxInstance = msgboxInstance || MessageBox(params)
  return new Promise<void>((resolve) => {
    msgboxInstance.vm.$el.addEventListener('click', () => {
      msgboxInstance.app.unmount()
      msgboxInstance = null
      resolve()
    })
  })
}

MessageBox.prompt = function (message: string, title?: string, options?: Record<string, any>) {
  const params = { type: 'prompt' as const, message, title, ...options }
  msgboxInstance = msgboxInstance || MessageBox(params)
  return new Promise<void>((resolve) => {
    msgboxInstance.vm.$el.addEventListener('click', () => {
      msgboxInstance.app.unmount()
      msgboxInstance = null
      resolve()
    })
  })
}

export default MessageBox
