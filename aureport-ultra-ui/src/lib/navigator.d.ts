export function setLibMode(mode: boolean): void
export function getLibMode(): boolean
export function createNavigator(component: { $router: any, $route: any }): {
  navigate: (options: { target: string, params?: any, openInNewTab?: boolean }) => void
  openPreview: (params: any, openInNewTab?: boolean) => void
  openDesigner: (params: any, openInNewTab?: boolean) => void
  refresh: () => void
  getRouteParams: () => Record<string, any>
}
export function getRouteParams(component: { $route: any }): Record<string, any>
