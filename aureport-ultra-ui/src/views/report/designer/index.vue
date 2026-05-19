<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { createNavigator, getLibMode } from '@/lib/navigator'
import { useReportStore } from '@/stores/report'
import ContentTable from '@/views/report/designer/edit-table/index.vue'
import ResourcePanel from '@/views/report/designer/resource-panel/index.vue'
import TopToolBar from '@/views/report/designer/tool-bar/index.vue'
import PrintLine from './print-line/index.vue'
import 'handsontable/dist/handsontable.min.css'

import 'codemirror/lib/codemirror.css'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/lint/lint.css'
import '@/assets/css/designer/tree.css'
import 'codemirror/mode/javascript/javascript.js'

defineOptions({ name: 'DesignerPage' })

const props = withDefaults(defineProps<{
  reportPath?: string
}>(), {
  reportPath: '',
})

const emit = defineEmits<{
  (e: 'navigate', data: unknown): void
  (e: 'save', data: unknown): void
  (e: 'error', err: unknown): void
}>()

const router = useRouter()
const route = useRoute()
const { locale } = useI18n()

const container = ref<HTMLDivElement | null>(null)
const topToolBar = ref(null)
const contentTable = ref(null)
const sidePanel = ref(null)
const printLine = ref(null)

const contextCreated = ref(false)
const selectedCells = ref<{ rowIndex: number | null, colIndex: number | null, row2Index: number | null, col2Index: number | null }>({
  rowIndex: null,
  colIndex: null,
  row2Index: null,
  col2Index: null,
})
const internalReportPath = ref(props.reportPath)

const navigator = createNavigator({ $router: router, $route: route })
const isLibMode = getLibMode()

watch(() => props.reportPath, (val) => {
  internalReportPath.value = val
})

function handleContextCreated() {
  contextCreated.value = true
}

function handleCellSelected({ rowIndex, colIndex, row2Index, col2Index }: { rowIndex: number | null, colIndex: number | null, row2Index: number | null, col2Index: number | null }) {
  selectedCells.value = { rowIndex, colIndex, row2Index, col2Index }
}

function handleNavigate(data: unknown) {
  emit('navigate', data)
}

function handleSave(data: unknown) {
  emit('save', data)
}

function handleError(err: unknown) {
  emit('error', err)
}

function getReportData() {
  return (contentTable.value as any)?.getReportData?.()
}

function saveReport() {
  return (contentTable.value as any)?.saveReport?.()
}

function navigateTo(target: string, params: Record<string, unknown> | undefined, openInNewTab = true) {
  (navigator as any).navigate({ target, params, openInNewTab })
}

function setReportPath(path: string) {
  internalReportPath.value = path
}

function setLocale(newLocale: string) {
  locale.value = newLocale
}

defineExpose({ getReportData, saveReport })
</script>

<template>
  <div id="container" ref="container">
    <div class="u-designer">
      <div class="left-part">
        <TopToolBar v-if="contextCreated" ref="topToolBar" :selectedCells="selectedCells" />
        <ContentTable
          ref="contentTable"
          :reportPath="internalReportPath"
          @cell-selected="handleCellSelected"
          @context-created="handleContextCreated"
          @navigate="handleNavigate"
          @save="handleSave"
          @error="handleError"
        />
      </div>
      <div class="right-part">
        <ResourcePanel v-if="contextCreated" ref="sidePanel" :selectedCells="selectedCells" />
      </div>
    </div>
    <PrintLine v-if="false" ref="printLine" />
  </div>
</template>

<style scoped>
#container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.u-designer {
  height: 100%;
  display: flex;
  flex-direction: row;
}

.left-part {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.right-part {
  width: 400px;
  overflow: hidden;
}
</style>
