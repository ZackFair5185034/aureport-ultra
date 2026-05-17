<script setup lang="ts">
import Handsontable from 'handsontable'
import { useI18n } from 'vue-i18n'
import imageIcon from '@/assets/icons/image.svg'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { addCell, getCell, setCell } from '@/utils/contextActions'
import { buildNewCellDef, setDirty, undoManager } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'ImageTool' })

const { t } = useI18n()

function checkSelection() {
  const hot = TableManager.get()
  const selected = hot.getSelected()
  if (!selected || selected.length === 0) {
    showAlert(t('selectTargetCellFirst'))
    return false
  }

  return true
}

function handleClick() {
  if (!checkSelection())
    return

  const hot = TableManager.get()
  const selected = hot.getSelected()
  let [startRow, startCol, endRow, endCol] = selected[0]

  if (startRow > endRow) { [startRow, endRow] = [endRow, startRow] }

  if (startCol > endCol) { [startCol, endCol] = [endCol, startCol] }

  let oldCellDef = getCell(startRow, startCol)
  let oldCellData = hot.getDataAtCell(startRow, startCol)
  let newCellDef = buildNewCellDef(startRow + 1, startCol + 1)
  newCellDef.value = {
    type: 'image',
    source: 'text',
    value: '',
  }

  addCell(newCellDef as any)
  const imagePath = imageIcon

  const td = hot.getCell(startRow, startCol)
  if (td) {
    td.innerHTML = ''
    const img = document.createElement('img')
    img.src = imagePath
    img.width = 20
    td.append(img)
  }

  setDirty()
  Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)

  undoManager.add({
    redo: () => {
      oldCellDef = deepCopy(getCell(startRow, startCol))
      oldCellData = hot.getDataAtCell(startRow, startCol)
      newCellDef = buildNewCellDef(startRow + 1, startCol + 1)
      newCellDef.value = {
        type: 'image',
        source: 'text',
        value: '',
      }
      addCell(newCellDef as any)
      hot.setDataAtCell(startRow, startCol, '')
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
    undo: () => {
      if (oldCellDef) {
        const newOldCellDef = deepCopy(oldCellDef)
        addCell({ i: startRow, j: startCol, cellDef: newOldCellDef } as any)
      }
      else {
        setCell(startRow, startCol, null as any)
      }

      hot.setDataAtCell(startRow, startCol, oldCellData)
      hot.render()
      setDirty()
      Handsontable.hooks.run(hot, 'afterSelectionEnd', startRow, startCol, endRow, endCol)
    },
  })
}
</script>

<template>
  <u-button
    :title="$t('tools.image.title')"
    type="info"
    class="info-button"
    icon="icon-image"
    @click="handleClick"
  />
</template>

<style scoped>
</style>
