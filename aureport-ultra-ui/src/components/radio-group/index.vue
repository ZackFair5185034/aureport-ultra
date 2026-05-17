<script setup lang="ts">
import type { FormItemContext } from '../form-item/index.vue'
import { inject, provide, reactive } from 'vue'

defineOptions({ name: 'URadioGroup' })

const props = withDefaults(defineProps<{
  modelValue?: boolean | string | number
  disabled?: boolean
  button?: boolean
}>(), {
  modelValue: false,
  disabled: false,
  button: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  'change': [value: unknown]
}>()

export interface RadioGroupContext {
  modelValue?: unknown
  disabled?: boolean
  button?: boolean
  onSelect: (label: unknown) => void
}

const formItemContext = inject<FormItemContext | undefined>('formItemContext')

const radioGroupContext: RadioGroupContext = reactive({
  get modelValue() { return props.modelValue },
  get disabled() { return props.disabled },
  get button() { return props.button },
  onSelect(label: unknown) {
    if (props.modelValue !== label) {
      emit('change', label)
    }

    emit('update:modelValue', label)
    formItemContext?.onFieldChange()
  },
})

provide('radioGroupContext', radioGroupContext)
</script>

<template>
  <div class="u-radio-group">
    <slot />
  </div>
</template>

<style scoped>
</style>
