import { createApp } from 'vue'
import MessageComponent from './index.vue'

let messageInstance: any = null

const newInstance = (params: Record<string, any> = {}) => {
  const app = createApp(MessageComponent, params)
  const vm = app.mount(document.createElement('div'))
  document.body.appendChild(vm.$el as HTMLElement)

  return { vm, app, add: (vm as any).add }
}

const showMessage = (_message: any) => {
  messageInstance = messageInstance || newInstance()
  messageInstance.add(_message)
}

;['info', 'success', 'warning', 'error'].forEach((key) => {
  ;(showMessage as any)[key] = (string: string) => {
    messageInstance = messageInstance || newInstance()
    messageInstance.add({
      message: string,
      type: key,
    })
  }
})

export default showMessage
