import MessageBox from '@/components/messagebox/instance'
import { $t } from '@/locales'
import request from '@/utils/request'

/**
 * 提示
 * @param message
 * @param options
 * @returns {Promise<unknown>}
 */
export function showAlert(message: string, options?: Record<string, unknown>): Promise<unknown> {
  return MessageBox.alert(message, $t('components.message.info'), options)
}

/**
 * 确认
 * @param message
 * @param options
 * @returns {Promise<unknown>}
 */
export function showConfirm(message: string, options?: Record<string, unknown>): Promise<unknown> {
  return MessageBox.confirm(message, $t('components.message.info'), options)
}

/**
 * 判断当前设备是否为移动设备
 */
export function isMobile(): boolean {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

/**
 * Blob 下载文件
 * @param url 请求 URL
 * @param params 查询参数
 * @param defaultFilename 默认文件名
 */
export async function downloadBlob(url: string, params: Record<string, unknown>, defaultFilename: string): Promise<void> {
  const queryString = buildQueryString(params)
  const fullUrl = queryString ? `${url}?${queryString}` : url

  const response = await request.get(fullUrl, {
    responseType: 'blob',
  })

  const blob = response.data || response
  const contentDisposition = response.headers?.['content-disposition']
  const filename = extractFilename(contentDisposition, defaultFilename)

  const downloadUrl = URL.createObjectURL(blob as Blob)
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = filename
  document.body.append(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(downloadUrl)
}

/**
 * 构建查询字符串
 */
export function buildQueryString(params: Record<string, unknown>): string {
  if (!params || typeof params !== 'object') {
    return ''
  }

  const pairs: string[] = []
  for (const key in params) {
    if (Object.hasOwn(params, key) && params[key] !== undefined && params[key] !== null) {
      pairs.push(`${key}=${params[key]}`)
    }
  }

  return pairs.join('&')
}

/**
 * 从 Content-Disposition 头中提取文件名
 */
function extractFilename(contentDisposition: string | undefined, defaultName: string): string {
  if (!contentDisposition) {
    return defaultName
  }

  const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
  const matches = filenameRegex.exec(contentDisposition)
  if (matches && matches[1]) {
    let filename = matches[1].replaceAll(/['"]/g, '')
    try {
      filename = decodeURIComponent(filename)
    }
    catch {
      // 解码失败则使用原始值
    }

    return filename
  }

  return defaultName
}
