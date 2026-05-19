<script setup lang="ts">
import Handsontable from 'handsontable'

import { useI18n } from 'vue-i18n'
import { loadReport } from '@/api/designer'
import Context from '@/components/Context.js'
import { getLibMode } from '@/lib/navigator'
import { useReportStore } from '@/stores/report'
import { showAlert } from '@/utils/comnon'
import { addRowHeader } from '@/utils/contextActions'
import * as utils from '@/utils/table'
import TableManager from './manager.js'
import { afterRenderer } from './utils/CellRenderer'
import buildMenuConfigure from './utils/ContextMenu'
import { renderRowHeader } from './utils/HeaderUtils'

defineOptions({ name: 'ContentTable' })

const props = withDefaults(defineProps<{
  reportPath?: string
}>(), {
  reportPath: '',
})
const emit = defineEmits<{
  (e: 'cell-selected', data: { rowIndex: number, colIndex: number, row2Index: number, col2Index: number }): void
  (e: 'context-created', context: unknown): void
  (e: 'navigate', data: unknown): void
  (e: 'save', data: unknown): void
  (e: 'error', err: unknown): void
}>()
const { t } = useI18n()
const store = useReportStore()

const contentTableEl = ref<HTMLDivElement | null>(null)

const hot = ref<any>(null)
const reportDef = ref<any>(null)
const cellsMap = ref<Map<string, unknown>>(new Map())
const context = ref<any>(null)
const internalReportPath = ref(props.reportPath)

const isLibMode = getLibMode()

watch(() => props.reportPath, (val) => {
  internalReportPath.value = val
  if (val) {
    loadFile(val, handleReportLoaded)
  }
})

onMounted(() => {
  initTable()
})

onBeforeUnmount(() => {
  if (hot.value) {
    hot.value.destroy()
  }
})

function initTable() {
  utils.undoManager.setLimit(100)
  initHandsontable()

  let filePath = ''
  if (isLibMode) {
    filePath = internalReportPath.value || 'classpath:template/template.ureport.xml'
  }
  else {
    filePath = utils.getParameter('reportPath') || ''
    if (!filePath || filePath === '') {
      filePath = 'classpath:template/template.ureport.xml'
    }
  }

  if (filePath && filePath !== 'classpath:template/template.ureport.xml') {
    store.setSaveStatus(true)
  }

  loadFile(filePath, handleReportLoaded)
}

function initHandsontable() {
  const el = contentTableEl.value
  if (!el)
    return

  hot.value = new Handsontable(el, {
    startCols: 1,
    startRows: 1,
    fillHandle: {
      autoInsertRow: false,
    },
    colHeaders: true,
    rowHeaders: true,
    autoColumnSize: false,
    autoRowSize: false,
    manualColumnResize: true,
    manualRowResize: true,
    // @ts-expect-error -- maxColsNumber 是自定义属性
    maxColsNumber: 700,
    outsideClickDeselects: false,
    width: '100%' as unknown as number,
    height: '100%' as unknown as number,
  })

  TableManager.set(hot.value)

  hot.value.addHook('afterRenderer', afterRenderer)
  bindRowResizeEvent()
  bindColumnResizeEvent()
  bindSelectionEvent()
}

function bindRowResizeEvent() {
  hot.value.addHook('afterRowResize', function (this: any, currentRow: number, newSize: number) {
    let rowHeights = this.getSettings().rowHeights
    let oldRowHeights = rowHeights.concat([])
    const newRowHeights = rowHeights.concat([])
    newRowHeights.splice(currentRow, 1, newSize)
    this.updateSettings({
      rowHeights: newRowHeights,
      manualRowResize: newRowHeights,
    })
    utils.undoManager.add({
      redo() {
        rowHeights = this.getSettings().rowHeights
        oldRowHeights = rowHeights.concat([])
        newRowHeights.splice(currentRow, 1, newSize)
        this.updateSettings({
          rowHeights: newRowHeights,
          manualRowResize: newRowHeights,
        })
        utils.setDirty()
      },
      undo() {
        this.updateSettings({
          rowHeights: oldRowHeights,
          manualRowResize: oldRowHeights,
        })
        utils.setDirty()
      },
    })
    utils.setDirty()
  })
}

function bindColumnResizeEvent() {
  hot.value.addHook('afterColumnResize', function (this: any, currentColumn: number, newSize: number) {
    let colWidths = this.getSettings().colWidths
    let newColWidths = colWidths.concat([])
    let oldColWidths = colWidths.concat([])
    newColWidths.splice(currentColumn, 1, newSize)
    this.updateSettings({
      colWidths: newColWidths,
      manualColumnResize: newColWidths,
    })
    utils.undoManager.add({
      redo() {
        colWidths = this.getSettings().colWidths
        newColWidths = colWidths.concat([])
        oldColWidths = colWidths.concat([])
        newColWidths.splice(currentColumn, 1, newSize)
        this.updateSettings({
          colWidths: newColWidths,
          manualColumnResize: newColWidths,
        })
        utils.setDirty()
      },
      undo() {
        this.updateSettings({
          colWidths: oldColWidths,
          manualColumnResize: oldColWidths,
        })
        utils.setDirty()
      },
    })
    utils.setDirty()
  })
}

function bindSelectionEvent() {
  // @ts-expect-error -- Handsontable.hooks.add is valid at runtime
  Handsontable.hooks.add('afterSelectionEnd', (rowIndex: number, colIndex: number, row2Index: number, col2Index: number) => {
    emit('cell-selected', { rowIndex, colIndex, row2Index, col2Index })
  }, hot.value)
}

function handleReportLoaded() {
  context.value = new Context({
    reportDef: reportDef.value,
    cellsMap: cellsMap.value,
  })

  store.setContext(context.value)
  emit('context-created', context.value)
  processRowHeaders()
}

function processRowHeaders() {
  if (reportDef.value && reportDef.value.rows) {
    const rows = reportDef.value.rows
    for (const row of rows) {
      const band = row.band
      if (!band)
        continue
      addRowHeader(row.rowNumber - 1, band)
    }

    renderRowHeader(hot.value)
  }
}

async function loadFile(filePath: string, callback: (...args: unknown[]) => void) {
  try {
    const formData = new FormData()
    formData.append('filePath', filePath)
    const rDef: any = await loadReport(formData)

    reportDef.value = rDef
    _buildReportData(rDef)
    hot.value.render()

    buildMenu()

    if (callback) {
      callback(rDef)
    }

    if (filePath === 'classpath:template/template.ureport.xml') {
      store.setFileName(`${t('table.report.tip')}`)
    }
    else {
      store.setFileName(filePath)
    }

    const masterElement = document.querySelector('.ht_master') as HTMLElement | null
    if (masterElement) {
      masterElement.style.background = rDef.paper?.bgImage ? `url(${rDef.paper.bgImage}) 50px 26px no-repeat` : 'transparent'
    }
  }
  catch (error: any) {
    emit('error', error)
    if (error.msg) {
      showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
    }
    else {
      showAlert(`${t('table.report.load')}${filePath}${t('table.report.fail')}`)
    }
  }
}

function _buildReportData(data: any) {
  cellsMap.value.clear()
  const rows = data.rows
  const rowHeights: number[] = []
  for (const row of rows) {
    const height = row.height
    rowHeights.push(utils.pointToPixel(height))
  }

  const columns = data.columns
  const colWidths: number[] = []
  for (const col of columns) {
    const width = col.width
    colWidths.push(utils.pointToPixel(width))
  }

  const dataCellsMap = data.cellsMap
  const dataArray: string[][] = []
  const mergeCells: Array<{ rowspan: number, colspan: number, row: number, col: number }> = []
  for (const row of rows) {
    const rowData: string[] = []
    for (const col of columns) {
      const key = `${row.rowNumber},${col.columnNumber}`
      const cell = dataCellsMap[key]
      if (cell) {
        cellsMap.value.set(key, cell)
        rowData.push(cell.value?.value || '')
        let rowspan = cell.rowSpan
        let colspan = cell.colSpan
        if (rowspan > 0 || colspan > 0) {
          if (rowspan === 0)
            rowspan = 1
          if (colspan === 0)
            colspan = 1
          mergeCells.push({
            rowspan,
            colspan,
            row: row.rowNumber - 1,
            col: col.columnNumber - 1,
          })
        }
      }
      else {
        rowData.push('')
      }
    }

    dataArray.push(rowData)
  }

  hot.value.loadData(dataArray)
  hot.value.updateSettings({
    colWidths,
    rowHeights,
    mergeCells,
    readOnly: true,
  })
}

function buildMenu() {
  hot.value.updateSettings({
    contextMenu: buildMenuConfigure(),
  })
}

function getReportData() {
  return utils.tableToXml(context.value)
}

function saveReport() {
  emit('save', { data: getReportData() })
}
</script>

<template>
  <div class="ud-page">
    <div ref="contentTableEl" class="ud-table" />
  </div>
</template>

<style scoped>
.ud-page {
  position: relative;
  display: flex;
  flex: 1;
  overflow: hidden;
  background: white;
}

.ud-slider {
  height: 200px;
  width: 50px;
}

.ud-table {
  width: 100%;
  min-height: 500px;
}
</style>
