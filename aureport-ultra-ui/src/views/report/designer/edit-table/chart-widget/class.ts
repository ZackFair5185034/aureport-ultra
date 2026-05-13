import { createApp, type App } from 'vue'
import ChartWidgetVue from './index.vue'
import chartWidgetManager from './manager.js'

export default class ChartWidget {
  private container: HTMLElement | null
  private rowIndex: number
  private colIndex: number
  private app: App<Element> | null = null

  constructor(container: HTMLElement | null, rowIndex: number, colIndex: number) {
    this.container = container
    this.rowIndex = rowIndex
    this.colIndex = colIndex
  }

  renderChart(container?: HTMLElement, context?: Record<string, unknown>, rowIndex?: number, colIndex?: number) {
    if (container) this.container = container
    if (!this.container) {
      console.error('Container element not provided for Class')
      return
    }

    if (this.app) {
      this.app.unmount()
      this.app = null
    }

    this.container.innerHTML = ''
    const mountPoint = document.createElement('div')
    mountPoint.className = 'test'
    this.container.appendChild(mountPoint)

    const app = createApp(ChartWidgetVue, {
      context: context || {},
      rowIndex: rowIndex ?? this.rowIndex,
      colIndex: colIndex ?? this.colIndex,
    })
    app.mount(mountPoint)
    this.app = app
  }

  refresh(context: Record<string, unknown>) {
    this.renderChart(this.container || undefined, context, this.rowIndex, this.colIndex)
  }

  destroy() {
    if (this.app) {
      this.app.unmount()
      this.app = null
    }
    const widgetKey = `${this.rowIndex}_${this.colIndex}`
    chartWidgetManager.remove(widgetKey)
  }
}

;(window as any).chartColors = {
  red: 'rgb(255, 99, 132)',
  orange: 'rgb(255, 159, 64)',
  yellow: 'rgb(255, 205, 86)',
  green: 'rgb(75, 192, 192)',
  blue: 'rgb(54, 162, 235)',
  purple: 'rgb(153, 102, 255)',
  grey: 'rgb(201, 203, 207)'
}
