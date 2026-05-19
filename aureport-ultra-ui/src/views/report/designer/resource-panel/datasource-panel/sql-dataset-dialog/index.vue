<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils'
import { useReportStore } from '@/stores/report'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'
import PreviewDataDialog from '@/views/report/designer/resource-panel/datasource-panel/preview-data-dialog/index.vue'
import ParameterEditor from './parameter-editor/index.vue'
import SearchTable from './search-table/index.vue'
import SqlEditor from './sql-editor/index.vue'

defineOptions({ name: 'SqlDatasetDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  db?: any
  datasetData?: any
}>(), {
  visible: false,
  db: null,
  datasetData: null,
})
const emit = defineEmits<{
  (e: 'save', name: string, oldName: string, sql: string, parameters: any[]): void
  (e: 'close'): void
}>()
const { t } = useI18n()
const store = useReportStore()

const datasetName = ref('')
const sql = ref('')
const parameters = ref<any[]>([])
const oldName = ref('')
const currentData = ref<any>({})
const previewDialogVisible = ref(false)
const previewParameters = ref<any>(null)
const sqlEditorRef = ref<any>(null)
const triggerLoadSearchTable = ref(false)

const context = computed(() => store.context)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    initDialog()
  }
})

function initDialog() {
  currentData.value = {}
  datasetName.value = ''
  sql.value = ''
  parameters.value = []
  oldName.value = ''

  if (props.datasetData) {
    currentData.value = { ...props.datasetData }
    datasetName.value = props.datasetData.name || ''
    sql.value = props.datasetData.sql || ''
    parameters.value = Array.isArray(props.datasetData.parameters) ? [...props.datasetData.parameters] : []
    currentData.value.parameters = parameters.value
    oldName.value = props.datasetData.name || ''
  }

  nextTick(() => {
    triggerLoadSearchTable.value = true
  })
}

function handleSearchTableLoadComplete() {
  triggerLoadSearchTable.value = false
}

function handleSqlChange(newSql: string) {
  sql.value = newSql || ''
}

function handleDatasetNameChange(newName: string) {
  datasetName.value = newName || ''
}

function handleDialogOpen() {
  nextTick(() => {
    sqlEditorRef.value?.refreshEditor()
  })
}

function handleAddSql(sqlText: string) {
  sqlEditorRef.value?.setSqlContent(sqlText || '')
}

function handleAddParameter(newParam: any) {
  parameters.value.push(newParam)
  currentData.value.parameters = [...parameters.value]
}

function handleEditParameter(index: number, updatedParam: any) {
  if (parameters.value && parameters.value[index]) {
    parameters.value[index] = { ...updatedParam }
    currentData.value.parameters = [...parameters.value]
  }
}

function handleRemoveParameter(index: number) {
  if (parameters.value) {
    parameters.value.splice(index, 1)
    currentData.value.parameters = [...parameters.value]
  }
}

function handlePreview() {
  const sqlText = sql.value || ''
  const type = props.db.type
  const params: any = {
    sql: sqlText,
    type,
    parameters: deepCopy(currentData.value.parameters),
  }

  if (type === 'jdbc') {
    params.username = props.db.username
    params.password = props.db.password
    params.driver = props.db.driver
    params.url = props.db.url
  }
  else if (type === 'buildin') {
    params.name = props.db.name
  }

  previewParameters.value = params
  previewDialogVisible.value = true
}

function handleConfirm() {
  const nameVal = datasetName.value || ''
  const sqlText = sql.value || ''

  if (!nameVal || nameVal === '') {
    showAlert(t('dialog.sql.nameTip'))
    return
  }

  if (!sqlText || sqlText === '') {
    showAlert(t('dialog.sql.sqlTip'))
    return
  }

  let check = false
  if (!oldName.value || nameVal !== oldName.value) {
    check = true
  }

  if (check) {
    for (const datasource of context.value!.reportDef.datasources) {
      const dsDatasets = datasource.datasets
      if (!dsDatasets || !Array.isArray(dsDatasets)) {
        continue
      }

      for (const dataset of dsDatasets) {
        if (dataset.name === nameVal) {
          showAlert(`${t('dialog.sql.ds')}[${nameVal}]${t('dialog.sql.exist')}`)
          return
        }
      }
    }
  }

  emit('save', nameVal, oldName.value, sqlText, currentData.value.parameters)
  setDirty()
  closeDialog()
}

function closeDialog() {
  emit('close')
}

function closePreviewDialog() {
  previewDialogVisible.value = false
}
</script>

<template>
  <div>
    <UDialog
      :title="$t('dialog.sql.title')"
      width="1080px"
      :visible="visible"
      :z-index="20000"
      @open="handleDialogOpen"
      @close="closeDialog"
    >
      <div class="dialog-content">
        <div class="content-layout">
          <!-- 左侧容器：搜索表格 -->
          <div class="left-panel">
            <SearchTable
              :db="db"
              :trigger-load="triggerLoadSearchTable"
              @add="handleAddSql"
              @load-complete="handleSearchTableLoadComplete"
            />
          </div>

          <!-- 右侧容器：SQL 编辑器和参数编辑器 -->
          <div class="right-panel">
            <SqlEditor
              ref="sqlEditorRef"
              :name="datasetName"
              :sql="sql"
              @sql-change="handleSqlChange"
              @dataset-name-change="handleDatasetNameChange"
            />
            <ParameterEditor
              :parameters="parameters"
              @add-parameter="handleAddParameter"
              @edit-parameter="handleEditParameter"
              @remove-parameter="handleRemoveParameter"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div style="text-align: right">
          <u-button type="info" style="margin-right: 10px;" @click="handlePreview">{{ $t('dialog.sql.preview') }}</u-button>
          <u-button @click="handleConfirm">{{ $t('dialog.sql.ok') }}</u-button>
        </div>
      </template>
    </UDialog>
    <PreviewDataDialog
      :visible="previewDialogVisible"
      :parameters="previewParameters"
      @close="closePreviewDialog"
    />
  </div>
</template>

<style scoped>
.content-layout {
  display: flex;
  gap: 15px;
  height: 100%;
}

.left-panel {
  flex: 0 0 300px;
  height: 100%;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
}
</style>
