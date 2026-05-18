<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils/index'
import { useReportStore } from '@/stores/report'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty } from '@/utils/table'
import chartWidgetManager from '@/views/report/designer/edit-table/chart-widget/manager'
import ChartAxis from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-axis/index.vue'
import ChartDataset from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-dataset/index.vue'
import ChartOption from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-option/index.vue'

defineOptions({ name: 'ChartValueEditor' })

const props = withDefaults(defineProps<{
  id?: string
  showAxis?: boolean
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
}>(), {
  id: 'bar',
  showAxis: true,
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0,
})
const emit = defineEmits<{
  (e: 'chart-option-change', value: { type: string, option: any }): void
  (e: 'data-labels-change', value: any): void
}>()
const { t } = useI18n()
const store = useReportStore()
const context = computed(() => store.context)

const container = ref<HTMLDivElement | null>(null)
const activeTab = ref('dataset')

const datasetConfig = ref({
  datasetName: '',
  categoryProperty: '',
  valueProperty: '',
  seriesType: 'text',
  seriesProperty: '',
  seriesText: '',
  collectType: '',
  format: '',
})

const chartConfig = ref<{
  title: { display: string | boolean; position: string; text: string }
  legend: { display: string | boolean; position: string }
  dataLabels: { display: string | boolean }
  animation: { duration: number; easing: string }
  layout: { top: number; bottom: number; left: number; right: number }
}>({
  title: {
    display: false,
    position: 'top',
    text: '',
  },
  legend: {
    display: true,
    position: 'top',
  },
  dataLabels: {
    display: false,
  },
  animation: {
    duration: 1000,
    easing: 'easeOutQuad',
  },
  layout: {
    top: 10,
    bottom: 10,
    left: 10,
    right: 10,
  },
})

const xAxesConfig = ref<{ rotation?: number; scaleLabel?: { display?: string | boolean; labelString?: string } }>({
  rotation: 0,
  scaleLabel: {
    display: false,
    labelString: '',
  },
})

const yAxesConfig = ref<{ rotation?: number; scaleLabel?: { display?: string | boolean; labelString?: string } }>({
  rotation: 0,
  scaleLabel: {
    display: false,
    labelString: '',
  },
})

watch(() => [props.rowIndex, props.colIndex], () => {
  loadChartConfig()
}, { immediate: true })

function loadChartConfig() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef || !cellDef.value || !cellDef.value.chart) {
    return
  }

  const chart = cellDef.value.chart

  if (chart.dataset) {
    datasetConfig.value = { ...datasetConfig.value, ...chart.dataset }
  }

  if (chart.xaxes) {
    xAxesConfig.value = { ...xAxesConfig.value, ...chart.xaxes }
  }

  if (chart.yaxes) {
    yAxesConfig.value = { ...yAxesConfig.value, ...chart.yaxes }
  }

  if (chart.options && Array.isArray(chart.options)) {
    for (const option of chart.options) {
      switch (option.type) {
        case 'title':
          chartConfig.value.title = { ...chartConfig.value.title, ...option }
          break
        case 'legend':
          chartConfig.value.legend = { ...chartConfig.value.legend, ...option }
          break
        case 'animation':
          chartConfig.value.animation = { ...chartConfig.value.animation, ...option }
          break
        case 'layout':
          chartConfig.value.layout = { ...chartConfig.value.layout, ...option.layout }
          break
      }
    }
  }

  if (chart.plugins && Array.isArray(chart.plugins)) {
    for (const plugin of chart.plugins) {
      if (plugin.name === 'data-labels') {
        chartConfig.value.dataLabels.display = plugin.display
      }
    }
  }
}

function handleDatasetChange(value: string) {
  datasetConfig.value.datasetName = value
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (cell && cell.value && cell.value.chart && cell.value.chart.dataset) {
    cell.value.chart.dataset.datasetName = value
    setCell(props.rowIndex, props.colIndex, cell)
  }

  setDirty()
}

function handleCategoryPropertyChange(value: string) {
  datasetConfig.value.categoryProperty = value
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (cell && cell.value && cell.value.chart && cell.value.chart.dataset) {
    cell.value.chart.dataset.categoryProperty = value
    setCell(props.rowIndex, props.colIndex, cell)
  }

  setDirty()
}

function handleValuePropertyChange(value: string) {
  datasetConfig.value.valueProperty = value
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (cell && cell.value && cell.value.chart && cell.value.chart.dataset) {
    cell.value.chart.dataset.valueProperty = value
    setCell(props.rowIndex, props.colIndex, cell)
  }

  setDirty()
}

function handleSeriesTypeChange(value: string) {
  datasetConfig.value.seriesType = value
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (cell && cell.value && cell.value.chart && cell.value.chart.dataset) {
    cell.value.chart.dataset.seriesType = value
    setCell(props.rowIndex, props.colIndex, cell)
  }

  setDirty()
}

function handleSeriesPropertyChange(value: string) {
  datasetConfig.value.seriesProperty = value
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (cell && cell.value && cell.value.chart && cell.value.chart.dataset) {
    cell.value.chart.dataset.seriesProperty = value
    setCell(props.rowIndex, props.colIndex, cell)
  }

  setDirty()
}

function handleSeriesTextChange(value: string) {
  datasetConfig.value.seriesText = value
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (cell && cell.value && cell.value.chart && cell.value.chart.dataset) {
    cell.value.chart.dataset.seriesText = value
    setCell(props.rowIndex, props.colIndex, cell)
  }

  setDirty()
}

function handleAggregateChange(value: string) {
  datasetConfig.value.collectType = value
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (cell && cell.value && cell.value.chart && cell.value.chart.dataset) {
    cell.value.chart.dataset.collectType = value
    setCell(props.rowIndex, props.colIndex, cell)
  }

  setDirty()
}

function handleChartOptionChange({ type, option }: { type: string, option: any }) {
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (!cell || !cell.value || !cell.value.chart) {
    return
  }

  const chart = cell.value.chart
  if (!chart.options) {
    chart.options = []
  }

  const existingOption = chart.options.find((opt: any) => opt.type === type)
  if (existingOption) {
    Object.assign(existingOption, option)
  }
  else {
    chart.options.push({ type, ...option })
  }

  setCell(props.rowIndex, props.colIndex, cell)
  updateChart()
  setDirty()
}

function handleDataLabelsChange(dataLabels: any) {
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (!cell || !cell.value || !cell.value.chart) {
    return
  }

  const chart = cell.value.chart
  if (!chart.plugins) {
    chart.plugins = []
  }

  const dataLabelPlugin = chart.plugins.find((p: any) => p.name === 'data-labels')
  if (dataLabelPlugin) {
    dataLabelPlugin.display = dataLabels.display
  }
  else {
    chart.plugins.push({
      name: 'data-labels',
      display: dataLabels.display,
    })
  }

  setCell(props.rowIndex, props.colIndex, cell)
  updateChart()
  setDirty()
}

function updateChart() {
  const widgetKey = `${props.rowIndex}_${props.colIndex}`
  const chartWidget = chartWidgetManager.get(widgetKey)
  if (chartWidget) {
    chartWidget.refresh(context.value)
  }
}

function handleAxisChange({ type, value }: { type: string, value: any }) {
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (!cell || !cell.value || !cell.value.chart) {
    return
  }

  const chart = cell.value.chart

  switch (type) {
    case 'x-rotation':
      if (!chart.xaxes) {
        chart.xaxes = {}
      }

      chart.xaxes.rotation = value
      break
    case 'x-title-display':
      if (!chart.xaxes) {
        chart.xaxes = {}
      }

      if (!chart.xaxes.scaleLabel) {
        chart.xaxes.scaleLabel = {}
      }

      chart.xaxes.scaleLabel.display = value
      break
    case 'x-title-text':
      if (!chart.xaxes) {
        chart.xaxes = {}
      }

      if (!chart.xaxes.scaleLabel) {
        chart.xaxes.scaleLabel = {}
      }

      chart.xaxes.scaleLabel.labelString = value
      break
    case 'y-rotation':
      if (!chart.yaxes) {
        chart.yaxes = {}
      }

      chart.yaxes.rotation = value
      break
    case 'y-title-display':
      if (!chart.yaxes) {
        chart.yaxes = {}
      }

      if (!chart.yaxes.scaleLabel) {
        chart.yaxes.scaleLabel = {}
      }

      chart.yaxes.scaleLabel.display = value
      break
    case 'y-title-text':
      if (!chart.yaxes) {
        chart.yaxes = {}
      }

      if (!chart.yaxes.scaleLabel) {
        chart.yaxes.scaleLabel = {}
      }

      chart.yaxes.scaleLabel.labelString = value
      break
    case 'format':
      if (!chart.dataset) {
        chart.dataset = {}
      }

      chart.dataset.format = value
      break
  }

  setCell(props.rowIndex, props.colIndex, cell)
  updateChart()
  setDirty()
}
</script>

<template>
  <div ref="container" class="bar-chart-value-editor">
    <u-tabs v-model="activeTab" type="button">
      <u-tab-pane :label="t('chart.datasetBind')" index="dataset">
        <!-- 数据集绑定选项卡 -->
        <ChartDataset
          :datasetConfig="datasetConfig"
          @dataset-change="handleDatasetChange"
          @category-property-change="handleCategoryPropertyChange"
          @value-property-change="handleValuePropertyChange"
          @series-type-change="handleSeriesTypeChange"
          @series-property-change="handleSeriesPropertyChange"
          @series-text-change="handleSeriesTextChange"
          @aggregate-change="handleAggregateChange"
        />
      </u-tab-pane>

      <u-tab-pane :label="t('chart.option')" index="option">
        <!-- 选项选项卡 -->
        <ChartOption
          :chartConfig="chartConfig"
          @chart-option-change="handleChartOptionChange"
          @data-labels-change="handleDataLabelsChange"
        />
      </u-tab-pane>

      <u-tab-pane v-if="showAxis" :label="t('chart.axisConfig')" index="axis">
        <!-- 轴配置选项卡 -->
        <ChartAxis
          v-show="activeTab === 'axis'"
          v-model:xAxesConfig="xAxesConfig"
          v-model:yAxesConfig="yAxesConfig"
          v-model:format="datasetConfig.format"
          @axis-change="handleAxisChange"
        />
      </u-tab-pane>
    </u-tabs>
  </div>
</template>

<style scoped>
</style>
