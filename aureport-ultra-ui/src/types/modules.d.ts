/**
 * Type declarations for untyped JS modules in the project
 */

// ====== Utility modules ======

declare module '@/utils/comnon' {
  export function showAlert(message: string, options?: Record<string, unknown>): Promise<unknown>
  export function showConfirm(message: string, options?: Record<string, unknown>): Promise<unknown>
  export function isMobile(): boolean
}

declare module '@/utils/comnon' {
  export function showAlert(message: string, options?: Record<string, unknown>): Promise<unknown>
  export function showConfirm(message: string, options?: Record<string, unknown>): Promise<unknown>
  export function isMobile(): boolean
}

declare module '@/utils/table' {
  import type UndoManager from 'undo-manager'
  export function resetTableData(hot: unknown): void
  export function buildNewCellDef(rowNumber: number, columnNumber: number): Record<string, unknown>
  export function tableToXml(context: unknown): string
  export function encode(text: string): string
  export function getParameter(name: string): string | null
  export function mmToPoint(mm: number): number
  export function pointToMM(point: number): number
  export function pointToPixel(point: number): number
  export function pixelToPoint(pixel: number): number
  export function formatDate(date: number | Date | string, format: string): string
  export function buildPageSizeList(): Record<string, { width: number; height: number }>
  export const undoManager: UndoManager
  export function setDirty(): void
  export function resetDirty(): void
  export function objToXml(obj: unknown, indent?: number, defaultTag?: string | null): string
}

declare module '@/utils/table' {
  export function resetTableData(hot: unknown): void
  export function buildNewCellDef(rowNumber: number, columnNumber: number): Record<string, unknown>
  export function tableToXml(context: unknown): string
  export function encode(text: string): string
  export function getParameter(name: string): string | null
  export function mmToPoint(mm: number): number
  export function pointToMM(point: number): number
  export function pointToPixel(point: number): number
  export function pixelToPoint(pixel: number): number
  export function formatDate(date: number | Date | string, format: string): string
  export function buildPageSizeList(): Record<string, { width: number; height: number }>
  export const undoManager: UndoManager
  export function setDirty(): void
  export function resetDirty(): void
  export function objToXml(obj: unknown, indent?: number, defaultTag?: string | null): string
}

declare module '@/utils/contextActions' {
  import type { CellDef, ReportContext, RowHeader } from '@/types'
  export function getContext(): ReportContext | null
  export function addCell(cell: CellDef): void
  export function removeCell(cell: CellDef): void
  export function setCell(rowIndex: number, colIndex: number, cell: CellDef): void
  export function deleteCell(rowNumber: number, columnNumber: number): void
  export function getCell(rowIndex: number, colIndex: number): CellDef | null
  export function getCellsMap(): Map<string, CellDef> | null
  export function addRowHeader(row: number, band: string): void
  export function adjustInsertRowHeaders(row: number): void
  export function adjustDelRowHeaders(row: number): void
  export function getCellName(rowIndex: number | null, colIndex: number): string
  export function setContext(ctx: ReportContext): void
  export function updateReportDef(reportDef: unknown): void
  export function updateProperty(property: string, value: unknown): void
  export default {
    getContext: typeof getContext,
    addCell: typeof addCell,
    removeCell: typeof removeCell,
    setCell: typeof setCell,
    deleteCell: typeof deleteCell,
    getCell: typeof getCell,
    getCellsMap: typeof getCellsMap,
    addRowHeader: typeof addRowHeader,
    adjustInsertRowHeaders: typeof adjustInsertRowHeaders,
    adjustDelRowHeaders: typeof adjustDelRowHeaders,
    getCellName: typeof getCellName,
    setContext: typeof setContext,
    updateReportDef: typeof updateReportDef,
    updateProperty: typeof updateProperty,
  }
}

declare module '@/utils/contextActions' {
  import type { CellDef, ReportContext, RowHeader } from '@/types'
  export function getContext(): ReportContext | null
  export function addCell(cell: CellDef): void
  export function removeCell(cell: CellDef): void
  export function setCell(rowIndex: number, colIndex: number, cell: CellDef): void
  export function deleteCell(rowNumber: number, columnNumber: number): void
  export function getCell(rowIndex: number, colIndex: number): CellDef | null
  export function getCellsMap(): Map<string, CellDef> | null
  export function addRowHeader(row: number, band: string): void
  export function adjustInsertRowHeaders(row: number): void
  export function adjustDelRowHeaders(row: number): void
  export function getCellName(rowIndex: number | null, colIndex: number): string
  export function setContext(ctx: ReportContext): void
  export function updateReportDef(reportDef: unknown): void
  export function updateProperty(property: string, value: unknown): void
  export default {
    getContext: typeof getContext,
    addCell: typeof addCell,
    removeCell: typeof removeCell,
    setCell: typeof setCell,
    deleteCell: typeof deleteCell,
    getCell: typeof getCell,
    getCellsMap: typeof getCellsMap,
    addRowHeader: typeof addRowHeader,
    adjustInsertRowHeaders: typeof adjustInsertRowHeaders,
    adjustDelRowHeaders: typeof adjustDelRowHeaders,
    getCellName: typeof getCellName,
    setContext: typeof setContext,
    updateReportDef: typeof updateReportDef,
    updateProperty: typeof updateProperty,
  }
}

// ====== Component modules ======

declare module '@/components/Context' {
  import type { ReportDef } from '@/types'
  class ReportContext {
    reportDef: ReportDef
    cellsMap: Map<string, unknown>
    rowHeaders: { rowNumber: number; band: string }[]
    LETTERS: string[]
    constructor(reportTable: { reportDef: ReportDef; cellsMap: Map<string, unknown> })
  }
  export default ReportContext
}

declare module '@/components/Context.js' {
  import type { ReportDef } from '@/types'
  class ReportContext {
    reportDef: ReportDef
    cellsMap: Map<string, unknown>
    rowHeaders: { rowNumber: number; band: string }[]
    LETTERS: string[]
    constructor(reportTable: { reportDef: ReportDef; cellsMap: Map<string, unknown> })
  }
  export default ReportContext
}

declare module '@/views/report/designer/edit-table/manager' {
  const TableManager: {
    get: () => any
    set: (table: any) => void
    has: () => boolean
    clear: () => void
  }
  export default TableManager
}

declare module '@/views/report/designer/edit-table/manager' {
  const TableManager: {
    get: () => any
    set: (table: any) => void
    has: () => boolean
    clear: () => void
  }
  export default TableManager
}

declare module '@/views/report/designer/edit-table/utils/ContextMenu' {
  const buildMenuConfigure: () => Record<string, unknown>
  export default buildMenuConfigure
}

declare module '@/views/report/designer/edit-table/utils/CellRenderer' {
  export function afterRenderer(
    TD: HTMLTableCellElement,
    row: number,
    col: number,
    prop: string | number,
    value: string,
    cellProperties: Record<string, unknown>
  ): void
}

declare module '@/views/report/designer/edit-table/utils/HeaderUtils' {
  export function renderRowHeader(hot: unknown): void
}

// ====== Navigator ======

declare module '@/lib/navigator' {
  interface Navigator {
    navigate(options: { target: string; params?: Record<string, unknown>; openInNewTab?: boolean }): void
    openPreview(options: Record<string, unknown>, openInNewTab?: boolean): void
  }
  export function createNavigator(component: { $router: unknown; $route: unknown }): Navigator
  export function getLibMode(): boolean
  export function setLibMode(mode: boolean): void
}

declare module '@/lib/navigator.js' {
  interface Navigator {
    navigate(options: { target: string; params?: Record<string, unknown>; openInNewTab?: boolean }): void
    openPreview(options: Record<string, unknown>, openInNewTab?: boolean): void
  }
  export function createNavigator(component: { $router: unknown; $route: unknown }): Navigator
  export function getLibMode(): boolean
  export function setLibMode(mode: boolean): void
}

// ====== Search form utils ======

declare module '@/views/report/designer/search-form/utils' {
  export function doSearch(context: unknown): void
  export function resetSearch(context: unknown): void
  export function deepClone<T>(obj: T): T
  export const beautifierConf: { html: Record<string, unknown> }
}

declare module '@/views/report/designer/search-form/utils/index' {
  export function doSearch(context: unknown): void
  export function resetSearch(context: unknown): void
  export function deepClone<T>(obj: T): T
  export const beautifierConf: { html: Record<string, unknown> }
}

declare module '@/views/report/designer/search-form/utils/html' {
  export function dialogWrapper(str: string): string
  export function vueTemplate(str: string): string
  export function vueScript(str: string): string
  export function cssStyle(cssStr: string): string
  export function makeUpHtml(conf: unknown, type: string): string
}

declare module '@/views/report/designer/search-form/utils/html.js' {
  export function dialogWrapper(str: string): string
  export function vueTemplate(str: string): string
  export function vueScript(str: string): string
  export function cssStyle(cssStr: string): string
  export function makeUpHtml(conf: unknown, type: string): string
}

declare module '@/views/report/designer/search-form/utils/js' {
  export function makeUpJs(conf: unknown, type: string): string
}

declare module '@/views/report/designer/search-form/utils/js.js' {
  export function makeUpJs(conf: unknown, type: string): string
}

declare module '@/views/report/designer/search-form/utils/css' {
  export function makeUpCss(conf: unknown): string
}

declare module '@/views/report/designer/search-form/utils/css.js' {
  export function makeUpCss(conf: unknown): string
}

declare module 'js-beautify' {
  const beautifier: {
    html(code: string, options?: Record<string, unknown>): string
    css(code: string, options?: Record<string, unknown>): string
    js(code: string, options?: Record<string, unknown>): string
  }
  export default beautifier
}

// ====== Cross-tab-widget utilities ======

declare module '@/views/report/designer/edit-table/cross-tab-widget/class' {
  export function buildCrossTabCell(cellDef: unknown, context: unknown, rowIndex: number, colIndex: number, row2Index: number, col2Index: number): Record<string, unknown>
}

declare module '@/views/report/designer/edit-table/cross-tab-widget/manager' {
  const CrossTabManager: {
    get: () => any
    set: (table: any) => void
    has: () => boolean
    clear: () => void
  }
  export default CrossTabManager
}

declare module '@/views/report/designer/search-form/utils/index' {
  export function doSearch(context: unknown): void
  export function resetSearch(context: unknown): void
}

// ====== Preview utils ======

declare module '@/views/report/preview/utils/chart' {
  export function convertChartConfig(chartJson: Record<string, unknown>): Record<string, unknown>
  export function buildChartDatas(chartData: Array<{ id: string; json: string }>): void
  export function buildChart(canvasId: string, chartJson: Record<string, unknown>): Promise<unknown>
}

declare module '@/views/report/preview/utils/chart.js' {
  export function convertChartConfig(chartJson: Record<string, unknown>): Record<string, unknown>
  export function buildChartDatas(chartData: Array<{ id: string; json: string }>): void
  export function buildChart(canvasId: string, chartJson: Record<string, unknown>): Promise<unknown>
}

declare module '@/views/report/designer/search-form/utils/render' {
  import type { Component } from 'vue'
  const RenderComponent: Component
  export default RenderComponent
}

declare module '@/views/report/designer/search-form/utils/render.jsx' {
  import type { Component } from 'vue'
  const RenderComponent: Component
  export default RenderComponent
}

declare module '@/views/report/preview/utils/render' {
  export function buildLocationSearchParameters(searchFormParameters: Record<string, unknown>): string
  export function renderTemplateToComponent(componentStr: string, mountNode: HTMLElement | string): Record<string, any>
  export function simplifyObject(obj: unknown): unknown
}


declare module '@/views/report/preview/utils/render.jsx' {
  export function buildLocationSearchParameters(searchFormParameters: Record<string, unknown>): string
  export function renderTemplateToComponent(componentStr: string, mountNode: HTMLElement | string): Record<string, any>
  export function simplifyObject(obj: unknown): unknown
}

// ====== External libraries ======

declare module 'raphael' {
  interface RaphaelPaper {
    rect(x: number, y: number, w: number, h: number, r?: number): RaphaelElement
    text(x: number, y: number, text: string): RaphaelElement
    path(pathString: string): RaphaelElement
    image(src: string, x: number, y: number, w: number, h: number): RaphaelElement
    setViewBox(x: number, y: number, w: number, h: number, fit?: boolean): void
    setSize(w: number, h: number): void
    remove(): void
    clear(): void
  }
  interface RaphaelElement {
    attr(attrs: Record<string, unknown>): RaphaelElement
    attr(name: string): string | number
    toFront(): void
    remove(): void
    node: SVGElement
  }
  function Raphael(element: HTMLElement, width: number, height: number): RaphaelPaper
  export default Raphael
}

declare module 'codemirror' {
  interface CodeMirrorEditor {
    setValue(value: string): void
    getValue(): string
    on(event: string, handler: (...args: unknown[]) => void): void
    off(event: string, handler: (...args: unknown[]) => void): void
    setSize(width: number | string, height: number | string): void
    refresh(): void
    focus(): void
    getCursor(): { line: number; ch: number }
    setCursor(pos: { line: number; ch: number }): void
    getLine(n: number): string
    lineCount(): number
    execCommand(name: string): void
    getOption(option: string): unknown
    setOption(option: string, value: unknown): void
    eachLine(callback: (line: { text: string }) => void): void
    getWrapperElement(): HTMLElement
    getInputField(): HTMLTextAreaElement
    onKeyDown: (e: KeyboardEvent) => void
  }
  interface CodeMirrorConfig {
    value?: string
    mode?: string | { name: string }
    lineNumbers?: boolean
    matchBrackets?: boolean
    indentUnit?: number
    tabSize?: number
    indentWithTabs?: boolean
    lineWrapping?: boolean
    readOnly?: boolean
    theme?: string
    gutters?: string[]
    lint?: boolean | { onGetLint: (text: string, callback: (errors: unknown[]) => void, options: unknown) => void }
    hintOptions?: Record<string, unknown>
    extraKeys?: Record<string, string | ((cm: CodeMirrorEditor) => void)>
    foldGutter?: boolean
    gutters?: string[]
    viewportMargin?: number
  }
  function fromTextArea(host: HTMLTextAreaElement, options?: CodeMirrorConfig): CodeMirrorEditor
  function fromTextArea(host: HTMLTextAreaElement, options?: Record<string, unknown>): CodeMirrorEditor
  export default { fromTextArea }
}

declare module 'uuid' {
  export function v4(): string
  export function v1(): string
  export function v3(name: string, namespace: string): string
  export function v5(name: string, namespace: string): string
}

// ====== Locales ======
declare module '@/locales/lang/zh.js' {
  const messages: Record<string, Record<string, string>>
  export default messages
}

declare module '@/locales/lang/en.js' {
  const messages: Record<string, Record<string, string>>
  export default messages
}
