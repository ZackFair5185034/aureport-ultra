<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import ButtonGroup from '@/components/button-group/index.vue'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'AlignTopTool' })

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null, colIndex: number | null, row2Index: number | null, col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null }),
})

const { t } = useI18n()

const currentAlign = ref('middle')

const menuItems = computed(() => [
  {
    text: t('tools.alignTop.topAlign'),
    icon: 'iconfont icon-justify-top',
    action: () => handleAlignTop(),
  },
  {
    text: t('tools.alignTop.middleAlign'),
    icon: 'iconfont icon-vertical-align-middle',
    action: () => handleAlignMiddle(),
  },
  {
    text: t('tools.alignTop.bottomAlign'),
    icon: 'iconfont icon-justify-bottom',
    action: () => handleAlignBottom(),
  },
])

const currentIcon = computed(() => {
  const iconMap: Record<string, string> = {
    top: 'iconfont icon-justify-top',
    middle: 'iconfont icon-justify',
    bottom: 'iconfont icon-justify-bottom',
  }
  return iconMap[currentAlign.value] || iconMap.left
})

watch(() => props.selectedCells, (newVal) => {
  if (newVal && newVal.rowIndex !== null && newVal.colIndex !== null) {
    refresh(newVal.rowIndex, newVal.colIndex, newVal.row2Index ?? 0, newVal.col2Index ?? 0)
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

function handleAlignTop() {
  if (!checkSelection())
    return
  applyAlign('top')
  currentAlign.value = 'top'
}

function handleAlignMiddle() {
  if (!checkSelection())
    return
  applyAlign('middle')
  currentAlign.value = 'middle'
}

function handleAlignBottom() {
  if (!checkSelection())
    return
  applyAlign('bottom')
  currentAlign.value = 'bottom'
}

function applyAlign(align: string) {
  const oldAligns = buildCellAlign(align)
  undoManager.add({
    undo: () => {
      buildCellAlign(null, oldAligns)
      setDirty()
    },
    redo: () => {
      buildCellAlign(align)
      setDirty()
    },
  })
  setDirty()
}

function buildCellAlign(align: string | null, prevAligns?: Record<string, string>) {
  const oldAligns: Record<string, string> = {}
  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) {
    [startRow, endRow] = [endRow, startRow]
  }

  if (startCol > endCol) {
    [startCol, endCol] = [endCol, startCol]
  }

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      const td = table.getCell(i, j)

      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldAligns[`${i},${j}`] = cellStyle.valign || ''

      if (prevAligns) {
        align = prevAligns[`${i},${j}`]
      }

      if (td) {
        td.style.verticalAlign = align
      }

      cellStyle.valign = align ?? undefined
      setCell(i, j, newCellDef)
    }
  }

  return oldAligns
}

function refresh(startRow: number, startCol: number, endRow: number, endCol: number) {
  if (startRow > endRow) {
    [startRow, endRow] = [endRow, startRow]
  }

  if (startCol > endCol) {
    [startCol, endCol] = [endCol, startCol]
  }

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const cellStyle = cellDef.cellStyle
      const valign = cellStyle.valign || 'top'
      currentAlign.value = valign
      return
    }
  }
}
</script>

<template>
  <ButtonGroup
    :iconClass="`info-button iconfont${currentIcon}`"
    :title="$t('tools.alignLeft.upDownAlign')"
    :menuItems="menuItems"
  />
</template>

<style scoped>
</style>
