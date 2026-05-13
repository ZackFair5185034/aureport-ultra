<template>
  <UDialog
    :title="$t('dialog.import.title')"
    width="800px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="dialog-content">
      <div class="form-group">
        <div class="import-description">{{ $t('dialog.import.desc') }}</div>
      </div>
      <div class="form-group">
        <label>{{ $t('dialog.import.file') }}：</label>
        <input
          type="file"
          class="form-control"
          :key="fileInputKey"
          accept=".xlsx,.xls"
          @change="handleFileChange"
        />
      </div>
    </div>

    <template #footer><div style="text-align: right">
      <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ $t('dialog.common.cancel') }}</u-button>
      <u-button @click="handleUpload">{{ $t('dialog.common.ok') }}</u-button>
    </div></template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon.js'
import { importExcelFile } from '@/api/designer'

defineOptions({ name: 'ImportDialog' })

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'import-success'): void
}>()

const props = withDefaults(defineProps<{
  visible?: boolean
}>(), {
  visible: false
})

const { t } = useI18n()
const selectedFile = ref<File | null>(null)
const fileInputKey = ref(0)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    selectedFile.value = null
    fileInputKey.value += 1
  }
})

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0]
  }
}

async function handleUpload() {
  if (!selectedFile.value) {
    showAlert(t('dialog.import.selectFile') || t('dialog.import.file'))
    return
  }

  try {
    await importExcelFile(selectedFile.value)
    emit('import-success')
    emit('update:visible', false)
  } catch (error: any) {
    console.error('上传文件失败:', error)
    if (error.msg) {
      showAlert(t('dialog.import.fail') + t('colon') + error.msg, { useHTMLString: true })
    } else {
      showAlert(t('dialog.import.fail'))
    }
  }
}

function handleClose() {
  emit('update:visible', false)
  selectedFile.value = null
}
</script>

<style scoped>

.import-description {
  margin-bottom: 10px;
  line-height: 2;
  color: #929191;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
</style>
