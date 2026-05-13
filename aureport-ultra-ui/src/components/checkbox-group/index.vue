<script setup lang="ts">
import { reactive, provide, inject } from 'vue'
import type { FormItemContext } from '../form-item/index.vue'

defineOptions({ name: 'UCheckboxGroup' })

export interface CheckboxGroupContext {
  modelValue?: unknown[]
  disabled?: boolean
  max?: number
  min?: number
  button?: boolean
  onSelect: (label: unknown) => void
}

const props = withDefaults(defineProps<{
  modelValue?: unknown[]
  disabled?: boolean
  max?: number
  min?: number
  button?: boolean
}>(), {
  modelValue: () => [],
  disabled: false,
  max: -1,
  min: -1,
  button: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown[]]
  change: [value: unknown[]]
}>()

const formItemContext = inject<FormItemContext>('formItemContext')

const checkboxGroupContext: CheckboxGroupContext = reactive({
  get modelValue() { return props.modelValue },
  get disabled() { return props.disabled },
  get max() { return props.max },
  get min() { return props.min },
  get button() { return props.button },
  onSelect(label: unknown) {
    const newValue = [...(props.modelValue || [])]
    const index = newValue.indexOf(label)
    if (index !== -1) {
      newValue.splice(index, 1)
    } else {
      if (props.max > -1 && newValue.length >= props.max) return
      newValue.push(label)
    }
    emit('update:modelValue', newValue)
    emit('change', newValue)
    formItemContext?.onFieldChange()
  },
})

provide('checkboxGroupContext', checkboxGroupContext)
</script>

<template>
  <div class="u-checkbox-group">
    <slot />
  </div>
</template>

<style scoped>
</style>
