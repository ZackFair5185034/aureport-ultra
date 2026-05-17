<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import CustomBorderDialog from '@/views/report/designer/resource-panel/property-panel/custom-border-dialog/index.vue'

defineOptions({ name: 'BorderConfig' })

const props = withDefaults(defineProps<{
  cellStyle?: any
}>(), {
  cellStyle: () => ({}),
})

const emit = defineEmits<{
  (e: 'border-change', value: any): void
  (e: 'border-save', value: any): void
}>()

const { t } = useI18n()

const borderChecked = ref(false)
const customBorderDialogVisible = ref(false)
const localCellStyle = ref<any>({})

watch(() => props.cellStyle, (newVal) => {
  loadBorderProperties(newVal)
}, { immediate: true, deep: true })

function loadBorderProperties(cellStyle: any) {
  if (!cellStyle)
    return
  borderChecked.value = !!(cellStyle.leftBorder || cellStyle.rightBorder || cellStyle.topBorder || cellStyle.bottomBorder)
}

function onBorderChange() {
  const defaultBorder = { color: '0,0,0', style: 'solid', width: 1 }

  emit('border-change', {
    checked: borderChecked.value,
    borders: borderChecked.value
      ? {
          leftBorder: JSON.parse(JSON.stringify(defaultBorder)),
          rightBorder: JSON.parse(JSON.stringify(defaultBorder)),
          topBorder: JSON.parse(JSON.stringify(defaultBorder)),
          bottomBorder: JSON.parse(JSON.stringify(defaultBorder)),
        }
      : {
          leftBorder: null,
          rightBorder: null,
          topBorder: null,
          bottomBorder: null,
        },
  })
}

function configBorder() {
  localCellStyle.value = JSON.parse(JSON.stringify(props.cellStyle))

  if (!localCellStyle.value.leftBorder) {
    localCellStyle.value.leftBorder = { color: '0,0,0', width: '1', style: 'solid' }
  }

  if (!localCellStyle.value.rightBorder) {
    localCellStyle.value.rightBorder = { color: '0,0,0', width: '1', style: 'solid' }
  }

  if (!localCellStyle.value.topBorder) {
    localCellStyle.value.topBorder = { color: '0,0,0', width: '1', style: 'solid' }
  }

  if (!localCellStyle.value.bottomBorder) {
    localCellStyle.value.bottomBorder = { color: '0,0,0', width: '1', style: 'solid' }
  }

  customBorderDialogVisible.value = true
}

function handleCustomBorderSave(borderData: any) {
  emit('border-save', {
    topBorder: borderData.topBorder,
    bottomBorder: borderData.bottomBorder,
    leftBorder: borderData.leftBorder,
    rightBorder: borderData.rightBorder,
  })
}
</script>

<template>
  <div>
    <u-checkbox-group>
      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="borderChecked" @change="onBorderChange">
            {{ t('dialog.propCondition.border') }}
          </u-checkbox>
        </div>
        <span v-show="borderChecked" style="margin-left: 10px;">
          <u-button @click="configBorder">
            <i class="iconfont icon-setting" /> {{ t('dialog.propCondition.borderConfig') }}
          </u-button>
        </span>
      </div>
    </u-checkbox-group>

    <CustomBorderDialog
      v-model:visible="customBorderDialogVisible"
      :cell-style="localCellStyle"
      @close="customBorderDialogVisible = false"
      @save="handleCustomBorderSave"
    />
  </div>
</template>
