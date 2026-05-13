declare const requestAdapter: {
  setRequest(request: any): void
  setRequestHandler(handler: any): void
  setBaseURL(url: string): void
  setDefaultHeaders(headers: Record<string, string>): void
  addRequestInterceptor(onFulfilled: any, onRejected: any): number
  addResponseInterceptor(onFulfilled: any, onRejected: any): number
  request(config: any): Promise<any>
  post(url: string, data?: any, config?: any): Promise<any>
  get(url: string, config?: any): Promise<any>
}
export default requestAdapter
