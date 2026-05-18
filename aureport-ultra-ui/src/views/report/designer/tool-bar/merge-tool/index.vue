<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { addCell, getCell } from '@/utils/contextActions'
import { buildNewCellDef, setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'MergeTool' })

const { t } = useI18n()

function handleClick() {
  const table = TableManager.get()
  const selected = table.getSelected()

  if (!selected) {
    showAlert(t('selectTargetCellFirst'))
    return
  }

  let mergeCells = table.getSettings().mergeCells || []
  let oldMergeCells = mergeCells.concat([])
  let [startRow, startCol, endRow, endCol] = selected[0]

  let tmp = endRow
  if (startRow > endRow) {
    endRow = startRow
    startRow = tmp
  }

  tmp = endCol
  if (startCol > endCol) {
    endCol = startCol
    startCol = tmp
  }

  doMergeCells(startRow, startCol, endRow, endCol, table)

  undoManager.add({
    redo() {
      mergeCells = table.getSettings().mergeCells || []
      oldMergeCells = mergeCells.concat([])
      doMergeCells(startRow, startCol, endRow, endCol, table)
      setDirty()
    },
    undo() {
      table.updateSettings({ mergeCells: oldMergeCells })
      setDirty()
    },
  })

  setDirty()
}

function doMergeCells(startRow: number, startCol: number, endRow: number, endCol: number, table: any) {
  let doMerge = true
  let doSplit = false
  const mergeCells = table.getSettings().mergeCells || []

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const td = table.getCell(i, j)
      if (!td)
        continue

      const colSpan = td.colSpan || 1
      const rowSpan = td.rowSpan || 1

      if (colSpan > 1 || rowSpan > 1) {
        let index = 0
        doSplit = true
        doMerge = false

        while (index < mergeCells.length) {
          const mergeItem = mergeCells[index]
          const row = mergeItem.row
          const col = mergeItem.col
          if (row === i && col === j) {
            mergeCells.splice(index, 1)
            break
          }

          index++
        }
      }
    }
  }

  if (doMerge) {
    if (endRow < startRow) {
      const tmp = startRow
      startRow = endRow
      endRow = tmp
    }

    if (endCol < startCol) {
      const tmp = startCol
      startCol = endCol
      endCol = tmp
    }

    let rowSpan = endRow - startRow
    let colSpan = endCol - startCol
    if (rowSpan === 0) {
      rowSpan = 1
    }
    else {
      rowSpan++
    }

    if (colSpan === 0) {
      colSpan = 1
    }
    else {
      colSpan++
    }

    const newMergeItem = { row: startRow, col: startCol, rowspan: rowSpan, colspan: colSpan }
    mergeCells.push(newMergeItem)
  }
  else {
    if (doSplit) {
      for (let i = startRow; i <= endRow; i++) {
        for (let j = startCol; j <= endCol; j++) {
          let cellDef = getCell(i, j)
          if (!cellDef) {
            cellDef = buildNewCellDef(i + 1, j + 1) as any
            addCell(cellDef as any)
          }
        }
      }
    }
    else {
      showAlert(t('selectMultiTargetCellFirst'))
    }
  }

  table.updateSettings({ mergeCells })
}
</script>

<template>
  <u-button
    type="info"
    :title="$t('mergeSplitCells')"
    class="info-button"
    icon="icon-merge"
    @click="handleClick"
  />
</template>

<style scoped>
/* 按钮样式继承自父组件 */
</style>
