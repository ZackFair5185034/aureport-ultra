<script setup lang="ts">
import type { FormItemContext } from '../form-item/index.vue'
import { inject, provide, reactive } from 'vue'

defineOptions({ name: 'UCheckboxGroup' })

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
  'change': [value: unknown[]]
}>()

export interface CheckboxGroupContext {
  modelValue?: unknown[]
  disabled?: boolean
  max?: number
  min?: number
  button?: boolean
  onSelect: (label: unknown) => void
}

const formItemContext = inject<FormItemContext | undefined>('formItemContext')

const checkboxGroupContext: CheckboxGroupContext = reactive({
  get modelValue() { return props.modelValue },
  get disabled() { return props.disabled },
  get max() { return props.max },
  get min() { return props.min },
  get button() { return props.button },
  onSelect(label: unknown) {
    const newValue = [...(props.modelValue || [])]
    const index = newValue.indexOf(label)
    if (index === -1) {
      if (props.max > -1 && newValue.length >= props.max)
        return
      newValue.push(label)
    }
    else {
      newValue.splice(index, 1)
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
