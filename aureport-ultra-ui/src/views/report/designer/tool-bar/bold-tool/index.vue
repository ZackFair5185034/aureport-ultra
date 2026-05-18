<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'BoldTool' })

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null, colIndex: number | null, row2Index: number | null, col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null }),
})

const { t } = useI18n()

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
  if (!checkSelection())
    return

  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) {
    ;[startRow, endRow] = [endRow, startRow]
  }

  if (startCol > endCol) {
    ;[startCol, endCol] = [endCol, startCol]
  }

  const oldBoldStyle = updateCellsBoldStyle(startRow, startCol, endRow, endCol)
  table.render()

  undoManager.add({
    redo: () => {
      updateCellsBoldStyle(startRow, startCol, endRow, endCol)
      table.render()
      setDirty()
    },
    undo: () => {
      restoreBoldStyle(startRow, startCol, endRow, endCol, oldBoldStyle)
      table.render()
      setDirty()
    },
  })

  setDirty()
}

function updateCellsBoldStyle(startRow: number, startCol: number, endRow: number, endCol: number) {
  const oldBoldStyle: Record<string, boolean> = {}

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldBoldStyle[`${i},${j}`] = newCellDef.cellStyle.bold
      cellStyle.bold = !cellStyle.bold
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        isActive.value = cellStyle.bold
      }
    }
  }

  return oldBoldStyle
}

function restoreBoldStyle(startRow: number, startCol: number, endRow: number, endCol: number, oldBoldStyle: Record<string, boolean>) {
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      cellStyle.bold = oldBoldStyle[`${i},${j}`]
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        isActive.value = cellStyle.bold
      }
    }
  }
}

function refresh(startRow: number, startCol: number, endRow: number, endCol: number) {
  if (startRow > endRow) {
    ;[startRow, endRow] = [endRow, startRow]
  }

  if (startCol > endCol) {
    ;[startCol, endCol] = [endCol, startCol]
  }

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      isActive.value = cellDef.cellStyle.bold || false
      return
    }
  }
}
</script>

<template>
  <u-button
    type="info"
    :title="$t('tools.bold.bold')"
    class="info-button"
    @click="handleClick"
  >
    <i class="iconfont iconfont icon-font-bold" :style="{ color: isActive ? 'black' : '#666' }" />
  </u-button>
</template>

<style scoped>
</style>
