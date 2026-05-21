import request from '@/utils/request'

export default {
  setBaseURL(url) {
    request.defaults.baseURL = url
  },

  setDefaultHeaders(headers) {
    Object.assign(request.defaults.headers.common, headers)
  },
}
