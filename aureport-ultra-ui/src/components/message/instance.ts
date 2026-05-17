import { createApp } from 'vue'
import MessageComponent from './index.vue'

let messageInstance: any = null

function newInstance(params: Record<string, any> = {}) {
  const app = createApp(MessageComponent, params)
  const vm = app.mount(document.createElement('div'))
  document.body.append(vm.$el as HTMLElement)

  return { vm, app, add: (vm as any).add }
}

function showMessage(_message: any) {
  messageInstance = messageInstance || newInstance()
  messageInstance.add(_message)
}

for (const key of ['info', 'success', 'warning', 'error']) {
  ;(showMessage as any)[key] = (string: string) => {
    messageInstance = messageInstance || newInstance()
    messageInstance.add({
      message: string,
      type: key,
    })
  }
}

export default showMessage
