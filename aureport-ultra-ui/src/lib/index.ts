import LuckDesignerElement from './components/LuckDesigner'

import LuckPreviewElement from './components/LuckPreview'
import navigationAdapter from './navigationAdapter'
import { getLibMode, setLibMode } from './navigator'
import requestAdapter from './requestAdapter'
import './publicPath'
import 'handsontable/dist/handsontable.min.css'
import 'codemirror/lib/codemirror.css'

import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/lint/lint.css'
import '@/assets/css/designer/tree.css'
import '@/assets/css/iconfont/iconfont.css'
import '@/assets/css/common/index.css'

function install() {
  if (!customElements.get('luck-designer')) {
    customElements.define('luck-designer', LuckDesignerElement)
  }

  if (!customElements.get('luck-preview')) {
    customElements.define('luck-preview', LuckPreviewElement)
  }
}

function autoInstall() {
  if (typeof window !== 'undefined') {
    install()
  }
}

autoInstall()

export default {
  install,
  requestAdapter,
  navigationAdapter,
  setLibMode,
  getLibMode,
  LuckDesignerElement,
  LuckPreviewElement,
}

export {

  install,

}

export { default as LuckDesignerElement } from './components/LuckDesigner'
export { default as LuckPreviewElement } from './components/LuckPreview'
export { default as navigationAdapter } from './navigationAdapter'
export { getLibMode, setLibMode } from './navigator'
export { default as requestAdapter } from './requestAdapter'
