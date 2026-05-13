import { h, ref, computed, defineComponent } from 'vue'
import { makeMap } from './index'

import UCheckbox from "@/components/checkbox/index.vue"
import UCheckboxGroup from "@/components/checkbox-group/index.vue"
import USelect from "@/components/select/index.vue"
import UOption from "@/components/option/index.vue"
import URadioGroup from "@/components/radio-group/index.vue"
import URadio from "@/components/radio/index.vue"
import USwitch from "@/components/switch/index.vue"
import UInput from "@/components/input/index.vue"
import UInputNumber from "@/components/input-number/index.vue"
import UButton from "@/components/button/index.vue"
import UDialog from "@/components/dialog/index.vue"
import UFormItem from "@/components/form-item/index.vue"
import UForm from "@/components/form/index.vue"
import URow from "@/components/row/index.vue"
import UCol from "@/components/col/index.vue"
import UDatePicker from "@/components/date-picker/index.vue"
import UTree from "@/components/tree/index.vue"
import UTabs from "@/components/tabs/index.vue"
import UTabPane from "@/components/tabs/pane.vue"

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
  + 'target,title,type,usemap,value,width,wrap'
)

const componentChild = {
  'u-input': {
    prepend(h, conf, key) {
      return h('template', { slot: 'prepend' }, conf[key])
    },
    append(h, conf, key) {
      return h('template', { slot: 'append' }, conf[key])
    }
  },
  'u-select': {
    options(h, conf, key) {
      const list = []
      if (conf.options && Array.isArray(conf.options)) {
        conf.options.forEach(item => {
          list.push(h(UOption, { label: item.label, value: item.value, disabled: item.disabled }))
        })
      }
      return list
    }
  },
  'u-radio-group': {
    options(h, conf, key) {
      const list = []
      if (conf.options && Array.isArray(conf.options)) {
        conf.options.forEach(item => {
          if (conf.optionType === 'button') {
            list.push(h(URadio, { label: item.value, border: conf.border }, () => item.label))
          } else {
            list.push(h(URadio, { label: item.value, border: conf.border }, () => item.label))
          }
        })
      }
      return list
    }
  },
  'u-checkbox-group': {
    options(h, conf, key) {
      const list = []
      if (conf.options && Array.isArray(conf.options)) {
        conf.options.forEach(item => {
          if (conf.optionType === 'button') {
            list.push(h(UCheckbox, { label: item.value }, () => item.label))
          } else {
            list.push(h(UCheckbox, { label: item.value, border: conf.border }, () => item.label))
          }
        })
      }
      return list
    }
  }
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
        style: {}
      }
      const confClone = JSON.parse(JSON.stringify(props.conf))
      const children = []

      const childObjs = componentChild[confClone.tag]
      if (childObjs) {
        Object.keys(childObjs).forEach(key => {
          const childFunc = childObjs[key]
          if (confClone[key]) {
            children.push(childFunc(h, confClone, key))
          }
        })
      }

      Object.keys(confClone).forEach(key => {
        const val = confClone[key]
        if (key === 'vModel') {
          dataObject.props.value = confClone.defaultValue
          dataObject.on.input = (val) => {
            emit('input', val)
          }
        } else if (dataObject[key] !== undefined) {
          dataObject[key] = val
        } else if (!isAttr(key)) {
          dataObject.props[key] = val
        } else {
          dataObject.attrs[key] = val
        }
      })

      return h(confClone.tag, dataObject, children)
    }
  }
})
