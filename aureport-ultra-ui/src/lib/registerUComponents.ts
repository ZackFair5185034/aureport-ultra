import type { App, Component } from 'vue'
import UButtonGroup from '@/components/button-group/index.vue'
import UButton from '@/components/button/index.vue'
import UCheckboxGroup from '@/components/checkbox-group/index.vue'
import UCheckbox from '@/components/checkbox/index.vue'
import UCol from '@/components/col/index.vue'
import UColorPicker from '@/components/color-picker/index.vue'
import UDatePicker from '@/components/date-picker/index.vue'
import UDialog from '@/components/dialog/index.vue'
import UDivider from '@/components/divider/index.vue'
import UFormItem from '@/components/form-item/index.vue'
import UForm from '@/components/form/index.vue'
import UInputNumber from '@/components/input-number/index.vue'
import UInput from '@/components/input/index.vue'
import ULoading from '@/components/loading/index.vue'
import UMessage from '@/components/message/index.vue'
import UMessageBox from '@/components/messagebox/index.vue'
import UOption from '@/components/option/index.vue'
import URadioGroup from '@/components/radio-group/index.vue'
import URadio from '@/components/radio/index.vue'
import URow from '@/components/row/index.vue'
import USelect from '@/components/select/index.vue'
import USwitch from '@/components/switch/index.vue'
import UTabPane from '@/components/tabs/pane.vue'
import UTabs from '@/components/tabs/index.vue'
import UTag from '@/components/tag/index.vue'
import UTree from '@/components/tree/index.vue'

const uComponents: Record<string, Component> = {
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

function toKebabComponentName(name: string) {
  return `u-${name.replace(/^U/, '').replaceAll(/([A-Z])/g, '-$1').toLowerCase().replace(/^-/, '')}`
}

export function registerUComponents(app: App) {
  for (const [name, component] of Object.entries(uComponents)) {
    app.component(name, component)
    app.component(toKebabComponentName(name), component)
  }
}
