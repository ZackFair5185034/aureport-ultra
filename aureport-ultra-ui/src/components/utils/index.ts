export function debounce(func: () => void, wait: number, name: string): void {
  const key = `__debounce_${name}`
  const timer = (window as unknown as Record<string, ReturnType<typeof setTimeout> | undefined>)[key]
  if (timer)
    clearTimeout(timer)
  ;(window as unknown as Record<string, ReturnType<typeof setTimeout> | undefined>)[key] = setTimeout(() => {
    func()
    ;(window as unknown as Record<string, ReturnType<typeof setTimeout> | undefined>)[key] = undefined
  }, wait)
}

export function oneOf<T>(value: T, validList: T[]): boolean {
  return validList.includes(value)
}

export function deepCopy<T>(data: T, hash = new WeakMap()): T {
  if (typeof data !== 'object' || data === null)
    return data
  if (hash.has(data))
    return hash.get(data) as T

  const t = typeOf(data)
  const o = (t === 'array' ? [] : {}) as T
  hash.set(data, o)

  if (t === 'array') {
    for (let i = 0; i < (data as unknown[]).length; i++) {
      ;(o as unknown[]).push(deepCopy((data as unknown[])[i], hash))
    }
  }
  else if (t === 'object') {
    for (const i in data as Record<string, unknown>) {
      ;(o as Record<string, unknown>)[i] = deepCopy((data as Record<string, unknown>)[i], hash)
    }
  }

  return o
}

function typeOf(obj: unknown): string {
  const toString = Object.prototype.toString
  const map: Record<string, string> = {
    '[object Boolean]': 'boolean',
    '[object Number]': 'number',
    '[object String]': 'string',
    '[object Function]': 'function',
    '[object Array]': 'array',
    '[object Date]': 'date',
    '[object RegExp]': 'regExp',
    '[object Undefined]': 'undefined',
    '[object Null]': 'null',
    '[object Object]': 'object',
  }
  return map[toString.call(obj)] || 'unknown'
}
