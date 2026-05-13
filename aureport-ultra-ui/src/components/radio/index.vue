<script setup lang="ts">
import { computed, inject } from 'vue'
import type { RadioGroupContext } from '../radio-group/index.vue'

defineOptions({ name: 'URadio' })

const props = withDefaults(defineProps<{
  modelValue?: string | number | boolean
  label?: string | number | boolean
  disabled?: boolean
  border?: boolean
  size?: 'large' | 'medium' | 'small' | 'mini'
}>(), {
  modelValue: '',
  label: '',
  disabled: false,
  border: false,
  size: 'medium',
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | boolean]
  change: [value: boolean]
}>()

const radioGroup = inject<RadioGroupContext>('radioGroupContext')

const selected = computed(() => {
  if (radioGroup) return radioGroup.modelValue === props.label
  return props.modelValue === props.label
})

const myDisabled = computed(() => radioGroup?.disabled ?? false)
const isButton = computed(() => radioGroup?.button ?? false)

function onClick() {
  if (props.disabled || myDisabled.value) return
  if (!selected.value) {
    emit('change', true)
  }
  if (radioGroup) {
    radioGroup.onSelect(props.label)
  } else {
    emit('update:modelValue', props.label)
  }
}
</script>

<template>
  <label
    class="u-radio"
    :class="{
      'u-radio-selected': selected,
      'u-radio-disabled': disabled || myDisabled,
      [`u-radio-${size}-border`]: border,
      [`u-radio-${size}-button`]: isButton,
      'u-radio-selected-button': selected && isButton,
    }"
  >
    <input
      class="u-radio-input"
      type="radio"
      @click="onClick"
      :disabled="disabled || myDisabled"
    />
    <span
      class="u-radio-icon"
      :class="{
        'u-radio-icon-selected': selected,
        'u-radio-icon-disabled': disabled || myDisabled,
        'u-radio-icon-button': isButton,
      }"
    />
    <span class="u-radio-label">
      <slot>{{ label }}</slot>
    </span>
  </label>
</template>

<style scoped>
.u-radio {
  display: inline-block;
  box-sizing: border-box;
  vertical-align: top;
  font-size: 16px;
  line-height: 20px;
  height: 20px;
  margin-right: 20px;
  cursor: pointer
}

.u-radio-input {
  display: none
}

.u-radio-icon {
  box-sizing: border-box;
  border: 1px solid #ddd;
  height: 14px;
  width: 14px;
  border-radius: 50%;
  background-color: #fff;
  display: inline-block;
  position: relative;
  top: 2px
}

.u-radio-icon:after {
  content: "";
  position: absolute;
  width: 4px;
  height: 4px;
  background-color: #fff;
  left: 50%;
  top: 50%;
  border-radius: 50%;
  transition: transform .2s;
  transform: translate(-50%, -50%) scale(0)
}

.u-radio-icon-selected {
  background-color: #00554a
}

.u-radio-icon-selected:after {
  transform: translate(-50%, -50%) scale(1)
}

.u-radio-icon-disabled {
  background-color: #eee
}

.u-radio-icon-disabled:after {
  background-color: #aaa
}

.u-radio-icon-button {
  display: none
}

.u-radio-label {
  display: inline-block;
  margin-left: 3px
}

.u-radio-selected {
  color: #00554a;
  border-color: #00554a !important
}

.u-radio-selected-button {
  background-color: #00554a !important;
  color: #fff
}

.u-radio-disabled {
  cursor: not-allowed;
  color: #c0c4cc;
  border-color: #c0c4cc !important
}

.u-radio-large-border {
  height: 40px;
  padding: 8px 8px 12px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px
}

.u-radio-medium-border {
  height: 36px;
  padding: 6px 8px 10px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px
}

.u-radio-small-border {
  height: 32px;
  padding: 4px 8px 8px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px
}

.u-radio-mini-border {
  height: 28px;
  padding: 2px 8px 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px
}

.u-radio-large-button {
  float: left;
  background-color: #fff;
  height: 40px;
  line-height: 38px;
  padding: 0 15px 0 12px;
  margin: 0;
  border: 1px solid #dcdfe6
}

.u-radio-medium-button {
  float: left;
  background-color: #fff;
  height: 36px;
  line-height: 34px;
  padding: 0 15px 0 12px;
  margin: 0;
  border: 1px solid #dcdfe6
}

.u-radio-small-button {
  float: left;
  background-color: #fff;
  height: 32px;
  line-height: 30px;
  padding: 0 15px 0 12px;
  margin: 0;
  border: 1px solid #dcdfe6;
  font-size: 14px
}

.u-radio-mini-button {
  float: left;
  background-color: #fff;
  height: 28px;
  line-height: 26px;
  padding: 0 15px 0 12px;
  margin: 0;
  border: 1px solid #dcdfe6;
  font-size: 14px
}
</style>
