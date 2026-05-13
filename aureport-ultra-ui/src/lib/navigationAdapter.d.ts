declare const navigationAdapter: {
  setNavigationHandler(handler: any): void
  setVueRouter(router: any): void
  setRouteMapping(mapping: Record<string, string>, options?: { actions?: Record<string, any> }): void
  getRouteMapping(): Record<string, string>
  resolveRouteName(target: string): string
  navigate(options: { target?: string; params?: any; openInNewTab?: boolean; action?: string }): void
  openPreview(params: any, openInNewTab?: boolean): void
  openDesigner(params: any, openInNewTab?: boolean): void
  refresh(): void
}
export default navigationAdapter
