import { defineComponent, h } from 'vue'
import UButton from '@/components/button/index.vue'
import UCheckbox from '@/components/checkbox/index.vue'
import UCheckboxGroup from '@/components/checkbox-group/index.vue'
import UCol from '@/components/col/index.vue'
import UDatePicker from '@/components/date-picker/index.vue'
import UForm from '@/components/form/index.vue'
import UFormItem from '@/components/form-item/index.vue'
import UInput from '@/components/input/index.vue'
import UInputNumber from '@/components/input-number/index.vue'
import UOption from '@/components/option/index.vue'
import URadio from '@/components/radio/index.vue'
import URadioGroup from '@/components/radio-group/index.vue'
import URow from '@/components/row/index.vue'
import USelect from '@/components/select/index.vue'
import USwitch from '@/components/switch/index.vue'
import { deepClone, makeMap } from './index'

// Component map for resolving string tags to component objects
// In Vue 3, h('u-select', ...) creates a native HTML element, NOT a component.
// We must use imported component objects instead.
const componentMap = {
  'u-button': UButton,
  'u-checkbox-group': UCheckboxGroup,
  'u-col': UCol,
  'u-date-picker': UDatePicker,
  'u-form': UForm,
  'u-form-item': UFormItem,
  'u-input': UInput,
  'u-input-number': UInputNumber,
  'u-option': UOption,
  'u-radio': URadio,
  'u-radio-group': URadioGroup,
  'u-row': URow,
  'u-select': USelect,
  'u-switch': USwitch,
}

const isAttr = makeMap(
  'accept,accept-charset,accesskey,action,align,alt,async,autocomplete,'
  + 'autofocus,autoplay,autosave,bgcolor,border,buffered,challenge,charset,'
  + 'checked,cite,class,code,codebase,color,cols,colspan,content,http-equiv,'
  + 'name,contenteditable,contextmenu,controls,coords,data,datetime,default,'
  + 'defer,dir,dirname,disabled,download,draggable,dropzone,enctype,method,for,'
  + 'form,formaction,headers,height,hidden,high,href,hreflang,http-equiv,'
  + 'icon,id,ismap,itemprop,keytype,kind,label,lang,language,list,loop,low,'
  + 'manifest,max,maxlength,media,method,GET,POST,min,multiple,email,file,'
  + 'muted,name,novalidate,open,optimum,pattern,ping,placeholder,poster,'
  + 'preload,radiogroup,readonly,rel,required,reversed,rows,rowspan,sandbox,'
  + 'scope,scoped,seamless,selected,shape,size,type,text,sizes,span,'
  + 'spellcheck,src,srcdoc,srclang,srcset,start,step,style,summary,tabindex,'
  + 'target,title,type,usemap,value,width,wrap',
)

const componentChild = {
  'u-button': {
    defaultValue(h, conf, key) {
      return [conf[key]]
    },
  },
  'u-select': {
    options(h, conf, key) {
      const list = []
      if (conf.options && Array.isArray(conf.options)) {
        for (const item of conf.options) {
          list.push(h(UOption, { label: String(item.label), value: item.value, disabled: item.disabled }))
        }
      }

      return list
    },
  },
  'u-radio-group': {
    options(h, conf, key) {
      const list = []
      if (conf.options && Array.isArray(conf.options)) {
        for (const item of conf.options) {
          if (conf.optionType === 'button') {
            list.push(h(URadio, { label: item.value, border: conf.border }, () => item.label))
          }
          else {
            list.push(h(URadio, { label: item.value, border: conf.border }, () => item.label))
          }
        }
      }

      return list
    },
  },
  'u-checkbox-group': {
    options(h, conf, key) {
      const list = []
      if (conf.options && Array.isArray(conf.options)) {
        for (const item of conf.options) {
          if (conf.optionType === 'button') {
            list.push(h(UCheckbox, { label: item.value }, () => item.label))
          }
          else {
            list.push(h(UCheckbox, { label: item.value, border: conf.border }, () => item.label))
          }
        }
      }

      return list
    },
  },
}

// Vue 3 functional component
export default defineComponent({
  name: 'RenderComponent',
  props: ['conf'],
  emits: ['input'],
  setup(props, { emit }) {
    return () => {
      const dataObject = {
        attrs: {},
        props: {},
        on: {},
        style: {},
      }
      const confClone = deepClone(props.conf)
      const children = []

      const childObjs = componentChild[confClone.tag]
      if (childObjs) {
        for (const key of Object.keys(childObjs)) {
          const childFunc = childObjs[key]
          if (confClone[key]) {
            children.push(childFunc(h, confClone, key))
          }
        }
      }

      for (const key of Object.keys(confClone)) {
        const val = confClone[key]
        if (key === 'vModel') {
          dataObject.props.value = confClone.defaultValue
          dataObject.on.input = (val) => {
            emit('input', val)
          }
        }
        else if (dataObject[key] !== undefined) {
          dataObject[key] = val
        }
        else if (isAttr(key)) {
          dataObject.attrs[key] = val
        }
        else {
          dataObject.props[key] = val
        }
      }

      // Resolve component from local map (Vue 3 h() with string tag creates native elements, not components)
      const component = componentMap[confClone.tag] || confClone.tag

      // Wrap children in function slot for Vue 3 performance
      const slotChildren = children.length > 0 ? () => children : undefined

      return h(component, dataObject, slotChildren)
    }
  },
})
