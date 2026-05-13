<template>
  <div class="cross-tab-container" ref="container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import Raphael from 'raphael'
import saveSvgAsPng from 'save-svg-as-png'
import { getCell, setCell } from '@/utils/contextActions'
import { deepCopy } from '@/components/utils'
import TableManager from '../manager.js'

defineOptions({ name: 'CrossTabWidget' })

const props = defineProps<{
  context: Record<string, unknown>
  rowIndex: number
  colIndex: number
  value?: string
}>()

const container = ref<HTMLDivElement | null>(null)

const slashData = ref<string[]>([])
const rowSpan = ref(1)
const colSpan = ref(1)
const width = ref(0)
const height = ref(0)
const paper = ref<any>(null)

onMounted(() => {
  if (props.value) {
    slashData.value = props.value.split('|')
  }
  refreshCell()
})

onBeforeUnmount(() => {
  if (paper.value) {
    paper.value.remove()
    paper.value = null
  }
})

function refreshCell() {
  const hot = TableManager.get()
  const td = getCellElement()

  rowSpan.value = parseInt(td.rowSpan) || 1
  colSpan.value = parseInt(td.colSpan) || 1

  width.value = -2
  height.value = -4

  const rowStart = props.rowIndex
  const rowEnd = props.rowIndex + rowSpan.value
  for (let i = rowStart; i < rowEnd; i++) {
    height.value += hot.getRowHeight(i)
  }

  const colStart = props.colIndex
  const colEnd = props.colIndex + colSpan.value
  for (let i = colStart; i < colEnd; i++) {
    width.value += hot.getColWidth(i)
  }

  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef?.value?.slashes) {
    _buildSlashes()
    doDraw()
  } else {
    doDraw()
  }
}

function getCellElement() {
  const hot = TableManager.get()
  return hot.getCell(props.rowIndex, props.colIndex)
}

function _buildSlashes() {
  const hot = TableManager.get()
  const colStart = props.colIndex
  const colEnd = props.colIndex + colSpan.value
  let colWidth = 0
  for (let i = colStart; i < colEnd; i++) {
    colWidth += hot.getColWidth(i)
  }

  let rowHeight = 0
  const rowStart = props.rowIndex
  const rowEnd = props.rowIndex + rowSpan.value
  for (let i = rowStart; i < rowEnd; i++) {
    rowHeight += hot.getRowHeight(i)
  }

  const dataSize = slashData.value.length
  let index = 1
  const slashes: Array<{ degree: number; x: number; y: number; text: string }> = []

  for (let i = 0; i < rowSpan.value; i++) {
    let h = 0
    for (let j = 0; j < i; j++) {
      h += hot.getRowHeight(props.rowIndex + j)
    }
    if (i === 0 || i + 1 < rowSpan.value) {
      h += 8
    } else {
      h -= 3
    }
    let itemName = '项目' + index
    if (dataSize > 0 && index - 1 < dataSize) {
      itemName = slashData.value[index - 1]
    } else if (dataSize > 0 && index - 1 >= dataSize) {
      break
    }
    const degree = _computeDegree(colWidth, h)
    const x = parseInt(String(colWidth - 30))
    slashes.push({ degree, x, y: h, text: itemName })
    index++
  }

  if (dataSize === 0 || index - 1 < dataSize) {
    let itemName = '项目' + index
    if (dataSize > 0 && index - 1 < dataSize) {
      itemName = slashData.value[index - 1]
    }
    const degree = _computeDegree(colWidth, rowHeight)
    let x = colWidth
    if (colSpan.value > 1) {
      x -= hot.getColWidth(props.colIndex + (colSpan.value - 1))
    } else {
      x -= parseInt(String(x / 5))
    }
    let y = rowHeight
    if (rowSpan.value > 1) {
      y -= parseInt(String(hot.getRowHeight(props.rowIndex + (rowSpan.value - 1)) / 2)) + 5
    } else {
      y -= parseInt(String(y / 2))
    }
    slashes.push({ degree, x, y, text: itemName })
    index++
  }

  for (let i = 0; i < colSpan.value; i++) {
    let w = 0
    for (let j = 0; j < i; j++) {
      w += hot.getColWidth(props.colIndex + j)
    }
    let itemName = '项目' + index
    if (dataSize > 0 && index - 1 < dataSize) {
      itemName = slashData.value[index - 1]
    } else if (dataSize > 0 && index - 1 >= dataSize) {
      break
    }
    w += 20
    const degree = _computeDegree(rowHeight, w)
    const y = rowHeight - 20
    slashes.push({ degree, x: w, y, text: itemName })
    index++
  }

  const cellDef = getCell(props.rowIndex, props.colIndex)!
  const cellDefCopy = deepCopy(cellDef)
  cellDefCopy.value = { slashes, type: 'slash' }
  setCell(props.rowIndex, props.colIndex, cellDefCopy)
}

function doDraw() {
  const hot = TableManager.get()
  const cellDef = getCell(props.rowIndex, props.colIndex)!
  const slashValue = cellDef.value
  const cellStyle = cellDef.cellStyle

  if (!cellStyle.forecolor) {
    cellStyle.forecolor = '0,0,0'
  }

  let index = 0
  const el = container.value
  if (!el) return

  const savedWidth = width.value
  const savedHeight = height.value

  while (el.firstChild) {
    el.removeChild(el.firstChild)
  }

  el.style.width = savedWidth + 'px'
  el.style.height = savedHeight + 'px'

  const p = Raphael(el, savedWidth, savedHeight)
  paper.value = p

  let fontStyle = cellStyle.fontSize + 'pt ' + (cellStyle.fontFamily ? cellStyle.fontFamily : '宋体')
  const bold = cellStyle.bold ? 'bold' : 'normal'
  const italic = cellStyle.italic ? 'italic' : 'normal'
  const underline = cellStyle.underline ? 'underline' : 'none'

  const textStyle = {
    fill: rgbToHex(cellStyle.forecolor),
    font: fontStyle,
    'font-weight': bold,
    'font-style': italic,
    'text-decoration': underline
  }

  const slashes = slashValue.slashes || []
  const size = slashes.length

  for (let i = 0; i < (rowSpan.value - 1); i++) {
    if (size > 0 && index >= size) break
    let h = 0
    for (let j = 0; j <= i; j++) {
      h += hot.getRowHeight(props.rowIndex + j)
    }
    if (size === 2) h = savedHeight
    if (index < size) {
      p.path('M0 0L' + savedWidth + ' ' + h).attr({ stroke: rgbToHex(cellStyle.forecolor) })
    }
    const slash = slashes[index]
    const text = p.text(0, 0, slash.text).attr(textStyle)
    text.attr({ transform: 'T' + slash.x + ',' + slash.y + 'R' + slash.degree })
    index++
  }

  if (size === 0 || index < size) {
    let h = savedHeight - (hot.getRowHeight(props.rowIndex + (rowSpan.value - 1))) / 3
    if (index + 1 < size) {
      if (size === 2) h = savedHeight
      p.path('M0 0L' + savedWidth + ' ' + h).attr({ stroke: rgbToHex(cellStyle.forecolor) })
    }
    let slash = slashes[index]
    index++
    let text = p.text(0, 0, slash.text).attr(textStyle)
    text.attr({ transform: 'T' + slash.x + ',' + slash.y + 'R' + slash.degree })

    if (size === 0 || index < size) {
      let w = savedWidth - (hot.getColWidth(props.colIndex + (colSpan.value - 1))) / 3
      if (index + 1 < size) {
        if (size === 2) w = savedWidth
        p.path('M0 0L' + w + ' ' + savedHeight).attr({ stroke: rgbToHex(cellStyle.forecolor) })
      }
      slash = slashes[index]
      index++
      text = p.text(0, 0, slash.text).attr(textStyle)
      text.attr({ transform: 'T' + slash.x + ',' + slash.y + 'R' + slash.degree })
    }
  }

  for (let i = 0; i < (colSpan.value - 1); i++) {
    if (size > 0 && index >= size) break
    let w = 0
    for (let j = 0; j <= i; j++) {
      w += hot.getColWidth(props.colIndex + j)
    }
    if (size === 2) w = savedWidth
    p.path('M0 0L' + w + ' ' + savedHeight).attr({ stroke: rgbToHex(cellStyle.forecolor) })
    const slash = slashes[index]
    index++
    const text = p.text(0, 0, slash.text).attr(textStyle)
    text.attr({ transform: 'T' + slash.x + ',' + slash.y + 'R' + slash.degree })
  }

  if (size === 0 || index < size) {
    const slash = slashes[index]
    const text = p.text(0, 0, slash.text).attr(textStyle)
    text.attr({ transform: 'T' + slash.x + ',' + slash.y + 'R' + slash.degree })
  }

  const svg = el.querySelector('svg')
  if (svg) {
    (saveSvgAsPng as any).svgAsPngUri(svg, { encoderOptions: 1 }, (base64Data: string) => {
      slashValue.base64Data = base64Data
    })
  }
}

function _computeDegree(a: number, b: number) {
  const c = Math.sqrt(a * a + b * b)
  const sin = Math.sin(b / c)
  return parseInt(String((180 / Math.PI) * Math.asin(sin)))
}

function rgbToHex(rgb: string) {
  const rgbArray = rgb.split(',')
  const r = parseInt(rgbArray[0])
  const g = parseInt(rgbArray[1])
  const b = parseInt(rgbArray[2])
  return '#' + componentToHex(r) + componentToHex(g) + componentToHex(b)
}

function componentToHex(c: number) {
  const hex = c.toString(16)
  return hex.length === 1 ? '0' + hex : hex
}

defineExpose({ doDraw })
</script>

<style scoped>
.cross-tab-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}
</style>
