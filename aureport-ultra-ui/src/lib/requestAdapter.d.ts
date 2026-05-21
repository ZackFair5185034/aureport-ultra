declare const requestAdapter: {
  setBaseURL: (url: string) => void
  setDefaultHeaders: (headers: Record<string, string>) => void
}
export default requestAdapter
