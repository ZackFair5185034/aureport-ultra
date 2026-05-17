<script setup lang="ts">
import type { FormItemContext } from '../form-item/index.vue'
import { inject, ref, watch } from 'vue'

defineOptions({ name: 'UInputNumber' })

const props = withDefaults(defineProps<{
  modelValue?: number | string
  disabled?: boolean
  step?: number
  stepStrictly?: boolean
  precision?: number
  max?: number
  min?: number
  size?: 'large' | 'medium' | 'small' | 'mini'
}>(), {
  modelValue: 0,
  disabled: false,
  step: 1,
  stepStrictly: false,
  precision: 0,
  size: 'medium',
})

const emit = defineEmits<{
  'update:modelValue': [value: number | string]
  'change': [value: number | string]
}>()

const formItemContext = inject<FormItemContext | undefined>('formItemContext')

const currentValue = ref<number | string>(0)
const increaseForbid = ref(false)
const decreaseForbid = ref(false)

watch(() => props.modelValue, (newVal) => {
  currentValue.value = newVal
  handleBlur(false)
}, { immediate: true })

function handleIncrease() {
  if (props.disabled || increaseForbid.value)
    return
  currentValue.value = Number(currentValue.value) + props.step
  emit('update:modelValue', currentValue.value)
  emit('change', currentValue.value)
}

function handleDecrease() {
  if (props.disabled || decreaseForbid.value)
    return
  currentValue.value = Number(currentValue.value) - props.step
  emit('update:modelValue', currentValue.value)
  emit('change', currentValue.value)
}

function onInputBlur() {
  handleBlur()
}

function handleBlur(emitEvent = true) {
  if (!currentValue.value)
    return
  let numVal: number
  numVal = typeof currentValue.value === 'number' ? currentValue.value : Number(String(currentValue.value).replaceAll(/[^\d.-]/g, ''))
  if (isNaN(numVal)) {
    numVal = 0
  }

  if (props.stepStrictly) {
    numVal = _approCalc(numVal, props.step)
  }

  if (props.precision > 0) {
    numVal = Number(numVal.toFixed(Math.floor(props.precision)))
  }

  if (props.min != null && numVal <= props.min) {
    decreaseForbid.value = true
    numVal = props.min
  }
  else {
    decreaseForbid.value = false
  }

  if (props.max != null && numVal >= props.max) {
    increaseForbid.value = true
    numVal = props.max
  }
  else {
    increaseForbid.value = false
  }

  currentValue.value = numVal
  if (emitEvent) {
    emit('update:modelValue', numVal)
    emit('change', numVal)
    formItemContext?.onFieldBlur()
  }
}

function handleInput(e: Event) {
  const target = e.target as HTMLInputElement
  currentValue.value = target.value
  formItemContext?.onFieldChange()
}

function _approCalc(num: number, base: number) {
  return Math.round(num / base) * base
}
</script>

<template>
  <div
    class="u-input-number"
    :class="{ [`u-input-number-size-${size}`]: true }"
  >
    <span
      class="u-input-number-button u-input-number-button-left"
      :class="{ 'u-input-number-button-disabled': disabled || decreaseForbid }"
      @click="handleDecrease"
    >
      -
    </span>
    <input
      class="u-input-number-input"
      :class="{ 'u-input-number-input-disabled': disabled }"
      type="text"
      :value="currentValue"
      :disabled="disabled"
      @blur="onInputBlur"
      @input="handleInput"
    />
    <span
      class="u-input-number-button u-input-number-button-right"
      :class="{ 'u-input-number-button-disabled': disabled || increaseForbid }"
      @click="handleIncrease"
    >
      +
    </span>
  </div>
</template>

<style scoped>
.u-input-number {
  position: relative;
  width: 140px;
  height: 36px;
  line-height: 32px;
}

.u-input-number-input {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  padding: 7px 36px;
  text-align: center;
  box-sizing: border-box;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  outline: 0;
  font-size: 14px;
}

.u-input-number-input:focus {
  border-color: #00554a;
  box-shadow: 0 0 4px #00554a;
}

.u-input-number-input-disabled {
  cursor: not-allowed;
  background-color: #f5f7fa;
  color: #b2b6be;
}

.u-input-number-button {
  box-sizing: border-box;
  position: absolute;
  width: 30px;
  height: calc(100% - 2px);
  text-align: center;
  font-size: 20px;
  margin: 0;
  padding: 0;
  border: none;
  cursor: pointer;
  background-color: #f5f7fa;
}

.u-input-number-button:active {
  background-color: rgba(245, 247, 250, 0.3);
}

.u-input-number-button-left {
  left: 1px;
  top: 1px;
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
  border-right: 1px solid #dcdfe6;
}

.u-input-number-button-right {
  right: 1px;
  top: 1px;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
  border-left: 1px solid #dcdfe6;
}

.u-input-number-button-disabled {
  cursor: not-allowed;
  color: #b2b6be;
}

.u-input-number-size-large {
  height: 40px;
  line-height: 36px;
}

.u-input-number-size-small {
  height: 32px;
  line-height: 28px;
}

.u-input-number-size-mini {
  height: 28px;
  line-height: 24px;
}
</style>
