import type { Router } from 'vue-router'

interface NavigateOptions {
  target: string
  params?: Record<string, string>
  openInNewTab?: boolean
  action?: string
}

let isLibMode = false
let router: Router | null = null

export function setLibMode(mode: boolean): void {
  isLibMode = mode
}

export function getLibMode(): boolean {
  return isLibMode
}

export function setRouter(instance: Router): void {
  router = instance
}

export function createNavigator() {
  return {
    navigate(options: NavigateOptions) {
      const { target, params, openInNewTab } = options
      if (openInNewTab) {
        const routeData = router!.resolve({ name: target, query: params })
        window.open(routeData.href, '_blank')
      }
      else {
        window.open(router!.resolve({ name: target, query: params }).href, '_self')
      }
    },

    openPreview(params: Record<string, string>, openInNewTab = true) {
      this.navigate({ target: 'Preview', params, openInNewTab })
    },

    openDesigner(params: Record<string, string>, openInNewTab = false) {
      this.navigate({ target: 'Designer', params, openInNewTab })
    },

    refresh() {
      window.location.reload()
    },

    getRouteParams(): Record<string, string> {
      if (!router)
        return {}
      return (router.currentRoute.value.query as Record<string, string>) || {}
    },
  }
}

export function getRouteParams(): Record<string, string> {
  if (!router)
    return {}
  return (router.currentRoute.value.query as Record<string, string>) || {}
}
