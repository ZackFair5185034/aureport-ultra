<template>
  <u-button
      type="info"
      :title="$t('italic')"
      class="info-button"
      @click="handleClick"
  >
    <i class="iconfont icon-font-italic" :style="{ color: isActive ? 'black' : '#666' }"></i>
  </u-button>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { undoManager, setDirty } from '@/utils/table'
import { showAlert } from '@/utils/comnon'
import { deepCopy } from '@/components/utils/index'
import { getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'ItalicTool' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null; colIndex: number | null; row2Index: number | null; col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null })
})

const isActive = ref(false)

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

  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) { [startRow, endRow] = [endRow, startRow] }
  if (startCol > endCol) { [startCol, endCol] = [endCol, startCol] }

  const oldItalicStyle = updateCellsItalicStyle(startRow, startCol, endRow, endCol)
  table.render()

  undoManager.add({
    redo: () => {
      updateCellsItalicStyle(startRow, startCol, endRow, endCol)
      table.render()
      setDirty()
    },
    undo: () => {
      restoreItalicStyle(startRow, startCol, endRow, endCol, oldItalicStyle)
      table.render()
      setDirty()
    }
  })

  setDirty()
}

function updateCellsItalicStyle(startRow: number, startCol: number, endRow: number, endCol: number) {
  const oldItalicStyle: Record<string, boolean> = {}

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldItalicStyle[i + ',' + j] = newCellDef.cellStyle.italic
      cellStyle.italic = !cellStyle.italic
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) { isActive.value = cellStyle.italic }
    }
  }

  return oldItalicStyle
}

function restoreItalicStyle(startRow: number, startCol: number, endRow: number, endCol: number, oldItalicStyle: Record<string, boolean>) {
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      cellStyle.italic = oldItalicStyle[i + ',' + j]
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) { isActive.value = cellStyle.italic }
    }
  }
}

function refresh(startRow: number, startCol: number, endRow: number, endCol: number) {
  if (startRow > endRow) { [startRow, endRow] = [endRow, startRow] }
  if (startCol > endCol) { [startCol, endCol] = [endCol, startCol] }

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue
      isActive.value = cellDef.cellStyle.italic || false
      return
    }
  }
}
</script>

<style scoped>
</style>
