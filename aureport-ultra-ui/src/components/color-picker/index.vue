<template>
  <div class="u-color-picker">
    <div class="u-color-picker-trigger" @click="togglePicker">
      <slot>
        <UButton
          :size="size"
          type="info"
          native-type="button"
          style="border: none"
        >
          <span class="color-block" :style="{ backgroundColor: displayColor }"></span>
        </UButton>
      </slot>
    </div>
    <div class="u-color-picker-popover" v-if="pickerVisible" ref="popoverRef">
      <Sketch v-model="colors" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { Sketch } from '@ckpack/vue-color'

defineOptions({ name: 'UColorPicker' })

interface FullColor {
  hex: string
  hex8?: string
  hsl: { h: number; s: number; l: number; a: number }
  hsv: { h: number; s: number; v: number; a: number }
  rgba: { r: number; g: number; b: number; a: number }
  a: number
}

const props = withDefaults(defineProps<{
  modelValue?: string
  colorMode?: 'hex' | 'rgb' | 'rgba' | 'hsl' | 'hsv'
  disabled?: boolean
  beforeToggle?: () => boolean | undefined
  closeOnChange?: boolean
  size?: 'large' | 'medium' | 'small' | 'mini'
}>(), {
  modelValue: '#000000',
  colorMode: 'hex',
  disabled: false,
  closeOnChange: false,
  size: 'medium',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'change': [value: string]
}>()

const pickerVisible = ref(false)

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const colors = ref<any>({
  hex: '#000000',
  hsl: { h: 0, s: 0, l: 0, a: 1 },
  hsv: { h: 0, s: 0, v: 0, a: 1 },
  rgba: { r: 0, g: 0, b: 0, a: 1 },
  a: 1,
})

const displayColor = computed(() => props.modelValue || '#ffffff')

function togglePicker() {
  if (props.disabled) return
  if (!pickerVisible.value && typeof props.beforeToggle === 'function' && !props.beforeToggle()) return
  pickerVisible.value = !pickerVisible.value
}

function closePicker() {
  pickerVisible.value = false
}

function handleClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (pickerVisible.value && !(target.closest('.u-color-picker') || target.closest('.vc-sketch'))) {
    closePicker()
  }
}

function emitColor(val: { hex: string; hsl: { h: number; s: number; l: number; a: number }; hsv: { h: number; s: number; v: number; a: number }; rgba: { r: number; g: number; b: number; a: number } }) {
  let colorValue: string
  switch (props.colorMode) {
    case 'hex':
      colorValue = val.hex
      break
    case 'rgb':
      colorValue = `rgb(${val.rgba.r}, ${val.rgba.g}, ${val.rgba.b})`
      break
    case 'rgba':
      colorValue = `rgba(${val.rgba.r}, ${val.rgba.g}, ${val.rgba.b}, ${val.rgba.a})`
      break
    case 'hsl':
      colorValue = `hsl(${val.hsl.h}, ${val.hsl.s * 100}%, ${val.hsl.l * 100}%)`
      break
    case 'hsv':
      colorValue = `hsv(${val.hsv.h}, ${val.hsv.s * 100}%, ${val.hsv.v * 100}%)`
      break
    default:
      colorValue = val.hex
  }
  emit('update:modelValue', colorValue)
  emit('change', colorValue)
}

function setColorFromValue(value: string) {
  if (!value) return
  if (value.startsWith('#')) {
    colors.value = { ...colors.value, hex: value }
  } else if (value.startsWith('rgb')) {
    const matches = value.match(/\d+/g)
    if (matches && matches.length >= 3) {
      colors.value = {
        ...colors.value,
        rgba: {
          r: parseInt(matches[0]),
          g: parseInt(matches[1]),
          b: parseInt(matches[2]),
          a: matches[3] ? parseFloat(matches[3]) : 1,
        },
      }
    }
  }
}

watch(colors, (val) => {
  emitColor(val)
  if (props.closeOnChange) {
    closePicker()
  }
}, { deep: true })

watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal !== colors.value.hex) {
    setColorFromValue(newVal)
  }
})

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.u-color-picker {
  position: relative;
  display: inline-block;
}

.u-color-picker-trigger {
  display: inline-block;
  cursor: pointer;
}

.u-color-picker-popover {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  z-index: 9999;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.color-block {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 2px;
  vertical-align: middle;
  border: 1px solid #dcdfe6;
}
</style>
