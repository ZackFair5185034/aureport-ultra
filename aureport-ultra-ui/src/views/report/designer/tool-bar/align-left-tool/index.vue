<template>
  <ButtonGroup
      :iconClass="'info-button iconfont' + currentIcon"
      :title="$t('tools.alignLeft.leftRightAlign')"
      :menuItems="menuItems"
  />
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { undoManager, setDirty } from '@/utils/table.js'
import { showAlert } from '@/utils/comnon.js'
import { deepCopy } from '@/components/utils/index.js'
import ButtonGroup from '@/components/button-group/index.vue'
import { getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager.js'

defineOptions({ name: 'AlignLeftTool' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null; colIndex: number | null; row2Index: number | null; col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null })
})

const currentAlign = ref('left')

const menuItems = computed(() => [
  {
    text: t('tools.alignLeft.leftAlign'),
    icon: 'iconfont icon-left-align',
    action: () => handleAlignLeft()
  },
  {
    text: t('tools.alignLeft.centerAlign'),
    icon: 'iconfont icon-center-align',
    action: () => handleAlignCenter()
  },
  {
    text: t('tools.alignLeft.rightAlign'),
    icon: 'iconfont icon-right-align',
    action: () => handleAlignRight()
  }
])

const currentIcon = computed(() => {
  const iconMap: Record<string, string> = {
    'left': ' icon-left-align',
    'center': ' icon-center-align',
    'right': ' icon-right-align'
  }
  return iconMap[currentAlign.value] || iconMap['left']
})

watch(() => props.selectedCells, (newVal) => {
  if (newVal && newVal.rowIndex !== null && newVal.colIndex !== null) {
    refresh(newVal.rowIndex, newVal.colIndex, newVal.row2Index, newVal.col2Index)
  }
}, { deep: true })

function handleAlignLeft() {
  if (!checkSelection()) return
  const oldAligns = buildCellAlign('left')
  undoManager.add({
    undo: () => { buildCellAlign(null, oldAligns); setDirty() },
    redo: () => { buildCellAlign('left'); setDirty() }
  })
  setDirty()
  currentAlign.value = 'left'
}

function handleAlignCenter() {
  if (!checkSelection()) return
  const oldAligns = buildCellAlign('center')
  undoManager.add({
    undo: () => { buildCellAlign(null, oldAligns); setDirty() },
    redo: () => { buildCellAlign('center'); setDirty() }
  })
  setDirty()
  currentAlign.value = 'center'
}

function handleAlignRight() {
  if (!checkSelection()) return
  const oldAligns = buildCellAlign('right')
  undoManager.add({
    undo: () => { buildCellAlign(null, oldAligns); setDirty() },
    redo: () => { buildCellAlign('right'); setDirty() }
  })
  setDirty()
  currentAlign.value = 'right'
}

function checkSelection() {
  const hot = TableManager.get()
  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    showAlert(t('selectTargetCellFirst'))
    return false
  }
  return true
}

function buildCellAlign(align: string | null, prevAligns?: Record<string, string>) {
  const oldAligns: Record<string, string> = {}
  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) { [startRow, endRow] = [endRow, startRow] }
  if (startCol > endCol) { [startCol, endCol] = [endCol, startCol] }

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      const td = table.getCell(i, j)

      if (!cellDef) continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldAligns[`${i},${j}`] = cellStyle.align || ""

      if (prevAligns) {
        align = prevAligns[`${i},${j}`]
      }

      if (td) {
        td.style.textAlign = align
      }

      cellStyle.align = align
      setCell(i, j, newCellDef)
    }
  }

  return oldAligns
}

function refresh(startRow: number, startCol: number, endRow: number, endCol: number) {
  if (startRow > endRow) { [startRow, endRow] = [endRow, startRow] }
  if (startCol > endCol) { [startCol, endCol] = [endCol, startCol] }

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue

      const cellStyle = cellDef.cellStyle
      const align = cellStyle.align || "left"
      currentAlign.value = align
      return
    }
  }
}
</script>

<style scoped>
</style>
