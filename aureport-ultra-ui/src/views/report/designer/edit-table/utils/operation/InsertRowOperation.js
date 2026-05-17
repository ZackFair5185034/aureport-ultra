/**
 * Created by Jacky.Gao on 2017-02-17.
 */
import {buildNewCellDef, resetTableData, setDirty, undoManager} from '@/utils/table';
import {renderRowHeader} from '@/views/report/designer/edit-table/utils/HeaderUtils';
import {$t} from "@/locales";
import {showAlert} from "@/utils/comnon";
import {
    addCell,
    adjustDelRowHeaders,
    adjustInsertRowHeaders,
    getCell,
    getContext,
    removeCell
} from "@/utils/contextActions";
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

function updateParentRowRefs(cell, thresholdRow, delta, context) {
    if (cell.topParentCellName) {
        const info = parseCellName(cell.topParentCellName);
        if (info && info.rowNumber >= thresholdRow) {
            cell.topParentCellName = context.LETTERS[info.colIndex] + (info.rowNumber + delta);
        }
    }
    if (cell.leftParentCellName) {
        const info = parseCellName(cell.leftParentCellName);
        if (info && info.rowNumber >= thresholdRow) {
            cell.leftParentCellName = context.LETTERS[info.colIndex] + (info.rowNumber + delta);
        }
    }
}

export function doInsertRow(above, number = 1) {
    const selected = this.getSelected();
    if (!selected) {
        showAlert($t('table.rowTip')).then(r => {
        });
        return;
    }
    const [startRow, startCol, endRow, endCol] = selected[0];
    let position = startRow;
    if (startRow > endRow) {
        if (above) {
            position = endRow;
        } else {
            position = startRow + 1;
        }
    } else {
        if (above) {
            position = startRow;
        } else {
            position = endRow + 1;
        }
    }
    let rowHeights = this.getSettings().rowHeights;
    let newRowHeights = rowHeights.concat([]);
    for (let i = 0; i < number; i++) {
        newRowHeights.splice(position, 0, 25);
    }
    this.alter("insert_row", position, number);
    adjustInsertRowHeaders(position);
    renderRowHeader(this);

    buildNewRowCells(this, position, number);
    this.updateSettings({
        rowHeights: newRowHeights,
        manualRowResize: newRowHeights
    });
    const context = getContext();
    resetTableData(this, context);
    setDirty();

    const _this = this;
    const cellsMap = context.cellsMap
    const removeCells = [];
    let removeRowHeight = 25;
    undoManager.add({
        redo: function () {
            rowHeights = _this.getSettings().rowHeights;
            newRowHeights = rowHeights.concat([]);
            for (let i = 0; i < number; i++) {
                newRowHeights.splice(position, 0, removeRowHeight);
            }
            _this.alter("insert_row", position, number);
            adjustInsertRowHeaders(position);
            renderRowHeader(_this);
            let changeCells = [];
            for (let cell of cellsMap.values()) {
                let rowIndex = cell.rowNumber - 1;
                if (rowIndex >= position) {
                    changeCells.push(cell);
                }
            }
            for (let cell of changeCells) {
                removeCell(cell);
            }
            for (let cell of changeCells) {
                cell.rowNumber = cell.rowNumber + number;
                updateParentRowRefs(cell, position + 1, number, context);
                addCell(cell);
            }
            for (let cell of removeCells) {
                addCell(cell);
            }
            _this.updateSettings({
                rowHeights: newRowHeights,
                manualRowResize: newRowHeights
            });
            resetTableData(_this, context);
            setDirty();
        },
        undo: function () {
            removeCells.splice(0, removeCells.length);
            rowHeights = _this.getSettings().rowHeights;
            newRowHeights = rowHeights.concat([]);
            for (let i = 0; i < number; i++) {
                removeRowHeight = newRowHeights[position];
                newRowHeights.splice(position, 1);
            }
            _this.alter('remove_row', position, number);
            adjustDelRowHeaders(position);
            renderRowHeader(_this);
            _this.updateSettings({
                rowHeights: newRowHeights,
                manualRowResize: newRowHeights
            });
            let countCols = _this.countCols();
            for (let i = 0; i < number; i++) {
                for (let j = 0; j < countCols; j++) {
                    let cell = getCell(position, j);
                    if (cell) {
                        removeCells.push(cell);
                        removeCell(cell);
                    }
                }
            }
            let changeCells = [];
            for (let cell of cellsMap.values()) {
                let rowIndex = cell.rowNumber - 1;
                if (rowIndex > position) {
                    changeCells.push(cell);
                }
            }
            for (let cell of changeCells) {
                removeCell(cell);
            }
            for (let cell of changeCells) {
                cell.rowNumber = cell.rowNumber - number;
                updateParentRowRefs(cell, position + 2, -number, context);
                addCell(cell);
            }
            resetTableData(_this, context);
            setDirty();
        }
    });
};


function buildNewRowCells(hot, position, number) {
    const countCols = hot.countCols();
    const countRows = hot.countRows();
    const context = getContext();
    const cellsMap = context.cellsMap;
    const changeCells = [];
    for (let cell of cellsMap.values()) {
        let rowIndex = cell.rowNumber - 1;
        if (rowIndex >= position) {
            changeCells.push(cell);
        }
    }
    for (let cell of changeCells) {
        removeCell(cell);
    }
    for (let cell of changeCells) {
        let newCell = deepCopy(cell);
        newCell.rowNumber = cell.rowNumber + number;
        updateParentRowRefs(newCell, position + 1, number, context);
        addCell(newCell);
    }
    for (let i = 0; i < number; i++) {
        for (let j = 0; j < countCols; j++) {
            let newCellDef = buildNewCellDef(position + i + 1, (j + 1));
            addCell(newCellDef);
        }
    }
};
