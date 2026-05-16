<template>
  <UDialog
    :title="isRow ? t('dialog.rowColNumber.insertRow') : t('dialog.rowColNumber.insertCol')"
    width="400px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form :label-width="100">
        <u-form-item :label="isRow ? t('dialog.rowColNumber.rowCount') : t('dialog.rowColNumber.colCount')">
          <u-input-number
            :placeholder="t('dialog.rowColNumber.tip')"
            v-model="number"
            :min="1"
            ref="input"
            @keyup.enter="handleOk"
          />
        </u-form-item>
      </u-form>
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'RowColNumberDialog' })

const { t } = useI18n()

const visible = ref(false)
const number = ref(1)
const isRow = ref(false)
const callback = ref<((val: number) => void) | null>(null)

function show(cb: (val: number) => void, row?: boolean) {
  visible.value = true
  number.value = 1
  isRow.value = !!row
  callback.value = cb
}

function handleOk() {
  const numValue = parseInt(String(number.value))
  if (!numValue || numValue < 1) {
    showAlert(t('dialog.rowColNumber.numValidate'))
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
    number.value = 1
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

<style scoped>
</style>
