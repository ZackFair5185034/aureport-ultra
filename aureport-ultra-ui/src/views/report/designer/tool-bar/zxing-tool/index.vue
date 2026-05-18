<script setup lang="ts">
import Handsontable from 'handsontable'
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const H: any = Handsontable
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
    action: () => insertBarcode(),
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
  const cellDef = getCell(startRow, startCol)!
  const oldValue = deepCopy(cellDef.value)
  const oldCellData = hot.getDataAtCell(startRow, startCol)

  hot.setDataAtCell(startRow, startCol, '')
  const td = hot.getCell(startRow, startCol)
  const width = _buildWidth(startCol, td.colSpan, hot)
  const height = _buildHeight(startRow, td.rowSpan, hot)

  const newCellDef = deepCopy(cellDef)
  newCellDef.value = { width, height, type: 'zxing', category: 'qrcode', source: 'text', data: '' }
  setCell(startRow, startCol, newCellDef)

  hot.render()
  setDirty()
  H.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)

  undoManager.add({
    redo: () => {
      const cell = getCell(startRow, startCol)!
      const oldVal = deepCopy(cell.value)
      const oldData = hot.getDataAtCell(startRow, startCol)
      hot.setDataAtCell(startRow, startCol, '')
      const ttd = hot.getCell(startRow, startCol)
      const w = _buildWidth(startCol, ttd.colSpan, hot)
      const h = _buildHeight(startRow, ttd.rowSpan, hot)
      const newCell = deepCopy(cell)
      newCell.value = { width: w, height: h, type: 'zxing', category: 'qrcode', source: 'text', data: '' }
      setCell(startRow, startCol, newCell)
      hot.render()
      setDirty()
      H.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
    undo: () => {
      const cell = getCell(startRow, startCol)!
      const newCel = deepCopy(cell)
      newCel.value = oldValue
      setCell(startRow, startCol, newCel)
      hot.setDataAtCell(startRow, startCol, oldCellData)
      hot.render()
      setDirty()
      H.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
  })
}

function insertBarcode() {
  if (!checkSelection())
    return

  const hot = TableManager.get()
  const selected = hot.getSelected()
  const [startRow, startCol, endRow, endCol] = selected[0]
  const cellDef = getCell(startRow, startCol)!
  const oldValue = deepCopy(cellDef.value)
  const oldCellData = hot.getDataAtCell(startRow, startCol)

  hot.setDataAtCell(startRow, startCol, '')
  const td = hot.getCell(startRow, startCol)
  const width = _buildWidth(startCol, td.colSpan, hot)
  const height = _buildHeight(startRow, td.rowSpan, hot)

  const newCellDef = deepCopy(cellDef)
  newCellDef.value = { width, height, type: 'zxing', category: 'barcode', source: 'text', format: 'CODE_128', data: '' }
  setCell(startRow, startCol, newCellDef)

  hot.render()
  setDirty()
  H.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)

  undoManager.add({
    redo: () => {
      const cell = getCell(startRow, startCol)!
      hot.setDataAtCell(startRow, startCol, '')
      const ttd = hot.getCell(startRow, startCol)
      const w = _buildWidth(startCol, ttd.colSpan, hot)
      const h = _buildHeight(startRow, ttd.rowSpan, hot)
      const newCell = deepCopy(cell)
      newCell.value = { width: w, height: h, type: 'zxing', category: 'barcode', source: 'text', format: 'CODE_128', data: '' }
      setCell(startRow, startCol, newCell)
      hot.render()
      setDirty()
      H.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
    undo: () => {
      const cell = getCell(startRow, startCol)!
      const newCel = deepCopy(cell)
      newCel.value = oldValue
      setCell(startRow, startCol, newCel)
      hot.setDataAtCell(startRow, startCol, oldCellData)
      hot.render()
      setDirty()
      H.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
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
