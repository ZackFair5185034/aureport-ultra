<script setup lang="ts">
import type { CheckboxGroupContext } from '../checkbox-group/index.vue'
import { computed, inject } from 'vue'

defineOptions({ name: 'UCheckbox' })

const props = withDefaults(defineProps<{
  modelValue?: boolean | number | string
  indeterminate?: boolean
  disabled?: boolean
  label?: boolean | number | string
  border?: boolean
  size?: 'large' | 'medium' | 'small' | 'mini'
}>(), {
  modelValue: false,
  indeterminate: false,
  disabled: false,
  label: '',
  border: false,
  size: 'medium',
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean | number | string]
  'change': [value: boolean | number | string]
}>()

const checkboxGroup = inject<CheckboxGroupContext>('checkboxGroupContext')

const checked = computed(() => {
  if (checkboxGroup) {
    return checkboxGroup.modelValue?.includes(props.label) ?? false
  }

  return !!props.modelValue
})

const limitDisabled = computed(() => {
  if (!checkboxGroup)
    return false
  const { max, min, modelValue } = checkboxGroup
  if (!checked.value && max != null && max > -1 && (modelValue?.length ?? 0) >= max)
    return true
  if (checked.value && min != null && min > -1 && (modelValue?.length ?? 0) <= min)
    return true
  return false
})

const forbidden = computed(() => props.disabled || (checkboxGroup?.disabled ?? false) || limitDisabled.value)
const isButton = computed(() => checkboxGroup?.button ?? false)

function handleClick() {
  if (forbidden.value)
    return
  if (checkboxGroup) {
    checkboxGroup.onSelect(props.label)
  }
  else {
    const newChecked = !checked.value
    emit('update:modelValue', newChecked)
    emit('change', newChecked)
  }
}
</script>

<template>
  <label
    class="u-checkbox"
    :class="{
      'u-checkbox-checked': checked,
      'u-checkbox-disabled': forbidden,
      [`u-checkbox-${size}-border`]: border,
      [`u-checkbox-${size}-button`]: isButton,
      'u-checkbox-checked-button': checked && isButton,
    }"
  >
    <input
      type="checkbox"
      class="u-checkbox-input"
      :disabled="forbidden"
      @click="handleClick"
    />
    <span
      class="u-checkbox-icon"
      :class="{
        'u-checkbox-icon-checked': checked,
        'u-checkbox-icon-indeterminate': indeterminate,
        'u-checkbox-icon-indeterminate-disabled': forbidden && indeterminate,
        'u-checkbox-icon-disabled': forbidden,
        'u-checkbox-icon-checked-disabled': forbidden && checked,
        'u-checkbox-icon-button': isButton,
      }"
    />
    <span class="u-checkbox-label">
      <slot />
    </span>
  </label>
</template>

<style scoped>
.u-checkbox {
  box-sizing: border-box;
  display: inline-block;
  cursor: pointer;
  height: 20px;
  font-size: 16px;
  vertical-align: top;
  margin-right: 20px;
}

.u-checkbox-input {
  display: none;
}

.u-checkbox-icon {
  display: inline-block;
  box-sizing: border-box;
  position: relative;
  top: 2px;
  width: 14px;
  height: 14px;
  border-radius: 2px;
  border: 1px solid #dcdfe6;
}

.u-checkbox-icon:after {
  content: '';
  position: absolute;
  box-sizing: border-box;
  width: 7px;
  height: 3px;
  border-top: 1px solid #fff;
  border-right: 1px solid #fff;
  left: 3px;
  top: 3.5px;
  transform: rotate(135deg);
}

.u-checkbox-icon-checked {
  background: #00554a;
  border-color: #00554a;
}

.u-checkbox-icon-checked-disabled {
  border-color: #c0c4cc;
}

.u-checkbox-icon-checked-disabled:after {
  border-color: #c0c4cc !important;
}

.u-checkbox-icon-disabled {
  background-color: #edf2fc;
}

.u-checkbox-icon-disabled:after {
  border-color: #edf2fc;
}

.u-checkbox-icon-indeterminate {
  background: #00554a;
  border-color: #00554a;
}

.u-checkbox-icon-indeterminate:after {
  transform: rotate(0);
  border-right: none;
  width: 8px;
  height: 0;
  left: 2px;
  top: 5.5px;
  border-color: #fff;
}

.u-checkbox-icon-indeterminate-disabled {
  background-color: #edf2fc;
  border-color: #c0c4cc;
}

.u-checkbox-icon-indeterminate-disabled:after {
  border-color: #c0c4cc !important;
}

.u-checkbox-icon-button {
  display: none;
}

.u-checkbox-label {
  display: inline-block;
  height: 100%;
  margin-left: 3px;
}

.u-checkbox-checked {
  color: #00554a;
  border-color: #00554a !important;
}

.u-checkbox-checked-button {
  color: #fff;
  background-color: #00554a !important;
  border-color: #dcdfe6 !important;
}

.u-checkbox-disabled {
  cursor: not-allowed;
  color: #c0c4cc;
}

.u-checkbox-large-border {
  height: 40px;
  padding: 8px 8px 12px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.u-checkbox-medium-border {
  height: 36px;
  padding: 6px 8px 10px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.u-checkbox-small-border {
  height: 32px;
  padding: 4px 8px 8px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.u-checkbox-mini-border {
  height: 28px;
  padding: 2px 8px 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.u-checkbox-large-button {
  float: left;
  background-color: #fff;
  height: 40px;
  line-height: 38px;
  padding: 0 15px 0 12px;
  margin: 0;
  border: 1px solid #dcdfe6;
}

.u-checkbox-medium-button {
  float: left;
  background-color: #fff;
  height: 36px;
  line-height: 34px;
  padding: 0 15px 0 12px;
  margin: 0;
  border: 1px solid #dcdfe6;
}

.u-checkbox-small-button {
  float: left;
  background-color: #fff;
  height: 32px;
  line-height: 30px;
  padding: 0 15px 0 12px;
  margin: 0;
  border: 1px solid #dcdfe6;
  font-size: 14px;
}

.u-checkbox-mini-button {
  float: left;
  background-color: #fff;
  height: 28px;
  line-height: 26px;
  padding: 0 15px 0 12px;
  margin: 0;
  border: 1px solid #dcdfe6;
  font-size: 14px;
}
</style>
