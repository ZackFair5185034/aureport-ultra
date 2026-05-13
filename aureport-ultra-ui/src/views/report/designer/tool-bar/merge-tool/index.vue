<template>
  <u-button
      type="info"
      :title="$t('mergeSplitCells')"
      class="info-button"
      icon="icon-merge"
      @click="handleClick"
  >
  </u-button>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { undoManager, setDirty, buildNewCellDef } from '@/utils/table.js'
import { showAlert } from '@/utils/comnon.js'
import { addCell, getCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager.js'

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
    redo: function () {
      mergeCells = table.getSettings().mergeCells || []
      oldMergeCells = mergeCells.concat([])
      doMergeCells(startRow, startCol, endRow, endCol, table)
      setDirty()
    },
    undo: function () {
      table.updateSettings({ mergeCells: oldMergeCells })
      setDirty()
    }
  })

  setDirty()
}

function doMergeCells(startRow: number, startCol: number, endRow: number, endCol: number, table: any) {
  let doMerge = true, doSplit = false
  const mergeCells = table.getSettings().mergeCells || []

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      let td = table.getCell(i, j)
      if (!td) continue

      let colSpan = td.colSpan || 1
      let rowSpan = td.rowSpan || 1

      if (colSpan > 1 || rowSpan > 1) {
        let index = 0
        doSplit = true
        doMerge = false

        while (index < mergeCells.length) {
          let mergeItem = mergeCells[index]
          let row = mergeItem.row, col = mergeItem.col
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
      let tmp = startRow
      startRow = endRow
      endRow = tmp
    }
    if (endCol < startCol) {
      let tmp = startCol
      startCol = endCol
      endCol = tmp
    }

    let rowSpan = endRow - startRow, colSpan = endCol - startCol
    if (rowSpan === 0) {
      rowSpan = 1
    } else {
      rowSpan++
    }
    if (colSpan === 0) {
      colSpan = 1
    } else {
      colSpan++
    }

    const newMergeItem = { row: startRow, col: startCol, rowspan: rowSpan, colspan: colSpan }
    mergeCells.push(newMergeItem)
  } else {
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
    } else {
      showAlert(t('selectMultiTargetCellFirst'))
    }
  }

  table.updateSettings({ mergeCells })
}
</script>

<style scoped>
/* 按钮样式继承自父组件 */
</style>
