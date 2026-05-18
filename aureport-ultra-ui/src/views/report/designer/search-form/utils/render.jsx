// @ts-check
import { defineComponent, h } from 'vue'
import UCheckbox from '@/components/checkbox/index.vue'
import UOption from '@/components/option/index.vue'
import URadio from '@/components/radio/index.vue'
import { makeMap } from './index'

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
  'u-input': {
    prepend(h, conf, key) {
      return h('template', { slot: 'prepend' }, conf[key])
    },
    append(h, conf, key) {
      return h('template', { slot: 'append' }, conf[key])
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
      const confClone = structuredClone(props.conf)
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

      return h(confClone.tag, dataObject, children)
    }
  },
})
