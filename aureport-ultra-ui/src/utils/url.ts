export function updateUrlParams(params: Record<string, string | number | boolean | null>, usePushState = false): void {
  const url = new URL(window.location.href)
  for (const key of Object.keys(params)) {
    const value = params[key]
    if (value != null && value !== '') {
      url.searchParams.set(key, String(value))
    }
    else {
      url.searchParams.delete(key)
    }
  }

  if (usePushState) {
    window.history.pushState({}, '', url.toString())
  }
  else {
    window.history.replaceState({}, '', url.toString())
  }
}

export function getUrlQueryString(): string {
  return window.location.search.slice(1)
}
