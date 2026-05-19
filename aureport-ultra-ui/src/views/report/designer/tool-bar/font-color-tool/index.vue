<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'FontColorTool' })

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null, colIndex: number | null, row2Index: number | null, col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null }),
})

const { t } = useI18n()

const currentColor = ref('0,0,0')
const selectedColor = ref('#000000')

const displayColor = computed(() => {
  return selectedColor.value || '#000000'
})

watch(() => props.selectedCells, (newVal) => {
  if (newVal && newVal.rowIndex !== null && newVal.colIndex !== null) {
    refresh(newVal.rowIndex, newVal.colIndex, newVal.row2Index ?? 0, newVal.col2Index ?? 0)
  }
}, { deep: true })

function checkSelection() {
  const hot = TableManager.get()
  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    showAlert(t('selectTargetCellFirst'))
    return false
  }

  return true
}

function onColorChange(color: string) {
  if (!checkSelection())
    return

  const rgb = hexToRgb(color)
  if (rgb) {
    const rgbStr = `${rgb.r},${rgb.g},${rgb.b}`
    currentColor.value = rgbStr

    const table = TableManager.get()
    const selected = table.getSelected()
    let [startRow, startCol, endRow, endCol] = selected[0]

    if (startRow > endRow) {
      ;[startRow, endRow] = [endRow, startRow]
    }

    if (startCol > endCol) {
      ;[startCol, endCol] = [endCol, startCol]
    }

    const oldForeColorStyle = updateCellsForeColorStyle(startRow, startCol, endRow, endCol, rgbStr)
    table.render()

    undoManager.add({
      redo: () => {
        updateCellsForeColorStyle(startRow, startCol, endRow, endCol, rgbStr)
        table.render()
        setDirty()
      },
      undo: () => {
        restoreForeColorStyle(startRow, startCol, endRow, endCol, oldForeColorStyle)
        table.render()
        setDirty()
      },
    })

    setDirty()
  }
}

function hexToRgb(hex: string) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16),
      }
    : null
}

function updateCellsForeColorStyle(startRow: number, startCol: number, endRow: number, endCol: number, color: string) {
  const oldForeColorStyle: Record<string, string> = {}

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldForeColorStyle[`${i},${j}`] = cellStyle.forecolor ?? ''
      cellStyle.forecolor = color
      setCell(i, j, newCellDef)
    }
  }

  return oldForeColorStyle
}

function restoreForeColorStyle(startRow: number, startCol: number, endRow: number, endCol: number, oldForeColorStyle: Record<string, string>) {
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      cellStyle.forecolor = oldForeColorStyle[`${i},${j}`]
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        currentColor.value = cellStyle.forecolor || '0,0,0'
        const rgbParts = currentColor.value.split(',')
        selectedColor.value = rgbParts.length === 3 ? rgbToHex(parseInt(rgbParts[0]), parseInt(rgbParts[1]), parseInt(rgbParts[2])) : '#000000'
      }
    }
  }
}

function rgbToHex(r: number, g: number, b: number) {
  return `#${[r, g, b].map((x) => {
    const hex = x.toString(16)
    return hex.length === 1 ? `0${hex}` : hex
  }).join('').toUpperCase()}`
}

function refresh(startRow: number, startCol: number, endRow: number, endCol: number) {
  if (startRow > endRow) {
    ;[startRow, endRow] = [endRow, startRow]
  }

  if (startCol > endCol) {
    ;[startCol, endCol] = [endCol, startCol]
  }

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const cellStyle = cellDef.cellStyle
      currentColor.value = cellStyle.forecolor || '0,0,0'

      const rgbParts = currentColor.value.split(',')
      selectedColor.value = rgbParts.length === 3 ? rgbToHex(parseInt(rgbParts[0]), parseInt(rgbParts[1]), parseInt(rgbParts[2])) : '#000000'

      return
    }
  }
}
</script>

<template>
  <div class="u-inline">
    <u-color-picker
      v-model="selectedColor"
      :before-toggle="checkSelection"
      @change="onColorChange"
    >
      <u-button
        type="info"
        native-type="button"
        class="font-color-btn"
        :title="$t('tools.foreColor.color')"
      >
        <div class="icon-wrapper">
          <i class="iconfont icon-font-color" />
          <span class="color-indicator" :style="{ backgroundColor: displayColor }" />
        </div>
      </u-button>
    </u-color-picker>
  </div>
</template>

<style scoped>
.font-color-btn {
  border: none;
  padding: 0 10px;
}

.icon-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.icon-wrapper .iconfont {
  font-size: 16px;
  line-height: 1;
}

.color-indicator {
  width: 14px;
  height: 3px;
  margin-top: 1px;
  border-radius: 1px;
}
</style>
