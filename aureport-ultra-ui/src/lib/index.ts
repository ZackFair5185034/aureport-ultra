import AureportDesignerElement from './components/AureportDesigner'
import AureportPreviewElement from './components/AureportPreview'
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
  setLibMode(true)

  if (!customElements.get('aureport-designer')) {
    customElements.define('aureport-designer', AureportDesignerElement)
  }

  if (!customElements.get('aureport-preview')) {
    customElements.define('aureport-preview', AureportPreviewElement)
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
  setLibMode,
  getLibMode,
  AureportDesignerElement,
  AureportPreviewElement,
}

export { install }

export { default as AureportDesignerElement } from './components/AureportDesigner'
export { default as AureportPreviewElement } from './components/AureportPreview'
export { getLibMode, setLibMode } from './navigator'
export { default as requestAdapter } from './requestAdapter'
