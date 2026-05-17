import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

request.interceptors.request.use((config) => {
  return config
}, (error) => {
  return Promise.reject(error)
})

request.interceptors.response.use((response: AxiosResponse) => {
  if (response.status !== 200) {
    throw new Error('请求异常')
  }

  return response
}, (error) => {
  console.error('API Error:', error)
  return Promise.reject(error)
})

function dealAxiosResult<T>(res: AxiosResponse<T> | T): Promise<T> {
  const realRes = (res as AxiosResponse<T>).data ?? res
  if ((res as AxiosResponse).request?.responseType === 'blob') {
    return Promise.resolve(res as T)
  }

  return Promise.resolve(realRes as T)
}

export async function post<T = unknown>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> {
  const res = await request.post<T>(url, data, config)
  return dealAxiosResult(res)
}

export async function get<T = unknown>(
  url: string,
  config?: AxiosRequestConfig,
): Promise<T> {
  const res = await request.get<T>(url, config)
  return dealAxiosResult(res)
}

export default request
