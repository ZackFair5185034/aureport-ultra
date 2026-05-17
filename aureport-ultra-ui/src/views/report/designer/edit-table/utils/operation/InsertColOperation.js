/**
 * Created by Jacky.Gao on 2017-02-17.
 */
import {buildNewCellDef, resetTableData, setDirty, undoManager} from '@/utils/table';
import {showAlert} from '@/utils/comnon';
import {$t} from "@/locales";
import {addCell, getCell, getCellsMap, getContext, removeCell} from '@/utils/contextActions';
import {deepCopy} from '@/components/utils';

function parseCellName(cellName) {
    const match = cellName.match(/^([A-Z]+)(\d+)$/);
    if (!match) return null;
    let colIndex = 0;
    const colStr = match[1];
    for (let i = 0; i < colStr.length; i++) {
        colIndex = colIndex * 26 + (colStr.charCodeAt(i) - 64);
    }
    return {colIndex: colIndex - 1, rowNumber: parseInt(match[2])};
}

function updateParentColRefs(cell, thresholdCol, delta, context) {
    if (cell.leftParentCellName) {
        const info = parseCellName(cell.leftParentCellName);
        if (info && info.colIndex >= thresholdCol) {
            cell.leftParentCellName = context.LETTERS[info.colIndex + delta] + info.rowNumber;
        }
    }
    if (cell.topParentCellName) {
        const info = parseCellName(cell.topParentCellName);
        if (info && info.colIndex >= thresholdCol) {
            cell.topParentCellName = context.LETTERS[info.colIndex + delta] + info.rowNumber;
        }
    }
}

/**
 * 插入列操作
 *
 * 架构说明：
 * - 使用 contextActions.js 中的方法通过 Vuex mutations 修改 context 数据
 * - 符合 Vuex 最佳实践，所有修改都通过 mutation 进行
 */
export function doInsertCol(left, number = 1) {
    const selected = this.getSelected();
    if (!selected) {
        showAlert($t('table.colTip'));
        return;
    }
    const [startRow, startCol, endRow, endCol] = selected[0];
    let position = startCol;
    if (startCol > endCol) {
        if (left) {
            position = endCol;
        } else {
            position = startCol + 1;
        }
    } else {
        if (left) {
            position = startCol;
        } else {
            position = endCol + 1;
        }
    }
    let colWidths = this.getSettings().colWidths;
    let newColWidths = colWidths.concat([]);
    for (let i = 0; i < number; i++) {
        newColWidths.splice(position, 0, 98);
    }
    this.alter("insert_col", position, number);

    const cellsMap = getCellsMap();
    const context = getContext();
    const changeCells = [];
    for (let cell of cellsMap.values()) {
        let colIndex = cell.columnNumber - 1;
        if (colIndex >= position) {
            changeCells.push(cell);
        }
    }
    for (let cell of changeCells) {
        removeCell(cell);
    }
    for (let cell of changeCells) {
        let newCell = deepCopy(cell);
        newCell.columnNumber = cell.columnNumber + number;
        updateParentColRefs(newCell, position, number, context);
        addCell(newCell);
    }
    let countRows = this.countRows();
    for (let i = 0; i < number; i++) {
        for (let j = 0; j < countRows; j++) {
            let newCellDef = buildNewCellDef(j + 1, position + i + 1);
            addCell(newCellDef);
        }
    }
    this.updateSettings({
        colWidths: newColWidths,
        manualColumnResize: newColWidths
    });
    resetTableData(this, context);
    setDirty();

    const _this = this, removeCells = [];
    let removeColWidth = 98;
    undoManager.add({
        redo: function () {
            colWidths = _this.getSettings().colWidths;
            newColWidths = colWidths.concat([]);
            for (let i = 0; i < number; i++) {
                newColWidths.splice(position, 0, removeColWidth);
            }
            _this.alter("insert_col", position, number);
            changeCells.splice(0, changeCells.length);
            for (let cell of cellsMap.values()) {
                let colIndex = cell.columnNumber - 1;
                if (colIndex >= position) {
                    changeCells.push(cell);
                }
            }
            for (let cell of changeCells) {
                removeCell(cell);
            }
            for (let cell of changeCells) {
                let newCell = deepCopy(cell);
                newCell.columnNumber = cell.columnNumber + number;
                updateParentColRefs(newCell, position, number, getContext());
                addCell(newCell);
            }
            for (let cell of removeCells) {
                addCell(cell);
            }
            _this.updateSettings({
                colWidths: newColWidths,
                manualColumnResize: newColWidths
            });
            resetTableData(_this, context);
            setDirty();
        },
        undo: function () {
            removeCells.splice(0, removeCells.length);
            colWidths = _this.getSettings().colWidths;
            newColWidths = colWidths.concat([]);
            for (let i = 0; i < number; i++) {
                removeColWidth = newColWidths[position];
                newColWidths.splice(position, 1);
            }
            _this.alter('remove_col', position, number);
            _this.updateSettings({
                colWidths: newColWidths,
                manualColumnResize: newColWidths
            });
            let countRows = _this.countRows();
            for (let i = 0; i < number; i++) {
                for (let j = 0; j < countRows; j++) {
                    const cell = getCell(j, position);
                    if (cell) {
                        removeCell(cell);
                        removeCells.push(cell);
                    }
                }
            }
            changeCells.splice(0, changeCells.length);
            for (let cell of cellsMap.values()) {
                let colIndex = cell.columnNumber - 1;
                if (colIndex > position) {
                    changeCells.push(cell);
                }
            }
            for (let cell of changeCells) {
                removeCell(cell);
            }
            for (let cell of changeCells) {
                let newCell = deepCopy(cell);
                newCell.columnNumber = cell.columnNumber - number;
                updateParentColRefs(newCell, position + 1, -number, getContext());
                addCell(newCell);
            }

            resetTableData(_this, context);
            setDirty();
        }
    });
};
