declare const chartWidgetManager: {
  get(key: string): any
  set(key: string, widget: any): void
  has(key: string): boolean
  remove(key: string): void
  clear(): void
}
export default chartWidgetManager
