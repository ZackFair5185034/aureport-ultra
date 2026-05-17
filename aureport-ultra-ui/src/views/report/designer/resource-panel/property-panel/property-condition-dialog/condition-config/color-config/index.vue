<script setup lang="ts">
import { onBeforeMount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
// @ts-ignore
import configOptions from '../constants/config-options.js'

defineOptions({ name: 'ColorConfig' })

const props = withDefaults(defineProps<{
  cellStyle?: any
}>(), {
  cellStyle: () => ({}),
})

const emit = defineEmits<{
  (e: 'color-change', value: any): void
}>()

const { t } = useI18n()

const forceChecked = ref(false)
const forceColor = ref('#000000')
const forceScope = ref('cell')
const bgcolorChecked = ref(false)
const bgColor = ref('#FFFFFF')
const bgcolorScope = ref('cell')
const scopeOptions = ref<any[]>([])

onBeforeMount(() => {
  scopeOptions.value = configOptions.getScopeOptions(t)
})

watch(() => props.cellStyle, (newVal) => {
  loadColorProperties(newVal)
}, { immediate: true, deep: true })

function hexToRgb(hex: string) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16),
      }
    : null
}

function rgbToHex(r: number, g: number, b: number) {
  return `#${[r, g, b].map((x) => {
    const hex = x.toString(16)
    return hex.length === 1 ? `0${hex}` : hex
  }).join('').toUpperCase()}`
}

function convertColorToRgb(color: string) {
  if (!color)
    return null

  if (color.startsWith('#')) {
    const rgb = hexToRgb(color)
    if (rgb) {
      return `${rgb.r},${rgb.g},${rgb.b}`
    }
  }
  else if (color.length > 5 && color.startsWith('rgb')) {
    return color.substring(4, color.length - 1)
  }

  return color
}

function convertRgbToHex(rgbString: string) {
  if (!rgbString)
    return null

  const rgbParts = rgbString.split(',')
  if (rgbParts.length === 3) {
    return rgbToHex(parseInt(rgbParts[0]), parseInt(rgbParts[1]), parseInt(rgbParts[2]))
  }

  return null
}

function loadColorProperties(cellStyle: any) {
  if (!cellStyle)
    return

  forceChecked.value = !!(cellStyle.forecolor && cellStyle.forecolor !== '')
  if (forceChecked.value) {
    const hexColor = convertRgbToHex(cellStyle.forecolor)
    forceColor.value = hexColor || '#000000'
  }
  else {
    forceColor.value = ''
  }

  forceScope.value = cellStyle.forecolorScope || 'cell'

  bgcolorChecked.value = !!(cellStyle.bgcolor && cellStyle.bgcolor !== '')
  if (bgcolorChecked.value) {
    const hexColor = convertRgbToHex(cellStyle.bgcolor)
    bgColor.value = hexColor || '#FFFFFF'
  }
  else {
    bgColor.value = ''
  }

  bgcolorScope.value = cellStyle.bgcolorScope || 'cell'
}

function onForceChange() {
  emit('color-change', {
    type: 'forecolor',
    checked: forceChecked.value,
    value: forceChecked.value ? '0,0,0' : null,
    scope: forceChecked.value ? 'cell' : null,
  })
}

function onForceColorChange() {
  const rgbColor = convertColorToRgb(forceColor.value)
  emit('color-change', {
    type: 'forecolor',
    checked: forceChecked.value,
    value: rgbColor,
    scope: forceScope.value,
  })
}

function onForceScopeChange() {
  emit('color-change', {
    type: 'forecolor',
    checked: forceChecked.value,
    value: convertColorToRgb(forceColor.value),
    scope: forceScope.value,
  })
}

function onBgcolorChange() {
  emit('color-change', {
    type: 'bgcolor',
    checked: bgcolorChecked.value,
    value: bgcolorChecked.value ? '0,0,0' : null,
    scope: bgcolorChecked.value ? 'cell' : null,
  })
}

function onBgColorChange() {
  const rgbColor = convertColorToRgb(bgColor.value)
  emit('color-change', {
    type: 'bgcolor',
    checked: bgcolorChecked.value,
    value: rgbColor,
    scope: bgcolorScope.value,
  })
}

function onBgcolorScopeChange() {
  emit('color-change', {
    type: 'bgcolor',
    checked: bgcolorChecked.value,
    value: convertColorToRgb(bgColor.value),
    scope: bgcolorScope.value,
  })
}
</script>

<template>
  <div>
    <u-checkbox-group>
      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="forceChecked" @change="onForceChange">
            {{ t('dialog.propCondition.forecolor') }}
          </u-checkbox>
        </div>

        <span v-show="forceChecked">
          <div class="u-inline">
            <UColorPicker
              v-model="forceColor"
              @input="onForceColorChange"
            />
          </div>

          <span>{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="forceScope"
              :clearable="true"
              @change="onForceScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>

      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="bgcolorChecked" @change="onBgcolorChange">
            {{ t('dialog.propCondition.bgcolor') }}
          </u-checkbox>
        </div>
        <span v-show="bgcolorChecked">
          <div class="u-inline">
            <UColorPicker
              v-model="bgColor"
              @input="onBgColorChange"
            />
          </div>
          <span>{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="bgcolorScope"
              :clearable="true"
              @change="onBgcolorScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>
    </u-checkbox-group>
  </div>
</template>
