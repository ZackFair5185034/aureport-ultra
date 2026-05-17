import { deepCopy } from '@/components/utils'
import { $t } from '@/locales'
import { showAlert } from '@/utils/comnon'
import {
  addCell,
  adjustDelRowHeaders,
  adjustInsertRowHeaders,
  getCell,
  getContext,
  removeCell,
} from '@/utils/contextActions'
/**
 * Created by Jacky.Gao on 2017-02-17.
 */
import { resetTableData, setDirty, undoManager } from '@/utils/table'
import { renderRowHeader } from '../HeaderUtils.js'

function parseCellName(cellName) {
  const match = cellName.match(/^([A-Z]+)(\d+)$/)
  if (!match)
    return null
  let colIndex = 0
  const colStr = match[1]
  for (let i = 0; i < colStr.length; i++) {
    colIndex = colIndex * 26 + (colStr.charCodeAt(i) - 64)
  }

  return { colIndex: colIndex - 1, rowNumber: parseInt(match[2]) }
}

function updateParentRowRefs(cell, thresholdRow, delta, context) {
  if (cell.topParentCellName) {
    const info = parseCellName(cell.topParentCellName)
    if (info && info.rowNumber >= thresholdRow) {
      cell.topParentCellName = context.LETTERS[info.colIndex] + (info.rowNumber + delta)
    }
  }

  if (cell.leftParentCellName) {
    const info = parseCellName(cell.leftParentCellName)
    if (info && info.rowNumber >= thresholdRow) {
      cell.leftParentCellName = context.LETTERS[info.colIndex] + (info.rowNumber + delta)
    }
  }
}

export function doDeleteRow() {
  const selected = this.getSelected()
  const context = getContext()
  if (!selected) {
    showAlert($t('table.rowTip')).then((r) => {})
    return
  }

  let [startRow, startCol, endRow, endCol] = selected[0]
  if (endRow < startRow) {
    const tempStartRow = startRow
    startRow = endRow
    endRow = tempStartRow
  }

  let rowHeights = this.getSettings().rowHeights; let mergeCells = this.getSettings().mergeCells
  let oldMergeCells = []
  let newMergeCells = mergeCells.concat([])
  for (const mergeItem of mergeCells) {
    oldMergeCells.push(Object.assign({}, mergeItem))
    const row = mergeItem.row; const rowspan = mergeItem.rowspan
    const rowEnd = row + rowspan - 1
    const index = newMergeCells.indexOf(mergeItem)
    if (row >= startRow && rowEnd <= endRow) {
      newMergeCells.splice(index, 1)
    }
    else if (row <= startRow && rowEnd >= endRow) {
      const span = endRow - startRow + 1
      let leftSpan = rowspan - span
      if (leftSpan === 0) {
        leftSpan = 1
      }

      if (leftSpan === 1 && mergeItem.colspan === 1) {
        newMergeCells.splice(index, 1)
      }
      else {
        newMergeCells[index] = {
          col: mergeItem.col,
          row,
          rowspan: leftSpan,
          colspan: mergeItem.colspan,
        }
      }
    }
    else if (row > endRow) {
      const totalRows = endRow - startRow + 1
      newMergeCells[index] = {
        col: mergeItem.col,
        row: row - totalRows,
        rowspan: mergeItem.rowspan,
        colspan: mergeItem.colspan,
      }
    }
  }

  this.updateSettings({ mergeCells: [] })
  const dif = endRow - startRow + 1
  let oldRowHeights = rowHeights.concat([])
  let newRowHeights = rowHeights.concat([])
  newRowHeights.splice(startRow, dif)
  let countCols = this.countCols(); const removeCells = []
  for (let i = endRow; i >= startRow; i--) {
    for (let j = 0; j < countCols; j++) {
      const cell = getCell(i, j)
      if (cell) {
        removeCell(cell)
        removeCells.push(cell)
      }
    }

    this.alter('remove_row', i)
    adjustDelRowHeaders(i)
  }

  renderRowHeader(this)
  const cellsMap = context.cellsMap; const changeCells = []
  for (const cell of cellsMap.values()) {
    const rowIndex = cell.rowNumber - 1
    if (rowIndex >= endRow) {
      changeCells.push(cell)
    }
  }

  for (const cell of changeCells) {
    removeCell(cell)
  }

  for (const cell of changeCells) {
    const newCell = deepCopy(cell)
    newCell.rowNumber = cell.rowNumber - dif
    updateParentRowRefs(newCell, endRow + 2, -dif, context)
    addCell(newCell)
  }

  this.updateSettings({ rowHeights: newRowHeights, mergeCells: newMergeCells })
  resetTableData(this, context)
  setDirty()

  const _this = this
  undoManager.add({
    redo() {
      rowHeights = _this.getSettings().rowHeights, mergeCells = _this.getSettings().mergeCells
      oldMergeCells = []
      newMergeCells = mergeCells.concat([])
      for (const mergeItem of mergeCells) {
        oldMergeCells.push(Object.assign({}, mergeItem))
        const row = mergeItem.row; const rowspan = mergeItem.rowspan
        const rowEnd = row + rowspan - 1
        const index = newMergeCells.indexOf(mergeItem)
        if (row >= startRow && rowEnd <= endRow) {
          newMergeCells.splice(index, 1)
        }
        else if (row <= startRow && rowEnd >= endRow) {
          const span = endRow - startRow + 1
          let leftSpan = rowspan - span
          if (leftSpan === 0) {
            leftSpan = 1
          }

          if (leftSpan === 1 && mergeItem.colspan === 1) {
            newMergeCells.splice(index, 1)
          }
          else {
            newMergeCells[index] = {
              col: mergeItem.col,
              row,
              rowspan: leftSpan,
              colspan: mergeItem.colspan,
            }
          }
        }
        else if (row > endRow) {
          const totalRows = endRow - startRow + 1
          newMergeCells[index] = {
            col: mergeItem.col,
            row: row - totalRows,
            rowspan: mergeItem.rowspan,
            colspan: mergeItem.colspan,
          }
        }
      }

      _this.updateSettings({ mergeCells: [] })
      oldRowHeights = rowHeights.concat([])
      newRowHeights = rowHeights.concat([])
      newRowHeights.splice(startRow, dif)
      countCols = _this.countCols()
      removeCells.splice(0)
      for (let i = endRow; i >= startRow; i--) {
        for (let j = 0; j < countCols; j++) {
          const cell = getCell(i, j)
          if (cell) {
            removeCell(cell)
            removeCells.push(cell)
          }
        }

        _this.alter('remove_row', i)
        adjustDelRowHeaders(i)
      }

      renderRowHeader(_this)
      changeCells.splice(0)
      for (const cell of cellsMap.values()) {
        const rowIndex = cell.rowNumber - 1
        if (rowIndex >= endRow) {
          changeCells.push(cell)
        }
      }

      for (const cell of changeCells) {
        removeCell(cell)
      }

      for (const cell of changeCells) {
        const newCell = deepCopy(cell)
        newCell.rowNumber = cell.rowNumber - dif
        updateParentRowRefs(newCell, endRow + 2, -dif, getContext())
        addCell(newCell)
      }

      _this.updateSettings({ rowHeights: newRowHeights, mergeCells: newMergeCells })
      resetTableData(_this, context)
      setDirty()
    },
    undo() {
      for (let i = endRow; i >= startRow; i--) {
        _this.alter('insert_row', i)
        adjustInsertRowHeaders(i)
      }

      renderRowHeader(_this)
      changeCells.splice(0)
      for (const cell of cellsMap.values()) {
        const rowIndex = cell.rowNumber - 1
        if (rowIndex >= startRow) {
          changeCells.push(cell)
        }
      }

      for (const cell of changeCells) {
        removeCell(cell)
      }

      for (const cell of changeCells) {
        const newCell = deepCopy(cell)
        newCell.rowNumber = cell.rowNumber + dif
        updateParentRowRefs(newCell, startRow + 1, dif, getContext())
        addCell(newCell)
      }

      for (const cell of removeCells) {
        addCell(cell)
      }

      _this.updateSettings({ rowHeights: oldRowHeights, mergeCells: oldMergeCells })
      resetTableData(_this, context)
      setDirty()
    },
  })
}
