<script setup lang="ts">
import Handsontable from 'handsontable'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ButtonGroup from '@/components/button-group/index.vue'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'ChartTool' })

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null, colIndex: number | null, row2Index: number | null, col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null }),
})

const { t } = useI18n()

const menuItems = computed(() => [
  { text: t('tools.chart.pie'), icon: 'iconfont icon-pie-chart', action: () => handleChartClick('pie') },
  { text: t('tools.chart.doughnut'), icon: 'iconfont icon-doughnut', action: () => handleChartClick('doughnut') },
  { text: t('tools.chart.line'), icon: 'iconfont icon-line', action: () => handleChartClick('line') },
  { text: t('tools.chart.bar'), icon: 'iconfont icon-bar', action: () => handleChartClick('bar') },
  { text: t('tools.chart.horizontalBar'), icon: 'iconfont icon-horizontal-bar', action: () => handleChartClick('horizontalBar') },
  { text: t('tools.chart.area'), icon: 'iconfont icon-area', action: () => handleChartClick('area') },
  { text: t('tools.chart.radar'), icon: 'iconfont icon-radar', action: () => handleChartClick('radar') },
  { text: t('tools.chart.polar'), icon: 'iconfont icon-polar', action: () => handleChartClick('polarArea') },
  { text: t('tools.chart.scatter'), icon: 'iconfont icon-scatter', action: () => handleChartClick('scatter') },
  { text: t('tools.chart.bubble'), icon: 'iconfont icon-bubble', action: () => handleChartClick('bubble') },
])

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

function handleChartClick(category: string) {
  if (!checkSelection())
    return

  const hot = TableManager.get()
  const selected = hot.getSelected()
  const [startRow, startCol, endRow, endCol] = selected[0]
  const cellDef = getCell(startRow, startCol)
  const oldValue = cellDef.value
  const oldCellData = hot.getDataAtCell(startRow, startCol)

  const newCellDef = deepCopy(cellDef)
  hot.setDataAtCell(startRow, startCol, '')
  newCellDef.value = {
    type: 'chart',
    chart: newChart(category),
  }
  setCell(startRow, startCol, newCellDef)
  hot.render()
  setDirty()
  Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)

  undoManager.add({
    redo: () => {
      const currentCellDef = getCell(startRow, startCol)
      const redoNewCellDef = deepCopy(currentCellDef)
      hot.setDataAtCell(startRow, startCol, '')
      redoNewCellDef.value = {
        type: 'chart',
        chart: newChart(category),
      }
      setCell(startRow, startCol, redoNewCellDef)
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
    undo: () => {
      const undoCellDef = getCell(startRow, startCol)
      const undoNewCellDef = deepCopy(undoCellDef)
      undoNewCellDef.value = oldValue
      setCell(startRow, startCol, undoNewCellDef)
      hot.setDataAtCell(startRow, startCol, oldCellData)
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
  })
}

function newChart(category: string) {
  return {
    dataset: {
      type: category,
    },
  }
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
      return
    }
  }
}
</script>

<template>
  <div class="u-inline">
    <ButtonGroup
      iconClass="iconfont icon-pie-chart"
      :title="$t('tools.chart.chart')"
      :menuItems="menuItems"
    />
  </div>
</template>

<style scoped>
</style>
