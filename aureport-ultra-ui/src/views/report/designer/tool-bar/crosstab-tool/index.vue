<template>
  <u-button
      :title="$t('tools.crosstab.title')"
      type="info"
      class="info-button"
      icon="icon-slash-header"
      @click="handleClick"
  >
    <CrosstabDialog :visible="dialogVisible" @saveAfter="handleSaveAfter" @close="dialogVisible = false" />
  </u-button>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { setDirty, undoManager } from '@/utils/table.js'
import CrossTabWidget from '@/views/report/designer/edit-table/cross-tab-widget/class.js'
import CrossTabWidgetManager from '@/views/report/designer/edit-table/cross-tab-widget/manager.js'
import Handsontable from 'handsontable'
import { showAlert } from '@/utils/comnon.js'
import { deepCopy } from '@/components/utils/index.js'
import { getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager.js'

defineOptions({ name: 'CrosstabTool' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null; colIndex: number | null; row2Index: number | null; col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null })
})

const isActive = ref(false)
const dialogVisible = ref(false)

interface SelectedCellInfo {
  rowIndex: number
  colIndex: number
  cellDef: any
  selected: any
}

const selectedCell = ref<SelectedCellInfo | null>(null)
const oldCellData = ref<any>(null)
const oldCellDataValue = ref<any>(null)

watch(() => props.selectedCells, (newVal) => {
  if (newVal && newVal.rowIndex !== null && newVal.colIndex !== null) {
    refresh(newVal.rowIndex, newVal.colIndex, newVal.row2Index, newVal.col2Index)
  }
}, { deep: true })

function checkSelection() {
  const hot = TableManager.get()
  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    showAlert(t('selectTargetCellFirst'))
    return false
  }
  return true
}

function handleClick() {
  if (!checkSelection()) return

  const hot = TableManager.get()
  const selected = hot.getSelected()
  const [rowIndex, colIndex] = selected[0]
  const cellDef = getCell(rowIndex, colIndex)

  selectedCell.value = { rowIndex, colIndex, cellDef, selected }
  oldCellData.value = hot.getDataAtCell(rowIndex, colIndex)
  oldCellDataValue.value = cellDef.value

  dialogVisible.value = true
}

function handleSaveAfter(value: string) {
  if (!selectedCell.value) return
  const { rowIndex, colIndex, cellDef, selected } = selectedCell.value
  const hot = TableManager.get()

  const newCellDef = deepCopy(cellDef)
  newCellDef.value = { type: 'slash' }
  setCell(rowIndex, colIndex, newCellDef)

  const widgetKey = `${rowIndex}_${colIndex}`
  if (CrossTabWidgetManager.has(widgetKey)) {
    CrossTabWidgetManager.remove(widgetKey)
  }
  CrossTabWidgetManager.set(widgetKey, new CrossTabWidget(hot, rowIndex, colIndex, value))

  hot.render()
  setDirty()
  Handsontable.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, selected[2], selected[3])

  undoManager.add({
    redo: () => {
      const redoCellDef = deepCopy(getCell(rowIndex, colIndex))
      redoCellDef.value = { type: 'slash' }
      setCell(rowIndex, colIndex, redoCellDef)
      const widgetKey = `${rowIndex}_${colIndex}`
      if (CrossTabWidgetManager.has(widgetKey)) {
        CrossTabWidgetManager.remove(widgetKey)
      }
      CrossTabWidgetManager.set(widgetKey, new CrossTabWidget(hot, rowIndex, colIndex, value))
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, selected[2], selected[3])
    },
    undo: () => {
      const undoCellDef = deepCopy(getCell(rowIndex, colIndex))
      undoCellDef.value = oldCellDataValue.value
      const widgetKey = `${rowIndex}_${colIndex}`
      if (CrossTabWidgetManager.has(widgetKey)) {
        CrossTabWidgetManager.remove(widgetKey)
      }
      setCell(rowIndex, colIndex, undoCellDef)
      hot.setDataAtCell(rowIndex, colIndex, oldCellData.value)
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, selected[2], selected[3])
    }
  })
}

function refresh(rowIndex: number, colIndex: number, _row2Index: number, _col2Index: number) {
  const cellDef = getCell(rowIndex, colIndex)
  const widgetKey = `${rowIndex}_${colIndex}`
  isActive.value = !!(cellDef && CrossTabWidgetManager.has(widgetKey))
}
</script>

<style scoped>
</style>
