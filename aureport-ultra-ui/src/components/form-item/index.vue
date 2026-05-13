<script setup lang="ts">
import { ref, computed, inject, provide, onMounted, onBeforeUnmount, type Ref } from 'vue'
import AsyncValidator from 'async-validator'
import type { FormContext, FormField } from '../form/index.vue'

defineOptions({ name: 'UFormItem' })

const form = inject<FormContext>('formContext')

const props = withDefaults(defineProps<{
  label?: string
  labelWidth?: number
  prop?: string
  required?: boolean
  rules?: Record<string, unknown> | Record<string, unknown>[]
  error?: string
  validateStatus?: boolean
  showMessage?: boolean
  labelFor?: string
}>(), {
  label: '',
  required: false,
  showMessage: true,
  labelFor: '',
})

const prefixCls = 'u-form-item'

const validateState = ref('')
const validateMessage = ref('')
const isRequired = ref(false)

const fieldValue = computed(() => {
  if (!form?.model || !props.prop) return undefined
  let path = props.prop.replace(/:/g, '.')
  return getPropByPath(form.model, path).v
})

const labelStyles = computed(() => {
  const style: Record<string, string> = {}
  if (form?.labelPosition === 'top') return style
  const labelWidth = props.labelWidth ?? form?.labelWidth ?? undefined
  if (labelWidth !== undefined) {
    style.width = `${labelWidth}px`
  }
  return style
})

const contentStyles = computed(() => {
  const lw = labelStyles.value.width
  return {
    width: (form?.labelPosition === 'top' || lw === '0px') ? undefined : `calc(100% - ${lw})`,
  }
})

const classes = computed(() => [
  prefixCls,
  {
    [`${prefixCls}-required`]: props.required || isRequired.value,
    [`${prefixCls}-error`]: validateState.value === 'error',
    [`${prefixCls}-validating`]: validateState.value === 'validating',
  },
])

function getPropByPath(obj: Record<string, unknown>, path: string): { o: Record<string, unknown>; k: string; v: unknown } {
  let tempObj = obj
  path = path.replace(/\[(\w+)\]/g, '.$1').replace(/^\./, '')
  let keyArr = path.split('.')
  let i = 0
  for (let len = keyArr.length; i < len - 1; ++i) {
    let key = keyArr[i]
    if (key in tempObj) {
      tempObj = tempObj[key] as Record<string, unknown>
    } else {
      throw new Error('[u-ui warn]: please transfer a valid prop path to form item!')
    }
  }
  return { o: tempObj, k: keyArr[i], v: tempObj[keyArr[i]] }
}

function getRules(): Record<string, unknown>[] {
  const formRules = form?.rules || {}
  const selfRules = props.rules
  const propRules = formRules[props.prop as string] as Record<string, unknown> | Record<string, unknown>[] | undefined
  if (selfRules) {
    return Array.isArray(selfRules) ? selfRules : [selfRules]
  }
  if (propRules) {
    return Array.isArray(propRules) ? propRules : [propRules]
  }
  return []
}

function getFilteredRule(trigger: string) {
  const rules = getRules()
  return rules.filter((rule: Record<string, unknown>) => !rule.trigger || (rule.trigger as string).indexOf(trigger) !== -1)
}

async function validate(trigger: string, callback?: (errors?: unknown) => void): Promise<boolean> {
  let rules = getFilteredRule(trigger)
  if (!rules || rules.length === 0) {
    if (!props.required) {
      if (callback) callback()
      return true
    } else {
      rules = [{ required: true }]
    }
  }

  validateState.value = 'validating'

  let descriptor: Record<string, unknown[]> = {}
  descriptor[props.prop as string] = rules

  const validator = new AsyncValidator(descriptor)
  let model: Record<string, unknown> = {}
  model[props.prop as string] = fieldValue.value

  try {
    await validator.validate(model, { firstFields: true })
    validateState.value = 'success'
    validateMessage.value = ''
    if (callback) callback()
    return true
  } catch (e: unknown) {
    const err = e as { errors?: Array<{ message: string }> }
    validateState.value = 'error'
    validateMessage.value = err.errors ? err.errors[0].message : ''
    if (callback) callback(validateMessage.value || true)
    return false
  }
}

function resetField() {
  validateState.value = ''
  validateMessage.value = ''
  if (form?.model && props.prop) {
    let path = props.prop.replace(/:/g, '.')
    let prop = getPropByPath(form.model, path)
    if (Array.isArray(prop.v)) {
      prop.o[prop.k] = [...(initialValue as unknown[])]
    } else {
      prop.o[prop.k] = initialValue
    }
  }
}

let initialValue: unknown

export interface FormItemContext {
  onFieldBlur: () => void
  onFieldChange: () => void
}

function onFieldBlur() {
  validate('blur')
}

function onFieldChange() {
  validate('change')
}

const formItemContext: FormItemContext = { onFieldBlur, onFieldChange }
provide('formItemContext', formItemContext)

const fieldInstance: FormField = {
  get prop() { return props.prop },
  validate,
  resetField,
}

onMounted(() => {
  if (props.prop) {
    form?.addField(fieldInstance)
    initialValue = fieldValue.value
    setRules()
  }
})

onBeforeUnmount(() => {
  if (props.prop) {
    form?.removeField(fieldInstance)
  }
})

function setRules() {
  let rules = getRules()
  if (rules.length && isRequired.value) return
  if (rules.length) {
    rules.every((rule: Record<string, unknown>) => {
      isRequired.value = rule.required as boolean
    })
  } else if (props.required) {
    isRequired.value = props.required
  }
}

import './style/index.css'
</script>

<template>
  <div :class="classes">
    <label
      v-if="label || $slots.label"
      :class="[prefixCls + '-label']"
      :for="labelFor"
      :style="labelStyles"
    >
      <slot name="label">{{ label }}</slot>
    </label>
    <label v-else :class="[prefixCls + '-label-empty']" :style="labelStyles" />
    <div :class="[prefixCls + '-content']" :style="contentStyles">
      <slot />
      <transition name="zoom-in-top">
        <div
          v-if="validateState === 'error' && showMessage && form?.showMessage"
          :class="[prefixCls + '-error-tip']"
        >
          {{ validateMessage }}
        </div>
      </transition>
    </div>
  </div>
</template>

<style scoped>
</style>
