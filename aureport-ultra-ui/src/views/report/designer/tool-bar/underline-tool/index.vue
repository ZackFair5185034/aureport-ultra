<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'UnderlineTool' })

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

  const oldUnderlineStyle = updateCellsUnderlineStyle(startRow, startCol, endRow, endCol)
  table.render()

  undoManager.add({
    redo: () => {
      updateCellsUnderlineStyle(startRow, startCol, endRow, endCol)
      table.render()
      setDirty()
    },
    undo: () => {
      restoreUnderlineStyle(startRow, startCol, endRow, endCol, oldUnderlineStyle)
      table.render()
      setDirty()
    },
  })

  setDirty()
}

function updateCellsUnderlineStyle(startRow: number, startCol: number, endRow: number, endCol: number) {
  const oldUnderlineStyle: Record<string, boolean> = {}

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldUnderlineStyle[`${i},${j}`] = newCellDef.cellStyle.underline
      cellStyle.underline = !cellStyle.underline
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        isActive.value = cellStyle.underline
      }
    }
  }

  return oldUnderlineStyle
}

function restoreUnderlineStyle(startRow: number, startCol: number, endRow: number, endCol: number, oldUnderlineStyle: Record<string, boolean>) {
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      cellStyle.underline = oldUnderlineStyle[`${i},${j}`]
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        isActive.value = cellStyle.underline
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
      isActive.value = cellDef.cellStyle.underline || false
      return
    }
  }
}
</script>

<template>
  <u-button
    type="info"
    :title="$t('underline')"
    class="info-button"
    @click="handleClick"
  >
    <i class="iconfont icon-font-underline" :style="{ color: isActive ? 'black' : '#666' }" />
  </u-button>
</template>

<style scoped>
</style>
