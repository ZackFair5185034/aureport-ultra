import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import { install as VueMonacoEditorPlugin } from '@guolao/vue-monaco-editor'
import UButtonGroup from './components/button-group/index.vue'
// Import u-* components for global registration
// unplugin-vue-components auto-registers as PascalCase (Button, Form, etc.)
// but templates use kebab-case with u- prefix (<u-button>, <u-form>, etc.)
import UButton from './components/button/index.vue'
import UCheckboxGroup from './components/checkbox-group/index.vue'
import UCheckbox from './components/checkbox/index.vue'

import UCol from './components/col/index.vue'
import UColorPicker from './components/color-picker/index.vue'
import UDatePicker from './components/date-picker/index.vue'
import UDialog from './components/dialog/index.vue'
import UDivider from './components/divider/index.vue'
import UFormItem from './components/form-item/index.vue'
import UForm from './components/form/index.vue'
import UInputNumber from './components/input-number/index.vue'
import UInput from './components/input/index.vue'
import ULoading from './components/loading/index.vue'
import UMessage from './components/message/index.vue'
import UMessageBox from './components/messagebox/index.vue'
import UOption from './components/option/index.vue'
import URadioGroup from './components/radio-group/index.vue'
import URadio from './components/radio/index.vue'
import URow from './components/row/index.vue'
import USelect from './components/select/index.vue'
import USwitch from './components/switch/index.vue'
import UTabs from './components/tabs/index.vue'
import UTabPane from './components/tabs/pane.vue'
import UTag from './components/tag/index.vue'
import UTree from './components/tree/index.vue'
import i18n from './locales'
import router from './router'
import '@/assets/css/iconfont/iconfont.css'
import './assets/css/common/index.css'

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.use(i18n)
app.use(VueMonacoEditorPlugin, {
  paths: {
    vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.55.1/min/vs',
  },
})

// Register u-* components globally (both PascalCase and kebab-case)
const uComponents: Record<string, any> = {
  UButton,
  UButtonGroup,
  UForm,
  UFormItem,
  URow,
  UCol,
  USelect,
  UOption,
  UInput,
  UInputNumber,
  USwitch,
  UCheckbox,
  UCheckboxGroup,
  URadio,
  URadioGroup,
  UDatePicker,
  UColorPicker,
  UDivider,
  ULoading,
  UDialog,
  UTabs,
  UTabPane,
  UTag,
  UTree,
  UMessage,
  UMessageBox,
}

for (const [name, component] of Object.entries(uComponents)) {
  if (component) {
    // PascalCase: <UButton>
    app.component(name, component)
    // kebab-case with u- prefix: <u-button>
    const kebab = `u-${name.replace(/^U/, '').replaceAll(/([A-Z])/g, '-$1').toLowerCase()}`
    app.component(kebab, component)
  }
}

app.mount('#app')
