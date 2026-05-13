import { post, get } from '@/utils/request'
import type { ProviderItem, ReportFile, DatasourceItem } from '@/types'

export function loadReport(formData: FormData): Promise<unknown> {
  return post('/designer/loadReport', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function testConnection(formData: FormData): Promise<unknown> {
  return post('/datasource/testConnection', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function previewData(parameters: Record<string, unknown>): Promise<unknown> {
  const formData = new URLSearchParams()
  for (const key in parameters) {
    const value = parameters[key]
    if (typeof value === 'object' && value !== null) {
      formData.append(key, JSON.stringify(value))
    } else {
      formData.append(key, String(value))
    }
  }
  return post('/datasource/previewData', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function loadBuildinDatasources(): Promise<DatasourceItem[]> {
  return get('/datasource/loadBuildinDatasources')
}

export function buildFields(parameters: Record<string, unknown>): Promise<unknown> {
  const formData = new FormData()
  for (const key in parameters) {
    formData.append(key, String(parameters[key]))
  }
  return post('/datasource/buildFields', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function scriptValidation(content: string): Promise<unknown> {
  const formData = new FormData()
  formData.append('content', content)
  return post('/designer/scriptValidation', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function conditionScriptValidation(content: string): Promise<unknown> {
  const formData = new FormData()
  formData.append('content', content)
  return post('/designer/conditionScriptValidation', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function buildDatabaseTables(parameters: Record<string, unknown>): Promise<unknown> {
  const formData = new FormData()
  for (const key in parameters) {
    formData.append(key, String(parameters[key]))
  }
  return post('/datasource/buildDatabaseTables', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function buildJdbcFields(parameters: Record<string, unknown>): Promise<unknown> {
  const formData = new FormData()
  for (const key in parameters) {
    formData.append(key, String(parameters[key]))
  }
  return post('/datasource/buildFields', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function loadMethods(beanId: string): Promise<unknown> {
  return get('/datasource/loadMethods', { params: { beanId } })
}

export function buildClass(clazz: string): Promise<unknown> {
  return get('/datasource/buildClass', { params: { clazz } })
}

export function parseDatasetName(expr: string): Promise<unknown> {
  const formData = new FormData()
  formData.append('expr', expr)
  return post('/designer/parseDatasetName', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function saveReportFile(file: string, content: string): Promise<unknown> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('content', content)
  return post('/designer/saveReportFile', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deleteReportFile(file: string): Promise<unknown> {
  const formData = new FormData()
  formData.append('file', file)
  return post('/designer/deleteReportFile', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function savePreviewFile(fileName: string, content: string): Promise<unknown> {
  const formData = new FormData()
  formData.append('content', content)
  formData.append('fileName', fileName)
  return post('/designer/savePreviewFile', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function loadReportProviders(): Promise<ProviderItem[]> {
  return get('/designer/loadReportProviders')
}

export function loadReportProvidersByPath(path: string): Promise<ProviderItem[]> {
  return get('/designer/loadReportProviders', { params: { path } })
}

export function importExcelFile(file: Blob): Promise<unknown> {
  const formData = new FormData()
  formData.append('_excel_file', file)
  return post('/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
