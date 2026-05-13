import UndoManager from 'undo-manager'
import type { CellDef, ReportContext, RowHeader } from '@/types'

let hotInstance: Handsontable | null = null

export function setTableInstance(hot: Handsontable): void {
  hotInstance = hot
}

export function getTableInstance(): Handsontable | null {
  return hotInstance
}

export function buildNewCellDef(rowNumber: number, columnNumber: number): CellDef {
  return {
    rowNumber,
    columnNumber,
    expand: 'None',
    cellStyle: {
      fontSize: 9,
      forecolor: '0,0,0',
      fontFamily: '宋体',
      align: 'center',
      valign: 'middle',
    },
    value: { type: 'simple', value: '' },
  }
}

export function resetTableData(hot: Handsontable, context: ReportContext): void {
  const countCols = hot.countCols()
  const countRows = hot.countRows()
  const data: string[][] = []
  for (let i = 0; i < countRows; i++) {
    const rowData: string[] = []
    for (let j = 0; j < countCols; j++) {
      const key = `${i + 1},${j + 1}`
      const cellDef = context.cellsMap.get(key)
      if (cellDef) {
        const valueType = cellDef.value.type
        const value = cellDef.value
        if (valueType === 'dataset') {
          let text = value.datasetName + '.' + value.aggregate + '('
          const prop = value.property || ''
          if (prop.length > 13) {
            text += prop.substring(0, 10) + '..)'
          } else {
            text += prop + ')'
          }
          rowData.push(text)
        } else if (valueType === 'expression') {
          const v = value.value || ''
          rowData.push(v.length > 16 ? v.substring(0, 13) + '...' : v)
        } else {
          rowData.push(value.value || '')
        }
      } else {
        rowData.push('')
      }
    }
    data.push(rowData)
  }
  hot.loadData(data)
}

export function getCellName(context: ReportContext, rowIndex: number | null, colIndex: number): string {
  if (!context || !context.LETTERS) return ''
  if (rowIndex != null) {
    return context.LETTERS[colIndex] + (rowIndex + 1)
  } else {
    return context.LETTERS[colIndex]
  }
}

function getSpan(hot: Handsontable, row: number, col: number): { rowspan: number; colspan: number } {
  const mergeCells = (hot.getSettings() as Record<string, unknown>).mergeCells as Array<{
    row: number
    col: number
    rowspan: number
    colspan: number
  }> || []
  for (const item of mergeCells) {
    if (item.row === row && item.col === col) {
      return { rowspan: item.rowspan, colspan: item.colspan }
    }
  }
  return { rowspan: 0, colspan: 0 }
}

function buildConditions(conditions: import('@/types').Condition[] | undefined): string {
  let cellXml = ''
  if (conditions) {
    const size = conditions.length
    for (const condition of conditions) {
      if (!condition.type || condition.type === 'property') {
        if (condition.property) {
          cellXml += `<condition property="${condition.property}" op="${encodeXml(condition.operation)}" id="${condition.id}"`
        } else {
          cellXml += `<condition op="${encodeXml(condition.operation)}" id="${condition.id}"`
        }
        cellXml += ` type="${condition.type || 'property'}"`
        if (condition.join && size > 1) {
          cellXml += ` join="${condition.join}">`
        } else {
          cellXml += '>'
        }
        cellXml += `<value><![CDATA[${condition.right}]]></value>`
      } else {
        cellXml += `<condition type="${condition.type}" op="${encodeXml(condition.operation)}" id="${condition.id}"`
        if (condition.join && size > 1) {
          cellXml += ` join="${condition.join}">`
        } else {
          cellXml += '>'
        }
        cellXml += `<left><![CDATA[${condition.left}]]></left>`
        cellXml += `<right><![CDATA[${condition.right}]]></right>`
      }
      cellXml += '</condition>'
    }
  }
  return cellXml
}

function buildCellStyle(cellStyle: import('@/types').CellStyle, condition?: boolean): string {
  let cellXml = '<cell-style'
  if (condition) {
    cellXml += ' for-condition="true"'
  }
  if (cellStyle.fontSize) {
    cellXml += ` font-size="${cellStyle.fontSize}"`
  }
  if (cellStyle.fontSizeScope) {
    cellXml += ` font-size-scope="${cellStyle.fontSizeScope}"`
  }
  if (cellStyle.forecolor && cellStyle.forecolor !== '') {
    cellXml += ` forecolor="${cellStyle.forecolor}"`
  }
  if (cellStyle.forecolorScope) {
    cellXml += ` forecolor-scope="${cellStyle.forecolorScope}"`
  }
  if (cellStyle.fontFamily) {
    if (cellStyle.fontFamily === '0') {
      cellXml += ' font-family=""'
    } else {
      cellXml += ` font-family="${cellStyle.fontFamily}"`
    }
  }
  if (cellStyle.fontFamilyScope) {
    cellXml += ` font-family-scope="${cellStyle.fontFamilyScope}"`
  }
  if (cellStyle.bgcolor && cellStyle.bgcolor !== '') {
    cellXml += ` bgcolor="${cellStyle.bgcolor}"`
  }
  if (cellStyle.bgcolorScope) {
    cellXml += ` bgcolor-scope="${cellStyle.bgcolorScope}"`
  }
  if (cellStyle.format && cellStyle.format !== '') {
    cellXml += ` format="${cellStyle.format}"`
  }
  if (cellStyle.bold !== undefined && cellStyle.bold !== null) {
    cellXml += ` bold="${cellStyle.bold}"`
  }
  if (cellStyle.boldScope) {
    cellXml += ` bold-scope="${cellStyle.boldScope}"`
  }
  if (cellStyle.italic !== undefined && cellStyle.italic !== null) {
    cellXml += ` italic="${cellStyle.italic}"`
  }
  if (cellStyle.italicScope) {
    cellXml += ` italic-scope="${cellStyle.italicScope}"`
  }
  if (cellStyle.underline !== undefined && cellStyle.underline !== null) {
    cellXml += ` underline="${cellStyle.underline}"`
  }
  if (cellStyle.underlineScope) {
    cellXml += ` underline-scope="${cellStyle.underlineScope}"`
  }
  if (cellStyle.wrapCompute !== undefined && cellStyle.wrapCompute !== null) {
    cellXml += ` wrap-compute="${cellStyle.wrapCompute}"`
  }
  if (cellStyle.align && cellStyle.align !== '') {
    cellXml += ` align="${cellStyle.align}"`
  }
  if (cellStyle.alignScope) {
    cellXml += ` align-scope="${cellStyle.alignScope}"`
  }
  if (cellStyle.valign && cellStyle.valign !== '') {
    cellXml += ` valign="${cellStyle.valign}"`
  }
  if (cellStyle.valignScope) {
    cellXml += ` valign-scope="${cellStyle.valignScope}"`
  }
  if (cellStyle.lineHeight) {
    cellXml += ` line-height="${cellStyle.lineHeight}"`
  }
  cellXml += '>'
  const addBorder = (pos: string, border?: import('@/types').BorderDef) => {
    if (border && typeof border !== 'string' && border.style !== 'none') {
      cellXml += `<${pos}-border width="${border.width}" style="${border.style}" color="${border.color}"/>`
    }
  }
  addBorder('left', cellStyle.leftBorder)
  addBorder('right', cellStyle.rightBorder)
  addBorder('top', cellStyle.topBorder)
  addBorder('bottom', cellStyle.bottomBorder)
  cellXml += '</cell-style>'
  return cellXml
}

export function encodeXml(text: string): string {
  return text.replace(/[<>&"]/g, (c) => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;' })[c]!)
}

export function mmToPoint(mm: number): number {
  return Math.round(mm * 2.834646)
}

export function pointToMM(point: number): number {
  return Math.round(point * 0.352778)
}

export function pointToPixel(point: number): number {
  return Math.round(point * 1.33)
}

export function pixelToPoint(pixel: number): number {
  return Math.round(pixel * 0.75)
}

export function formatDate(date: Date | number | string, fmt: string): string {
  if (typeof date === 'number') {
    date = new Date(date)
  }
  if (typeof date === 'string') {
    return date
  }
  const o: Record<string, number> = {
    'M+': date.getMonth() + 1,
    'd+': date.getDate(),
    'H+': date.getHours(),
    'm+': date.getMinutes(),
    's+': date.getSeconds(),
  }
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  for (const k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? String(o[k]) : ('00' + o[k]).substr(('' + o[k]).length))
    }
  }
  return fmt
}

export function buildPageSizeList(): Record<string, { width: number; height: number }> {
  return {
    A0: { width: 841, height: 1189 },
    A1: { width: 594, height: 841 },
    A2: { width: 420, height: 594 },
    A3: { width: 297, height: 420 },
    A4: { width: 210, height: 297 },
    A5: { width: 148, height: 210 },
    A6: { width: 105, height: 148 },
    A7: { width: 74, height: 105 },
    A8: { width: 52, height: 74 },
    A9: { width: 37, height: 52 },
    A10: { width: 26, height: 37 },
    B0: { width: 1000, height: 1414 },
    B1: { width: 707, height: 1000 },
    B2: { width: 500, height: 707 },
    B3: { width: 353, height: 500 },
    B4: { width: 250, height: 353 },
    B5: { width: 176, height: 250 },
    B6: { width: 125, height: 176 },
    B7: { width: 88, height: 125 },
    B8: { width: 62, height: 88 },
    B9: { width: 44, height: 62 },
    B10: { width: 31, height: 44 },
  }
}

export const undoManager = new UndoManager()

export function getParameter(name: string): string | null {
  const reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)')
  const r = window.location.search.substr(1).match(reg)
  return r ? r[2] : null
}

export function objToXml(obj: Record<string, unknown> | null | undefined, indent = 0, defaultTag: string | null = null): string {
  if (!obj || typeof obj !== 'object') {
    return ''
  }

  const specialArrays = ['children', 'fields', 'options']
  const indentStr = '    '.repeat(indent)
  let tagName = (obj.tag as string) || defaultTag || ''
  if (!tagName) return ''

  if (tagName === 'u-option' || tagName === 'option') {
    tagName = 'option'
  } else if (tagName.startsWith('u-')) {
    tagName = tagName.substring(2)
  }

  let attributes = ''
  for (const key in obj) {
    if (specialArrays.includes(key)) continue
    const value = obj[key]
    if (Array.isArray(value) || typeof value === 'object') {
      attributes += ` ${key}="${JSON.stringify(value).replace(/"/g, '&quot;')}"`
    } else {
      attributes += ` ${key}="${value}"`
    }
  }

  let childrenContent = ''
  for (const key of specialArrays) {
    if (obj[key] && Array.isArray(obj[key])) {
      const childDefaultTag = key === 'options' ? 'option' : null
      for (const child of obj[key] as Record<string, unknown>[]) {
        childrenContent += objToXml(child, indent + 1, childDefaultTag)
      }
    }
  }

  if (childrenContent) {
    return `${indentStr}<${tagName}${attributes}>\n${childrenContent}${indentStr}</${tagName}>\n`
  } else {
    return `${indentStr}<${tagName}${attributes}></${tagName}>\n`
  }
}
