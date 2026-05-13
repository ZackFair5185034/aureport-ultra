<template>
  <div ref="sidePanel" class="ud-panel">
      <u-tabs v-model="activeTab" type="card" class="resource-tabs" @tab-change="handleTabChange">
          <u-tab-pane :label="t('panel.property')" index="property" />
          <u-tab-pane :label="t('panel.datasource')" index="datasource" />
      </u-tabs>
      <div class="tab-content" ref="tabContent">
          <PropertyPanel v-show="activeTab === 'property'" ref="propertyPanel" :row-index="rowIndex" :col-index="colIndex" :row2-index="row2Index" :col2-index="col2Index" :refresh-trigger="refreshTrigger" @refresh="handlePropertyPanelRefresh" />
          <DatasourcePanel v-show="activeTab === 'datasource'" />
      </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import DatasourcePanel from './datasource-panel/index.vue'
import PropertyPanel from '@/views/report/designer/resource-panel/property-panel/index.vue'

defineOptions({ name: 'SidePanel' })

const { t } = useI18n()
const store = useReportStore()

const props = withDefaults(defineProps<{
  selectedCells?: {
    rowIndex: number | null
    colIndex: number | null
    row2Index: number | null
    col2Index: number | null
  }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null })
})

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const sidePanel = ref<HTMLDivElement | null>(null)
const propertyPanel = ref(null)
const tabContent = ref<HTMLDivElement | null>(null)

const activeTab = ref('property')
const rowIndex = ref(0)
const colIndex = ref(0)
const row2Index = ref(0)
const col2Index = ref(0)
const refreshTrigger = ref(0)

const context = computed(() => store.context || {})

watch(() => props.selectedCells, (newVal) => {
  if (newVal && newVal.rowIndex !== null && newVal.colIndex !== null) {
    refreshPropertyPanel(newVal.rowIndex, newVal.colIndex, newVal.row2Index, newVal.col2Index)
  }
}, { deep: true })

function refreshPropertyPanel(rIdx: number | null, cIdx: number | null, r2Idx: number | null, c2Idx: number | null) {
  rowIndex.value = rIdx as number
  colIndex.value = cIdx as number
  row2Index.value = r2Idx as number
  col2Index.value = c2Idx as number
  if (!activeTab.value) activeTab.value = 'property'
  refreshTrigger.value++
}

function handleTabChange() {
  if (activeTab.value === 'property') {
    refreshTrigger.value++
  }
}

function handlePropertyPanelRefresh() {
  refreshTrigger.value++
}
</script>

<style scoped>
.ud-panel{
  position: relative;
  width: 400px;
  background: #ffffff;
  box-shadow: -5px 0 5px rgba(0, 0, 0, 0.1);
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.tab-content{
  border-left: 1px #e0e0e0 solid;
  flex-grow: 1;
  overflow-y: auto;
}

.resource-tabs {
  height: 50px;
  border: none !important;
}

.resource-tabs :deep(.nav){
  height: 50px;
  background: #00554a !important;
}

.resource-tabs :deep(.nav li){
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border: none !important;
}

.resource-tabs :deep(.nav li:hover) {
  color: grey !important;
}

.resource-tabs :deep(.nav li:active) {
  color: #00554a !important;
}
</style>
