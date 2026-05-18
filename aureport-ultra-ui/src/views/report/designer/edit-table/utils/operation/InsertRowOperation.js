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
import { buildNewCellDef, resetTableData, setDirty, undoManager } from '@/utils/table'
import { renderRowHeader } from '@/views/report/designer/edit-table/utils/HeaderUtils'

function parseCellName(cellName) {
  const match = cellName.match(/^([A-Z]+)(\d+)$/)
  if (!match)
    return null
  let colIndex = 0
  const colStr = match[1]
  for (let i = 0; i < colStr.length; i++) {
    colIndex = colIndex * 26 + (colStr.codePointAt(i) - 64)
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

export function doInsertRow(above, number = 1) {
  const selected = this.getSelected()
  if (!selected) {
    showAlert($t('table.rowTip')).then((r) => {})
    return
  }

  const [, startRow, , endRow] = selected[0]
  let position = startRow
  if (startRow > endRow) {
    position = above ? endRow : startRow + 1
  }
  else {
    position = above ? startRow : endRow + 1
  }

  let rowHeights = this.getSettings().rowHeights
  let newRowHeights = rowHeights.concat([])
  for (let i = 0; i < number; i++) {
    newRowHeights.splice(position, 0, 25)
  }

  this.alter('insert_row', position, number)
  adjustInsertRowHeaders(position)
  renderRowHeader(this)

  buildNewRowCells(this, position, number)
  this.updateSettings({
    rowHeights: newRowHeights,
    manualRowResize: newRowHeights,
  })
  const context = getContext()
  resetTableData(this, context)
  setDirty()

  // eslint-disable-next-line unicorn/no-this-assignment -- _this needed for undo/redo closures
  const _this = this
  const cellsMap = context.cellsMap
  const removeCells = []
  let removeRowHeight = 25
  undoManager.add({
    redo() {
      rowHeights = _this.getSettings().rowHeights
      newRowHeights = rowHeights.concat([])
      for (let i = 0; i < number; i++) {
        newRowHeights.splice(position, 0, removeRowHeight)
      }

      _this.alter('insert_row', position, number)
      adjustInsertRowHeaders(position)
      renderRowHeader(_this)
      const changeCells = []
      for (const cell of cellsMap.values()) {
        const rowIndex = cell.rowNumber - 1
        if (rowIndex >= position) {
          changeCells.push(cell)
        }
      }

      for (const cell of changeCells) {
        removeCell(cell)
      }

      for (const cell of changeCells) {
        cell.rowNumber = cell.rowNumber + number
        updateParentRowRefs(cell, position + 1, number, context)
        addCell(cell)
      }

      for (const cell of removeCells) {
        addCell(cell)
      }

      _this.updateSettings({
        rowHeights: newRowHeights,
        manualRowResize: newRowHeights,
      })
      resetTableData(_this, context)
      setDirty()
    },
    undo() {
      removeCells.splice(0)
      rowHeights = _this.getSettings().rowHeights
      newRowHeights = rowHeights.concat([])
      for (let i = 0; i < number; i++) {
        removeRowHeight = newRowHeights[position]
        newRowHeights.splice(position, 1)
      }

      _this.alter('remove_row', position, number)
      adjustDelRowHeaders(position)
      renderRowHeader(_this)
      _this.updateSettings({
        rowHeights: newRowHeights,
        manualRowResize: newRowHeights,
      })
      const countCols = _this.countCols()
      for (let i = 0; i < number; i++) {
        for (let j = 0; j < countCols; j++) {
          const cell = getCell(position, j)
          if (cell) {
            removeCells.push(cell)
            removeCell(cell)
          }
        }
      }

      const changeCells = []
      for (const cell of cellsMap.values()) {
        const rowIndex = cell.rowNumber - 1
        if (rowIndex > position) {
          changeCells.push(cell)
        }
      }

      for (const cell of changeCells) {
        removeCell(cell)
      }

      for (const cell of changeCells) {
        cell.rowNumber = cell.rowNumber - number
        updateParentRowRefs(cell, position + 2, -number, context)
        addCell(cell)
      }

      resetTableData(_this, context)
      setDirty()
    },
  })
}

;

function buildNewRowCells(hot, position, number) {
  const countCols = hot.countCols()
  const context = getContext()
  const cellsMap = context.cellsMap
  const changeCells = []
  for (const cell of cellsMap.values()) {
    const rowIndex = cell.rowNumber - 1
    if (rowIndex >= position) {
      changeCells.push(cell)
    }
  }

  for (const cell of changeCells) {
    removeCell(cell)
  }

  for (const cell of changeCells) {
    const newCell = deepCopy(cell)
    newCell.rowNumber = cell.rowNumber + number
    updateParentRowRefs(newCell, position + 1, number, context)
    addCell(newCell)
  }

  for (let i = 0; i < number; i++) {
    for (let j = 0; j < countCols; j++) {
      const newCellDef = buildNewCellDef(position + i + 1, (j + 1))
      addCell(newCellDef)
    }
  }
}

;
