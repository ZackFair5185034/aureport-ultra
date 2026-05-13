<template>
  <UDialog
    :title="$t('dialog.fontSetting.title')"
    width="400px"
    :visible="visible"
    :z-index="20000"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form :label-width="60">
        <u-form-item :label="$t('dialog.fontSetting.font')">
          <u-select v-model="localStyle.fontFamily">
            <u-option
              v-for="option in fontFamilyOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item :label="$t('dialog.fontSetting.color')">
          <UColorPicker v-model="colorValue" />
        </u-form-item>

        <u-form-item :label="$t('dialog.fontSetting.size')">
          <u-select v-model="localStyle.fontSize">
            <u-option
              v-for="option in fontSizeOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item :label="$t('dialog.fontSetting.bold')">
          <u-select v-model="localStyle.bold">
            <u-option
              v-for="option in booleanOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item :label="$t('dialog.fontSetting.italic')">
          <u-select v-model="localStyle.italic">
            <u-option
              v-for="option in booleanOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item :label="$t('dialog.fontSetting.underline')">
          <u-select v-model="localStyle.underline">
            <u-option
              v-for="option in booleanOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>
      </u-form>
    </div>
    <template #footer><div style="text-align: right">
      <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ $t('dialog.common.cancel') }}</u-button>
      <u-button @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
    </div></template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

defineOptions({ name: 'FontSettingDialog' })

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'ok', style: any): void
}>()

const props = withDefaults(defineProps<{
  visible?: boolean
  fontStyle?: any
}>(), {
  visible: false,
  fontStyle: () => ({
    fontFamily: '宋体',
    fontSize: 10,
    forecolor: '0,0,0',
    bold: false,
    italic: false,
    underline: false
  })
})

const { t } = useI18n()

const localStyle = ref({
  fontFamily: '宋体',
  fontSize: '10',
  forecolor: '0,0,0',
  bold: 'false',
  italic: 'false',
  underline: 'false'
})

const fontFamilies = ref([
  '宋体', '仿宋', '黑体', '楷体', '微软雅黑',
  'Arial', 'Impact', 'Times New Roman', 'Comic Sans MS', 'Courier New'
])
const fontSizes = ref(Array.from({ length: 100 }, (_, i) => i + 1))

const colorValue = computed({
  get(): string {
    if (localStyle.value.forecolor) {
      const rgb = localStyle.value.forecolor.split(',')
      if (rgb.length === 3) {
        return '#' + rgb.map((val: string) => {
          const hex = parseInt(val).toString(16)
          return hex.length === 1 ? '0' + hex : hex
        }).join('')
      }
    }
    return '#000000'
  },
  set(value: string) {
    if (value && value.startsWith('#')) {
      const hex = value.substring(1)
      if (hex.length === 6) {
        const r = parseInt(hex.substring(0, 2), 16)
        const g = parseInt(hex.substring(2, 4), 16)
        const b = parseInt(hex.substring(4, 6), 16)
        localStyle.value.forecolor = `${r},${g},${b}`
      }
    }
  }
})

const fontFamilyOptions = computed(() => {
  return fontFamilies.value.map(font => ({ value: font, label: font }))
})

const fontSizeOptions = computed(() => {
  return fontSizes.value.map(size => ({ value: String(size), label: String(size) }))
})

const booleanOptions = computed(() => [
  { value: 'true', label: t('dialog.fontSetting.yes') },
  { value: 'false', label: t('dialog.fontSetting.no') }
])

watch(() => props.visible, (newVal) => {
  if (newVal) {
    initializeStyle()
  }
})

watch(() => props.fontStyle, () => {
  if (props.visible) {
    initializeStyle()
  }
}, { deep: true })

initializeStyle()

function initializeStyle() {
  localStyle.value = {
    fontFamily: props.fontStyle.fontFamily || '宋体',
    forecolor: props.fontStyle.forecolor || '0,0,0',
    fontSize: props.fontStyle.fontSize !== undefined ? String(props.fontStyle.fontSize) : '10',
    bold: props.fontStyle.bold !== undefined ? String(props.fontStyle.bold) : 'false',
    italic: props.fontStyle.italic !== undefined ? String(props.fontStyle.italic) : 'false',
    underline: props.fontStyle.underline !== undefined ? String(props.fontStyle.underline) : 'false'
  }
}

function handleOk() {
  const resultStyle = {
    ...localStyle.value,
    bold: localStyle.value.bold === 'true',
    italic: localStyle.value.italic === 'true',
    underline: localStyle.value.underline === 'true'
  }
  emit('ok', resultStyle)
  handleClose()
}

function handleClose() {
  emit('close')
}
</script>

<style scoped>
</style>
