<script setup lang="ts">
import { ref, reactive, provide, computed } from 'vue'
import { oneOf } from '../utils'

defineOptions({ name: 'UForm' })

export interface FormContext {
  model: Record<string, unknown>
  rules: Record<string, unknown>
  labelWidth: number
  labelPosition: 'left' | 'right' | 'top'
  inline: boolean
  showMessage: boolean
  size: 'large' | 'medium' | 'small' | 'mini'
  addField: (field: FormField) => void
  removeField: (field: FormField) => void
  validateField: (prop: string, cb?: (valid: boolean) => void) => void
}

export interface FormField {
  prop?: string
  validate: (trigger: string, cb?: (errors?: unknown) => void) => Promise<boolean>
  resetField: () => void
}

const props = withDefaults(defineProps<{
  model?: Record<string, unknown>
  rules?: Record<string, unknown>
  labelWidth?: number
  labelPosition?: 'left' | 'right' | 'top'
  inline?: boolean
  showMessage?: boolean
  autocomplete?: 'on' | 'off'
  size?: 'large' | 'medium' | 'small' | 'mini'
}>(), {
  labelPosition: 'right',
  inline: false,
  showMessage: true,
  autocomplete: 'off',
  size: 'medium',
})

const emit = defineEmits<{
  validate: [valid: boolean]
}>()

const fields = ref<FormField[]>([])

const prefixCls = 'u-form'

const classes = computed(() => [
  prefixCls,
  `${prefixCls}--${props.size}`,
  `${prefixCls}-label-${props.labelPosition}`,
  { [`${prefixCls}-inline`]: props.inline },
])

function addField(field: FormField) {
  if (field) fields.value.push(field)
}

function removeField(field: FormField) {
  const index = fields.value.indexOf(field)
  if (index !== -1) fields.value.splice(index, 1)
}

function resetFields() {
  fields.value.forEach(field => {
    field.resetField()
  })
}

function validate(callback?: (valid: boolean) => void): Promise<boolean> {
  return new Promise(resolve => {
    let valid = true
    let count = 0
    if (fields.value.length === 0) {
      resolve(true)
      if (callback) callback(true)
      return
    }
    fields.value.forEach(field => {
      field.validate('', (errors?: unknown) => {
        if (errors) valid = false
        if (++count === fields.value.length) {
          resolve(valid)
          emit('validate', valid)
          if (callback) callback(valid)
        }
      })
    })
  })
}

function validateField(prop: string, cb?: (valid: boolean) => void) {
  const field = fields.value.find(f => f.prop === prop)
  if (!field) {
    console.warn('[u-ui warn]: 必须使用有效的 prop 字符串调用 validateField !')
    return
  }
  field.validate('', (errors?: unknown) => {
    cb?.(!errors)
  })
}

const formContext: FormContext = reactive({
  get model() { return props.model || {} },
  get rules() { return props.rules || {} },
  get labelWidth() { return props.labelWidth || 0 },
  get labelPosition() { return props.labelPosition },
  get inline() { return props.inline },
  get showMessage() { return props.showMessage },
  get size() { return props.size },
  addField,
  removeField,
  validateField,
})

provide('formContext', formContext)

defineExpose({ validate, validateField, resetFields })
</script>

<template>
  <form :class="classes" :autocomplete="autocomplete">
    <slot />
  </form>
</template>

<style scoped>
</style>
