<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { previewData } from '@/api/designer/index'
import { LoadingDirective } from '@/components/loading/instance.js'

defineOptions({ name: 'PreviewDataDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  parameters?: any
}>(), {
  visible: false,
  parameters: null,
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

const { t } = useI18n()

const loading = ref(false)
const errorInfo = ref<string | null>(null)
const resultData = ref<any>(null)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadPreviewData()
  }
})

async function loadPreviewData() {
  if (!props.parameters) {
    return
  }

  loading.value = true
  errorInfo.value = null
  resultData.value = null

  try {
    const data = await previewData(props.parameters)
    loading.value = false
    resultData.value = data
  }
  catch (error: any) {
    let msg = t('dialog.sql.previewFail')
    if (error.msg) {
      msg = msg + t('colon') + error.msg
    }

    loading.value = false
    errorInfo.value = `<div style='color: #d30e00;'>${msg}</div>`
  }
}

function closeDialog() {
  emit('close')
}
</script>

<template>
  <UDialog
    :title="$t('dialog.preview.title')"
    width="1200px"
    top="50px"
    :visible="visible"
    :z-index="20000"
    @close="closeDialog"
  >
    <div v-loading="loading" class="preview-body-container">
      <div v-if="errorInfo" v-html="errorInfo" />
      <div v-else-if="resultData">
        <div style="height: 30px; background: #fdfdfd;">
          <span style="margin: 4px;">{{ $t('dialog.preview.total') }}{{ resultData.total }}{{ $t('dialog.preview.totalMid') }}{{ resultData.currentTotal }}{{ $t('dialog.preview.item') }}</span>
        </div>
        <div class="table-container">
          <table class="table table-bordered" style="margin-top: 2px; table-layout: fixed;">
            <thead>
              <tr style="background: #f3f3f3;">
                <td v-for="field in resultData.fields" :key="field" style="word-wrap: break-word; width: 120px;">
                  {{ field }}
                </td>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in resultData.data" :key="index">
                <td v-for="field in resultData.fields" :key="`${index}-${field}`" style="word-wrap: break-word;">
                  {{ item[field] }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="closeDialog">{{ $t('dialog.common.cancel') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
:root {
  --dialog-height: 600px;
}

.preview-body-container {
  min-height: 300px;
  max-height: var(--dialog-height);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.table-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  min-height: 0;
  max-height: var(--dialog-height);
}

.preview-body-container table {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 0;
}

.preview-body-container table td,
.data-table th {
  border: 1px solid #ddd;
}

.preview-body-container table thead th {
  vertical-align: bottom;
  border-bottom: 2px solid #ddd;
}
</style>
