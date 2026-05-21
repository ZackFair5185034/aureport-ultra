import type { CellDef, ReportContext, RowHeader } from '@/types'
// @ts-expect-error -- undo-manager 没有类型声明
import UndoManager from 'undo-manager'
import MessageBox from '@/components/messagebox/instance'

import { useReportStore } from '@/stores/report'
import { getCell } from '@/utils/contextActions'
import { getUrlQueryString } from '@/utils/url'
// 报表转换为 XML
import TableManager from '@/views/report/designer/edit-table/manager.js'

let hotInstance: Handsontable | null = null

function getStore() {
  return useReportStore()
}

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
          let text = `${value.datasetName}.${value.aggregate}(`
          const prop = value.property || ''
          text += prop.length > 13 ? `${prop.slice(0, 10)}..)` : `${prop})`
          rowData.push(text)
        }
        else if (valueType === 'expression') {
          const v = value.value || ''
          rowData.push(v.length > 16 ? `${v.slice(0, 13)}...` : v)
        }
        else {
          rowData.push(value.value || '')
        }
      }
      else {
        rowData.push('')
      }
    }

    data.push(rowData)
  }

  hot.loadData(data)
}

export function getCellName(context: ReportContext, rowIndex: number | null, colIndex: number): string {
  if (!context || !context.LETTERS)
    return ''
  return rowIndex == null ? context.LETTERS[colIndex] : context.LETTERS[colIndex] + (rowIndex + 1)
}

export function encodeXml(text: string): string {
  return text.replaceAll(/[<>&"]/g, c => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;' })[c]!)
}

export function encode(text: string): string {
  return text.replaceAll(/[<>&"]/g, c => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;' })[c]!)
}

function serializeSpringFields(fields: any[], parentPath: string): string {
  let xml = ''
  for (const field of fields) {
    const path = parentPath ? `${parentPath}.${field.name}` : field.name
    if (field.children?.length) {
      xml += `<field name="${field.name}"${field.label ? ` label="${encode(field.label)}"` : ''}>`
      xml += serializeSpringFields(field.children, path)
      xml += `</field>`
    }
    else {
      xml += `<field name="${field.name}"${field.label ? ` label="${encode(field.label)}"` : ''}/>`
    }
  }
  return xml
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
    const match = fmt.match(/(y+)/)
    if (match) {
      fmt = fmt.replace(match[1], (`${date.getFullYear()}`).slice(4 - match[1].length))
    }
  }

  for (const k in o) {
    const re = new RegExp(`(${k})`)
    const match = fmt.match(re)
    if (match) {
      fmt = fmt.replace(match[1], match[1].length === 1 ? String(o[k]) : (`00${o[k]}`).slice((`${o[k]}`).length))
    }
  }

  return fmt
}

export function buildPageSizeList(): Record<string, { width: number, height: number }> {
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
  const reg = new RegExp(`(^|&)${name}=([^&]*)(&|$)`)
  const r = window.location.search.slice(1).match(reg)
  return r ? r[2] : null
}

export function objToXml(obj: Record<string, unknown> | null | undefined, indent = 0, defaultTag: string | null = null): string {
  if (!obj || typeof obj !== 'object') {
    return ''
  }

  const specialArrays = ['children', 'fields', 'options']
  const indentStr = '    '.repeat(indent)
  let tagName = (obj.tag as string) || defaultTag || ''
  if (!tagName)
    return ''

  if (tagName === 'u-option' || tagName === 'option') {
    tagName = 'option'
  }
  else if (tagName.startsWith('u-')) {
    tagName = tagName.slice(2)
  }

  let attributes = ''
  for (const key in obj) {
    if (specialArrays.includes(key))
      continue
    const value = obj[key]
    attributes += Array.isArray(value) || typeof value === 'object' ? ` ${key}="${JSON.stringify(value).replaceAll('"', '&quot;')}"` : ` ${key}="${value}"`
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

  return childrenContent ? `${indentStr}<${tagName}${attributes}>\n${childrenContent}${indentStr}</${tagName}>\n` : `${indentStr}<${tagName}${attributes}></${tagName}>\n`
}

// 设置脏数据状态，启用保存按钮
export function setDirty(): void {
  const store = getStore()
  store.setSaveBtnDisable(false)
}

// 重置脏数据状态，禁用保存按钮
export function resetDirty(): void {
  const store = getStore()
  store.setSaveBtnDisable(true)
}

function getSpan(hot: Handsontable, row: number, col: number): { rowspan: number, colspan: number } {
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
        cellXml += condition.property ? `<condition property="${condition.property}" op="${encode(condition.operation)}" id="${condition.id}"` : `<condition op="${encode(condition.operation)}" id="${condition.id}"`
        cellXml += ` type="${condition.type || 'property'}"`
        cellXml += condition.join && size > 1 ? ` join="${condition.join}">` : '>'
        cellXml += `<value><![CDATA[${condition.right}]]></value>`
      }
      else {
        cellXml += `<condition type="${condition.type}" op="${encode(condition.operation)}" id="${condition.id}"`
        cellXml += condition.join && size > 1 ? ` join="${condition.join}">` : '>'
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
    cellXml += cellStyle.fontFamily === '0' ? ' font-family=""' : ` font-family="${cellStyle.fontFamily}"`
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

export function tableToXml(context: ReportContext): string {
  const hot = TableManager.get()
  if (!hot)
    return ''

  const countRows = hot.countRows()
  const countCols = hot.countCols()
  let xml = `<?xml version="1.0" encoding="UTF-8"?><ureport>`
  let rowsXml = ''
  let columnXml = ''
  const rowHeaders = context.rowHeaders

  for (let i = 0; i < countRows; i++) {
    let height = hot.getRowHeight(i) || 16
    height = pixelToPoint(height)
    let band: string | null = null
    for (const header of rowHeaders) {
      if (header.rowNumber === i) {
        band = header.band
        break
      }
    }

    rowsXml += band ? `<row row-number="${i + 1}" height="${height}" band="${band}"/>` : `<row row-number="${i + 1}" height="${height}"/>`
  }

  for (let i = 0; i < countCols; i++) {
    let width = hot.getColWidth(i) || 30
    width = pixelToPoint(width)
    columnXml += `<column col-number="${i + 1}" width="${width}"/>`
  }

  let cellXml = ''
  const spanData: string[] = []
  for (let i = 0; i < countRows; i++) {
    for (let j = 0; j < countCols; j++) {
      if (spanData.includes(`${i},${j}`)) {
        continue
      }

      const cellDef = getCell(i, j)
      if (!cellDef) {
        continue
      }

      const cellName = getCellName(context, i, j)
      cellXml += `<cell expand="${cellDef.expand}" name="${cellName}" row="${(i + 1)}" col="${(j + 1)}"`
      if (cellDef.leftParentCellName && cellDef.leftParentCellName !== '') {
        cellXml += ` left-cell="${cellDef.leftParentCellName}"`
      }

      if (cellDef.topParentCellName && cellDef.topParentCellName !== '') {
        cellXml += ` top-cell="${cellDef.topParentCellName}"`
      }

      if (cellDef.fillBlankRows) {
        cellXml += ` fill-blank-rows="${cellDef.fillBlankRows}"`
        if (cellDef.multiple) {
          cellXml += ` multiple="${cellDef.multiple}"`
        }
      }

      const span = getSpan(hot, i, j)
      const rowSpan = span.rowspan
      const colSpan = span.colspan
      const startRow = i
      const endRow = i + rowSpan - 1
      const startCol = j
      const endCol = j + colSpan - 1
      for (let r = startRow; r <= endRow; r++) {
        for (let c = startCol; c <= endCol; c++) {
          spanData.push(`${r},${c}`)
        }
      }

      if (rowSpan > 1) {
        cellXml += ` row-span="${rowSpan}"`
      }

      if (colSpan > 1) {
        cellXml += ` col-span="${colSpan}"`
      }

      if (cellDef.linkUrl && cellDef.linkUrl !== '') {
        cellXml += ` link-url="${cellDef.linkUrl}"`
      }

      if (cellDef.linkTargetWindow && cellDef.linkTargetWindow !== '') {
        cellXml += ` link-target-window="${cellDef.linkTargetWindow}"`
      }

      if (cellDef.tooltip && cellDef.tooltip !== '') {
        cellXml += ` tooltip="${cellDef.tooltip}"`
      }

      cellXml += '>'
      const cellStyle = cellDef.cellStyle
      cellXml += buildCellStyle(cellStyle)
      if (cellDef.linkParameters && cellDef.linkParameters.length > 0) {
        for (const param of cellDef.linkParameters) {
          cellXml += `<link-parameter name="${param.name}">`
          cellXml += `<value><![CDATA[${param.value}]]></value>`
          cellXml += `</link-parameter>`
        }
      }

      const value = cellDef.value
      switch (value.type) {
        case 'dataset': {
          let msg: string | null = null
          if (!value.datasetName) {
            msg = `${cellName}单元格数据集属性不能为空！`
          }

          if (!msg && !value.property) {
            msg = `${cellName}单元格属性不能为空！`
          }

          if (!msg && !value.aggregate) {
            msg = `${cellName}单元格聚合方式属性不能为空！`
          }

          if (msg) {
            MessageBox.alert(msg)
            throw msg
          }

          const mappingType = value.mappingType || 'simple'
          let dsAttrs = `dataset-name="${encode(value.datasetName || '')}" aggregate="${encode(value.aggregate || '')}" property="${encode(value.property || '')}" order="${encode(value.order || '')}" mapping-type="${mappingType}"`
          if (value.nestProperty)
            dsAttrs += ` nest-property="${encode(value.nestProperty)}"`
          cellXml += `<dataset-value ${dsAttrs}>`
          if (mappingType === 'dataset') {
            cellXml += ` mapping-dataset="${value.mappingDataset}" mapping-key-property="${value.mappingKeyProperty}" mapping-value-property="${value.mappingValueProperty}"`
          }

          cellXml += '>'
          cellXml += buildConditions(value.conditions)
          if (value.aggregate === 'customgroup') {
            const groupItems = value.groupItems!
            for (const groupItem of groupItems) {
              cellXml += `<group-item name="${groupItem.name}">`
              for (const condition of groupItem.conditions) {
                const op = (condition as any).operation || (condition as any).op || ''
                cellXml += `<condition property="${condition.left}" op="${encode(op)}" id="${condition.id}"`
                cellXml += condition.join ? ` join="${condition.join}">` : '>'

                cellXml += `<value><![CDATA[${condition.right}]]></value>`
                cellXml += `</condition>`
              }

              cellXml += '</group-item>'
            }
          }

          if (mappingType === 'simple') {
            const mappingItems = value.mappingItems
            if (mappingItems && mappingItems.length > 0) {
              for (const mappingItem of mappingItems) {
                cellXml += `<mapping-item value="${encode(mappingItem.value)}" label="${encode(mappingItem.label)}"/>`
              }
            }
          }

          cellXml += `</dataset-value>`

          break
        }

        case 'expression': {
          if (!value.value || value.value === '') {
            const msg = `${cellName}单元格表达式不能为空`
            MessageBox.alert(msg)
            throw msg
          }

          cellXml += `<expression-value>`
          cellXml += `<![CDATA[${value.value}]]>`
          cellXml += `</expression-value>`

          break
        }

        case 'simple': {
          cellXml += `<simple-value>`
          cellXml += `<![CDATA[${value.value || ''}]]>`
          cellXml += `</simple-value>`

          break
        }

        case 'image': {
          cellXml += `<image-value source="${value.source}"`
          if (value.width) {
            cellXml += ` width="${value.width}"`
          }

          if (value.height) {
            cellXml += ` height="${value.height}"`
          }

          cellXml += `>`
          cellXml += `<text>`
          cellXml += `<![CDATA[${value.value}]]>`
          cellXml += `</text>`
          cellXml += `</image-value>`

          break
        }

        case 'zxing': {
          cellXml += `<zxing-value source="${value.source}" category="${value.category}" width="${value.width}" height="${value.height}"`
          if (value.format) {
            cellXml += ` format="${value.format}"`
          }

          cellXml += `>`
          cellXml += `<text>`
          cellXml += `<![CDATA[${value.value}]]>`
          cellXml += `</text>`
          cellXml += `</zxing-value>`

          break
        }

        case 'slash': {
          cellXml += `<slash-value>`
          const slashes = value.slashes || []
          for (const slash of slashes) {
            cellXml += `<slash text="${slash.text}" x="${slash.x}" y="${slash.y}" degree="${slash.degree}"/>`
          }

          cellXml += `<base64-data>`
          cellXml += `<![CDATA[${value.base64Data}]]>`
          cellXml += `</base64-data>`
          cellXml += `</slash-value>`

          break
        }

        case 'chart': {
          cellXml += `<chart-value>`
          const chart = value.chart!
          const dataset = chart.dataset!
          cellXml += `<dataset dataset-name="${dataset.datasetName}" type="${dataset.type}"`
          if (dataset.categoryProperty) {
            cellXml += ` category-property="${dataset.categoryProperty}"`
          }

          if (dataset.seriesProperty) {
            cellXml += ` series-property="${dataset.seriesProperty}"`
          }

          if (dataset.seriesType) {
            cellXml += ` series-type="${dataset.seriesType}"`
          }

          if (dataset.seriesText) {
            cellXml += ` series-text="${dataset.seriesText}"`
          }

          if (dataset.valueProperty) {
            cellXml += ` value-property="${dataset.valueProperty}"`
          }

          if ((dataset as any).rProperty) {
            cellXml += ` r-property="${(dataset as any).rProperty}"`
          }

          if ((dataset as any).xProperty) {
            cellXml += ` x-property="${(dataset as any).xProperty}"`
          }

          if ((dataset as any).yProperty) {
            cellXml += ` y-property="${(dataset as any).yProperty}"`
          }

          if ((dataset as any).collectType) {
            cellXml += ` collect-type="${(dataset as any).collectType}"`
          }

          cellXml += `/>`
          const xaxes = chart.xaxes!
          if (xaxes) {
            cellXml += `<xaxes`
            if (xaxes.rotation) {
              cellXml += ` rotation="${xaxes.rotation}"`
            }

            cellXml += `>`
            const scaleLabel = xaxes.scaleLabel
            if (scaleLabel) {
              cellXml += `<scale-label display="${scaleLabel.display}"`
              if (scaleLabel.labelString) {
                cellXml += ` label-string="${scaleLabel.labelString}"`
              }

              cellXml += `/>`
            }

            cellXml += `</xaxes>`
          }

          const yaxes = chart.yaxes!
          if (yaxes) {
            cellXml += `<yaxes`
            if (yaxes.rotation) {
              cellXml += ` rotation="${yaxes.rotation}"`
            }

            cellXml += `>`
            const scaleLabel = yaxes.scaleLabel
            if (scaleLabel) {
              cellXml += `<scale-label display="${scaleLabel.display}"`
              if (scaleLabel.labelString) {
                cellXml += ` label-string="${scaleLabel.labelString}"`
              }

              cellXml += `/>`
            }

            cellXml += `</yaxes>`
          }

          const options = chart.options!
          if (options) {
            for (const option of options) {
              cellXml += `<option type="${option.type}"`
              if (option.position) {
                cellXml += ` position="${option.position}"`
              }

              if (option.display !== undefined && option.display !== null) {
                cellXml += ` display="${option.display}"`
              }

              if (option.duration) {
                cellXml += ` duration="${option.duration}"`
              }

              if (option.easing) {
                cellXml += ` easing="${option.easing}"`
              }

              if (option.text) {
                cellXml += ` text="${option.text}"`
              }

              cellXml += `/>`
            }
          }

          const plugins = chart!.plugins || []
          for (const plugin of plugins) {
            cellXml += `<plugin name="${plugin.name}" display="${plugin.display}"/>`
          }

          cellXml += `</chart-value>`

          break
        }
      // No default
      }

      const propertyConditions = (cellDef as any).conditionPropertyItems || []
      for (const pc of propertyConditions) {
        cellXml += `<condition-property-item name="${pc.name}"`
        const rowHeight = pc.rowHeight
        if (rowHeight !== null && rowHeight !== undefined && rowHeight !== -1) {
          cellXml += ` row-height="${rowHeight}"`
        }

        const colWidth = pc.colWidth
        if (colWidth !== null && colWidth !== undefined && colWidth !== -1) {
          cellXml += ` col-width="${colWidth}"`
        }

        if (pc.newValue && pc.newValue !== '') {
          cellXml += ` new-value="${pc.newValue}"`
        }

        if (pc.linkUrl && pc.linkUrl !== '') {
          cellXml += ` link-url="${pc.linkUrl}"`
          let targetWindow = pc.linkTargetWindow
          if (!targetWindow || targetWindow === '') {
            targetWindow = '_self'
          }

          cellXml += ` link-target-window="${pc.linkTargetWindow}"`
        }

        cellXml += `>`
        const paging = pc.paging
        if (paging) {
          cellXml += `<paging position="${paging.position}" line="${paging.line}"/>`
        }

        if (pc.linkParameters && pc.linkParameters.length > 0) {
          for (const param of pc.linkParameters) {
            cellXml += `<link-parameter name="${param.name}">`
            cellXml += `<value><![CDATA[${param.value}]]></value>`
            cellXml += `</link-parameter>`
          }
        }

        const style = pc.cellStyle
        if (style) {
          cellXml += buildCellStyle(style, true)
        }

        cellXml += buildConditions(pc.conditions)
        cellXml += `</condition-property-item>`
      }

      cellXml += '</cell>'
    }
  }

  xml += cellXml
  xml += rowsXml
  xml += columnXml

  const header = context.reportDef.header
  if (header && (header.left || header.center || header.right)) {
    xml += '<header '
    if (header.fontFamily) {
      xml += ` font-family="${header.fontFamily}"`
    }

    if (header.fontSize) {
      xml += ` font-size="${header.fontSize}"`
    }

    if (header.forecolor) {
      xml += ` forecolor="${header.forecolor}"`
    }

    if (header.bold) {
      xml += ` bold="${header.bold}"`
    }

    if (header.italic) {
      xml += ` italic="${header.italic}"`
    }

    if (header.underline) {
      xml += ` underline="${header.underline}"`
    }

    if (header.margin) {
      xml += ` margin="${header.margin}"`
    }

    xml += '>'
    if (header.left) {
      xml += `<left><![CDATA[${header.left}]]></left>`
    }

    if (header.center) {
      xml += `<center><![CDATA[${header.center}]]></center>`
    }

    if (header.right) {
      xml += `<right><![CDATA[${header.right}]]></right>`
    }

    xml += '</header>'
  }

  const footer = context.reportDef.footer
  if (footer && (footer.left || footer.center || footer.right)) {
    xml += '<footer '
    if (footer.fontFamily) {
      xml += ` font-family="${footer.fontFamily}"`
    }

    if (footer.fontSize) {
      xml += ` font-size="${footer.fontSize}"`
    }

    if (footer.forecolor) {
      xml += ` forecolor="${footer.forecolor}"`
    }

    if (footer.bold) {
      xml += ` bold="${footer.bold}"`
    }

    if (footer.italic) {
      xml += ` italic="${footer.italic}"`
    }

    if (footer.underline) {
      xml += ` underline="${footer.underline}"`
    }

    if (footer.margin) {
      xml += ` margin="${footer.margin}"`
    }

    xml += '>'
    if (footer.left) {
      xml += `<left><![CDATA[${footer.left}]]></left>`
    }

    if (footer.center) {
      xml += `<center><![CDATA[${footer.center}]]></center>`
    }

    if (footer.right) {
      xml += `<right><![CDATA[${footer.right}]]></right>`
    }

    xml += '</footer>'
  }

  let datasourceXml = ''
  const datasources = context.reportDef.datasources
  for (const datasource of datasources) {
    let ds = `<datasource name="${encode(datasource.name)}" type="${datasource.type}"`
    const type = datasource.type
    switch (type) {
      case 'jdbc': {
        ds += ` username="${encode(datasource.username!)}"`
        ds += ` password="${encode(datasource.password!)}"`
        ds += ` url="${encode(datasource.url!)}"`
        ds += ` driver="${datasource.driver!}"`
        ds += '>'
        for (const dataset of datasource.datasets) {
          ds += `<dataset name="${encode(dataset.name)}" type="sql">`
          ds += `<sql><![CDATA[${dataset.sql}]]></sql>`
          for (const field of dataset.fields) {
            ds += `<field name="${field.name}"${field.label ? ` label="${encode(field.label)}"` : ''}/>`
          }

          for (const parameter of dataset.parameters) {
            ds += `<parameter name="${encode(parameter.name)}" type="${parameter.type}" default-value="${encode(parameter.defaultValue)}"/>`
          }

          ds += `</dataset>`
        }

        break
      }

      case 'spring': {
        ds += ` bean="${datasource.beanId}">`
        for (const dataset of datasource.datasets) {
          ds += `<dataset name="${encode(dataset.name)}" type="bean" method="${dataset.method}" clazz="${dataset.clazz}">`
          ds += serializeSpringFields(dataset.fields || [], '')

          ds += `</dataset>`
        }

        break
      }

      case 'buildin': {
        ds += '>'
        for (const dataset of datasource.datasets) {
          ds += `<dataset name="${encode(dataset.name)}" type="sql">`
          ds += `<sql><![CDATA[${dataset.sql}]]></sql>`
          for (const field of dataset.fields) {
            ds += `<field name="${field.name}"${field.label ? ` label="${encode(field.label)}"` : ''}/>`
          }

          for (const parameter of dataset.parameters) {
            ds += `<parameter name="${parameter.name}" type="${parameter.type}" default-value="${parameter.defaultValue}"/>`
          }

          ds += `</dataset>`
        }

        break
      }

      case 'http': {
        ds += ` protocol="${encode(datasource.protocolType || 'standard')}"`
        ds += ` baseUrl="${encode(datasource.baseUrl || '')}"`
        if (datasource.hostType) ds += ` hostType="${encode(datasource.hostType)}"`
        if (datasource.host) ds += ` host="${encode(datasource.host)}"`
        if (datasource.serviceName) ds += ` serviceName="${encode(datasource.serviceName)}"`
        ds += '>'
        for (const dataset of datasource.datasets) {
          ds += `<dataset name="${encode(dataset.name)}" type="http"`
          if (dataset.url) ds += ` url="${encode(dataset.url)}"`
          if (dataset.method) ds += ` method="${encode(dataset.method)}"`
          if (dataset.beanId) ds += ` beanId="${encode(dataset.beanId)}"`
          if (dataset.beanMethod) ds += ` beanMethod="${encode(dataset.beanMethod)}"`
          if (dataset.headers) ds += ` headers="${encode(dataset.headers)}"`
          if (dataset.body) ds += ` body="${encode(dataset.body)}"`
          if (dataset.responsePath) ds += ` responsePath="${encode(dataset.responsePath)}"`
          ds += '>'
          for (const field of dataset.fields) {
            ds += `<field name="${field.name}"${field.label ? ` label="${encode(field.label)}"` : ''}/>`
          }
          if (dataset.requestParameters && dataset.requestParameters.length > 0) {
            for (const param of dataset.requestParameters) {
              ds += `<requestParameter name="${encode(param.name)}" value="${encode(param.value)}"/>`
            }
          }
          ds += `</dataset>`
        }
        break
      }
    // No default
    }

    ds += '</datasource>'
    datasourceXml += ds
  }

  xml += datasourceXml

  const paper = context.reportDef.paper
  let htmlIntervalRefreshValue = 0
  if (paper.htmlIntervalRefreshValue !== null && paper.htmlIntervalRefreshValue !== undefined) {
    htmlIntervalRefreshValue = paper.htmlIntervalRefreshValue
  }

  xml += `<paper type="${paper.paperType}" left-margin="${paper.leftMargin}" right-margin="${paper.rightMargin}"
    top-margin="${paper.topMargin}" bottom-margin="${paper.bottomMargin}" paging-mode="${paper.pagingMode}" fixrows="${paper.fixRows}"
    width="${paper.width}" height="${paper.height}" orientation="${paper.orientation}" html-report-align="${paper.htmlReportAlign}" bg-image="${paper.bgImage || ''}" html-interval-refresh-value="${htmlIntervalRefreshValue}" column-enabled="${paper.columnEnabled}"`
  if (paper.columnEnabled) {
    xml += ` column-count="${paper.columnCount}" column-margin="${paper.columnMargin}"`
  }

  xml += `></paper>`
  if (context.reportDef.searchForm) {
    const searchFormXml = objToXml(context.reportDef.searchForm as Record<string, unknown>)
    xml += searchFormXml
  }

  xml += '</ureport>'
  xml = encodeURIComponent(xml)
  return xml
}
