<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import ButtonGroup from '@/components/button-group/index.vue'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'
import CustomBorderDialog from '@/views/report/designer/resource-panel/property-panel/custom-border-dialog/index.vue'

defineOptions({ name: 'BorderTool' })

const { t } = useI18n()

const customBorderVisible = ref(false)
const customBorderData = ref({
  topBorder: { style: 'solid', width: 1, color: '#000000' },
  bottomBorder: { style: 'solid', width: 1, color: '#000000' },
  leftBorder: { style: 'solid', width: 1, color: '#000000' },
  rightBorder: { style: 'solid', width: 1, color: '#000000' },
})

const menuItems = computed(() => [
  {
    text: t('tools.border.allLine'),
    icon: 'iconfont icon-full-border',
    action: () => handleFullBorder(),
  },
  {
    text: t('tools.border.noBorder'),
    icon: 'iconfont icon-no-border',
    action: () => handleNoBorder(),
  },
  {
    text: t('tools.border.leftBorder'),
    icon: 'iconfont icon-left-border',
    action: () => handleLeftBorder(),
  },
  {
    text: t('tools.border.rightBorder'),
    icon: 'iconfont icon-right-border',
    action: () => handleRightBorder(),
  },
  {
    text: t('tools.border.topBorder'),
    icon: 'iconfont icon-top-border',
    action: () => handleTopBorder(),
  },
  {
    text: t('tools.border.bottomBorder'),
    icon: 'iconfont icon-bottom-border',
    action: () => handleBottomBorder(),
  },
  {
    text: t('tools.border.customBorder'),
    icon: 'iconfont icon-full-border',
    action: () => handleCustomBorder(),
  },
])

function checkSelection() {
  const hot = TableManager.get()
  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    showAlert(t('selectTargetCellFirst'))
    return false
  }

  return true
}

function handleFullBorder() {
  if (!checkSelection())
    return

  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) {
    [startRow, endRow] = [endRow, startRow]
  }

  if (startCol > endCol) {
    [startCol, endCol] = [endCol, startCol]
  }

  const newBorder = { width: 1, color: '0,0,0', style: 'solid' }
  const oldBorderStyle = updateBorderStyles(startRow, startCol, endRow, endCol, newBorder)
  table.render()

  undoManager.add({
    redo: () => {
      updateBorderStyles(startRow, startCol, endRow, endCol, newBorder)
      table.render()
      setDirty()
    },
    undo: () => {
      updateOldBorderStyles(startRow, startCol, endRow, endCol, oldBorderStyle)
      setDirty()
    },
  })

  setDirty()
}

function handleNoBorder() {
  if (!checkSelection())
    return

  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) {
    [startRow, endRow] = [endRow, startRow]
  }

  if (startCol > endCol) {
    [startCol, endCol] = [endCol, startCol]
  }

  const newBorder = ''
  const oldBorderStyle = updateBorderStyles(startRow, startCol, endRow, endCol, newBorder)
  table.render()

  undoManager.add({
    redo: () => {
      updateBorderStyles(startRow, startCol, endRow, endCol, newBorder)
      table.render()
      setDirty()
    },
    undo: () => {
      updateOldBorderStyles(startRow, startCol, endRow, endCol, oldBorderStyle)
      setDirty()
    },
  })

  setDirty()
}

function handleLeftBorder() {
  applyBorder('left')
}

function handleRightBorder() {
  applyBorder('right')
}

function handleTopBorder() {
  applyBorder('top')
}

function handleBottomBorder() {
  applyBorder('bottom')
}

function applyBorder(target: string) {
  if (!checkSelection())
    return

  const table = TableManager.get()
  const selected = table.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) {
    [startRow, endRow] = [endRow, startRow]
  }

  if (startCol > endCol) {
    [startCol, endCol] = [endCol, startCol]
  }

  const newBorder = { width: 1, color: '0,0,0', style: 'solid' }
  const oldBorderStyle = updateBorderStyles(startRow, startCol, endRow, endCol, newBorder, target)
  table.render()

  undoManager.add({
    redo: () => {
      updateBorderStyles(startRow, startCol, endRow, endCol, newBorder, target)
      table.render()
      setDirty()
    },
    undo: () => {
      updateOldBorderStyles(startRow, startCol, endRow, endCol, oldBorderStyle)
      setDirty()
    },
  })

  setDirty()
}

function handleCustomBorder() {
  if (!checkSelection())
    return

  const selected = TableManager.get().getSelected()
  const [startRow, startCol] = selected[0]
  const cellDef = getCell(startRow, startCol)

  const defaultBorderStyle = { style: 'solid', width: 1, color: '#000000' }

  const convertColorToHex = (borderStyle: any) => {
    if (!borderStyle)
      return { ...defaultBorderStyle }
    const result = { ...borderStyle }
    if (typeof result.color === 'string' && result.color.includes(',')) {
      const rgb = result.color.split(',')
      result.color = rgbToHex(parseInt(rgb[0]), parseInt(rgb[1]), parseInt(rgb[2]))
    }

    return result
  }

  if (cellDef && cellDef.cellStyle) {
    customBorderData.value.topBorder = convertColorToHex(cellDef.cellStyle.topBorder)
    customBorderData.value.bottomBorder = convertColorToHex(cellDef.cellStyle.bottomBorder)
    customBorderData.value.leftBorder = convertColorToHex(cellDef.cellStyle.leftBorder)
    customBorderData.value.rightBorder = convertColorToHex(cellDef.cellStyle.rightBorder)
  }
  else {
    customBorderData.value.topBorder = { ...defaultBorderStyle }
    customBorderData.value.bottomBorder = { ...defaultBorderStyle }
    customBorderData.value.leftBorder = { ...defaultBorderStyle }
    customBorderData.value.rightBorder = { ...defaultBorderStyle }
  }

  customBorderVisible.value = true
}

function handleSave(topBorder: any, bottomBorder: any, leftBorder: any, rightBorder: any) {
  const selected = TableManager.get().getSelected()
  const [startRow, startCol, endRow, endCol] = selected[0]

  let oldBorderStyle = updateCustomBorderStyle(startRow, startCol, endRow, endCol, leftBorder, rightBorder, topBorder, bottomBorder)

  undoManager.add({
    redo: () => {
      oldBorderStyle = updateCustomBorderStyle(startRow, startCol, endRow, endCol, leftBorder, rightBorder, topBorder, bottomBorder)
      setDirty()
    },
    undo: () => {
      updateOldBorderStyles(startRow, startCol, endRow, endCol, oldBorderStyle)
      setDirty()
    },
  })

  setDirty()
}

function rgbToHex(r: number, g: number, b: number) {
  return `#${[r, g, b].map((x) => {
    const hex = x.toString(16)
    return hex.length === 1 ? `0${hex}` : hex
  }).join('')}`
}

function updateCustomBorderStyle(startRow: number, startCol: number, endRow: number, endCol: number, leftBorderStyle: any, rightBorderStyle: any, topBorderStyle: any, bottomBorderStyle: any) {
  const hot = TableManager.get()
  let left = leftBorderStyle
  let right = rightBorderStyle
  let top = topBorderStyle
  let bottom = bottomBorderStyle

  if (leftBorderStyle.style === 'none')
    left = ''
  if (rightBorderStyle.style === 'none')
    right = ''
  if (topBorderStyle.style === 'none')
    top = ''
  if (bottomBorderStyle.style === 'none')
    bottom = ''

  const oldBorderStyle: Record<string, any> = {}

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldBorderStyle[`${i},${j}`] = {
        leftBorder: cellStyle.leftBorder,
        rightBorder: cellStyle.rightBorder,
        topBorder: cellStyle.topBorder,
        bottomBorder: cellStyle.bottomBorder,
      }

      cellStyle.leftBorder = cloneBorder(left)
      cellStyle.rightBorder = cloneBorder(right)
      cellStyle.topBorder = cloneBorder(top)
      cellStyle.bottomBorder = cloneBorder(bottom)

      setCell(i, j, newCellDef)
    }
  }

  hot.render()
  return oldBorderStyle
}

function cloneBorder(border: any) {
  return border && border !== '' ? structuredClone(border) : border
}

function updateOldBorderStyles(startRow: number, startCol: number, endRow: number, endCol: number, oldBorderStyle: Record<string, any>) {
  const hot = TableManager.get()

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const oldBorder = oldBorderStyle[`${i},${j}`]
      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle

      cellStyle.leftBorder = oldBorder.leftBorder || ''
      cellStyle.rightBorder = oldBorder.rightBorder || ''
      cellStyle.topBorder = oldBorder.topBorder || ''
      cellStyle.bottomBorder = oldBorder.bottomBorder || ''

      setCell(i, j, newCellDef)
    }
  }

  hot.render()
}

function updateBorderStyles(startRow: number, startCol: number, endRow: number, endCol: number, newBorder: any, target?: string) {
  const oldStyle: Record<string, any> = {}
  const hot = TableManager.get()

  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue

      const newCellDef = deepCopy(cellDef)
      const cellStyle = newCellDef.cellStyle
      oldStyle[`${i},${j}`] = {
        leftBorder: cellStyle.leftBorder,
        rightBorder: cellStyle.rightBorder,
        topBorder: cellStyle.topBorder,
        bottomBorder: cellStyle.bottomBorder,
      }

      if (target) {
        switch (target) {
          case 'left': {
            cellStyle.leftBorder = newBorder
            cellStyle.rightBorder = ''
            cellStyle.topBorder = ''
            cellStyle.bottomBorder = ''

            break
          }

          case 'right': {
            cellStyle.rightBorder = newBorder
            cellStyle.leftBorder = ''
            cellStyle.topBorder = ''
            cellStyle.bottomBorder = ''

            break
          }

          case 'top': {
            cellStyle.topBorder = newBorder
            cellStyle.leftBorder = ''
            cellStyle.rightBorder = ''
            cellStyle.bottomBorder = ''

            break
          }

          case 'bottom': {
            cellStyle.bottomBorder = newBorder
            cellStyle.leftBorder = ''
            cellStyle.rightBorder = ''
            cellStyle.topBorder = ''

            break
          }
 // No default
        }
      }
      else {
        cellStyle.leftBorder = newBorder
        cellStyle.rightBorder = newBorder
        cellStyle.topBorder = newBorder
        cellStyle.bottomBorder = newBorder
      }

      setCell(i, j, newCellDef)
    }
  }

  return oldStyle
}
</script>

<template>
  <div class="u-inline">
    <ButtonGroup
      iconClass="iconfont icon-no-border"
      :title="$t('tools.border.borderLine')"
      customClass="border-tool-dropdown"
      :menuItems="menuItems"
    />

    <!-- 自定义边框对话框 -->
    <CustomBorderDialog
      :visible="customBorderVisible"
      :topBorder="customBorderData.topBorder"
      :bottomBorder="customBorderData.bottomBorder"
      :leftBorder="customBorderData.leftBorder"
      :rightBorder="customBorderData.rightBorder"
      @close="customBorderVisible = false"
      @save="handleSave"
    />
  </div>
</template>

<style scoped>
</style>
