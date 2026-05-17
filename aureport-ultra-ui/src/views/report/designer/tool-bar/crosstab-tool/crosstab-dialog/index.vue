<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

defineOptions({ name: 'CrosstabDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
}>(), {
  visible: false,
})

const emit = defineEmits<{
  (e: 'saveAfter', value: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()
const crosstabValue = ref('')

watch(() => props.visible, (newVal) => {
  if (newVal) {
    crosstabValue.value = ''
  }
})

function handleKeydown(e: KeyboardEvent) {
  if (props.visible && e.key === 'Escape') {
    handleClose()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleOk() {
  emit('saveAfter', crosstabValue.value)
  handleClose()
}

function handleClose() {
  emit('close')
  crosstabValue.value = ''
}
</script>

<template>
  <UDialog
    :title="$t('dialog.crosstab.title')"
    width="520px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form ref="form" :label-width="60">
        <u-form-item :label="$t('dialog.crosstab.crosstab')">
          <u-input
            ref="input"
            v-model="crosstabValue"
            style="width: 300px"
            :placeholder="$t('dialog.crosstab.tip')"
            @keyup.enter="handleOk"
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="handleClose">{{ $t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
.tip-text {
  font-size: 12px;
  color: #4e4e4e;
  font-weight: normal;
}
</style>
