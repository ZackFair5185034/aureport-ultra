import { get, post } from '@/utils/request'

function buildQueryString(params: Record<string, string | number | boolean>): string {
  if (!params || typeof params !== 'object')
    return ''
  const pairs: string[] = []
  for (const key in params) {
    if (params[key] !== undefined && params[key] !== null) {
      pairs.push(`${key}=${params[key]}`)
    }
  }

  return pairs.join('&')
}

export function loadHtml(params: Record<string, string | number | boolean>): Promise<unknown> {
  return get('/html/loadHtml', { params })
}

export function loadPrintPages(formData: Record<string, unknown>): Promise<unknown> {
  return post('/html/loadPrintPages', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function loadPagePaper(formData: Record<string, unknown>): Promise<unknown> {
  return post('/html/loadPagePaper', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function pdfNewPaging(formData: Record<string, unknown>): Promise<unknown> {
  return post('/pdf/newPaging', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function getPdfPreviewUrl(params: Record<string, string | number | boolean>, refreshIndex = 0): string {
  const queryString = buildQueryString(params)
  const separator = queryString ? '&' : ''
  return `/api/pdf/show?${queryString}${separator}_r=${refreshIndex}`
}

export function getPdfDirectPrintUrl(params: Record<string, string | number | boolean>, index = 0): string {
  const queryString = buildQueryString(params)
  const separator = queryString ? '&' : ''
  return `/api/pdf/show?${queryString}${separator}_i=${index}`
}

export function getPdfExportUrl(params: Record<string, string | number | boolean>): string {
  const queryString = buildQueryString(params)
  return queryString ? `/api/pdf/build?${queryString}` : '/api/pdf/build'
}

export function getWordExportUrl(params: Record<string, string | number | boolean>): string {
  const queryString = buildQueryString(params)
  return queryString ? `/api/word/build?${queryString}` : '/api/word/build'
}

export function getExcelExportUrl(params: Record<string, string | number | boolean>): string {
  const queryString = buildQueryString(params)
  return queryString ? `/api/excel/build?${queryString}` : '/api/excel/preview'
}

export function getExcelPagingExportUrl(params: Record<string, string | number | boolean>): string {
  const queryString = buildQueryString(params)
  return queryString ? `/api/excel/paging?${queryString}` : '/api/excel/paging'
}

export function getExcelSheetPagingExportUrl(params: Record<string, string | number | boolean>): string {
  const queryString = buildQueryString(params)
  return queryString ? `/api/excel/sheet?${queryString}` : '/api/excel/sheet'
}

export function loadReportData(params: Record<string, string | number | boolean>): Promise<unknown> {
  const formData = new FormData()
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      formData.append(key, String(value))
    }
  }

  return post('/html/loadData', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function storeChartData(formData: FormData): Promise<unknown> {
  return post('/chart/storeData', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
