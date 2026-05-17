<script setup lang="ts">
// @ts-nocheck
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ButtonGroup from '@/components/button-group/index.vue'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'FontFamilyTool' })

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null, colIndex: number | null, row2Index: number | null, col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null }),
})

const { t } = useI18n()

const currentFontFamily = ref('宋体')
const fontFamilies = ref([
  '宋体',
  '仿宋',
  '黑体',
  '楷体',
  '微软雅黑',
  'Arial',
  'Impact',
  'Times New Roman',
  'Comic Sans MS',
  'Courier New',
])

const menuItems = computed(() => {
  return fontFamilies.value.map(font => ({
    text: font,
    action: () => applyFontFamily(font),
  }))
})

watch(() => props.selectedCells, (newVal) => {
  if (newVal && newVal.rowIndex !== null && newVal.colIndex !== null) {
    refresh(newVal.rowIndex, newVal.colIndex, newVal.row2Index, newVal.col2Index)
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

function applyFontFamily(fontFamily: string) {
  if (!checkSelection())
    return

  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) { [startRow, endRow] = [endRow, startRow] }

  if (startCol > endCol) { [startCol, endCol] = [endCol, startCol] }

  const oldFontFamily = updateFontFamily(startRow, startCol, endRow, endCol, fontFamily)
  table.render()

  undoManager.add({
    redo: () => {
      updateFontFamily(startRow, startCol, endRow, endCol, fontFamily)
      table.render()
      setDirty()
    },
    undo: () => {
      restoreFontFamily(startRow, startCol, endRow, endCol, oldFontFamily)
      table.render()
      setDirty()
    },
  })

  setDirty()
}

function updateFontFamily(startRow: number, startCol: number, endRow: number, endCol: number, fontFamily: string) {
  const oldFontFamily: Record<string, string> = {}

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldFontFamily[`${i},${j}`] = newCellDef.cellStyle.fontFamily
      cellStyle.fontFamily = fontFamily
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        currentFontFamily.value = fontFamily
      }
    }
  }

  return oldFontFamily
}

function restoreFontFamily(startRow: number, startCol: number, endRow: number, endCol: number, oldFontFamily: Record<string, string>) {
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      cellStyle.fontFamily = oldFontFamily[`${i},${j}`]
      setCell(i, j, newCellDef)

      if (i === startRow && j === startCol) {
        currentFontFamily.value = cellStyle.fontFamily || '宋体'
      }
    }
  }
}

function refresh(startRow: number, startCol: number, endRow: number, endCol: number) {
  if (startRow > endRow) { [startRow, endRow] = [endRow, startRow] }

  if (startCol > endCol) { [startCol, endCol] = [endCol, startCol] }

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const cellStyle = cellDef.cellStyle
      const fontFamily = cellStyle.fontFamily || '宋体'
      currentFontFamily.value = fontFamily
      return
    }
  }
}
</script>

<template>
  <div class="u-inline">
    <ButtonGroup
      :buttonText="currentFontFamily"
      :showText="true"
      :title="$t('tools.font.font')"
      customClass="font-family-tool-dropdown"
      :menuItems="menuItems"
    />
  </div>
</template>

<style scoped>
.font-family-tool-dropdown :deep(.button-text) {
  display: inline-block;
  vertical-align: top;
  width: 28px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-left: 0;
}
</style>
