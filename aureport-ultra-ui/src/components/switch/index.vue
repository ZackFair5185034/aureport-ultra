<script setup lang="ts">
import { computed, inject } from 'vue'
import type { FormItemContext } from '../form-item/index.vue'

defineOptions({ name: 'USwitch' })

const props = withDefaults(defineProps<{
  modelValue?: boolean | string | number
  disabled?: boolean
  activeValue?: boolean | string | number
  inactiveValue?: boolean | string | number
  activeText?: string
  inactiveText?: string
  activeColor?: string
  inactiveColor?: string
}>(), {
  modelValue: false,
  disabled: false,
  activeValue: true,
  inactiveValue: false,
  activeText: '',
  inactiveText: '',
  activeColor: '',
  inactiveColor: '',
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean | string | number]
}>()

const formItemContext = inject<FormItemContext | undefined>('formItemContext', undefined)

const _activeColor = computed(() =>
  props.modelValue === props.activeValue && props.activeColor ? props.activeColor : ''
)
const _inactiveColor = computed(() =>
  props.modelValue === props.inactiveValue && props.inactiveColor ? props.inactiveColor : ''
)

function handleClick() {
  const newVal = props.modelValue === props.activeValue ? props.inactiveValue : props.activeValue
  emit('update:modelValue', newVal)
  formItemContext?.onFieldChange()
}
</script>

<template>
  <label class="u-switch" :class="{ 'u-switch-disabled': disabled }">
    <input
      type="checkbox"
      class="u-switch-input"
      :disabled="disabled"
      @click="handleClick"
    />
    <span
      class="u-switch-label"
      :class="{ 'u-switch-label-selected': modelValue === inactiveValue }"
      :style="{ color: _inactiveColor }"
    >{{ inactiveText }}</span>
    <span
      class="u-switch-dot"
      :class="{
        'u-switch-dot-selected': modelValue === activeValue,
        'u-switch-dot-disabled': disabled,
      }"
      :style="{
        backgroundColor: modelValue === activeValue ? _activeColor : _inactiveColor,
        borderColor: modelValue === activeValue ? _activeColor : _inactiveColor,
      }"
    />
    <span
      class="u-switch-label"
      :class="{ 'u-switch-label-selected': modelValue === activeValue }"
      :style="{ color: _activeColor }"
    >{{ activeText }}</span>
  </label>
</template>

<style scoped>
.u-switch {
  display: inline-block;
  line-height: 22px
}

.u-switch-input {
  display: none
}

.u-switch-dot {
  display: inline-block;
  vertical-align: top;
  width: 40px;
  height: 22px;
  background-color: #d8d8d8;
  border-radius: 11px;
  box-sizing: border-box;
  border: 1px solid #d8d8d8;
  position: relative;
  cursor: pointer;
  transition: all .3s
}

.u-switch-dot:after {
  content: "";
  position: absolute;
  top: 1px;
  left: 1px;
  border-radius: 100%;
  transition: all .3s;
  width: 18px;
  height: 18px;
  background-color: #fff
}

.u-switch-dot-selected {
  background-color: #00554a;
  border-color: #00554a
}

.u-switch-dot-selected:after {
  top: 1px;
  left: 18px
}

.u-switch-label {
  vertical-align: top;
  display: inline-block;
  line-height: 22px;
  transition: color .3s
}

.u-switch-label-selected {
  color: #00554a
}

.u-switch-disabled {
  cursor: not-allowed;
  filter: grayscale(70%)
}

.u-switch-dot-disabled {
  cursor: not-allowed
}
</style>
