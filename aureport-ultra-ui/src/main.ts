import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import i18n from './locales'
import '@/assets/css/iconfont/iconfont.css'
import './assets/css/common/index.css'

// Manually register u-* prefixed components used by search-form
// unplugin-vue-components registers them without the U prefix
import UButton from './components/button/index.vue'
import UCheckbox from './components/checkbox/index.vue'
import UCheckboxGroup from './components/checkbox-group/index.vue'
import UCol from './components/col/index.vue'
import UColorPicker from './components/color-picker/index.vue'
import UDatePicker from './components/date-picker/index.vue'
import UDialog from './components/dialog/index.vue'
import UDivider from './components/divider/index.vue'
import UForm from './components/form/index.vue'
import UFormItem from './components/form-item/index.vue'
import UInput from './components/input/index.vue'
import UInputNumber from './components/input-number/index.vue'
import ULoading from './components/loading/index.vue'
import UOption from './components/option/index.vue'
import URadio from './components/radio/index.vue'
import URadioGroup from './components/radio-group/index.vue'
import URow from './components/row/index.vue'
import USelect from './components/select/index.vue'
import USwitch from './components/switch/index.vue'
import UTabs from './components/tabs/index.vue'
import UTabPane from './components/tabs/pane.vue'
import UTag from './components/tag/index.vue'
import UTree from './components/tree/index.vue'
import UMessage from './components/message/index.vue'
import UMessageBox from './components/messagebox/index.vue'

const app = createApp(App)
const pinia = createPinia()

// Register all u-* components globally
const uComponents = {
  UButton, UCheckbox, UCheckboxGroup, UCol, UColorPicker, UDatePicker,
  UDialog, UDivider, UForm, UFormItem, UInput, UInputNumber,
  ULoading, UOption, URadio, URadioGroup, URow, USelect,
  USwitch, UTabs, UTabPane, UTag, UTree,
  UMessage, UMessageBox,
}
for (const [name, component] of Object.entries(uComponents)) {
  if (component) {
    app.component(name, component)
    // Also register kebab-case
    app.component(name.replace(/^U/, 'u-').replace(/([A-Z])/g, '-$1').toLowerCase(), component)
  }
}

app.use(router)
app.use(pinia)
app.use(i18n)

app.mount('#app')
