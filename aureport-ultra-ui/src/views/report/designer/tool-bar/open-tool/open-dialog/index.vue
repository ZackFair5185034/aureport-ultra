<template>
  <UDialog
    :title="$t('dialog.open.title')"
    width="800px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="open-dialog-content">
      <div class="form-group">
        <label>{{ $t('dialog.open.source') }}：</label>
        <div class="u-inline">
          <u-select
            v-model="selectedProvider"
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
          icon="icon-left"
        >
          {{ $t('dialog.save.backToParent') }}
        </u-button>
      </div>

      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr class="data-table-header">
              <td><span>{{ $t('dialog.open.fileName') }}</span></td>
              <td style="width:200px;"><span>{{ $t('dialog.open.modDate') }}</span></td>
              <td style="width:50px;"><span>{{ $t('dialog.open.open') }}</span></td>
              <td style="width:50px;"><span>{{ $t('dialog.open.del') }}</span></td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(file, index) in currentFiles" :key="index" style="height: 35px;">
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
             <td><span>{{ formatDateLocal(file.updateDate) }}</span></td>
              <td class="data-table-btn">
                <a @click.stop="openFile(file)">
                  <i :class="file.directory ? 'iconfont icon-folder-open' : 'iconfont icon-open'" class="open-button"></i>
                </a>
              </td>
              <td class="data-table-btn" v-if="!file.directory">
                <a @click.stop="deleteFile(file, index)">
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
    </div></template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatDate } from '@/utils/table'
import { showAlert, showConfirm } from '@/utils/comnon'
import { loadReportProviders, loadReportProvidersByPath, deleteReportFile } from '@/api/designer'
import { createNavigator, getLibMode } from '@/lib/navigator'
import { useRouter, useRoute } from 'vue-router'

defineOptions({ name: 'OpenDialog' })

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'open-file', value: string): void
}>()

const props = withDefaults(defineProps<{
  visible?: boolean
}>(), {
  visible: false
})

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

const providers = ref<any[]>([])
const selectedProvider = ref('')
const reportFilesData = ref<Record<string, any>>({})
const currentFiles = ref<any[]>([])
const currentPath = ref('')
const pathHistory = ref<string[]>([])

const providerOptions = computed(() => {
  return providers.value.map((provider: any) => ({
    value: provider.prefix,
    label: provider.name
  }))
})

const canGoBack = computed(() => {
  return pathHistory.value.length > 0
})

const isLibMode = computed(() => {
  return getLibMode()
})

const navigator = computed(() => {
  return createNavigator({ $router: router, $route: route })
})

watch(() => props.visible, (val) => {
  if (val) {
    loadProviders()
  }
})

function loadProviders() {
  loadReportProviders()
    .then((response: any) => {
      providers.value = response

      for (let provider of response) {
        let { reportFiles, prefix } = provider
        reportFilesData.value[prefix] = reportFiles
      }

      if (providers.value.length > 0) {
        selectedProvider.value = providers.value[0].prefix
        onProviderChange()
      }
    })
    .catch((error: any) => {
      console.error('Error loading providers:', error)
      if (error.msg) {
        showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
      } else {
        showAlert(t('dialog.open.loadFail'))
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
        showAlert(t('dialog.open.loadFail'))
      }
    })
}

function onProviderChange() {
  if (!selectedProvider.value) {
    currentFiles.value = []
    return
  }

  const key = currentPath.value ? `${selectedProvider.value}:${currentPath.value}` : selectedProvider.value
  currentFiles.value = reportFilesData.value[key] || []
}

function handleProviderChange() {
  currentPath.value = ''
  pathHistory.value = []
  onProviderChange()
}

function formatDateLocal(date: string) {
  return (formatDate as any)(date, 'yyyy-MM-dd HH:mm:ss')
}

function openFile(file: any) {
  if (file.directory) {
    pathHistory.value.push(currentPath.value)
    currentPath.value = file.path
    loadProvidersByPath(currentPath.value)
    return
  }

  showConfirm(`${t('dialog.open.openConfirm')}[${file.name}]？`).then(function () {
    let fullFile = selectedProvider.value + encodeURI(encodeURI(file.path || file.name))

    handleClose()

    if (isLibMode.value) {
      ;(emit as any)('open-file', fullFile)
      ;(navigator.value as any).openDesigner({
        reportPath: fullFile
      }, false)
    } else {
      window.location.replace("?reportPath=" + fullFile)
    }
  })
}

function handleFileClick(file: any) {
  if (file.directory) {
    openFile(file)
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
  showConfirm(`${t('dialog.open.delConfirm')}` + file.name).then(function () {
    let fullFile = selectedProvider.value + (file.path || file.name)

    deleteReportFile(fullFile)
      .then(() => {
        currentFiles.value.splice(index, 1)

        let reportFiles = reportFilesData.value[selectedProvider.value]
        if (reportFiles) {
          let dataIndex = reportFiles.indexOf(file)
          if (dataIndex > -1) {
            reportFiles.splice(dataIndex, 1)
          }
        }
      })
      .catch((error: any) => {
        console.error('Error deleting file:', error)
        if (error.msg) {
          showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
        } else {
          showAlert(t('dialog.open.delFail'))
        }
      })
  })
}

function handleClose() {
  emit('update:visible', false)
  currentPath.value = ''
  pathHistory.value = []
}
</script>

<style scoped>
.open-dialog-content {
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
.table-container {
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
.icon-folder-open {
  margin-right: 5px;
  color: #ffc107;
}
.open-button{
  color: #008ed3;
  font-size: 14pt;
}
.del-button{
  color: red;
  font-size: 14pt;
  cursor: pointer
}
</style>
