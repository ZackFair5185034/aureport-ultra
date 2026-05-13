<template>
  <div class="u-inline">
    <ButtonGroup
      :buttonText="currentFontSize.toString()"
      :showText="true"
      :title="$t('tools.fontSize.size')"
      :customClass="'font-size-tool-dropdown'"
      :menuItems="menuItems"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { undoManager, setDirty } from '@/utils/table.js'
import { showAlert } from '@/utils/comnon.js'
import { deepCopy } from '@/components/utils/index.js'
import ButtonGroup from '@/components/button-group/index.vue'
import { getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager.js'

defineOptions({ name: 'FontSizeTool' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null; colIndex: number | null; row2Index: number | null; col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null })
})

const currentFontSize = ref(10)
const fontSizes = ref(Array.from({ length: 100 }, (_, i) => i + 1))

const menuItems = computed(() => {
  return fontSizes.value.map(size => ({
    text: String(size),
    action: () => applyFontSize(size)
  }))
})

watch(() => props.selectedCells, (newVal) => {
  if (newVal && newVal.rowIndex !== null && newVal.colIndex !== null) {
    refresh(newVal.rowIndex!, newVal.colIndex!, newVal.row2Index!, newVal.col2Index!)
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

function applyFontSize(fontSize: number) {
  if (!checkSelection()) return

  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) { [startRow, endRow] = [endRow, startRow] }
  if (startCol > endCol) { [startCol, endCol] = [endCol, startCol] }

  const oldFontSize = updateFontSize(startRow, startCol, endRow, endCol, fontSize)
  table.render()

  undoManager.add({
    redo: () => {
      updateFontSize(startRow, startCol, endRow, endCol, fontSize)
      table.render()
      setDirty()
    },
    undo: () => {
      restoreFontSize(startRow, startCol, endRow, endCol, oldFontSize)
      table.render()
      setDirty()
    }
  })

  setDirty()
}

function updateFontSize(startRow: number, startCol: number, endRow: number, endCol: number, fontSize: number) {
  const oldFontSize: Record<string, number> = {}

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldFontSize[i + ',' + j] = newCellDef.cellStyle.fontSize ?? 0
      cellStyle.fontSize = fontSize
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        currentFontSize.value = cellStyle.fontSize
      }
    }
  }

  return oldFontSize
}

function restoreFontSize(startRow: number, startCol: number, endRow: number, endCol: number, oldFontSize: Record<string, number>) {
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      cellStyle.fontSize = oldFontSize[i + ',' + j]
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        currentFontSize.value = cellStyle.fontSize || 10
      }
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

      const cellStyle = cellDef.cellStyle
      const fontSize = cellStyle.fontSize || 10
      currentFontSize.value = fontSize
      return
    }
  }
}
</script>

<style scoped>
.font-size-tool-dropdown ::v-deep .button-text {
  display: inline-block;
  vertical-align: top;
  width: 28px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-left: 0;
}
</style>
