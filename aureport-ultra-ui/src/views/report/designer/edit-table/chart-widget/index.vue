<script setup lang="ts">
import type { ChartTypeRegistry } from 'chart.js'
import { Chart, registerables } from 'chart.js'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { $t } from '@/locales'
import { showAlert } from '@/utils/comnon'
import { getCell } from '@/utils/contextActions'
import TableManager from '../manager.js'

defineOptions({ name: 'ChartWidget' })

const props = defineProps<{
  context: Record<string, unknown>
  rowIndex: number
  colIndex: number
}>()

Chart.register(...registerables, ChartDataLabels)

const chartContainer = ref<HTMLDivElement | null>(null)
const chartCanvas = ref<HTMLCanvasElement | null>(null)

const chart = ref<any>(null)
const width = ref(-2)
const height = ref(-2)

const chartColors = computed(() => ({
  red: 'rgb(255, 99, 132)',
  orange: 'rgb(255, 159, 64)',
  yellow: 'rgb(255, 205, 86)',
  green: 'rgb(75, 192, 192)',
  blue: 'rgb(54, 162, 235)',
  purple: 'rgb(153, 102, 255)',
  grey: 'rgb(201, 203, 207)',
}))

onMounted(() => {
  renderChart()
})

onBeforeUnmount(() => {
  if (chart.value) {
    chart.value.destroy()
  }
})

function colorWithAlpha(rgbString: string, alpha: number) {
  const match = rgbString.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/)
  if (match) {
    const [, r, g, b] = match
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
  }

  return rgbString
}

function getCellData() {
  return getCell(props.rowIndex, props.colIndex)
}

function renderChart() {
  const { rowIndex, colIndex } = props
  const hot = TableManager.get()
  const container = chartContainer.value
  const canvas = chartCanvas.value
  if (!container || !canvas)
    return

  const tdElement = getTDByCell(rowIndex, colIndex)
  const rowSpan = tdElement ? parseInt(String(tdElement.rowSpan)) || 1 : 1
  const colSpan = tdElement ? parseInt(String(tdElement.colSpan)) || 1 : 1

  width.value = -2
  height.value = -2

  const rowStart = rowIndex
  const rowEnd = rowIndex + rowSpan
  for (let i = rowStart; i < rowEnd; i++) {
    height.value += hot.getRowHeight(i)
  }

  const colStart = colIndex
  const colEnd = colIndex + colSpan
  for (let i = colStart; i < colEnd; i++) {
    width.value += hot.getColWidth(i)
  }

  container.style.width = `${width.value}px`
  container.style.height = `${height.value}px`
  canvas.style.width = `${width.value}px`
  canvas.style.height = `${height.value}px`

  const cell = getCellData()!
  const chartConfig = cell.value.chart!
  const type = chartConfig.dataset.type
  let data: any = null
  const options: any = {}
  let chartType: string

  const defaultOptions = chartConfig.options
  if (defaultOptions) {
    for (const option of defaultOptions) {
      options[option.type] = option
    }
  }

  const xaxes = chartConfig.xaxes
  if (xaxes) {
    if (!options.scales)
      options.scales = {}
    if (xaxes.rotation) {
      if (!(xaxes as any).ticks)
        (xaxes as any).ticks = {}
      ;(xaxes as any).ticks.minRotation = xaxes.rotation
    }

    options.scales.x = xaxes
  }

  const yaxes = chartConfig.yaxes
  if (yaxes) {
    if (!options.scales)
      options.scales = {}
    if (yaxes.rotation) {
      if (!(yaxes as any).ticks)
        (yaxes as any).ticks = {}
      ;(yaxes as any).ticks.minRotation = yaxes.rotation
    }

    options.scales.y = yaxes
  }

  const colors = chartColors.value

  switch (type) {
    case 'bar':
      chartType = 'bar'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4', '类型5', '类型6'],
        datasets: [{
          label: '系列1',
          backgroundColor: colorWithAlpha(colors.red, 0.5),
          borderColor: colors.red,
          borderWidth: 1,
          data: [21, 25, 8, 12, 31, 19],
        }, {
          label: '系列2',
          backgroundColor: colorWithAlpha(colors.blue, 0.5),
          borderColor: colors.blue,
          borderWidth: 1,
          data: [11, 13, 18, 9, 23, 29],
        }],
      }
      break
    case 'horizontalBar':
      chartType = 'bar'
      options.indexAxis = 'y'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4', '类型5', '类型6'],
        datasets: [{
          label: '系列1',
          backgroundColor: colorWithAlpha(colors.red, 0.5),
          borderColor: colors.red,
          borderWidth: 1,
          data: [21, 25, 8, 12, 31, 19],
        }, {
          label: '系列2',
          backgroundColor: colorWithAlpha(colors.blue, 0.5),
          borderColor: colors.blue,
          borderWidth: 1,
          data: [11, 13, 18, 9, 23, 29],
        }],
      }
      break
    case 'line':
      chartType = 'line'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4', '类型5', '类型6'],
        datasets: [{
          label: '系列1',
          backgroundColor: colorWithAlpha(colors.red, 0.5),
          borderColor: colors.red,
          borderWidth: 1,
          fill: false,
          data: [21, 25, 8, 12, 31, 19],
        }, {
          label: '系列2',
          backgroundColor: colorWithAlpha(colors.blue, 0.5),
          borderColor: colors.blue,
          borderWidth: 1,
          fill: false,
          data: [11, 13, 18, 9, 23, 29],
        }],
      }
      break
    case 'area':
      chartType = 'line'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4', '类型5', '类型6'],
        datasets: [{
          label: '系列1',
          backgroundColor: colorWithAlpha(colors.red, 0.5),
          borderColor: colors.red,
          borderWidth: 1,
          data: [21, 25, 8, 12, 31, 19],
        }, {
          label: '系列2',
          backgroundColor: colorWithAlpha(colors.blue, 0.5),
          borderColor: colors.blue,
          borderWidth: 1,
          data: [11, 13, 18, 9, 23, 29],
        }],
      }
      options.scales = options.scales || {}
      options.scales.y = { stacked: true }
      break
    case 'pie':
      chartType = 'pie'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4'],
        datasets: [{
          label: '系列1',
          backgroundColor: [colors.red, colors.orange, colors.yellow, colors.green],
          data: [21, 25, 8, 12],
        }],
      }
      break
    case 'doughnut':
      chartType = 'doughnut'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4'],
        datasets: [{
          label: '系列1',
          backgroundColor: [colors.red, colors.orange, colors.yellow, colors.green],
          data: [21, 25, 8, 12],
        }],
      }
      break
    case 'radar':
      chartType = 'radar'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4', '类型5'],
        datasets: [{
          label: '系列1',
          backgroundColor: colorWithAlpha(colors.red, 0.5),
          borderColor: colors.red,
          borderWidth: 1,
          data: [21, 25, 8, 12, 31],
        }, {
          label: '系列2',
          backgroundColor: colorWithAlpha(colors.blue, 0.5),
          borderColor: colors.blue,
          borderWidth: 1,
          data: [11, 13, 18, 9, 23, 9],
        }],
      }
      break
    case 'polarArea':
      chartType = 'polarArea'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4'],
        datasets: [{
          label: '系列1',
          backgroundColor: [colors.red, colors.orange, colors.yellow, colors.green],
          data: [21, 25, 12, 31],
        }],
      }
      break
    case 'scatter':
      chartType = 'scatter'
      data = {
        datasets: [
          {
            label: '系列1',
            borderColor: colors.red,
            backgroundColor: colorWithAlpha(colors.red, 0.2),
            data: [{ x: 10, y: 10 }, { x: 5, y: 15 }, { x: 8, y: 12 }, { x: 18, y: 10 }],
          },
          {
            label: '系列2',
            borderColor: colors.blue,
            backgroundColor: colorWithAlpha(colors.blue, 0.2),
            data: [{ x: 13, y: 6 }, { x: 25, y: 10 }, { x: 18, y: 11 }, { x: 14, y: 16 }],
          },
        ],
      }
      break
    case 'bubble':
      chartType = 'bubble'
      data = {
        datasets: [
          {
            label: '系列1',
            borderColor: colors.red,
            backgroundColor: colorWithAlpha(colors.red, 0.2),
            data: [{ x: 10, y: 10, r: 4 }, { x: 5, y: 15, r: 6 }, { x: 8, y: 12, r: 2 }, { x: 18, y: 10, r: 8 }],
          },
          {
            label: '系列2',
            borderColor: colors.blue,
            backgroundColor: colorWithAlpha(colors.blue, 0.2),
            data: [{ x: 13, y: 6, r: 3 }, { x: 25, y: 10, r: 9 }, { x: 18, y: 11, r: 2 }, { x: 14, y: 16, r: 10 }],
          },
        ],
      }
      break
    case 'mix':
      chartType = 'bar'
      data = {
        labels: ['类型1', '类型2', '类型3', '类型4', '类型5', '类型6'],
        datasets: [{
          type: 'line',
          label: '系列1',
          backgroundColor: colorWithAlpha(colors.red, 0.5),
          borderColor: colors.red,
          data: [21, 25, 8, 12, 31, 19],
        }, {
          type: 'bar',
          label: '系列2',
          backgroundColor: colorWithAlpha(colors.blue, 0.5),
          borderColor: colors.blue,
          borderWidth: 1,
          data: [11, 13, 18, 9, 23, 29],
        }],
      }
      break
    default:
      showAlert($t('tools.chart.unknownChartType') + $t('colon') + type)
      return
  }

  const optionList = chartConfig.options || []
  for (const op of optionList) {
    switch (op.type) {
      case 'title':
        if (op.display) {
          options.plugins = options.plugins || {}
          options.plugins.title = { display: true, position: op.position, text: op.text }
        }

        break
      case 'legend':
        options.plugins = options.plugins || {}
        options.plugins.legend = {
          display: op.display || false,
          position: op.position,
          labels: (op as any).labels || {},
        }
        break
      case 'layout':
        if ((op as any).padding) {
          options.layout = {
            padding: { left: (op as any).padding.left, right: (op as any).padding.right, top: (op as any).padding.top, bottom: (op as any).padding.bottom },
          }
        }

        break
    }
  }

  if (chart.value) {
    chart.value.destroy()
  }

  chart.value = new Chart(canvas, {
    type: chartType as keyof ChartTypeRegistry,
    data,
    options: options || {},
  })
}

function getTDByCell(rowIndex: number, colIndex: number): HTMLTableCellElement | null {
  const hot = TableManager.get()
  if (!hot || !hot.view || !hot.view.wtTable)
    return null
  const wtTable = hot.view.wtTable
  if (wtTable.getCell && wtTable.getCell(rowIndex, colIndex)) {
    return wtTable.getCell(rowIndex, colIndex).parentNode
  }

  const cellElements = document.querySelectorAll(`.htCore td[data-row="${rowIndex}"][data-col="${colIndex}"]`)
  return cellElements.length > 0 ? cellElements[0] as HTMLTableCellElement : null
}

function updateChart() {
  if (chart.value)
    chart.value.update()
}

defineExpose({ updateChart })
</script>

<template>
  <div ref="chartContainer" class="chart-container">
    <canvas ref="chartCanvas" />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.chart-container canvas {
  max-width: 100%;
  max-height: 100%;
}
</style>
