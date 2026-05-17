import { deepCopy } from '@/components/utils'
import { $t } from '@/locales'
import { showAlert } from '@/utils/comnon'
import { addCell, getCell, getContext, removeCell } from '@/utils/contextActions'
/**
 * Created by Jacky.Gao on 2017-02-17.
 */
import { resetTableData, setDirty, undoManager } from '@/utils/table'

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

function updateParentColRefs(cell, thresholdCol, delta, context) {
  if (cell.leftParentCellName) {
    const info = parseCellName(cell.leftParentCellName)
    if (info && info.colIndex >= thresholdCol) {
      cell.leftParentCellName = context.LETTERS[info.colIndex + delta] + info.rowNumber
    }
  }

  if (cell.topParentCellName) {
    const info = parseCellName(cell.topParentCellName)
    if (info && info.colIndex >= thresholdCol) {
      cell.topParentCellName = context.LETTERS[info.colIndex + delta] + info.rowNumber
    }
  }
}

export function doDeleteCol() {
  const selected = this.getSelected()
  const context = getContext()
  if (!selected) {
    showAlert($t('table.colTip'))
    return
  }

  let [startRow, startCol, endRow, endCol] = selected[0]
  if (endCol < startCol) {
    const tempStartCol = startCol
    startCol = endCol
    endCol = tempStartCol
  }

  let colWidths = this.getSettings().colWidths; let mergeCells = this.getSettings().mergeCells
  let oldMergeCells = []
  let newMergeCells = mergeCells.concat([])
  for (const mergeItem of mergeCells) {
    oldMergeCells.push(Object.assign({}, mergeItem))
    const col = mergeItem.col; const colspan = mergeItem.colspan
    const colEnd = col + colspan - 1
    const index = newMergeCells.indexOf(mergeItem)
    if (col >= startCol && colEnd <= endCol) {
      newMergeCells.splice(index, 1)
    }
    else if (col <= startCol && colEnd >= endCol) {
      const span = endCol - startCol + 1
      let leftSpan = colspan - span
      if (leftSpan === 0) {
        leftSpan = 1
      }

      if (leftSpan === 1 && mergeItem.rowspan === 1) {
        newMergeCells.splice(index, 1)
      }
      else {
        newMergeCells[index] = {
          col,
          row: mergeItem.row,
          rowspan: mergeItem.rowspan,
          colspan: leftSpan,
        }
      }
    }
    else if (col > endCol) {
      const totalCols = endCol - startCol + 1
      newMergeCells[index] = {
        row: mergeItem.row,
        col: col - totalCols,
        rowspan: mergeItem.rowspan,
        colspan: mergeItem.colspan,
      }
    }
  }

  this.updateSettings({ mergeCells: [] })
  const dif = endCol - startCol + 1
  let oldColWidths = colWidths.concat([])
  let newColWidths = colWidths.concat([])
  newColWidths.splice(startCol, dif)
  let countRows = this.countRows(); const removeCells = []
  for (let i = endCol; i >= startCol; i--) {
    this.alter('remove_col', i)
    for (let j = 0; j < countRows; j++) {
      const cell = getCell(j, i)
      if (cell) {
        removeCell(cell)
        removeCells.push(cell)
      }
    }
  }

  const cellsMap = context.cellsMap; const changeCells = []
  for (const cell of cellsMap.values()) {
    const colIndex = cell.columnNumber - 1
    if (colIndex >= endCol) {
      changeCells.push(cell)
    }
  }

  for (const cell of changeCells) {
    removeCell(cell)
  }

  for (const cell of changeCells) {
    const newCell = deepCopy(cell)
    newCell.columnNumber = cell.columnNumber - dif
    updateParentColRefs(newCell, endCol + 1, -dif, context)
    addCell(newCell)
  }

  this.updateSettings({ colWidths: newColWidths, mergeCells: newMergeCells })
  resetTableData(this, context)
  setDirty()

  const _this = this
  undoManager.add({
    redo() {
      colWidths = _this.getSettings().colWidths, mergeCells = _this.getSettings().mergeCells
      oldMergeCells = []
      newMergeCells = mergeCells.concat([])
      for (const mergeItem of mergeCells) {
        oldMergeCells.push(Object.assign({}, mergeItem))
        const col = mergeItem.col; const colspan = mergeItem.colspan
        const colEnd = col + colspan - 1
        const index = newMergeCells.indexOf(mergeItem)

        if (col >= startCol && colEnd <= endCol) {
          newMergeCells.splice(index, 1)
        }
        else if (col <= startCol && colEnd >= endCol) {
          const span = endCol - startCol + 1
          let leftSpan = colspan - span
          if (leftSpan === 0) {
            leftSpan = 1
          }

          if (leftSpan === 1 && mergeItem.rowspan === 1) {
            newMergeCells.splice(index, 1)
          }
          else {
            newMergeCells[index] = {
              col,
              row: mergeItem.row,
              rowspan: mergeItem.rowspan,
              colspan: leftSpan,
            }
          }
        }
        else if (col > endCol) {
          const totalCols = endCol - startCol + 1
          newMergeCells[index] = {
            row: mergeItem.row,
            col: col - totalCols,
            rowspan: mergeItem.rowspan,
            colspan: mergeItem.colspan,
          }
        }
      }

      _this.updateSettings({ mergeCells: [] })
      oldColWidths = colWidths.concat([])
      newColWidths = colWidths.concat([])
      newColWidths.splice(startCol, dif)
      countRows = _this.countRows()
      removeCells.splice(0)
      for (let i = endCol; i >= startCol; i--) {
        for (let j = 0; j < countRows; j++) {
          const cell = getCell(j, i)
          if (cell) {
            removeCell(cell)
            removeCells.push(cell)
          }
        }

        _this.alter('remove_col', i)
      }

      changeCells.splice(0)
      for (const cell of cellsMap.values()) {
        const colIndex = cell.columnNumber - 1
        if (colIndex >= endCol) {
          changeCells.push(cell)
        }
      }

      for (const cell of changeCells) {
        removeCell(cell)
      }

      for (const cell of changeCells) {
        const newCell = deepCopy(cell)
        newCell.columnNumber = cell.columnNumber - dif
        updateParentColRefs(newCell, endCol + 1, -dif, context)
        addCell(newCell)
      }

      _this.updateSettings({ colWidths: newColWidths, mergeCells: newMergeCells })
      resetTableData(_this, context)
      setDirty()
    },
    undo() {
      for (let i = endCol; i >= startCol; i--) {
        _this.alter('insert_col', i)
      }

      changeCells.splice(0)
      for (const cell of cellsMap.values()) {
        const colIndex = cell.columnNumber - 1
        if (colIndex >= startCol) {
          changeCells.push(cell)
        }
      }

      for (const cell of changeCells) {
        removeCell(cell)
      }

      for (const cell of changeCells) {
        const newCell = deepCopy(cell)
        newCell.columnNumber = cell.columnNumber + dif
        updateParentColRefs(newCell, startCol, dif, context)
        addCell(newCell)
      }

      for (const cell of removeCells) {
        addCell(cell)
      }

      _this.updateSettings({ colWidths: oldColWidths, mergeCells: oldMergeCells })
      resetTableData(_this, context)
      setDirty()
    },
  })
}

;
