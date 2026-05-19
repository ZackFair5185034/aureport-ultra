import type { DatasourceItem, ProviderItem, ReportFile } from '@/types'
import { get, post } from '@/utils/request'

export function loadReport(filePath: string): Promise<unknown> {
  return get('/designer/loadReport', { params: { filePath } })
}

export function testConnection(formData: FormData): Promise<unknown> {
  return post('/datasource/testConnection', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function previewData(parameters: Record<string, unknown>): Promise<unknown> {
  const params: Record<string, string> = {}
  for (const key in parameters) {
    const value = parameters[key]
    params[key] = typeof value === 'object' && value !== null
      ? JSON.stringify(value)
      : String(value)
  }
  return get('/datasource/previewData', { params })
}

export function loadBuildinDatasources(): Promise<DatasourceItem[]> {
  return get('/datasource/loadBuildinDatasources')
}

export interface FieldInfo {
  name: string
  label?: string
  type?: string
  children?: FieldInfo[]
}

export function buildFields(parameters: Record<string, unknown>): Promise<unknown> {
  return get('/datasource/buildFields', { params: parameters })
}

export function buildClass(clazz: string): Promise<FieldInfo[]> {
  return get('/datasource/buildClass', { params: { clazz } })
}

export function scriptValidation(content: string): Promise<unknown> {
  return get('/designer/scriptValidation', { params: { content } })
}

export function conditionScriptValidation(content: string): Promise<unknown> {
  return get('/designer/conditionScriptValidation', { params: { content } })
}

export function buildDatabaseTables(parameters: Record<string, unknown>): Promise<unknown> {
  return get('/datasource/buildDatabaseTables', { params: parameters })
}

export function buildJdbcFields(parameters: Record<string, unknown>): Promise<unknown> {
  return get('/datasource/buildFields', { params: parameters })
}

export interface MethodInfo {
  method: string
  returnClass: string | null
}

export function loadMethods(beanId: string): Promise<MethodInfo[]> {
  return get('/datasource/loadMethods', { params: { beanId } })
}

export interface ReportBeanInfo {
  beanId: string
  name: string
  className: string
}

export function listReportBeans(): Promise<ReportBeanInfo[]> {
  return get('/report-beans')
}

export function parseDatasetName(expr: string): Promise<unknown> {
  return get('/designer/parseDatasetName', { params: { expr } })
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
