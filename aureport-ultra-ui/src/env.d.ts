/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

declare module 'async-validator' {
  import { DefineComponent } from 'vue'
  interface RuleItem {
    type?: string
    required?: boolean
    pattern?: RegExp
    min?: number
    max?: number
    len?: number
    enum?: unknown[]
    trigger?: string
    message?: string
    [key: string]: unknown
  }
  interface ValidateError {
    message: string
    field: string
  }
  export default class AsyncValidator {
    constructor(descriptor: Record<string, unknown[]>)
    validate(model: Record<string, unknown>, options?: Record<string, unknown>): Promise<void | { errors: ValidateError[] }>
  }
}

declare module 'handsontable' {
  import Handsontable from 'handsontable'
  export default Handsontable
}

declare module 'undo-manager' {
  interface UndoManager {
    add: (action: { undo: () => void; redo: () => void }, group?: string) => void
    undo: () => void
    redo: () => void
    hasUndo: () => boolean
    hasRedo: () => boolean
    clear: () => void
    setCallback: (fn: () => void) => void
    setLimit: (limit: number) => void
    getCommandsLength: () => number
  }
  const UndoManager: new () => UndoManager
  export default UndoManager
}

declare module 'save-svg-as-png' {
  export function saveSvgAsPng(el: SVGElement, filename: string, opts?: Record<string, unknown>): void
}

declare module '@ffrosch/vue-simple-suggest' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

declare module 'vue-color' {
  import type { DefineComponent } from 'vue'
  export const Chrome: DefineComponent<object, object, unknown>
  export const Compact: DefineComponent<object, object, unknown>
  export const Sketch: DefineComponent<object, object, unknown>
}

interface Window {
  __aureport_public_path__?: string
}
