import { createApp } from 'vue'
import MessageBoxTemplate from './index.vue'

const MessageBox = function (params: Record<string, any>) {
  const app = createApp(MessageBoxTemplate, {
    visible: true,
    ...params,
  })
  const vm = app.mount(document.createElement('div'))
  document.body.appendChild(vm.$el as HTMLElement)

  return { app, vm }
}

let msgboxInstance: any = null

MessageBox.alert = function (message: string, title?: string, options?: Record<string, any>) {
  const params = { type: 'alert' as const, message, title, ...options }
  msgboxInstance = msgboxInstance || MessageBox(params)
  return new Promise((resolve) => {
    msgboxInstance.vm.$el.addEventListener('click', () => {
      msgboxInstance.app.unmount()
      msgboxInstance = null
      resolve(undefined)
    })
  })
}

MessageBox.confirm = function (message: string, title?: string, options?: Record<string, any>) {
  const params = { type: 'confirm' as const, message, title, ...options }
  msgboxInstance = msgboxInstance || MessageBox(params)
  return new Promise((resolve) => {
    msgboxInstance.vm.$el.addEventListener('click', () => {
      msgboxInstance.app.unmount()
      msgboxInstance = null
      resolve(undefined)
    })
  })
}

MessageBox.prompt = function (message: string, title?: string, options?: Record<string, any>) {
  const params = { type: 'prompt' as const, message, title, ...options }
  msgboxInstance = msgboxInstance || MessageBox(params)
  return new Promise((resolve) => {
    msgboxInstance.vm.$el.addEventListener('click', () => {
      msgboxInstance.app.unmount()
      msgboxInstance = null
      resolve(undefined)
    })
  })
}

export default MessageBox
