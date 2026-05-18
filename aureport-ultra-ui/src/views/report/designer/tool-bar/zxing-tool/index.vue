<script setup lang="ts">
import Handsontable from 'handsontable'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import ButtonGroup from '@/components/button-group/index.vue'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'ZxingTool' })

const { t } = useI18n()

const menuItems = computed(() => [
  {
    text: t('tools.zxing.qrcode'),
    icon: 'iconfont icon-qrcode',
    action: () => insertQRCode(),
  },
  {
    text: t('tools.zxing.barcode'),
    icon: 'iconfont icon-barcode',
    action: () => insertBarCode(),
  },
])

function checkSelection() {
  const hot = TableManager.get()
  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    showAlert(t('selectTargetCellFirst'))
    return false
  }

  return true
}

function insertQRCode() {
  if (!checkSelection())
    return

  const hot = TableManager.get()
  const selected = hot.getSelected()
  const [startRow, startCol, endRow, endCol] = selected[0]
  let cellDef = getCell(startRow, startCol)
  let oldValue = deepCopy(cellDef.value)
  let oldCellData = hot.getDataAtCell(startRow, startCol)

  hot.setDataAtCell(startRow, startCol, '')
  let td = hot.getCell(startRow, startCol)
  let width = _buildWidth(startCol, td.colSpan, hot)
  let height = _buildHeight(startRow, td.rowSpan, hot)

  const newCellDef = deepCopy(cellDef)
  newCellDef.value = { width, height, type: 'zxing', category: 'qrcode', source: 'text', data: '' }
  setCell(startRow, startCol, newCellDef)

  hot.render()
  setDirty()
  Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)

  undoManager.add({
    redo: () => {
      cellDef = getCell(startRow, startCol)
      oldValue = deepCopy(cellDef.value)
      oldCellData = hot.getDataAtCell(startRow, startCol)
      hot.setDataAtCell(startRow, startCol, '')
      td = hot.getCell(startRow, startCol)
      width = _buildWidth(startCol, td.colSpan, hot)
      height = _buildHeight(startRow, td.rowSpan, hot)
      const newCellDef = deepCopy(cellDef)
      newCellDef.value = { width, height, type: 'zxing', category: 'qrcode', source: 'text', data: '' }
      setCell(startRow, startCol, newCellDef)
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
    undo: () => {
      cellDef = getCell(startRow, startCol)
      const newCellDef = deepCopy(cellDef)
      newCellDef.value = oldValue
      setCell(startRow, startCol, newCellDef)
      hot.setDataAtCell(startRow, startCol, oldCellData)
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
  })
}

function insertBarCode() {
  if (!checkSelection())
    return

  const hot = TableManager.get()
  const selected = hot.getSelected()
  const [startRow, startCol, endRow, endCol] = selected[0]
  let cellDef = getCell(startRow, startCol)
  let oldValue = deepCopy(cellDef.value)
  let oldCellData = hot.getDataAtCell(startRow, startCol)

  hot.setDataAtCell(startRow, startCol, '')
  let td = hot.getCell(startRow, startCol)
  let width = _buildWidth(startCol, td.colSpan, hot)
  let height = _buildHeight(startRow, td.rowSpan, hot)

  const newCellDef = deepCopy(cellDef)
  newCellDef.value = { width, height, type: 'zxing', category: 'barcode', source: 'text', format: 'CODE_128', data: '' }
  setCell(startRow, startCol, newCellDef)

  hot.render()
  setDirty()
  Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)

  undoManager.add({
    redo: () => {
      cellDef = getCell(startRow, startCol)
      oldValue = deepCopy(cellDef.value)
      oldCellData = hot.getDataAtCell(startRow, startCol)
      hot.setDataAtCell(startRow, startCol, '')
      td = hot.getCell(startRow, startCol)
      width = _buildWidth(startCol, td.colSpan, hot)
      height = _buildHeight(startRow, td.rowSpan, hot)
      const newCellDef = deepCopy(cellDef)
      newCellDef.value = { width, height, type: 'zxing', category: 'barcode', source: 'text', format: 'CODE_128', data: '' }
      setCell(startRow, startCol, newCellDef)
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
    undo: () => {
      cellDef = getCell(startRow, startCol)
      const newCellDef = deepCopy(cellDef)
      newCellDef.value = oldValue
      setCell(startRow, startCol, newCellDef)
      hot.setDataAtCell(startRow, startCol, oldCellData)
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
  })
}

function _buildWidth(colIndex: number, colspan: number, hot: any) {
  let width = hot.getColWidth(colIndex) - 3
  if (!colspan || colspan < 2) {
    return width
  }
  const start = colIndex + 1
  const end = colIndex + colspan
  for (let i = start; i < end; i++) {
    width += hot.getColWidth(i)
  }

  return width
}

function _buildHeight(rowIndex: number, rowspan: number, hot: any) {
  let height = hot.getRowHeight(rowIndex) - 3
  if (!rowspan || rowspan < 2) {
    return height
  }
  const start = rowIndex + 1
  const end = rowIndex + rowspan
  for (let i = start; i < end; i++) {
    height += hot.getRowHeight(i)
  }

  return height
}
</script>

<template>
  <div class="u-inline">
    <ButtonGroup
      iconClass="iconfont icon-qrcode"
      :title="$t('tools.zxing.title')"
      customClass="zxing-tool-dropdown"
      :menuItems="menuItems"
    />
  </div>
</template>

<style scoped>
</style>
