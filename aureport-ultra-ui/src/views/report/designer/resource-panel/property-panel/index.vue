<template>
  <div class="property-panel">
    <!-- 单元格值编辑器组件 -->
    <cell-value-editor
      :show-parent-group="showParentGroup"
      :show-renderer-group="showRendererGroup"
      :show-link-group="showLinkGroup"
      :show-type-group="showTypeGroup"
      :row-index="rowIndex"
      :col-index="colIndex"
      @select-renderer="handleSelectRenderer"
      @cell-type-change="handleCellTypeChange"
    />

    <!-- 表达式值编辑器Vue组件 -->
    <expression-value-editor
      ref="expressionValueEditor"
      v-if="expressionValueEditorVisible"
      :row-index="rowIndex"
      :col-index="colIndex"
      :row2-index="row2Index"
      :col2-index="col2Index"
    />

    <!-- 简单值编辑器Vue组件 -->
    <simple-value-editor
      ref="simpleValueEditor"
      v-if="simpleValueEditorVisible"
      :row-index="rowIndex"
      :col-index="colIndex"
      :row2-index="row2Index"
      :col2-index="col2Index"
    />

    <!-- 数据集值编辑器Vue组件 -->
    <dataset-value-editor
      ref="datasetValueEditor"
      v-if="datasetValueEditorVisible"
      :row-index="rowIndex"
      :col-index="colIndex"
      :row2-index="row2Index"
      :col2-index="col2Index"
    />

    <!-- 图片值编辑器Vue组件 -->
    <image-value-editor
      ref="imageValueEditor"
      v-if="imageValueEditorVisible"
      :row-index="rowIndex"
      :col-index="colIndex"
      :row2-index="row2Index"
      :col2-index="col2Index"
    />

    <!-- 斜线值编辑器Vue组件 -->
    <slash-value-editor
      ref="slashValueEditor"
      v-if="slashValueEditorVisible"
      :row-index="rowIndex"
      :col-index="colIndex"
      :row2-index="row2Index"
      :col2-index="col2Index"
    />

    <!-- 二维码/条形码值编辑器Vue组件 -->
    <zxing-value-editor
      ref="zxingValueEditor"
      v-if="zxingValueEditorVisible"
      :row-index="rowIndex"
      :col-index="colIndex"
      :row2-index="row2Index"
      :col2-index="col2Index"
    />

    <!-- 富文本值编辑器Vue组件 -->
    <rich-text-value-editor
      ref="richTextEditor"
      v-if="richTextValueEditorVisible"
      :row-index="rowIndex"
      :col-index="colIndex"
      :row2-index="row2Index"
      :col2-index="col2Index"
    />

    <!-- 图表编辑器容器 -->
    <div ref="chartEditorContainer">
      <template v-for="(chartType, index) in chartEditorTypes" :key="index">
        <chart-value-editor
          ref="chartEditor"
          v-if="currentChartType === chartType.id"
          :id="chartType.id"
          :show-axis="chartType.showAxis"
          :row-index="rowIndex"
          :col-index="colIndex"
          :row2-index="row2Index"
          :col2-index="col2Index"
        />
      </template>
      <bubble-chart-value-editor
        ref="bubbleChartEditor"
        v-if="bubbleChartValueEditorVisible"
        :row-index="rowIndex"
        :col-index="colIndex"
        :row2-index="row2Index"
        :col2-index="col2Index"
      />
      <scatter-chart-value-editor
        ref="scatterChartEditor"
        v-if="scatterChartValueEditorVisible"
        :row-index="rowIndex"
        :col-index="colIndex"
        :row2-index="row2Index"
        :col2-index="col2Index"
      />
    </div>

  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, watch } from 'vue'
import { useReportStore } from '@/stores/report';
import { setDirty } from '@/utils/table';
import { deepCopy } from '@/components/utils/index';
import { getCell, getCellName, setCell } from "@/utils/contextActions";
import ExpressionValueEditor from './expression-value-editor/index.vue';
import SimpleValueEditor from './simple-value-editor/index.vue';
import DatasetValueEditor from './dataset-value-editor/index.vue';
import ImageValueEditor from './image-value-editor/index.vue';
import SlashValueEditor from './slash-value-editor/index.vue';
import ZxingValueEditor from './zxing-value-editor/index.vue';
import ChartValueEditor from './chart-value-editor/index.vue';
import BubbleChartValueEditor from './bubble-chart-value-editor/index.vue';
import ScatterChartValueEditor from './scatter-chart-value-editor/index.vue';
import CellValueEditor from './cell-value-editor/index.vue';
import CrossTabWidget from '@/views/report/designer/edit-table/cross-tab-widget/class';
import TableManager from '@/views/report/designer/edit-table/manager';
import RichTextEditor from './rich-text-value-editor/index.vue';

defineOptions({ name: 'PropertyPanel' })

const store = useReportStore()

const props = withDefaults(defineProps<{
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
  refreshTrigger?: number
}>(), {
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0,
  refreshTrigger: 0
})

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const showParentGroup = ref(false)
const showRendererGroup = ref(false)
const showLinkGroup = ref(false)
const showTypeGroup = ref(false)
const initialized = ref(false)

const chartEditorTypes = ref([
  { id: 'bar', showAxis: true },
  { id: 'line', showAxis: true },
  { id: 'horbar', showAxis: true },
  { id: 'area', showAxis: true },
  { id: 'radar', showAxis: false },
  { id: 'polar', showAxis: false },
  { id: 'doughnut', showAxis: false },
  { id: 'pie', showAxis: false }
])

const currentChartType = ref('')

const expressionValueEditorVisible = ref(false)
const simpleValueEditorVisible = ref(false)
const datasetValueEditorVisible = ref(false)
const imageValueEditorVisible = ref(false)
const slashValueEditorVisible = ref(false)
const zxingValueEditorVisible = ref(false)
const bubbleChartValueEditorVisible = ref(false)
const scatterChartValueEditorVisible = ref(false)
const richTextValueEditorVisible = ref(false)

const expressionValueEditor = ref<any>(null)
const simpleValueEditor = ref<any>(null)
const datasetValueEditor = ref<any>(null)
const imageValueEditor = ref<any>(null)
const slashValueEditor = ref<any>(null)
const zxingValueEditor = ref<any>(null)
const chartEditor = ref<any>(null)
const bubbleChartEditor = ref<any>(null)
const scatterChartEditor = ref<any>(null)
const chartEditorContainer = ref<HTMLDivElement | null>(null)
const richTextEditor = ref<any>(null)

watch(() => props.refreshTrigger, () => {
  refresh()
})

function hideAllEditors() {
  expressionValueEditorVisible.value = false
  simpleValueEditorVisible.value = false
  datasetValueEditorVisible.value = false
  imageValueEditorVisible.value = false
  slashValueEditorVisible.value = false
  zxingValueEditorVisible.value = false
  bubbleChartValueEditorVisible.value = false
  scatterChartValueEditorVisible.value = false
  richTextValueEditorVisible.value = false
  currentChartType.value = ''
}

function refresh() {
  const cellDef = getCell(props.rowIndex, props.colIndex);
  if (!cellDef) {
    return;
  }

  showParentGroup.value = true
  showTypeGroup.value = true
  showLinkGroup.value = true

  initialized.value = true

  currentChartType.value = ''
  expressionValueEditorVisible.value = false
  simpleValueEditorVisible.value = false
  datasetValueEditorVisible.value = false
  imageValueEditorVisible.value = false
  slashValueEditorVisible.value = false
  zxingValueEditorVisible.value = false
  bubbleChartValueEditorVisible.value = false
  scatterChartValueEditorVisible.value = false
  richTextValueEditorVisible.value = false

  let type = cellDef.value.type || 'simple';
  if (type === 'chart') {
    const chartType = cellDef.value.chart.dataset.type;

    let actualChartType = chartType;
    if (chartType === 'horizontalBar') {
      actualChartType = 'horbar';
    } else if (chartType === 'polarArea') {
      actualChartType = 'polar';
    }

    if (chartType === 'scatter') {
      currentChartType.value = 'scatter';
      scatterChartValueEditorVisible.value = true;
    } else if (chartType === 'bubble') {
      currentChartType.value = 'bubble';
      bubbleChartValueEditorVisible.value = true;
    } else {
      currentChartType.value = actualChartType;
    }
  } else {
    currentChartType.value = '';

    if (type === 'simple') {
      simpleValueEditorVisible.value = true;
    } else if (type === 'expression') {
      expressionValueEditorVisible.value = true;
    } else if (type === 'dataset') {
      datasetValueEditorVisible.value = true;
    } else if (type === 'image') {
      imageValueEditorVisible.value = true;
    } else if (type === 'slash') {
      slashValueEditorVisible.value = true;
    } else if (type === 'zxing') {
      zxingValueEditorVisible.value = true;
    } else if (type === 'richtext') {
      richTextValueEditorVisible.value = true;
    }
  }

  initialized.value = false;
}

function refreshProperty() {
  const cellDef = getCell(props.rowIndex, props.colIndex);
  if (!cellDef) {
    return;
  }
  const oldCellDef = getCell(props.rowIndex, props.colIndex);
  const typeChanged = cellDef.value.type !== cellDef.value.type;
  const crossTabWidgetChanged = !!(cellDef.crossTabWidget) !== !!(oldCellDef && oldCellDef.crossTabWidget);

  if (typeChanged || crossTabWidgetChanged) {
    emit('refresh')
  }
}

function handleSelectRenderer() {
  // TODO: implement
}

function handleCellTypeChange(value: string) {
  const cellDef = getCell(props.rowIndex, props.colIndex);
  if (!cellDef) {
    return;
  }
  const newCellDef = deepCopy(cellDef);

  if (value === 'simple') {
    if (newCellDef.value.type !== 'simple') {
      newCellDef.value = { type: 'simple' };
    }
    newCellDef.expand = 'None';
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
    simpleValueEditorVisible.value = true;
  } else if (value === 'expression') {
    if (newCellDef.value.type !== 'expression') {
      newCellDef.value = { type: 'expression', value: '' };
    }
    newCellDef.expand = 'None';
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
    expressionValueEditorVisible.value = true;
  } else if (value === 'dataset') {
    if (newCellDef.value.type !== 'dataset') {
      newCellDef.value = { type: 'dataset', datasetName: '', property: '', aggregate: '', conditions: [], order: 'none' };
    }
    newCellDef.expand = 'Down';
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
    datasetValueEditorVisible.value = true;
  } else if (value === 'image') {
    if (newCellDef.value.type !== 'image') {
      newCellDef.value = { type: 'image', source: 'text' };
    }
    newCellDef.expand = 'None';
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
    imageValueEditorVisible.value = true;
  } else if (value === 'qrcode') {
    if (newCellDef.value.type !== 'zxing' || newCellDef.value.category !== 'qrcode') {
      const hot = TableManager.get();
      const width = buildWidth(props.colIndex, hot.getCell(props.rowIndex, props.colIndex).colSpan, hot);
      const height = buildHeight(props.rowIndex, hot.getCell(props.rowIndex, props.colIndex).rowSpan, hot);
      newCellDef.value = { width, height, type: 'zxing', source: 'text', category: 'qrcode', data: '' };
      newCellDef.expand = 'None';
    }
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
    zxingValueEditorVisible.value = true;
  } else if (value === 'barcode') {
    if (newCellDef.value.type !== 'zxing' || newCellDef.value.category !== 'barcode') {
      const hot = TableManager.get();
      const width = buildWidth(props.colIndex, hot.getCell(props.rowIndex, props.colIndex).colSpan, hot);
      const height = buildHeight(props.rowIndex, hot.getCell(props.rowIndex, props.colIndex).rowSpan, hot);
      newCellDef.value = { width, height, type: 'zxing', source: 'text', category: 'barcode', data: '', format: 'CODE_128' };
      newCellDef.expand = 'None';
    }
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
    zxingValueEditorVisible.value = true;
  } else if (value === 'slash') {
    newCellDef.crossTabWidget = new CrossTabWidget(store.context, props.rowIndex, props.colIndex);
    newCellDef.expand = 'None';
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
    slashValueEditorVisible.value = true;
  } else if (value === 'chart') {
    const hot = TableManager.get();
    const width = buildWidth(props.colIndex, hot.getCell(props.rowIndex, props.colIndex).colSpan, hot);
    const height = buildHeight(props.rowIndex, hot.getCell(props.rowIndex, props.colIndex).rowSpan, hot);
    newCellDef.value = {
      width,
      height,
      type: 'chart',
      chart: {
        dataset: {
          type: 'pie'
        }
      }
    };
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
  } else if (value === 'richtext') {
    if (newCellDef.value.type !== 'richtext') {
      newCellDef.value = { type: 'richtext', value: '' };
    }
    newCellDef.expand = 'None';
    setCell(props.rowIndex, props.colIndex, newCellDef);
    hideAllEditors();
    richTextValueEditorVisible.value = true;
  }

  const hot = TableManager.get();
  hot.setDataAtCell(props.rowIndex, props.colIndex, '');
  setDirty();
}

function buildWidth(colIndex: number, colspan: number, hot: any): number {
  let width = hot.getColWidth(colIndex) - 3;
  if (!colspan || colspan < 2) {
    return width;
  }
  let start = colIndex + 1, end = colIndex + colspan;
  for (let i = start; i < end; i++) {
    width += hot.getColWidth(i);
  }
  return width;
}

function buildHeight(rowIndex: number, rowspan: number, hot: any): number {
  let height = hot.getRowHeight(rowIndex) - 3;
  if (!rowspan || rowspan < 2) {
    return height;
  }
  let start = rowIndex + 1, end = rowIndex + rowspan;
  for (let i = start; i < end; i++) {
    height += hot.getRowHeight(i);
  }
  return height;
}
</script>

<style scoped>
.property-panel {
  margin: 8px
}
</style>
