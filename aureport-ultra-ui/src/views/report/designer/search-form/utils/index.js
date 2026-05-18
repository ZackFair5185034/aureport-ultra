/**
 * 通用js方法封装处理
 * Copyright (c) 2019 ruoyi
 */

// 日期格式化
export function parseTime(time, pattern = '{y}-{m}-{d} {h}:{i}:{s}') {
  if (arguments.length === 0 || !time) {
    return null
  }

  let date
  if (typeof time === 'object') {
    date = time
  }
  else {
    if ((typeof time === 'string') && (/^\d+$/.test(time))) {
      time = parseInt(time)
    }
    else if (typeof time === 'string') {
      time = time.replaceAll('-', '/').replace('T', ' ').replaceAll(/\.\d{3}/gu, '')
    }

    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000
    }

    date = new Date(time)
  }

  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay(),
  }
  const time_str = pattern.replaceAll(/\{([ymdhisa])+\}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }

    if (result.length > 0 && value < 10) {
      value = `0${value}`
    }

    return value || 0
  })
  return time_str
}

/**
 * 表格时间格式化
 */
export function formatDate(cellValue) {
  if (cellValue == null || cellValue == '')
    return ''
  const date = new Date(cellValue)
  const year = date.getFullYear()
  const month = date.getMonth() + 1 < 10 ? `0${date.getMonth() + 1}` : date.getMonth() + 1
  const day = date.getDate() < 10 ? `0${date.getDate()}` : date.getDate()
  const hours = date.getHours() < 10 ? `0${date.getHours()}` : date.getHours()
  const minutes = date.getMinutes() < 10 ? `0${date.getMinutes()}` : date.getMinutes()
  const seconds = date.getSeconds() < 10 ? `0${date.getSeconds()}` : date.getSeconds()
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * @param {number} time
 * @param {string} option
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time, option) {
  time = (`${time}`).length === 10 ? parseInt(time) * 1000 : +time
  const d = new Date(time)
  const now = Date.now()

  const diff = (now - d) / 1000

  if (diff < 30) {
    return '刚刚'
  }
  else if (diff < 3600) {
    // less 1 hour
    return `${Math.ceil(diff / 60)}分钟前`
  }
  else if (diff < 3600 * 24) {
    return `${Math.ceil(diff / 3600)}小时前`
  }
  else if (diff < 3600 * 24 * 2) {
    return '1天前'
  }

  return option
    ? parseTime(time, option)
    : (
        `${d.getMonth()
        + 1
        }月${
          d.getDate()
        }日${
          d.getHours()
        }时${
          d.getMinutes()
        }分`
      )
}

/**
 * @param {string} url
 * @returns {object} 解析后的参数对象
 */
export function getQueryObject(url) {
  url = url ?? window.location.href
  const search = url.slice(Math.max(0, url.lastIndexOf('?') + 1))
  const obj = {}
  const reg = /([^?&=]+)=([^?&=]*)/g
  search.replaceAll(reg, (rs, $1, $2) => {
    const name = decodeURIComponent($1)
    let val = decodeURIComponent($2)
    val = String(val)
    obj[name] = val
    return rs
  })
  return obj
}

/**
 * @param {string} str
 * @returns {number} output value
 */
export function byteLength(str) {
  // returns the byte length of an utf8 string
  let s = str.length
  for (let i = str.length - 1; i >= 0; i--) {
    const code = str.codePointAt(i)
    if (code > 0x7F && code <= 0x7FF)
      s++
    else if (code > 0x7FF && code <= 0xFFFF)
      s += 2
    if (code >= 0xDC00 && code <= 0xDFFF)
      i--
  }

  return s
}

/**
 * @param {Array} actual
 * @returns {Array} 过滤后的新数组
 */
export function cleanArray(actual) {
  const newArray = []
  for (const element of actual) {
    if (element) {
      newArray.push(element)
    }
  }

  return newArray
}

/**
 * @param {object} json
 * @returns {Array} URL 参数字符串
 */
export function param(json) {
  if (!json)
    return ''
  return cleanArray(
    Object.keys(json).map((key) => {
      if (json[key] === undefined)
        return ''
      return `${encodeURIComponent(key)}=${encodeURIComponent(json[key])}`
    }),
  ).join('&')
}

/**
 * @param {string} url
 * @returns {object} 解析后的参数对象
 */
export function param2Obj(url) {
  const search = decodeURIComponent(url.split('?')[1]).replaceAll('+', ' ')
  if (!search) {
    return {}
  }

  const obj = {}
  const searchArr = search.split('&')
  for (const v of searchArr) {
    const index = v.indexOf('=')
    if (index !== -1) {
      const name = v.slice(0, Math.max(0, index))
      const val = v.slice(index + 1)
      obj[name] = val
    }
  }

  return obj
}

/**
 * @param {string} val
 * @returns {string} 提取后的纯文本内容
 */
export function html2Text(val) {
  const div = document.createElement('div')
  div.innerHTML = val
  return div.textContent
}

/**
 * Merges two objects, giving the last one precedence
 * @param {object} target
 * @param {(object | Array)} source
 * @returns {object} 合并后的对象
 */
export function objectMerge(target, source) {
  if (typeof target !== 'object') {
    target = {}
  }

  if (Array.isArray(source)) {
    return source.slice()
  }

  for (const property of Object.keys(source)) {
    const sourceProperty = source[property]
    target[property] = typeof sourceProperty === 'object' ? objectMerge(target[property], sourceProperty) : sourceProperty
  }

  return target
}

/**
 * @param {HTMLElement} element
 * @param {string} className
 */
export function toggleClass(element, className) {
  if (!element || !className) {
    return
  }

  let classString = element.className
  const nameIndex = classString.indexOf(className)
  if (nameIndex === -1) {
    classString += `${className}`
  }
  else {
    classString
      = classString.slice(0, Math.max(0, nameIndex))
        + classString.slice(nameIndex + className.length)
  }

  element.className = classString
}

/**
 * @param {string} type
 * @returns {Date} 计算后的日期对象
 */
export function getTime(type) {
  return type === 'start' ? Date.now() - 3600 * 1000 * 24 * 90 : new Date(new Date().toDateString())
}

/**
 * @param {Function} func
 * @param {number} wait
 * @param {boolean} immediate
 * @return {*}
 */
export function debounce(func, wait, immediate) {
  let timeout, timestamp, result

  const later = (...args) => {
    // 据上一次触发时间间隔
    const last = Date.now() - timestamp

    // 上次被包装函数被调用时间间隔 last 小于设定时间间隔 wait
    if (last < wait && last > 0) {
      timeout = setTimeout(later, wait - last)
    }
    else {
      timeout = null
      // 如果设定为immediate===true，因为开始边界已经调用过了此处无需调用
      if (!immediate) {
        result = Reflect.apply(func, this, args)
      }
    }
  }

  return function (...args) {
    timestamp = Date.now()
    const callNow = immediate && !timeout
    // 如果延时不存在，重新设定延时
    if (!timeout)
      timeout = setTimeout(later, wait)
    if (callNow) {
      result = Reflect.apply(func, this, args)
    }

    return result
  }
}

/**
 * This is just a simple version of deep copy
 * Has a lot of edge cases bug
 * If you want to use a perfect deep copy, use lodash's _.cloneDeep
 * @param {object} source
 * @returns {object} 深度克隆后的对象
 */
export function deepClone(source) {
  if (!source && typeof source !== 'object') {
    throw new Error('error arguments', 'deepClone')
  }

  const targetObj = source.constructor === Array ? [] : {}
  for (const keys of Object.keys(source)) {
    targetObj[keys] = source[keys] && typeof source[keys] === 'object' ? deepClone(source[keys]) : source[keys]
  }

  return targetObj
}

/**
 * @param {Array} arr
 * @returns {Array} 去重后的新数组
 */
export function uniqueArr(arr) {
  return Array.from(new Set(arr))
}

/**
 * @returns {string} 生成的唯一字符串
 */
export function createUniqueString() {
  const timestamp = `${Date.now()}`
  const randomNum = `${parseInt((1 + Math.random()) * 65536)}`
  return (+(randomNum + timestamp)).toString(32)
}

/**
 * Check if an element has a class
 * @param {HTMLElement} ele
 * @param {string} cls
 * @returns {boolean} 是否包含指定 class
 */
export function hasClass(ele, cls) {
  return !!new RegExp(String.raw`(\s|^)` + cls + String.raw`(\s|$)`).test(ele.className)
}

/**
 * Add class to element
 * @param {HTMLElement} ele
 * @param {string} cls
 */
export function addClass(ele, cls) {
  if (!hasClass(ele, cls))
    ele.className += ` ${cls}`
}

/**
 * Remove class from element
 * @param {HTMLElement} ele
 * @param {string} cls
 */
export function removeClass(ele, cls) {
  if (hasClass(ele, cls)) {
    const reg = new RegExp(String.raw`(\s|^)` + cls + String.raw`(\s|$)`)
    ele.className = ele.className.replace(reg, ' ')
  }
}

export function makeMap(str, expectsLowerCase) {
  const map = Object.create(null)
  const list = str.split(',')
  for (const element of list) {
    map[element] = true
  }

  return expectsLowerCase
    ? val => map[val.toLowerCase()]
    : val => map[val]
}

export const exportDefault = 'export default '

export const beautifierConf = {
  html: {
    indent_size: '2',
    indent_char: ' ',
    max_preserve_newlines: '-1',
    preserve_newlines: false,
    keep_array_indentation: false,
    break_chained_methods: false,
    indent_scripts: 'separate',
    brace_style: 'end-expand',
    space_before_conditional: true,
    unescape_strings: false,
    jslint_happy: false,
    end_with_newline: true,
    wrap_line_length: '110',
    indent_inner_html: true,
    comma_first: false,
    e4x: true,
    indent_empty_lines: true,
  },
  js: {
    indent_size: '2',
    indent_char: ' ',
    max_preserve_newlines: '-1',
    preserve_newlines: false,
    keep_array_indentation: false,
    break_chained_methods: false,
    indent_scripts: 'normal',
    brace_style: 'end-expand',
    space_before_conditional: true,
    unescape_strings: false,
    jslint_happy: true,
    end_with_newline: true,
    wrap_line_length: '110',
    indent_inner_html: true,
    comma_first: false,
    e4x: true,
    indent_empty_lines: true,
  },
}

// 首字母大小
export function titleCase(str) {
  return str.replaceAll(/( |^)[a-z]/g, L => L.toUpperCase())
}

// 下划转驼峰
export function camelCase(str) {
  return str.replaceAll(/_[a-z]/g, str1 => str1.slice(-1).toUpperCase())
}

export function isNumberStr(str) {
  return /^[+-]?(0|([1-9]\d*))(\.\d+)?$/.test(str)
}
