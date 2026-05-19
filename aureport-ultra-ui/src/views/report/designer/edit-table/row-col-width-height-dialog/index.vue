<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'RowColWidthHeightDialog' })

const { t } = useI18n()

const visible = ref(false)
const inputValue = ref<string | number>('')
const isCol = ref(false)
const callback = ref<((val: number) => void) | null>(null)

function show(cb: (val: number) => void, value?: string | number, col?: boolean) {
  visible.value = true
  inputValue.value = value || ''
  isCol.value = !!col
  callback.value = cb
}

function handleOk() {
  const numValue = parseInt(String(inputValue.value))
  if (!numValue) {
    showAlert(t('dialog.rowColWidthHeight.numValidate'))
    return
  }

  if (typeof callback.value === 'function') {
    callback.value(numValue)
  }

  handleClose()
}

function handleClose() {
  visible.value = false
  setTimeout(() => {
    inputValue.value = ''
    callback.value = null
  }, 300)
}

function handleKeydown(e: KeyboardEvent) {
  if (visible.value && e.key === 'Escape') {
    handleClose()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

defineExpose({ show })
</script>

<template>
  <UDialog
    :title="isCol ? t('dialog.rowColWidthHeight.colWidth') : t('dialog.rowColWidthHeight.rowHeight')"
    width="400px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form :label-width="100">
        <u-form-item :label="isCol ? t('dialog.rowColWidthHeight.colWidth') : t('dialog.rowColWidthHeight.rowHeight')">
          <u-input-number
            v-model="inputValue"
            :placeholder="t('dialog.rowColWidthHeight.tip')"
            @keyup.enter="handleOk"
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click.stop="handleClose">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
