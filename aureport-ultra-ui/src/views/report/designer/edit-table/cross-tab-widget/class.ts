import { createApp, type App } from 'vue'
import CrossTabWidgetVue from './index.vue'
import TableManager from '../manager.js'

export default class CrossTabWidget {
  private context: Record<string, unknown>
  private hot: any
  private rowIndex: number
  private colIndex: number
  private value: string | undefined
  private app: App<Element> | null = null
  private vm: any = null
  private container: HTMLElement | null = null

  constructor(context: Record<string, unknown>, rowIndex: number, colIndex: number, value?: string) {
    this.context = context
    this.hot = TableManager.get()
    this.rowIndex = rowIndex
    this.colIndex = colIndex
    this.value = value
    this.refreshCell()
  }

  refreshCell() {
    if (this.app) {
      this.app.unmount()
      this.app = null
      this.vm = null
    }

    if (this.container && this.container.parentNode) {
      this.container.parentNode.removeChild(this.container)
      this.container = null
    }

    const td = this.hot.getCell(this.rowIndex, this.colIndex) as HTMLTableCellElement
    while (td.firstChild) {
      td.removeChild(td.firstChild)
    }

    this.container = document.createElement('div')
    td.appendChild(this.container)

    const app = createApp(CrossTabWidgetVue, {
      context: this.context,
      rowIndex: this.rowIndex,
      colIndex: this.colIndex,
      value: this.value,
    })
    this.vm = app.mount(this.container)
    this.app = app
  }

  doDraw() {
    if (this.vm) {
      ;(this.vm as any).doDraw?.()
    }
  }

  destroy() {
    if (this.app) {
      this.app.unmount()
      this.app = null
      this.vm = null
    }
    if (this.container && this.container.parentNode) {
      this.container.parentNode.removeChild(this.container)
      this.container = null
    }
  }
}
