<template>
  <div class="scatter-chart-value-editor" ref="container">
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
            :showRProperty="false"
            @update-dataset="handleDatasetUpdate"
        />
      </u-tab-pane>

      <u-tab-pane :label="t('chart.option')" index="option">
        <ChartOption
            :chartConfig="chartConfig"
            :showDataLabel="true"
            @chart-option-change="handleChartOptionChange"
            @data-labels-change="handleDataLabelsChange"
        />
      </u-tab-pane>

      <u-tab-pane :label="t('chart.axisConfig')" index="axis">
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

<script setup lang="ts">
// @ts-nocheck
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { setDirty } from '@/utils/table.js'
import { deepCopy } from '@/components/utils/index.js'
import { getCell, setCell } from '@/utils/contextActions'
import chartWidgetManager from '@/views/report/designer/edit-table/chart-widget/manager.js'
import ChartAxis from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-axis/index.vue'
import ChartOption from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-option/index.vue'
import ChartDataConfig from '@/views/report/designer/resource-panel/property-panel/chart-value-editor/chart-dataset-bob/index.vue'

defineOptions({ name: 'ScatterChartValueEditor' })
const { t } = useI18n()
const store = useReportStore()
const context = computed(() => store.context)

const props = withDefaults(defineProps<{
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
}>(), {
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0
})

const container = ref<HTMLDivElement | null>(null)
const datasetTab = ref<any>(null)
const activeTab = ref('dataset')

const datasetValues = ref({
  selectedDataset: '',
  selectedCategoryProperty: '',
  selectedXProperty: '',
  selectedYProperty: ''
})

const xAxesConfig = ref({
  rotation: 0,
  scaleLabel: {
    display: false,
    labelString: ''
  }
})

const yAxesConfig = ref({
  rotation: 0,
  scaleLabel: {
    display: false,
    labelString: ''
  }
})

const xAxisFormat = ref('')

const chartConfig = ref({
  title: {
    display: false,
    position: 'top',
    text: ''
  },
  legend: {
    display: false,
    position: 'top'
  },
  animation: {
    duration: 1000,
    easing: 'easeOutQuart'
  },
  dataLabels: {
    display: false
  }
})

watch(() => [props.rowIndex, props.colIndex], () => {
  loadChartConfig()
}, { immediate: true })

function loadChartConfig() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef || !cellDef.value || !cellDef.value.chart) return

  const chart = cellDef.value.chart

  const dataset = chart.dataset || {}
  datasetValues.value = {
    selectedDataset: dataset.datasetName || '',
    selectedCategoryProperty: dataset.categoryProperty || '',
    selectedXProperty: dataset.xProperty || '',
    selectedYProperty: dataset.yProperty || ''
  }
  xAxisFormat.value = dataset.format || ''

  const xaxes = chart.xaxes || {}
  xAxesConfig.value = {
    rotation: xaxes.rotation || 0,
    scaleLabel: {
      display: xaxes.scaleLabel?.display || false,
      labelString: xaxes.scaleLabel?.labelString || ''
    }
  }

  const yaxes = chart.yaxes || {}
  yAxesConfig.value = {
    rotation: yaxes.rotation || 0,
    scaleLabel: {
      display: yaxes.scaleLabel?.display || false,
      labelString: yaxes.scaleLabel?.labelString || ''
    }
  }

  chartConfig.value = {
    title: { display: false, position: 'top', text: '' },
    legend: { display: false, position: 'top' },
    animation: { duration: 1000, easing: 'easeOutQuart' },
    dataLabels: { display: false }
  }

  const options = chart.options || []
  for (const option of options) {
    switch (option.type) {
      case 'animation':
        chartConfig.value.animation.duration = option.duration || 1000
        chartConfig.value.animation.easing = option.easing || 'easeOutQuart'
        break
      case 'title':
        chartConfig.value.title.display = option.display || false
        chartConfig.value.title.position = option.position || 'top'
        chartConfig.value.title.text = option.text || ''
        break
      case 'legend':
        chartConfig.value.legend.display = option.display || false
        chartConfig.value.legend.position = option.position || 'top'
        break
    }
  }

  const plugins = chart.plugins || []
  for (const plugin of plugins) {
    if (plugin.name === 'data-labels') {
      chartConfig.value.dataLabels.display = plugin.display || false
      break
    }
  }
}

function handleDatasetUpdate(config: any) {
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (!cell.value.chart) {
    cell.value.chart = {}
  }

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
  if (!cell || !cell.value || !cell.value.chart) return

  const chart = cell.value.chart
  if (!chart.options) {
    chart.options = []
  }

  let existingOption = chart.options.find((opt: any) => opt.type === type)
  if (existingOption) {
    Object.assign(existingOption, option)
  } else {
    chart.options.push({ type, ...option })
  }

  setCell(props.rowIndex, props.colIndex, cell)
  updateChart()
  setDirty()
}

function handleDataLabelsChange(dataLabels: any) {
  const cell = deepCopy(getCell(props.rowIndex, props.colIndex))
  if (!cell || !cell.value || !cell.value.chart) return

  const chart = cell.value.chart
  if (!chart.plugins) {
    chart.plugins = []
  }

  let dataLabelPlugin = chart.plugins.find((p: any) => p.name === 'data-labels')
  if (dataLabelPlugin) {
    dataLabelPlugin.display = dataLabels.display
  } else {
    chart.plugins.push({
      name: 'data-labels',
      display: dataLabels.display
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
  if (!cell || !cell.value || !cell.value.chart) return

  const chart = cell.value.chart

  switch (type) {
    case 'x-rotation':
      if (!chart.xaxes) chart.xaxes = {}
      chart.xaxes.rotation = value
      break
    case 'x-title-display':
      if (!chart.xaxes) chart.xaxes = {}
      if (!chart.xaxes.scaleLabel) chart.xaxes.scaleLabel = {}
      chart.xaxes.scaleLabel.display = value
      break
    case 'x-title-text':
      if (!chart.xaxes) chart.xaxes = {}
      if (!chart.xaxes.scaleLabel) chart.xaxes.scaleLabel = {}
      chart.xaxes.scaleLabel.labelString = value
      break
    case 'y-rotation':
      if (!chart.yaxes) chart.yaxes = {}
      chart.yaxes.rotation = value
      break
    case 'y-title-display':
      if (!chart.yaxes) chart.yaxes = {}
      if (!chart.yaxes.scaleLabel) chart.yaxes.scaleLabel = {}
      chart.yaxes.scaleLabel.display = value
      break
    case 'y-title-text':
      if (!chart.yaxes) chart.yaxes = {}
      if (!chart.yaxes.scaleLabel) chart.yaxes.scaleLabel = {}
      chart.yaxes.scaleLabel.labelString = value
      break
    case 'format':
      if (!chart.dataset) chart.dataset = {}
      chart.dataset.format = value
      break
  }

  setCell(props.rowIndex, props.colIndex, cell)
  updateChart()
  setDirty()
}
</script>

<style scoped>
.chart-fieldset {
  padding: 10px;
  border: solid 1px #dddddd;
  border-radius: 8px;
  margin-bottom: 10px;
  margin-top: 10px;
}

.chart-fieldset legend {
  width: auto;
  margin-bottom: 1px;
  border-bottom: none;
  font-size: inherit;
  color: #4b4b4b;
}
</style>
