import Handsontable from 'handsontable'
/**
 * Created by Jacky.Gao on 2017-01-31.
 */
import { $t } from '@/locales'
import { showAlert } from '@/utils/comnon'
import { addCell, addRowHeader, adjustDelRowHeaders, getCell, removeCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import RowColNumberDialogClass from '@/views/report/designer/edit-table/row-col-number-dialog/class'
import Class from '@/views/report/designer/edit-table/row-col-width-height-dialog/class'
import { renderRowHeader } from '@/views/report/designer/edit-table/utils/HeaderUtils'
import { doDeleteCol } from '@/views/report/designer/edit-table/utils/operation/DeleteColOperation'
import { doDeleteRow } from '@/views/report/designer/edit-table/utils/operation/DeleteRowOperation'
import { doInsertCol } from '@/views/report/designer/edit-table/utils/operation/InsertColOperation'
import { doInsertRow } from '@/views/report/designer/edit-table/utils/operation/InsertRowOperation'
import TableManager from '../manager.js'

function undoCleanCells(startRow, endRow, startCol, endCol, removeCellsMap, type) {
  const hot = TableManager.get()
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cell = getCell(i, j)
      if (!cell) {
        continue
      }

      const key = `${cell.rowNumber},${cell.columnNumber}`
      switch (type) {
        case 'content': {
          const orgData = removeCellsMap.get(key)
          if (!orgData) {
            showAlert($t('table.contextMenu.cancelConetntFail'))
            return
          }

          cell.value = orgData.value
          cell.leftParentCellName = orgData.leftParentCellName
          cell.topParentCellName = orgData.topParentCellName
          const value = cell.value
          const valueType = value.type
          let text = value.value
          if (valueType === 'dataset') {
            text = `${value.datasetName}.${value.aggregate}(${value.property})`
          }

          hot.setDataAtCell(i, j, text)

          break
        }

        case 'style': {
          const orgStyle = removeCellsMap.get(key)
          if (!orgStyle) {
            showAlert($t('table.contextMenu.cancelStyleFail'))
            return
          }

          cell.cellStyle = orgStyle

          break
        }

        case 'all': {
          removeCell(cell)
          const orgCell = removeCellsMap.get(key)
          if (!orgCell) {
            showAlert($t('table.contextMenu.cancelClearFail'))
            return
          }

          addCell(orgCell)
          const value = orgCell.value
          const valueType = value.type
          let text = value.value
          if (valueType === 'dataset') {
            text = `${value.datasetName}.${value.aggregate}(${value.property})`
          }

          hot.setDataAtCell(i, j, text)

          break
        }
      // No default
      }
    }
  }

  Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
  hot.render()
}

function undoPasteStyle(startRow, endRow, startCol, endCol, oldStyleMap) {
  const cellsMap = new Map()
  const hot = TableManager.get()
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cell = getCell(i, j)
      if (!cell) {
        continue
      }

      const key = `${cell.rowNumber},${cell.columnNumber}`
      const oldStyle = oldStyleMap.get(key)
      if (oldStyle) {
        cell.cellStyle = oldStyle
      }
    }
  }

  Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
  hot.render()
  return cellsMap
}

function pasteStyle(startRow, endRow, startCol, endCol) {
  const style = window.__copy_cell_style__
  const cellsMap = new Map()
  const hot = TableManager.get()
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cell = getCell(i, j)
      if (!cell) {
        continue
      }

      const key = `${cell.rowNumber},${cell.columnNumber}`
      if (!cell.cellStyle) {
        cell.cellStyle = {}
      }

      const oldStyle = structuredClone(cell.cellStyle)
      cellsMap.set(key, oldStyle)
      cell.cellStyle.fontSize = style.fontSize
      cell.cellStyle.forecolor = style.forecolor
      cell.cellStyle.fontFamily = style.fontFamily
      cell.cellStyle.valign = style.valign
      cell.cellStyle.align = style.align
      cell.cellStyle.bgcolor = style.bgcolor
      cell.cellStyle.bold = style.bold
      cell.cellStyle.italic = style.italic
      cell.cellStyle.underline = style.underline
    }
  }

  Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
  hot.render()
  return cellsMap
}

function cleanCells(startRow, endRow, startCol, endCol, type) {
  const removeCellsMap = new Map()
  const hot = TableManager.get()
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cell = getCell(i, j)
      if (!cell) {
        continue
      }

      cell.cellStyle.format = null
      const key = `${cell.rowNumber},${cell.columnNumber}`
      switch (type) {
        case 'content': {
          removeCellsMap.set(key, {
            value: cell.value,
            leftParentCellName: cell.leftParentCellName,
            topParentCellName: cell.topParentCellName,
          })
          cell.value = {
            type: 'simple',
            value: '',
          }
          cell.expand = 'None'
          cell.conditionPropertyItems = null
          cell.leftParentCellName = null
          cell.topParentCellName = null
          hot.setDataAtCell(i, j, '')

          break
        }

        case 'style': {
          removeCellsMap.set(key, cell.cellStyle)
          cell.cellStyle = {
            fontSize: 9,
            forecolor: '0,0,0',
            fontFamily: '宋体',
            align: 'center',
            valign: 'middle',
          }

          break
        }

        case 'all': {
          removeCell(cell)
          removeCellsMap.set(key, cell)
          const newCell = {
            rowNumber: cell.rowNumber,
            columnNumber: cell.columnNumber,
            expand: 'None',
            value: {
              type: 'simple',
              value: '',
            },
            cellStyle: {
              fontSize: 9,
              forecolor: '0,0,0',
              fontFamily: '宋体',
              align: 'center',
              valign: 'middle',
            },
          }
          addCell(newCell)
          hot.setDataAtCell(i, j, '')

          break
        }
      // No default
      }
    }
  }

  Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
  hot.render()
  return removeCellsMap
}

function checkCopyOperationDisabled() {
  const hot = TableManager.get()
  if (!hot || typeof hot.getSelected !== 'function') {
    return true
  }

  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    return true
  }

  return false
}

function checkPasteOperationDisabled() {
  const hot = TableManager.get()
  if (!hot || typeof hot.getSelected !== 'function') {
    return true
  }

  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    return true
  }

  if (window.__copy_cell_style__) {
    return false
  }

  return true
}

function checkRowDeleteOperationDisabled() {
  const hot = TableManager.get()
  if (!hot || typeof hot.getSelected !== 'function') {
    return true
  }

  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    return true
  }

  const selection = selected[0]
  if (!selection) {
    return true
  }

  const [startRow, , endRow] = selection
  const dif = Math.abs(startRow - endRow) + 1
  const countRows = typeof hot.countRows === 'function' ? hot.countRows() : 0
  return dif >= countRows
}

function checkColDeleteOperationDisabled() {
  const hot = TableManager.get()
  if (!hot || typeof hot.getSelected !== 'function') {
    return true
  }

  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    return true
  }

  const selection = selected[0]
  if (!selection) {
    return true
  }

  const [, startCol, , endCol] = selection
  const dif = Math.abs(startCol - endCol) + 1
  const countCols = typeof hot.countCols === 'function' ? hot.countCols() : 0
  return dif >= countCols
}

function checkCleanOperationDisabled() {
  const hot = TableManager.get()
  if (!hot || typeof hot.getSelected !== 'function') {
    return true
  }

  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    return true
  }

  return false
}

export default function buildMenuConfigure() {
  return {
    callback(key, options) {
      const _this = this
      switch (key) {
        case 'insert_row_above': {
          const dialog = new RowColNumberDialogClass()
          dialog.show((number) => {
            doInsertRow.call(_this, true, number)
          }, true)

          break
        }

        case 'insert_row_below': {
          const dialog = new RowColNumberDialogClass()
          dialog.show((number) => {
            doInsertRow.call(_this, false, number)
          }, true)

          break
        }

        case 'insert_col_left': {
          const dialog = new RowColNumberDialogClass()
          dialog.show((number) => {
            doInsertCol.call(_this, true, number)
          }, false)

          break
        }

        case 'insert_col_right': {
          const dialog = new RowColNumberDialogClass()
          dialog.show((number) => {
            doInsertCol.call(_this, false, number)
          }, false)

          break
        }

        case 'del_row': {
          doDeleteRow.call(this)

          break
        }

        case 'del_col': {
          doDeleteCol.call(this)

          break
        }

        case 'clean_content': {
          const selected = this.getSelected()
          const [startRow, startCol, endRow, endCol] = selected[0]
          let removeCellsMap = cleanCells(startRow, endRow, startCol, endCol, 'content')
          undoManager.add({
            redo() {
              removeCellsMap = cleanCells(startRow, endRow, startCol, endCol, 'content')
            },
            undo() {
              undoCleanCells(startRow, endRow, startCol, endCol, removeCellsMap, 'content')
            },
          })

          break
        }

        case 'clean_style': {
          const selected = this.getSelected()
          const [startRow, startCol, endRow, endCol] = selected[0]
          let removeCellsMap = cleanCells(startRow, endRow, startCol, endCol, 'style')
          undoManager.add({
            redo() {
              removeCellsMap = cleanCells(startRow, endRow, startCol, endCol, 'style')
            },
            undo() {
              undoCleanCells(startRow, endRow, startCol, endCol, removeCellsMap, 'style')
            },
          })

          break
        }

        case 'clean': {
          const selected = this.getSelected()
          const [startRow, startCol, endRow, endCol] = selected[0]
          let removeCellsMap = cleanCells(startRow, endRow, startCol, endCol, 'all')
          undoManager.add({
            redo() {
              removeCellsMap = cleanCells(startRow, endRow, startCol, endCol, 'all')
            },
            undo() {
              undoCleanCells(startRow, endRow, startCol, endCol, removeCellsMap, 'all')
            },
          })

          break
        }

        case 'repeat_row_header': {
          const selected = this.getSelected()
          const [startRow, , endRow] = selected[0]
          for (let rowNumber = startRow; rowNumber <= endRow; rowNumber++) {
            addRowHeader(rowNumber, 'headerrepeat')
          }

          renderRowHeader(this)
          setDirty()

          break
        }

        case 'title_row': {
          const selected = this.getSelected()
          const [startRow, , endRow] = selected[0]
          for (let rowNumber = startRow; rowNumber <= endRow; rowNumber++) {
            addRowHeader(rowNumber, 'title')
          }

          renderRowHeader(this)
          setDirty()

          break
        }

        case 'repeat_row_footer': {
          const selected = this.getSelected()
          const [startRow, , endRow] = selected[0]
          for (let rowNumber = startRow; rowNumber <= endRow; rowNumber++) {
            addRowHeader(rowNumber, 'footerrepeat')
          }

          renderRowHeader(this)
          setDirty()

          break
        }

        case 'summary_row': {
          const selected = this.getSelected()
          const [startRow, , endRow] = selected[0]
          for (let rowNumber = startRow; rowNumber <= endRow; rowNumber++) {
            addRowHeader(rowNumber, 'summary')
          }

          renderRowHeader(this)
          setDirty()

          break
        }

        case 'repeat_cancel': {
          const selected = this.getSelected()
          const [startRow, , endRow] = selected[0]
          for (let rowNumber = startRow; rowNumber <= endRow; rowNumber++) {
            adjustDelRowHeaders(rowNumber)
          }

          renderRowHeader(this)
          setDirty()

          break
        }

        case 'row_height': {
          const selected = this.getSelected()
          const [startRow, , , endRow] = selected[0]
          const rowHeight = this.getRowHeight(startRow)
          const dialog = new Class()
          dialog.show((newHeight) => {
            const rowHeights = _this.getSettings().rowHeights
            for (let i = startRow; i <= endRow; i++) {
              rowHeights[i] = newHeight
            }

            _this.updateSettings({
              rowHeights,
              manualRowResize: rowHeights,
            })
          }, rowHeight, false)
          setDirty()

          break
        }

        case 'col_width': {
          const selected = this.getSelected()
          const [, startCol, , endCol] = selected[0]
          const colWidth = this.getColWidth(startCol)
          const dialog = new Class()
          dialog.show((newColWidth) => {
            const colWidths = _this.getSettings().colWidths
            for (let i = startCol; i <= endCol; i++) {
              colWidths[i] = newColWidth
            }

            _this.updateSettings({
              colWidths,
              manualColumnResize: colWidths,
            })
          }, colWidth, true)
          setDirty()

          break
        }

        case 'copy_style': {
          const selected = this.getSelected()
          const [startRow, startCol] = selected[0]
          const cell = getCell(startRow, startCol)
          if (!cell) {
            showAlert($t('selectTargetCellFirst'))
            return
          }

          window.__copy_cell_style__ = cell.cellStyle

          break
        }

        case 'paste_style': {
          if (!window.__copy_cell_style__) {
            showAlert($t('copyStyleFirst'))
            return
          }

          const selected = this.getSelected()
          const [startRow, startCol, endRow, endCol] = selected[0]
          let oldCellsStyleMap = pasteStyle(startRow, endRow, startCol, endCol)
          undoManager.add({
            redo() {
              oldCellsStyleMap = pasteStyle(startRow, endRow, startCol, endCol)
            },
            undo() {
              undoPasteStyle(startRow, endRow, startCol, endCol, oldCellsStyleMap)
            },
          })

          break
        }
      // No default
      }
    },
    items: {
      insert_row_above: {
        name: `<i class="iconfont icon-insertrow" style="color: #3344d3;font-size: 13px"></i> ${$t('table.contextMenu.insertRowUp')}`,
      },
      insert_row_below: {
        name: `<i class="iconfont icon-insertrow" style="color: #3344d3;font-size: 13px"></i> ${$t('table.contextMenu.insertRowDown')}`,
      },
      insert_col_left: {
        name: `<i class="iconfont icon-insert-column" style="color: #008ed3;font-size: 13px"></i> ${$t('table.contextMenu.insertColBefore')}`,
      },
      insert_col_right: {
        name: `<i class="iconfont icon-insert-column" style="color: #008ed3;font-size: 13px"></i> ${$t('table.contextMenu.insertColAfter')}`,
      },
      del_row: {
        name: `<i class="iconfont icon-deleterow" style="color: #d30a16;font-size: 13px"></i>  ${$t('table.contextMenu.delRow')}`,
        disabled: checkRowDeleteOperationDisabled,
      },
      del_col: {
        name: `<i class="iconfont icon-deletecolumn" style="color: #d30a16;font-size: 13px"></i>  ${$t('table.contextMenu.delCol')}`,
        disabled: checkColDeleteOperationDisabled,
      },
      row_height: {
        name: `<i class="iconfont icon-height" style="color: #d30a16;font-size: 13px;font-weight:bold"></i>  ${$t('table.contextMenu.rowHeight')}`,
        disabled: checkRowDeleteOperationDisabled,
      },
      col_width: {
        name: `<i class="iconfont icon-width" style="color: #d30a16;font-size: 13px;font-weight:bold"></i>  ${$t('table.contextMenu.colWidth')}`,
        disabled: checkColDeleteOperationDisabled,
      },
      title_row: {
        name: `<i class="iconfont icon-title" style="color: #9C27B0;font-size: 13px"></i>  ${$t('table.contextMenu.title')}`,
        disabled: checkRowDeleteOperationDisabled,
      },
      repeat_row_header: {
        name: `<i class="iconfont icon-header-repeat" style="color: #9C27B0;font-size: 13px"></i>  ${$t('table.contextMenu.repeatHeader')}`,
        disabled: checkRowDeleteOperationDisabled,
      },
      repeat_row_footer: {
        name: `<i class="iconfont icon-footer-repeat" style="color: #9C27B0;font-size: 13px"></i>  ${$t('table.contextMenu.repeatFooter')}`,
        disabled: checkRowDeleteOperationDisabled,
      },
      summary_row: {
        name: `<i class="iconfont icon-summary" style="color: #9C27B0;font-size: 13px"></i>  ${$t('table.contextMenu.summary')}`,
        disabled: checkRowDeleteOperationDisabled,
      },
      repeat_cancel: {
        name: `<i class="iconfont icon-error" style="color: #d30e00;font-size: 13px"></i>  ${$t('table.contextMenu.cancel')}`,
        disabled: checkRowDeleteOperationDisabled,
      },
      copy_style: {
        name: `<i class="iconfont icon-copy" style="color: #d30e00;font-size: 13px"></i>  ${$t('table.contextMenu.copy')}`,
        disabled: checkCopyOperationDisabled,
      },
      paste_style: {
        name: `<i class="iconfont icon-paste" style="color: #d30e00;font-size: 13px"></i>  ${$t('table.contextMenu.paste')}`,
        disabled: checkPasteOperationDisabled,
      },
      clean_content: {
        name: `<i class="iconfont icon-clean-content" style="color: #007471;font-size: 13px"></i>  ${$t('table.contextMenu.clearContent')}`,
        disabled: checkCleanOperationDisabled,
      },
      clean_style: {
        name: `<i class="iconfont icon-clean-style" style="color: #00746f;font-size: 13px"></i>  ${$t('table.contextMenu.clearStyle')}`,
        disabled: checkCleanOperationDisabled,
      },
      clean: {
        name: `<i class="iconfont icon-clean" style="color: #d30e00;font-size: 13px"></i>  ${$t('table.contextMenu.clearAll')}`,
        disabled: checkCleanOperationDisabled,
      },
    },
  }
}
