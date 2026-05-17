import type { CellDef, ReportContext } from '@/types'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useReportStore = defineStore('report', () => {
  const context = ref<ReportContext | null>(null)
  const fileName = ref('')
  const saveBtnDisable = ref(true)
  const saveStatus = ref(false)

  const hasContext = computed(() => context.value !== null)
  const displayName = computed(() => fileName.value.replace('.ureport.xml', ''))

  function setContext(ctx: ReportContext) {
    context.value = ctx
  }

  function clearContext() {
    context.value = null
    fileName.value = ''
    saveBtnDisable.value = true
    saveStatus.value = false
  }

  function setFileName(name: string) {
    const suffix = '.ureport.xml'
    const pos = name.indexOf(suffix)
    if (pos !== -1) {
      name = name.slice(0, Math.max(0, pos))
    }

    fileName.value = decodeURI(name)
  }

  function setSaveBtnDisable(disable: boolean) {
    saveBtnDisable.value = disable
  }

  function setSaveStatus(status: boolean) {
    saveStatus.value = status
  }

  // CellsMap operations
  function addCell(cell: CellDef) {
    if (context.value?.cellsMap) {
      const key = `${cell.rowNumber},${cell.columnNumber}`
      context.value.cellsMap.set(key, cell)
    }
  }

  function removeCell(cell: CellDef) {
    if (context.value?.cellsMap) {
      const key = `${cell.rowNumber},${cell.columnNumber}`
      context.value.cellsMap.delete(key)
    }
  }

  function setCell(rowIndex: number, colIndex: number, cell: CellDef) {
    if (context.value?.cellsMap) {
      const key = `${rowIndex},${colIndex}`
      context.value.cellsMap.set(key, cell)
    }
  }

  function deleteCell(rowNumber: number, columnNumber: number) {
    if (context.value?.cellsMap) {
      const key = `${rowNumber},${columnNumber}`
      context.value.cellsMap.delete(key)
    }
  }

  function getCell(rowIndex: number, colIndex: number): CellDef | null {
    if (!context.value?.cellsMap)
      return null
    const key = `${rowIndex + 1},${colIndex + 1}`
    return context.value.cellsMap.get(key) || null
  }

  function getCellsMap(): Map<string, CellDef> | null {
    return context.value?.cellsMap || null
  }

  // RowHeaders operations
  function addRowHeader(row: number, band: string) {
    if (context.value?.rowHeaders) {
      const target = context.value.rowHeaders.find(h => h.rowNumber === row)
      if (target) {
        target.band = band as import('@/types').BandType
      }
      else {
        context.value.rowHeaders.push({ rowNumber: row, band: band as import('@/types').BandType })
      }
    }
  }

  function adjustInsertRowHeaders(row: number) {
    if (context.value?.rowHeaders) {
      for (const header of context.value.rowHeaders) {
        if (header.rowNumber >= row) {
          header.rowNumber += 1
        }
      }
    }
  }

  function adjustDelRowHeaders(row: number) {
    if (context.value?.rowHeaders) {
      const idx = context.value.rowHeaders.findIndex(h => h.rowNumber === row)
      if (idx !== -1) {
        context.value.rowHeaders.splice(idx, 1)
      }
    }
  }

  function getCellName(rowIndex: number | null, colIndex: number): string {
    if (!context.value?.LETTERS)
      return ''
    if (rowIndex != null) {
      return context.value.LETTERS[colIndex] + (rowIndex + 1)
    }

    return context.value.LETTERS[colIndex]
  }

  // ReportDef operations
  function updateReportDef(reportDef: import('@/types').ReportDef) {
    if (context.value) {
      context.value.reportDef = reportDef
    }
  }

  function updateProperty(property: string, value: unknown) {
    if (context.value) {
      (context.value as Record<string, unknown>)[property] = value
    }
  }

  return {
    context,
    fileName,
    saveBtnDisable,
    saveStatus,
    hasContext,
    displayName,
    setContext,
    clearContext,
    setFileName,
    setSaveBtnDisable,
    setSaveStatus,
    addCell,
    removeCell,
    setCell,
    deleteCell,
    getCell,
    getCellsMap,
    addRowHeader,
    adjustInsertRowHeaders,
    adjustDelRowHeaders,
    getCellName,
    updateReportDef,
    updateProperty,
  }
})
