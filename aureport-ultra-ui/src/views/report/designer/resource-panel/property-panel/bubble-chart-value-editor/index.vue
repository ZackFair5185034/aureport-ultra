<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils/index'
import { useReportStore } from '@/stores/report'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty } from '@/utils/table'
import chartWidgetManager from '@/views/report/designer/edit-table/chart-widget/manager'
import ChartAxis from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-axis/index.vue'
import ChartDataConfig from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-dataset-bob/index.vue'
import ChartOption from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-option/index.vue'

defineOptions({ name: 'BubbleChartValueEditor' })

const props = withDefaults(defineProps<{
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
}>(), {
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0,
})
const { t } = useI18n()
const store = useReportStore()
const context = computed(() => store.context)

const container = ref<HTMLDivElement | null>(null)
const datasetTab = ref<any>(null)
const activeTab = ref('dataset')

const datasetValues = ref({
  selectedDataset: '',
  selectedCategoryProperty: '',
  selectedXProperty: '',
  selectedYProperty: '',
  selectedRProperty: '',
})

const xAxesConfig = ref({
  rotation: 0,
  scaleLabel: {
    display: false,
    labelString: '',
  },
})

const yAxesConfig = ref({
  rotation: 0,
  scaleLabel: {
    display: false,
    labelString: '',
  },
})

const xAxisFormat = ref('')

const chartConfig = ref({
  title: {
    display: false,
    position: 'top',
    text: '',
  },
  legend: {
    display: false,
    position: 'top',
  },
  dataLabels: {
    display: false,
  },
  animation: {
    duration: 1000,
    easing: 'easeOutQuart',
  },
  layout: {
    top: 10,
    bottom: 10,
    left: 10,
    right: 10,
  },
})

watch(() => [props.rowIndex, props.colIndex], () => {
  loadChartConfig()
}, { immediate: true })

function loadChartConfig() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef || !cellDef.value || !cellDef.value.chart)
    return

  const chart = cellDef.value.chart
  const dataset = chart.dataset || {}
  datasetValues.value = {
    selectedDataset: dataset.datasetName || '',
    selectedCategoryProperty: dataset.categoryProperty || '',
    selectedXProperty: dataset.xProperty || '',
    selectedYProperty: dataset.yProperty || '',
    selectedRProperty: dataset.rProperty || '',
  }
  xAxisFormat.value = dataset.format || ''

  const xaxes = chart.xaxes || {}
  xAxesConfig.value = {
    rotation: xaxes.rotation || 0,
    scaleLabel: {
      display: xaxes.scaleLabel?.display || false,
      labelString: xaxes.scaleLabel?.labelString || '',
    },
  }

  const yaxes = chart.yaxes || {}
  yAxesConfig.value = {
    rotation: yaxes.rotation || 0,
    scaleLabel: {
      display: yaxes.scaleLabel?.display || false,
      labelString: yaxes.scaleLabel?.labelString || '',
    },
  }

  const options = chart.options || []
  for (const option of options) {
    switch (option.type) {
      case 'animation':
        chartConfig.value.animation = { ...chartConfig.value.animation, ...option }
        break
      case 'title':
        chartConfig.value.title = { ...chartConfig.value.title, ...option }
        break
      case 'legend':
        chartConfig.value.legend = { ...chartConfig.value.legend, ...option }
        break
      case 'layout':
        chartConfig.value.layout = { ...chartConfig.value.layout, ...option.layout }
        break
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

function handleDatasetUpdate(config: any) {
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (!cell || !cell.value || !cell.value.chart)
    return

  if (!cell.value.chart.dataset) {
    cell.value.chart.dataset = {}
  }

  Object.assign(cell.value.chart.dataset, config)
  Object.assign(datasetValues.value, config)

  setCell(props.rowIndex, props.colIndex, cell)
  setDirty()
}

function handleChartOptionChange({ type, option }: { type: string, option: any }) {
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (!cell || !cell.value || !cell.value.chart)
    return

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
  if (!cell || !cell.value || !cell.value.chart)
    return

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
  if (!cell || !cell.value || !cell.value.chart)
    return

  const chart = cell.value.chart

  switch (type) {
    case 'x-rotation':
      if (!chart.xaxes)
        chart.xaxes = {}
      chart.xaxes.rotation = value
      break
    case 'x-title-display':
      if (!chart.xaxes)
        chart.xaxes = {}
      if (!chart.xaxes.scaleLabel)
        chart.xaxes.scaleLabel = {}
      chart.xaxes.scaleLabel.display = value
      break
    case 'x-title-text':
      if (!chart.xaxes)
        chart.xaxes = {}
      if (!chart.xaxes.scaleLabel)
        chart.xaxes.scaleLabel = {}
      chart.xaxes.scaleLabel.labelString = value
      break
    case 'y-rotation':
      if (!chart.yaxes)
        chart.yaxes = {}
      chart.yaxes.rotation = value
      break
    case 'y-title-display':
      if (!chart.yaxes)
        chart.yaxes = {}
      if (!chart.yaxes.scaleLabel)
        chart.yaxes.scaleLabel = {}
      chart.yaxes.scaleLabel.display = value
      break
    case 'y-title-text':
      if (!chart.yaxes)
        chart.yaxes = {}
      if (!chart.yaxes.scaleLabel)
        chart.yaxes.scaleLabel = {}
      chart.yaxes.scaleLabel.labelString = value
      break
    case 'format':
      if (!chart.dataset)
        chart.dataset = {}
      chart.dataset.format = value
      break
  }

  setCell(props.rowIndex, props.colIndex, cell)
  updateChart()
  setDirty()
}
</script>

<template>
  <div ref="container" class="bubble-chart-value-editor">
    <!-- 选项卡导航 -->
    <u-tabs v-model="activeTab" type="button">
      <u-tab-pane :label="t('chart.datasetBind')" index="dataset">
        <!-- 数据集绑定选项卡 -->
        <ChartDataConfig
          ref="datasetTab"
          :selectedDataset="datasetValues.selectedDataset"
          :selectedCategoryProperty="datasetValues.selectedCategoryProperty"
          :selectedXProperty="datasetValues.selectedXProperty"
          :selectedYProperty="datasetValues.selectedYProperty"
          :selectedRProperty="datasetValues.selectedRProperty"
          @update-dataset="handleDatasetUpdate"
        />
      </u-tab-pane>
      <u-tab-pane :label="t('chart.option')" index="option">
        <ChartOption
          :chartConfig="chartConfig"
          :showDataLabel="false"
          @chart-option-change="handleChartOptionChange"
          @data-labels-change="handleDataLabelsChange"
        />
      </u-tab-pane>
      <u-tab-pane :label="t('chart.axisConfig')" index="axis">
        <!-- 使用ChartAxis组件 -->
        <ChartAxis
          v-model:xAxesConfig="xAxesConfig"
          v-model:yAxesConfig="yAxesConfig"
          v-model:format="xAxisFormat"
          @axis-change="handleAxisChange"
        />
      </u-tab-pane>
    </u-tabs>
  </div>
</template>

<style scoped>
.tab-content {
  min-height: 300px;
}
</style>
