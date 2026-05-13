import { createApp, type App } from 'vue'
import RowColWidthHeightDialogVue from './index.vue'
import i18n from '@/locales'

export default class RowColWidthHeightDialog {
  private app: App<Element> | null = null
  private vm: any = null

  constructor() {
    const mountPoint = document.createElement('div')
    document.body.appendChild(mountPoint)

    const app = createApp(RowColWidthHeightDialogVue)
    app.use(i18n)
    this.vm = app.mount(mountPoint)
    this.app = app
  }

  show(callback: (val: number) => void, value?: string | number, iscol?: boolean) {
    ;(this.vm as any).show(callback, value, iscol)
  }
}
