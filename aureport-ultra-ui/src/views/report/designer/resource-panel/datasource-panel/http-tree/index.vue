<script setup lang="ts">
import { v1 as uuidv1 } from 'uuid'

import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils/index'
import { useReportStore } from '@/stores/report'
import { showAlert, showConfirm } from '@/utils/comnon'
import { addCell, getCell, setCell } from '@/utils/contextActions'
import { httpStandardProxy } from '@/api/designer/index'
import TableManager from '@/views/report/designer/edit-table/manager'
import HttpDatasetDialog from '@/views/report/designer/resource-panel/datasource-panel/http-dataset-dialog/index.vue'
import HttpDialog from '@/views/report/designer/resource-panel/datasource-panel/http-dialog/index.vue'
import ContextMenu from '../context-menu/index.vue'
import FieldNameDialog from '../field-name-dialog/index.vue'

defineOptions({ name: 'HttpTree' })

const props = withDefaults(defineProps<{
  name?: string
  datasets?: any[]
  datasources?: any[]
  protocolType?: string
  baseUrl?: string
  hostType?: string
  host?: string
  serviceName?: string
  headers?: string
}>(), {
  name: '',
  datasets: () => [],
  datasources: () => [],
  protocolType: 'standard',
  baseUrl: '',
  hostType: 'manual',
  host: '',
  serviceName: '',
  headers: '',
})

const emit = defineEmits<{
  (e: 'remove', name: string): void
  (e: 'update-datasource', data: any): void
  (e: 'update-datasets', datasets: any[]): void
}>()

const { t } = useI18n()
const store = useReportStore()

const id = `http_${uuidv1()}`
const datasourceExpanded = ref(true)
const datasetExpanded = ref<Record<number, boolean>>({})
const localName = ref(props.name)
const localDatasets = ref<any[]>(props.datasets)
const localProtocolType = ref(props.protocolType)
const localBaseUrl = ref(props.baseUrl)
const localHostType = ref(props.hostType)
const localHost = ref(props.host)
const localServiceName = ref(props.serviceName)
const localHeaders = ref(props.headers)
const currentDataset = ref<any>(null)
const currentField = ref<any>(null)
const httpDatasetDialogVisible = ref(false)
const httpStandardDatasetDialogVisible = ref(false)
const httpDialogVisible = ref(false)
const currentHttpDatasource = ref<any>(null)
const fieldNameDialogVisible = ref(false)
const contextMenu = ref<any>(null)

const context = computed(() => store.context)

onMounted(() => {
  if (localDatasets.value && localDatasets.value.length > 0) {
    for (let i = 0; i < localDatasets.value.length; i++) {
      datasetExpanded.value[i] = true
    }
  }
})

watch(() => props.name, (newName) => { localName.value = newName })
watch(() => props.protocolType, (v) => { localProtocolType.value = v })
watch(() => props.baseUrl, (v) => { localBaseUrl.value = v })
watch(() => props.hostType, (v) => { localHostType.value = v })
watch(() => props.host, (v) => { localHost.value = v })
watch(() => props.serviceName, (v) => { localServiceName.value = v })
watch(() => props.headers, (v) => { localHeaders.value = v })
watch(() => props.datasets, (newDatasets) => {
  localDatasets.value = newDatasets || []
}, { deep: true })

function toggleDatasource() { datasourceExpanded.value = !datasourceExpanded.value }

function toggleDataset(index: number) { datasetExpanded.value[index] = !datasetExpanded.value[index] }

function showDatasourceContextMenu(event: MouseEvent) {
  const items = [
    { key: 'add', name: t('tree.addDataset'), icon: 'add' },
    { key: 'edit', name: t('tree.edit'), icon: 'edit' },
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
  ]
  contextMenu.value.show(event, items, (key: string) => {
    switch (key) {
      case 'add':
        if (localProtocolType.value === 'standard') {
          currentDataset.value = null
          httpStandardDatasetDialogVisible.value = true
        } else {
          currentDataset.value = null
          httpDatasetDialogVisible.value = true
        }
        break
      case 'delete':
        showConfirm(`${t('tree.delConfirm')}[${props.name}]？`).then(() => emit('remove', props.name))
        break
      case 'edit':
        currentHttpDatasource.value = {
          name: localName.value,
          protocolType: localProtocolType.value,
          baseUrl: localBaseUrl.value,
          hostType: localHostType.value,
          host: localHost.value,
          serviceName: localServiceName.value,
          headers: localHeaders.value,
        }
        httpDialogVisible.value = true
        break
    }
  })
}

function showDatasetContextMenu(event: MouseEvent, dataset: any, index: number) {
  const items = [
    { key: 'refresh', name: t('tree.refresh'), icon: 'loading' },
    { key: 'edit', name: t('tree.edit'), icon: 'edit' },
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
  ]
  contextMenu.value.show(event, items, (key: string) => {
    switch (key) {
      case 'refresh':
        refreshDatasetFields(dataset, index)
        break
      case 'delete':
        showConfirm(`${t('tree.delDatasetConfirm')}[${dataset.name}]?`).then(() => {
          const newDatasets = deepCopy(localDatasets.value)
          newDatasets.splice(index, 1)
          delete datasetExpanded.value[index]
          emit('update-datasets', newDatasets)
        })
        break
      case 'edit':
        currentDataset.value = dataset
        if (localProtocolType.value === 'standard') {
          httpStandardDatasetDialogVisible.value = true
        } else {
          httpDatasetDialogVisible.value = true
        }
        break
    }
  })
}

function handleDatasetSave(nameVal: string, data: any, oldName: string) {
  const newDatasets = deepCopy(localDatasets.value)
  if (oldName && oldName !== '') {
    const index = newDatasets.findIndex((d: any) => d.name === oldName)
    if (index !== -1) {
      newDatasets[index] = { ...newDatasets[index], ...data }
      emit('update-datasets', newDatasets)
      return
    }
  }
  newDatasets.push(data)
  datasetExpanded.value[newDatasets.length - 1] = true
  emit('update-datasets', newDatasets)
}

function handleStandardDatasetSave(nameVal: string, beanMethod: string, clazz: string, oldName: string, beanId: string) {
  const newDatasets = deepCopy(localDatasets.value)
  if (oldName && oldName !== '') {
    const index = newDatasets.findIndex((d: any) => d.name === oldName)
    if (index !== -1) {
      newDatasets[index] = { ...newDatasets[index], name: nameVal, beanMethod, clazz, beanId, fields: [] }
      emit('update-datasets', newDatasets)
      if (clazz) {
        refreshDatasetFields(newDatasets[index], index)
      }
      return
    }
  }
  const dataset = { name: nameVal, beanMethod, clazz, beanId, fields: [] }
  newDatasets.push(dataset)
  datasetExpanded.value[newDatasets.length - 1] = true
  emit('update-datasets', newDatasets)
  // 新数据集自动加载字段
  if (clazz) {
    refreshDatasetFields(dataset, newDatasets.length - 1)
  }
}

async function refreshDatasetFields(dataset: any, index: number) {
  if (!dataset.clazz) return
  try {
    const result = await httpStandardProxy({
      datasourceName: localName.value,
      endpoint: 'datasource/buildClass',
      clazz: dataset.clazz,
      host: localHost.value,
      hostType: localHostType.value,
      serviceName: localServiceName.value,
    })
    const fields = (result || []) as any[]
    initFieldExpandState(fields)
    buildFieldPaths(fields)
    const newDatasets = deepCopy(localDatasets.value)
    const target = newDatasets.find((d: any) => d.name === dataset.name)
    if (target) {
      target.fields = fields
      emit('update-datasets', newDatasets)
    }
  } catch {
    showAlert(t('tree.loadFieldFail'))
  }
}

function initFieldExpandState(fieldList: any[]) {
  if (!fieldList) return
  for (const f of fieldList) {
    if (f.children && f.children.length > 0) {
      f._expanded = true
      initFieldExpandState(f.children)
    }
  }
}

function buildFieldPaths(fieldList: any[], parentPath = '') {
  for (const field of fieldList) {
    field.path = parentPath ? `${parentPath}.${field.name}` : field.name
    if (field.children?.length) {
      buildFieldPaths(field.children, field.path)
    }
  }
}

function toggleFieldChildren(field: any) {
  field._expanded = !field._expanded
}

function showFieldContextMenu(event: MouseEvent, dataset: any, field: any) {
  const items = [
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
  ]
  contextMenu.value.show(event, items, (key: string) => {
    if (key === 'delete') {
      showConfirm(`${t('tree.delFieldConfirm')}[${field.name}]?`).then(() => {
        const newDatasets = deepCopy(localDatasets.value)
        const target = newDatasets.find((d: any) => d.name === dataset.name)
        if (target && target.fields) {
          target.fields = target.fields.filter((f: any) => f.name !== field.name)
          emit('update-datasets', newDatasets)
        }
      })
    }
  })
}

function handleFieldDoubleClick(dataset: any, field: any) {
  const hot = TableManager.get()
  if (!hot) { showAlert(t('tree.cellTip')); return }
  const cellsMap = context.value?.cellsMap
  const selected = hot.getSelected()
  if (!selected || selected.length === 0) { showAlert(t('tree.cellTip')); return }
  const [rowIndex, colIndex, endRow, endCol] = selected[0]
  const cellDef = getCell(rowIndex, colIndex)
  let newCellDef: any = deepCopy(cellDef)
  if (newCellDef?.value?.type === 'dataset') {
    setCell(rowIndex, colIndex, newCellDef)
  } else {
    newCellDef = {
      value: { type: 'dataset', conditions: [] },
      rowNumber: cellDef?.rowNumber,
      columnNumber: cellDef?.columnNumber,
      cellStyle: cellDef?.cellStyle,
    }
    addCell(newCellDef)
  }
  newCellDef.expand = 'Down'
  const value = newCellDef.value
  value.aggregate = 'group'
  value.datasetName = dataset.name
  value.property = field.path || field.name
  value.order = 'none'
  hot.setDataAtCell(rowIndex, colIndex, `${value.datasetName}.${value.aggregate}(${value.property})`)
  hot.render()
  if (hot.hooks) hot.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)
}

function handleHttpDatasourceSave(datasourceData: any) {
  localName.value = datasourceData.name
  localProtocolType.value = datasourceData.protocolType
  localBaseUrl.value = datasourceData.baseUrl
  localHostType.value = datasourceData.hostType || 'manual'
  localHost.value = datasourceData.host || ''
  localServiceName.value = datasourceData.serviceName || ''
  localHeaders.value = datasourceData.headers || ''
  emit('update-datasource', datasourceData)
}
</script>

<template>
  <div class="tree" style="margin-left: 10px">
    <ul class="tree-root">
      <li>
        <span :id="id" class="ds-row" @click="toggleDatasource" @contextmenu.prevent.stop="showDatasourceContextMenu($event)">
          <i class="iconfont" :class="datasourceExpanded ? 'icon-minus-circle' : 'icon-plus-circle'" style="margin-right:2px" />
          <i class="iconfont icon-link" />
          <a href="###" class="ds_name">{{ localName }}</a>
          <span v-if="localProtocolType === 'standard'" class="protocol-badge standard">标准</span>
          <span v-else class="protocol-badge thirdparty">三方</span>
        </span>

        <ul v-show="datasourceExpanded" class="node-list">
          <li v-for="(dataset, index) in localDatasets" :key="`${dataset.name}_${index}`">
            <span @click="toggleDataset(index)" @contextmenu.prevent.stop="showDatasetContextMenu($event, dataset, index)">
              <i class="iconfont" :class="datasetExpanded[index] ? 'icon-minus-circle' : 'icon-plus-circle'" style="margin-right:2px" />
              <i class="iconfont icon-sqlds" />
              <a href="###" class="dataset_name">{{ dataset.name }}</a>
            </span>

            <ul v-show="datasetExpanded[index]" style="padding-left: 22px;">
              <template v-for="(field, fieldIndex) in dataset.fields || []" :key="`${field.name}_${fieldIndex}`">
                <li v-if="field.children && field.children.length > 0">
                  <span @click="toggleFieldChildren(field)">
                    <i class="iconfont" :class="field._expanded ? 'icon-minus-circle' : 'icon-plus-circle'" style="margin-right:2px" />
                    <i class="iconfont icon-property" />
                    <a href="###">{{ field.label ? `${field.name} (${field.label})` : field.name }}</a>
                  </span>
                  <ul v-show="field._expanded" style="padding-left: 22px;">
                    <li v-for="(child, childIndex) in field.children" :key="`${field.name}_${child.name}_${childIndex}`">
                      <span
                        :title="child.label ? `${child.name} (${child.label})` : child.name"
                        @dblclick="handleFieldDoubleClick(dataset, child)"
                        @contextmenu.prevent.stop="showFieldContextMenu($event, dataset, child)"
                      >
                        <i class="iconfont icon-property" />
                        <a href="###">{{ child.label ? `${child.name} (${child.label})` : child.name }}</a>
                      </span>
                    </li>
                  </ul>
                </li>
                <li v-else>
                  <span
                    :title="field.label ? `${field.name} (${field.label})` : field.name"
                    @dblclick="handleFieldDoubleClick(dataset, field)"
                    @contextmenu.prevent.stop="showFieldContextMenu($event, dataset, field)"
                  >
                    <i class="iconfont icon-property" />
                    <a href="###">{{ field.label ? `${field.name} (${field.label})` : field.name }}</a>
                  </span>
                </li>
              </template>
            </ul>
          </li>
        </ul>
      </li>
    </ul>

    <HttpDatasetDialog
      :visible="httpDatasetDialogVisible"
      :dataset="currentDataset"
      :datasources="datasources"
      :protocol-type="localProtocolType"
      @save="handleDatasetSave"
      @close="httpDatasetDialogVisible = false"
    />

    <HttpStandardDatasetDialog
      :visible="httpStandardDatasetDialogVisible"
      :dataset="currentDataset"
      :datasources="datasources"
      :datasource-name="localName"
      :host="localHost"
      :host-type="localHostType"
      :service-name="localServiceName"
      @save="handleStandardDatasetSave"
      @close="httpStandardDatasetDialogVisible = false"
    />

    <HttpDialog
      :datasources="datasources"
      :visible="httpDialogVisible"
      :datasource="currentHttpDatasource"
      @close="httpDialogVisible = false"
      @save="handleHttpDatasourceSave"
    />

    <FieldNameDialog
      :visible="fieldNameDialogVisible"
      :dataset="currentDataset"
      :field="currentField"
      :fields="currentDataset?.fields || []"
      @save="(fieldName: string) => {}"
      @close="fieldNameDialogVisible = false"
    />

    <ContextMenu ref="contextMenu" />
  </div>
</template>

<style scoped>
.tree a {
  text-decoration: none;
  margin-left: 4px;
  color: #000;
}
.ds-row {
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
}
.protocol-badge {
  font-size: 9px;
  padding: 0 3px;
  border-radius: 2px;
  margin-left: 4px;
  vertical-align: middle;
  line-height: 1.4;
}
.protocol-badge.standard {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}
.protocol-badge.thirdparty {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}
</style>
