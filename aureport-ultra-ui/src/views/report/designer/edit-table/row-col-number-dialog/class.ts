import type { App } from 'vue'
import { createApp } from 'vue'
import UButton from '@/components/button/index.vue'
import UDialog from '@/components/dialog/index.vue'
import UFormItem from '@/components/form-item/index.vue'
import UForm from '@/components/form/index.vue'
import UInputNumber from '@/components/input-number/index.vue'
import i18n from '@/locales'
import RowColNumberDialogVue from './index.vue'

export default class RowColNumberDialog {
  private app: App<Element> | null = null
  private vm: any = null

  constructor() {
    const mountPoint = document.createElement('div')
    document.body.append(mountPoint)

    const app = createApp(RowColNumberDialogVue)
    app.use(i18n)
    app.component('UDialog', UDialog)
    app.component('u-dialog', UDialog)
    app.component('UForm', UForm)
    app.component('u-form', UForm)
    app.component('UFormItem', UFormItem)
    app.component('u-form-item', UFormItem)
    app.component('UInputNumber', UInputNumber)
    app.component('u-input-number', UInputNumber)
    app.component('UButton', UButton)
    app.component('u-button', UButton)
    this.vm = app.mount(mountPoint)
    this.app = app
  }

  show(callback: (val: number) => void, isRow: boolean) {
    ;(this.vm as any).show(callback, isRow)
  }
}
