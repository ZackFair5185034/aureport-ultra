<template>
  <UDialog
    :title="$t('dialog.save.title')"
    width="800px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="save-dialog-content">
      <div class="form-group">
        <label>{{ $t('dialog.save.fileName') }}：</label>
        <div class="u-inline">
          <u-input
            v-model="fileName"
            ref="fileNameInput"
            style="width: 480px"
          />
        </div>
      </div>

      <div class="form-group">
        <label>{{ $t('dialog.save.source') }}：</label>
        <div class="u-inline">
          <u-select
            v-model="selectedProvider"
            style="width:450px;"
            @change="handleProviderChange"
          >
            <u-option
              v-for="option in providerOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </div>
      </div>

      <div class="path-bar" v-if="currentPath || canGoBack">
        <div class="path-display">
          <span class="path-label">{{ $t('dialog.save.currentPath') }}：</span>
          <span class="path-text">{{ currentPath || '/' }}</span>
        </div>
        <u-button
          v-if="canGoBack"
          @click="goBack"
          type="primary"
          size="small"
        >
          <i class="iconfont icon-back"></i>
          {{ $t('dialog.save.backToParent') }}
        </u-button>
      </div>

      <div class="file-list-container">
        <table class="data-table">
          <thead>
            <tr class="data-table-header">
              <td><span>{{ $t('dialog.save.fileName') }}</span></td>
              <td style="width:200px;"><span>{{ $t('dialog.save.modDate') }}</span></td>
              <td style="width:50px;"><span>{{ $t('dialog.save.del') }}</span></td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(file, index) in currentReportFiles" :key="index" style="height: 35px;">
              <td>
                <span
                  :class="{ 'folder-name': file.directory }"
                  @click="handleFileClick(file)"
                  :style="{ cursor: file.directory ? 'pointer' : 'default' }"
                >
                  <i v-if="file.directory" class="iconfont icon-folder"></i>
                  {{ file.name }}
                </span>
              </td>
              <td><span>{{ formatDateItem(file.updateDate) }}</span></td>
              <td class="data-table-btn" v-if="!file.directory">
                <a @click.prevent="deleteFile(file, index)">
                  <i class="iconfont icon-delete del-button"></i>
                </a>
              </td>
              <td v-else></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <template #footer><div style="text-align: right">
      <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ $t('dialog.common.cancel') }}</u-button>
      <u-button @click="handleSave">{{ $t('dialog.save.save') }}</u-button>
    </div></template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { formatDate, resetDirty, tableToXml } from '@/utils/table.js'
import { saveReportFile, deleteReportFile, loadReportProviders, loadReportProvidersByPath } from '@/api/designer'
import { showAlert, showConfirm } from '@/utils/comnon.js'

defineOptions({ name: 'SaveDialog' })

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saveAfter', value: string): void
}>()

const props = withDefaults(defineProps<{
  visible?: boolean
}>(), {
  visible: false
})

const { t } = useI18n()
const store = useReportStore()

const fileName = ref('')
const selectedProvider = ref('')
const providers = ref<any[]>([])
const reportFilesData = ref<Record<string, any>>({})
const currentReportFiles = ref<any[]>([])
const currentProviderPrefix = ref('')
const currentPath = ref('')
const pathHistory = ref<string[]>([])

const context = computed(() => store.context)

const providerOptions = computed(() => {
  return providers.value.map((provider: any) => ({
    value: provider.prefix,
    label: provider.name
  }))
})

const canGoBack = computed(() => {
  return pathHistory.value.length > 0
})

watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadReports()
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function loadReports() {
  loadReportProviders()
    .then((response: any) => {
      let providersData
      if (response && Array.isArray(response)) {
        providersData = response
      } else if (response && response.data && Array.isArray(response.data)) {
        providersData = response.data
      } else {
        showAlert(t('dialog.save.loadFail'))
        return
      }

      if (!Array.isArray(providersData)) {
        showAlert(t('dialog.save.loadFail'))
        return
      }
      providers.value = providersData

      for (let provider of providersData) {
        let { reportFiles, prefix } = provider
        reportFilesData.value[prefix] = reportFiles || []
      }

      if (providers.value.length > 0) {
        selectedProvider.value = providers.value[0].prefix
        onProviderChange()
      }
    })
    .catch((error: any) => {
      if (error.msg) {
        showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
      } else {
        showAlert(t('dialog.save.loadFail'))
      }
    })
}

function loadProvidersByPath(path: string) {
  loadReportProvidersByPath(path)
    .then((result: any) => {
      for (let prefix in result) {
        let providerData = result[prefix]
        reportFilesData.value[`${prefix}:${path}`] = providerData.reportFiles
      }
      onProviderChange()
    })
    .catch((error: any) => {
      console.error('Error loading providers by path:', error)
      if (error.msg) {
        showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
      } else {
        showAlert(t('dialog.save.loadFail'))
      }
    })
}

function onProviderChange() {
  if (!selectedProvider.value || selectedProvider.value === '') {
    currentReportFiles.value = []
    return
  }

  const key = currentPath.value ? `${selectedProvider.value}:${currentPath.value}` : selectedProvider.value
  currentReportFiles.value = reportFilesData.value[key] || []
  currentProviderPrefix.value = selectedProvider.value
}

function handleProviderChange() {
  currentPath.value = ''
  pathHistory.value = []
  onProviderChange()
}

function handleFileClick(file: any) {
  if (file.directory) {
    pathHistory.value.push(currentPath.value)
    currentPath.value = file.path
    loadProvidersByPath(currentPath.value)
  }
}

function goBack() {
  if (pathHistory.value.length > 0) {
    currentPath.value = pathHistory.value.pop() || ''
    if (currentPath.value === '') {
      onProviderChange()
    } else {
      loadProvidersByPath(currentPath.value)
    }
  }
}

function deleteFile(file: any, index: number) {
  showConfirm(t('dialog.save.delConfirm') + file.name).then(() => {
    const fullFile = currentProviderPrefix.value + (file.path || file.name)

    deleteReportFile(fullFile)
      .then(() => {
        currentReportFiles.value.splice(index, 1)

        const reportFiles = reportFilesData.value[currentProviderPrefix.value]
        const dataIndex = reportFiles.findIndex((f: any) => f.name === file.name)
        if (dataIndex > -1) {
          reportFiles.splice(dataIndex, 1)
        }
      })
      .catch((error: any) => {
        console.error('删除文件失败:', error)
        if (error.msg) {
          showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
        } else {
          showAlert(t('dialog.save.delFail'))
        }
      })
  })
}

function handleSave() {
  if (fileName.value === '') {
    showAlert(t('dialog.save.nameTip'))
    return
  }

  if (!currentProviderPrefix.value || !currentReportFiles.value) {
    showAlert(t('dialog.save.locationTip'))
    return
  }

  for (let file of currentReportFiles.value) {
    if (!file.directory) {
      let fName = file.name
      let pos = fName.indexOf(".")
      fName = fName.substring(0, pos)
      if (fName === fileName.value) {
        showAlert(t('dialog.save.file') + '[' + fileName.value + ']' + t('dialog.save.exist'))
        return
      }
    }
  }

  let filePath = currentPath.value ? currentPath.value + '/' + fileName.value : fileName.value
  const fullFileName = currentProviderPrefix.value + filePath + ".ureport.xml"
  const content = tableToXml(context.value)

  saveReportFile(fullFileName, content)
    .then(() => {
      store.setSaveStatus(true)
      store.setFileName(fullFileName)
      resetDirty()
      showAlert(t('dialog.save.success')).then(() => {
        handleClose()
        emit('saveAfter', fullFileName)
      })
    })
    .catch((error: any) => {
      console.error('保存文件失败:', error)
      if (error.msg) {
        showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
      } else {
        showAlert(t('dialog.save.fail'))
      }
    })
}

function handleClose() {
  emit('update:visible', false)
  setTimeout(() => {
    fileName.value = ''
    selectedProvider.value = ''
    currentReportFiles.value = []
    currentPath.value = ''
    pathHistory.value = []
  }, 300)
}

function handleKeydown(e: KeyboardEvent) {
  if (props.visible) {
    if (e.key === 'Escape') {
      handleClose()
    }
  }
}

function formatDateItem(date: string) {
  return (formatDate as any)(date)
}
</script>

<style scoped>
.save-dialog-content {
  padding: 15px;
}
.form-group {
  margin-bottom: 15px;
}
.path-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border: 1px solid #ddd;
  margin-bottom: 10px;
  border-radius: 4px;
}
.path-display {
  display: flex;
  align-items: center;
}
.path-label {
  font-weight: bold;
  margin-right: 8px;
  color: #333;
}
.path-text {
  color: #666;
  font-family: monospace;
}
.file-list-container {
  height: 300px;
  overflow-y: auto;
  border: 1px solid #ddd;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table td {
  padding: 8px;
  border-bottom: 1px solid #eee;
}
.data-table-header {
  background: #f4f4f4;
  height: 30px;
  position: sticky;
  top: 0;
  z-index: 10;
}
.folder-name {
  color: #008ed3;
  font-weight: bold;
  cursor: pointer;
}
.folder-name:hover {
  text-decoration: underline;
}
.icon-folder {
  margin-right: 5px;
  color: #ffc107;
}
.del-button{
  color: red;
  font-size: 14pt;
  cursor: pointer
}
</style>
