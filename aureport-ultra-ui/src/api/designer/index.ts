import type { DatasourceItem, ProviderItem, ReportFile } from '@/types'
import { get, post } from '@/utils/request'

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
    }
    else {
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

function toFormParams(data: Record<string, unknown>): URLSearchParams {
  const params = new URLSearchParams()
  for (const key in data) {
    params.append(key, String(data[key]))
  }
  return params
}

export function buildFields(parameters: Record<string, unknown>): Promise<unknown> {
  return post('/datasource/buildFields', toFormParams(parameters), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function scriptValidation(content: string): Promise<unknown> {
  return post('/designer/scriptValidation', toFormParams({ content }), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function conditionScriptValidation(content: string): Promise<unknown> {
  return post('/designer/conditionScriptValidation', toFormParams({ content }), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function buildDatabaseTables(parameters: Record<string, unknown>): Promise<unknown> {
  return post('/datasource/buildDatabaseTables', toFormParams(parameters), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export function buildJdbcFields(parameters: Record<string, unknown>): Promise<unknown> {
  return post('/datasource/buildFields', toFormParams(parameters), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
}

export interface MethodInfo {
  method: string
  returnClass: string | null
}

export function loadMethods(beanId: string): Promise<MethodInfo[]> {
  return get('/datasource/loadMethods', { params: { beanId } })
}

export function buildClass(clazz: string): Promise<unknown> {
  return get('/datasource/buildClass', { params: { clazz } })
}

export function parseDatasetName(expr: string): Promise<unknown> {
  return post('/designer/parseDatasetName', toFormParams({ expr }), {
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
