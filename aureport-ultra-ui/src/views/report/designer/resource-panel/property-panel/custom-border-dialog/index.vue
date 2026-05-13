<template>
  <UDialog
    :title="t('tools.border.customBorderLine')"
    width="400px"
    :visible="visible"
    :z-index="zIndex"
    @close="handleClose"
  >
    <div class="border-config-container">
      <!-- 选项卡导航 -->
      <u-tabs v-model="activeTab">
        <u-tab-pane :label="t('tools.border.up')" index="top"></u-tab-pane>
        <u-tab-pane :label="t('tools.border.down')" index="bottom"></u-tab-pane>
        <u-tab-pane :label="t('tools.border.left')" index="left"></u-tab-pane>
        <u-tab-pane :label="t('tools.border.right')" index="right"></u-tab-pane>
      </u-tabs>

      <!-- 选项卡内容 -->
      <div class="tab-content" style="padding-top: 20px">
        <u-form ref="form" :label-width="60">
          <!-- 上边框配置 -->
          <div v-show="activeTab === 'top'">
            <u-form-item :label="t('tools.border.lineStyle')">
              <u-select v-model="localTopBorder.style">
                <u-option
                  v-for="option in lineStyleOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </u-select>
            </u-form-item>
            <u-form-item :label="t('tools.border.size')">
              <u-select v-model="localTopBorder.width">
                <u-option
                  v-for="option in lineWidthOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </u-select>
            </u-form-item>
            <u-form-item :label="t('tools.border.color')">
              <UColorPicker v-model="localTopBorder.color" />
            </u-form-item>
          </div>

          <!-- 下边框配置 -->
          <div v-show="activeTab === 'bottom'">
            <u-form-item :label="t('tools.border.lineStyle')">
              <u-select v-model="localBottomBorder.style">
                <u-option
                  v-for="option in lineStyleOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </u-select>
            </u-form-item>
            <u-form-item :label="t('tools.border.size')">
              <u-select v-model="localBottomBorder.width">
                <u-option
                  v-for="option in lineWidthOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </u-select>
            </u-form-item>
            <u-form-item :label="t('tools.border.color')">
              <UColorPicker v-model="localBottomBorder.color" :inline="true" />
            </u-form-item>
          </div>

          <!-- 左边框配置 -->
          <div v-show="activeTab === 'left'">
            <u-form-item :label="t('tools.border.lineStyle')">
              <u-select v-model="localLeftBorder.style">
                <u-option
                  v-for="option in lineStyleOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </u-select>
            </u-form-item>
            <u-form-item :label="t('tools.border.size')">
              <u-select v-model="localLeftBorder.width">
                <u-option
                  v-for="option in lineWidthOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </u-select>
            </u-form-item>
            <u-form-item :label="t('tools.border.color')">
              <UColorPicker v-model="localLeftBorder.color" :inline="true" />
            </u-form-item>
          </div>

          <!-- 右边框配置 -->
          <div v-show="activeTab === 'right'">
            <u-form-item :label="t('tools.border.lineStyle')">
              <u-select v-model="localRightBorder.style">
                <u-option
                  v-for="option in lineStyleOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </u-select>
            </u-form-item>
            <u-form-item :label="t('tools.border.size')">
              <u-select v-model="localRightBorder.width">
                <u-option
                  v-for="option in lineWidthOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </u-select>
            </u-form-item>
            <u-form-item :label="t('tools.border.color')">
              <UColorPicker v-model="localRightBorder.color" :inline="true" />
            </u-form-item>
          </div>
        </u-form>
      </div>
    </div>

    <template #footer>
      <div style="text-align: right">
        <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import UDialog from '@/components/dialog/index.vue';
import USelect from '@/components/select/index.vue';
import UOption from '@/components/option/index.vue';
import UButton from "@/components/button/index.vue";
import UTabs from "@/components/tabs/index.vue";
import UTabPane from "@/components/tabs/pane.vue";
import UColorPicker from "@/components/color-picker/index.vue";
import UForm from '@/components/form/index.vue';
import UFormItem from '@/components/form-item/index.vue';

defineOptions({ name: 'CustomBorderDialog' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  visible?: boolean
  cellStyle?: any | null
  topBorder?: any
  bottomBorder?: any
  leftBorder?: any
  rightBorder?: any
  zIndex?: number
}>(), {
  visible: false,
  cellStyle: null,
  topBorder: () => ({ style: 'solid', width: 1, color: '#000000' }),
  bottomBorder: () => ({ style: 'solid', width: 1, color: '#000000' }),
  leftBorder: () => ({ style: 'solid', width: 1, color: '#000000' }),
  rightBorder: () => ({ style: 'solid', width: 1, color: '#000000' }),
  zIndex: 20000
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'close'): void
  (e: 'save', ...args: any[]): void
}>()

const form = ref<any>(null)
const activeTab = ref('top')

const localTopBorder = ref({ style: 'solid', width: 1, color: '#000000' })
const localBottomBorder = ref({ style: 'solid', width: 1, color: '#000000' })
const localLeftBorder = ref({ style: 'solid', width: 1, color: '#000000' })
const localRightBorder = ref({ style: 'solid', width: 1, color: '#000000' })

const lineStyleOptions = computed(() => [
  { value: 'solid', label: t('tools.border.solidLine') },
  { value: 'dashed', label: t('tools.border.dashed') },
  { value: 'none', label: t('tools.border.none') }
])

const lineWidthOptions = computed(() =>
  Array.from({ length: 10 }, (_, i) => ({
    value: i + 1,
    label: (i + 1).toString()
  }))
)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadBorderData()
  }
})

watch(() => props.cellStyle, () => {
  if (props.cellStyle) {
    loadBorderData()
  }
}, { deep: true })

function loadBorderData() {
  if (props.cellStyle) {
    if (props.cellStyle.topBorder) {
      localTopBorder.value = { ...props.cellStyle.topBorder }
      localTopBorder.value.color = rgbToHexIfNeeded(localTopBorder.value.color)
    }
    if (props.cellStyle.bottomBorder) {
      localBottomBorder.value = { ...props.cellStyle.bottomBorder }
      localBottomBorder.value.color = rgbToHexIfNeeded(localBottomBorder.value.color)
    }
    if (props.cellStyle.leftBorder) {
      localLeftBorder.value = { ...props.cellStyle.leftBorder }
      localLeftBorder.value.color = rgbToHexIfNeeded(localLeftBorder.value.color)
    }
    if (props.cellStyle.rightBorder) {
      localRightBorder.value = { ...props.cellStyle.rightBorder }
      localRightBorder.value.color = rgbToHexIfNeeded(localRightBorder.value.color)
    }
  } else {
    localTopBorder.value = { ...props.topBorder }
    localBottomBorder.value = { ...props.bottomBorder }
    localLeftBorder.value = { ...props.leftBorder }
    localRightBorder.value = { ...props.rightBorder }
  }
}

function rgbToHexIfNeeded(color: string): string {
  if (typeof color === 'string' && color.includes(',')) {
    const rgb = color.split(',')
    return rgbToHex(parseInt(rgb[0]), parseInt(rgb[1]), parseInt(rgb[2]))
  }
  return color
}

function handleClose() {
  emit('close')
  emit('update:visible', false)
}

function handleOk() {
  const topBorder = { ...localTopBorder.value }
  const bottomBorder = { ...localBottomBorder.value }
  const leftBorder = { ...localLeftBorder.value }
  const rightBorder = { ...localRightBorder.value }

  topBorder.color = hexToRgb(localTopBorder.value.color)
  bottomBorder.color = hexToRgb(localBottomBorder.value.color)
  leftBorder.color = hexToRgb(localLeftBorder.value.color)
  rightBorder.color = hexToRgb(localRightBorder.value.color)

  if (props.cellStyle) {
    emit('save', {
      topBorder,
      bottomBorder,
      leftBorder,
      rightBorder
    })
  } else {
    emit('save', topBorder, bottomBorder, leftBorder, rightBorder)
  }
  emit('close')
  emit('update:visible', false)
}

function hexToRgb(hex: string): string {
  if (typeof hex === 'string' && hex.includes(',')) {
    return hex
  }

  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result ?
    `${parseInt(result[1], 16)},${parseInt(result[2], 16)},${parseInt(result[3], 16)}` :
    '0,0,0'
}

function rgbToHex(r: number, g: number, b: number): string {
  return "#" + [r, g, b].map(x => {
    const hex = x.toString(16)
    return hex.length === 1 ? '0' + hex : hex
  }).join('')
}
</script>

<style scoped>
.tab-content {
  overflow: auto;
  height: 400px;
}
</style>
