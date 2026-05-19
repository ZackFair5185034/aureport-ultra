<script setup lang="ts">
import { v1 as uuidv1 } from 'uuid'

import { useI18n } from 'vue-i18n'
import { buildJdbcFields } from '@/api/designer/index'
import { deepCopy } from '@/components/utils/index'
import { useReportStore } from '@/stores/report'
import { showAlert, showConfirm } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager'
import DatasourceDialog from '@/views/report/designer/resource-panel/datasource-panel/datasource-dialog/index.vue'
import SqlDatasetDialog from '@/views/report/designer/resource-panel/datasource-panel/sql-dataset-dialog/index.vue'
import ContextMenu from '../context-menu/index.vue'
import FieldNameDialog from '../field-name-dialog/index.vue'

defineOptions({ name: 'DatabaseTree' })

const props = defineProps<{
  ds: any
  datasources: any[]
  datasets?: any[]
}>()
const emit = defineEmits<{
  (e: 'remove', name: string): void
  (e: 'update-datasource', data: any): void
  (e: 'update-datasets', datasets: any[]): void
}>()
const { t } = useI18n()
const store = useReportStore()

const type = 'jdbc'
const id = uuidv1()
const name = ref(props.ds.name)
const username = ref(props.ds.username)
const password = ref(props.ds.password)
const driver = ref(props.ds.driver)
const url = ref(props.ds.url)
const datasets = ref<any[]>(props.datasets || [])
const datasourceExpanded = ref(true)
const datasetExpanded = ref<Record<number, boolean>>({})
const currentDataset = ref<any>(null)
const datasourceDialogVisible = ref(false)
const currentDatasource = ref<any>(null)
const fieldNameDialogVisible = ref(false)
const currentField = ref<any>(null)
const sqlDatasetDialogVisible = ref(false)
const currentDbInfo = ref<any>(null)
const currentDatasetData = ref<any>(null)
const contextMenu = ref<any>(null)
const datasourceDialogRef = ref<any>(null)

const context = computed(() => store.context)

watch(() => props.datasets, (newDatasets) => {
  if (newDatasets) {
    datasets.value = newDatasets
    initDatasetExpanded()
  }
}, { deep: true, immediate: true })

watch(() => props.ds, (newDs) => {
  if (newDs) {
    name.value = newDs.name
    username.value = newDs.username
    password.value = newDs.password
    driver.value = newDs.driver
    url.value = newDs.url
  }
}, { deep: true })


function initDatasetExpanded() {
  for (const [index, dataset] of datasets.value.entries()) {
    datasetExpanded.value[index] = true
    if (!dataset.fields) {
      buildFields(dataset, index)
    }
  }
}

function toggleDatasource() {
  datasourceExpanded.value = !datasourceExpanded.value
}

function toggleDataset(index: number) {
  datasetExpanded.value[index] = !datasetExpanded.value[index]
}

function showDatasourceContextMenu(event: MouseEvent) {
  const items = [
    { key: 'add', name: t('tree.addDataset'), icon: 'add' },
    { key: 'edit', name: t('tree.edit'), icon: 'edit' },
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
  ]

  if (contextMenu.value) {
    contextMenu.value.show(event, items, (key: string) => {
      switch (key) {
        case 'add': {
          addDatasetAction()

          break
        }

        case 'edit': {
          editDatasourceAction()

          break
        }

        case 'delete': {
          deleteDatasourceAction()

          break
        }
      // No default
      }
    })
  }
  else {
    console.error('contextMenu ref not found')
  }
}

function showDatasetContextMenu(event: MouseEvent, dataset: any, index: number) {
  const items = [
    { key: 'add', name: t('tree.addField'), icon: 'add' },
    { key: 'edit', name: t('tree.edit'), icon: 'edit' },
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
    { key: 'refresh', name: t('tree.refresh'), icon: 'loading' },
  ]

  if (contextMenu.value) {
    contextMenu.value.show(event, items, (key: string) => {
      switch (key) {
        case 'add': {
          addFieldAction(dataset)

          break
        }

        case 'edit': {
          editDatasetAction(dataset, index)

          break
        }

        case 'delete': {
          deleteDatasetAction(dataset, index)

          break
        }

        case 'refresh': {
          refreshDatasetAction(dataset, index)

          break
        }
      // No default
      }
    })
  }
  else {
    console.error('contextMenu ref not found')
  }
}

function showFieldContextMenu(event: MouseEvent, dataset: any, field: any, fieldIndex: number) {
  const items = [
    { key: 'edit', name: t('tree.edit'), icon: 'edit' },
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
  ]

  contextMenu.value.show(event, items, (key: string) => {
    if (key === 'edit') {
      editFieldAction(dataset, field)
    }
    else if (key === 'delete') {
      deleteFieldAction(dataset, field, fieldIndex)
    }
  })
}

function editDatasourceAction() {
  currentDatasource.value = {
    name: name.value,
    username: username.value,
    password: password.value,
    driver: driver.value,
    url: url.value,
    type,
  }
  datasourceDialogVisible.value = true
}

function handleDatasourceSave(datasourceData: any) {
  name.value = datasourceData.name
  username.value = datasourceData.username
  password.value = datasourceData.password
  driver.value = datasourceData.driver
  url.value = datasourceData.url

  emit('update-datasource', datasourceData)
}

function deleteDatasourceAction() {
  showConfirm(`${t('tree.delConfirm')}[${name.value}]？`).then(() => {
    emit('remove', name.value)
  })
}

function addFieldAction(dataset: any) {
  currentDataset.value = dataset
  currentField.value = null
  fieldNameDialogVisible.value = true
}

function editFieldAction(dataset: any, field: any) {
  currentDataset.value = dataset
  currentField.value = field
  fieldNameDialogVisible.value = true
}

function handleFieldNameSave(fieldName: string, dataset: any, label?: string) {
  if (fieldName) {
    const newDatasets = deepCopy(datasets.value)
    const targetDataset = newDatasets.find((d: any) => d.name === dataset.name)
    if (!targetDataset) return

    if (!targetDataset.fields) {
      targetDataset.fields = []
    }

    const editField = currentField.value
    if (editField) {
      const nameConflict = editField.name !== fieldName
        && targetDataset.fields.some((f: any) => f.name === fieldName)
      if (nameConflict) {
        showAlert(t('tree.fieldExist'))
        return
      }
      const oldField = targetDataset.fields.find((f: any) => f.name === editField.name)
      if (oldField) {
        oldField.name = fieldName
        if (label) {
          oldField.label = label
        }
        else {
          delete oldField.label
        }
      }
    }
    else {
      const exists = targetDataset.fields.some((field: any) => field.name === fieldName)
      if (exists) {
        showAlert(t('tree.fieldExist'))
        return
      }
      const field: any = { name: fieldName }
      if (label) {
        field.label = label
      }
      targetDataset.fields.push(field)
    }

    currentField.value = null
    datasets.value = newDatasets
    emit('update-datasets', newDatasets)
  }
}

function addDatasetAction() {
  currentDbInfo.value = {
    name: name.value,
    username: username.value,
    password: password.value,
    driver: driver.value,
    url: url.value,
    type,
    datasources: props.datasources,
  }
  currentDatasetData.value = { parameters: [] }
  sqlDatasetDialogVisible.value = true
}

function editDatasetAction(dataset: any, index: number) {
  currentDbInfo.value = {
    name: name.value,
    username: username.value,
    password: password.value,
    driver: driver.value,
    url: url.value,
    type,
    datasources: props.datasources,
  }
  currentDatasetData.value = dataset
  sqlDatasetDialogVisible.value = true
}

function deleteDatasetAction(dataset: any, index: number) {
  showConfirm(`${t('tree.delDatasetConfirm')}[${dataset.name}]?`).then(() => {
    const newDatasets = deepCopy(datasets.value)
    newDatasets.splice(index, 1)
    delete datasetExpanded.value[index]

    const datasourceData = {
      name: name.value,
      oldName: name.value,
      username: username.value,
      password: password.value,
      driver: driver.value,
      url: url.value,
      type,
      datasets: newDatasets.map((d: any) => ({ ...d })),
    }

    emit('update-datasource', datasourceData)
  })
}

async function refreshDatasetAction(dataset: any, index: number) {
  dataset.fields = null
  await buildFields(dataset, index)
  emitDatasourceUpdate()
}

function emitDatasourceUpdate() {
  const datasourceData = {
    name: name.value,
    oldName: name.value,
    username: username.value,
    password: password.value,
    driver: driver.value,
    url: url.value,
    type,
    datasets: datasets.value.map((d: any) => ({ ...d })),
  }

  emit('update-datasource', datasourceData)
}

function deleteFieldAction(dataset: any, field: any, fieldIndex: number) {
  showConfirm(`${t('tree.delFieldConfirm')}[${field.name}]?`).then(() => {
    if (dataset.fields) {
      dataset.fields.splice(fieldIndex, 1)
      emitDatasourceUpdate()
    }
  })
}

function handleFieldDoubleClick(dataset: any, field: any) {
  _buildClickEvent(dataset, field, context.value)
}

async function handleSqlDatasetSave(nameVal: string, oldName: string, sql: string, parameters: any[]) {
  const datasourceData = {
    name: name.value,
    oldName: name.value,
    username: username.value,
    password: password.value,
    driver: driver.value,
    url: url.value,
    type,
    datasets: datasets.value.map(dataset => ({ ...dataset })),
  }

  let dataset = datasourceData.datasets.find((d: any) => d.name === oldName)
  if (dataset) {
    dataset.name = nameVal
    dataset.sql = sql
    dataset.parameters = parameters
    dataset.fields = null
  }
  else {
    dataset = { name: nameVal, sql, parameters }
    datasourceData.datasets.push(dataset)
  }

  // 先加载字段再 emit
  await buildFields(dataset, -1, datasourceData.datasets)
  emit('update-datasource', datasourceData)
}

async function buildFields(dataset: any, index: number, targetDatasets?: any[]) {
  const defaultFields = dataset.fields

  if (defaultFields) {
    return
  }

  const params = {
    sql: dataset.sql,
    parameters: JSON.stringify(dataset.parameters || []),
    username: username.value,
    password: password.value,
    driver: driver.value,
    url: url.value,
    type: 'jdbc',
  }

  try {
    const fields = await buildJdbcFields(params)
    dataset.fields = fields
    // 如果传入了目标数据集数组，直接 emit 持久化
    if (targetDatasets) {
      const datasourceData = {
        name: name.value,
        oldName: name.value,
        username: username.value,
        password: password.value,
        driver: driver.value,
        url: url.value,
        type,
        datasets: targetDatasets.map((d: any) => ({ ...d })),
      }
      emit('update-datasource', datasourceData)
    }
  }
  catch (error: any) {
    if (error.msg) {
      showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
    }
    else {
      showAlert(t('tree.loadFieldFail'))
    }
  }
}

function _buildClickEvent(dataset: any, field: any, ctx: any) {
  const hot = TableManager.get()
  if (!hot) {
    showAlert(t('tree.cellTip'))
    return
  }

  const cellsMap = ctx.cellsMap
  const selected = hot.getSelected()

  if (!selected || selected.length === 0) {
    showAlert(t('tree.cellTip'))
    return
  }

  const [rowIndex, colIndex, endRow, endCol] = selected[0]
  const cellDef = getCell(rowIndex, colIndex)!

  const oldCellDef: any = deepCopy(cellDef)

  const newCellDef: any = cellDef.value.type === 'dataset'
    ? deepCopy(cellDef)
    : {
        value: { type: 'dataset', conditions: [] },
        rowNumber: cellDef.rowNumber,
        columnNumber: cellDef.columnNumber,
        cellStyle: cellDef.cellStyle,
      }

  newCellDef.expand = 'Down'
  const value = newCellDef.value
  value.aggregate = 'group'
  value.datasetName = dataset.name
  value.property = field.name
  value.order = 'none'

  let text = `${value.datasetName}.${value.aggregate}(`
  const prop = value.property
  text += `${prop})`

  setCell(rowIndex, colIndex, newCellDef)
  hot.setDataAtCell(rowIndex, colIndex, text)

  ;(window as any).setDirty?.()

  hot.render()

  ;(window as any).Handsontable?.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)

  ;(window as any).undoManager?.add({
    redo: () => {
      const currentCellDef = getCell(rowIndex, colIndex)!
      const redoCellDef: any = currentCellDef.value.type === 'dataset'
        ? deepCopy(currentCellDef)
        : {
            value: { type: 'dataset', conditions: [] },
            rowNumber: currentCellDef.rowNumber,
            columnNumber: currentCellDef.columnNumber,
            cellStyle: currentCellDef.cellStyle,
          }
      redoCellDef.expand = 'Down'
      const redoValue = redoCellDef.value
      redoValue.aggregate = 'group'
      redoValue.datasetName = dataset.name
      redoValue.property = field.name
      redoValue.order = 'none'

      let redoText = `${redoValue.datasetName}.${redoValue.aggregate}(`
      redoText += `${redoValue.property})`
      setCell(rowIndex, colIndex, redoCellDef)
      hot.setDataAtCell(rowIndex, colIndex, redoText)
      ;(window as any).setDirty?.()
      hot.render()
      ;(window as any).Handsontable?.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)
    },
    undo: () => {
      setCell(rowIndex, colIndex, oldCellDef)
      const val = oldCellDef.value
      let text = val.value || ''
      if (val.type === 'dataset') {
        text = `${val.datasetName}.${val.aggregate}(`
        text += `${val.property})`
      }

      hot.setDataAtCell(rowIndex, colIndex, text)
      ;(window as any).setDirty?.()
      hot.render()
      ;(window as any).Handsontable?.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)
    },
  })
}
</script>

<template>
  <div class="tree" style="margin-left: 10px">
    <ul class="tree-root">
      <li>
        <!-- 数据源节点 -->
        <span
          :id="id"
          @click="toggleDatasource"
          @contextmenu.prevent.stop="showDatasourceContextMenu($event)"
        >
          <i
            class="iconfont"
            :class="datasourceExpanded ? 'icon-minus-circle' : 'icon-plus-circle'"
            style="margin-right:2px"
          />
          <i class="iconfont icon-database" />
          <a href="###" class="ds_name">{{ name }}</a>
        </span>

        <!-- 数据集列表 -->
        <ul
          v-show="datasourceExpanded"
          class="node-list"
        >
          <li
            v-for="(dataset, index) in datasets"
            :key="`${dataset.name}_${index}`"
          >
            <!-- 数据集节点 -->
            <span
              :id="`dataset_${dataset.name}_${index}`"
              @click="toggleDataset(index)"
              @contextmenu.prevent.stop="showDatasetContextMenu($event, dataset, index)"
            >
              <i
                class="iconfont"
                :class="datasetExpanded[index] ? 'icon-minus-circle' : 'icon-plus-circle'"
                style="margin-right:2px"
              />
              <i class="iconfont icon-sqlds" />
              <a href="###" class="dataset_name">{{ dataset.name }}</a>
            </span>

            <!-- 字段列表 -->
            <ul
              v-show="datasetExpanded[index]"
              style="padding-left: 22px;"
            >
              <li
                v-for="(field, fieldIndex) in dataset.fields"
                :key="`${field.name}_${fieldIndex}`"
              >
                <span
                  :id="`field_${dataset.name}_${field.name}_${fieldIndex}`"
                  :title="$t('tree.doubleClick')"
                  @dblclick="handleFieldDoubleClick(dataset, field)"
                  @contextmenu.prevent.stop="showFieldContextMenu($event, dataset, field, fieldIndex)"
                >
                  <i class="iconfont icon-property" />
                  <a href="###">{{ field.name }}</a>
                </span>
              </li>
            </ul>
          </li>
        </ul>
      </li>
    </ul>

    <!-- SQL数据集对话框 -->
    <SqlDatasetDialog
      :visible="sqlDatasetDialogVisible"
      :db="currentDbInfo"
      :datasetData="currentDatasetData"
      @save="handleSqlDatasetSave"
      @close="sqlDatasetDialogVisible = false"
    />

    <!-- 数据源对话框 -->
    <DatasourceDialog
      ref="datasourceDialogRef"
      :visible="datasourceDialogVisible"
      :datasources="datasources"
      :datasource="currentDatasource"
      @close="datasourceDialogVisible = false"
      @save="handleDatasourceSave"
    />

    <!-- 字段名输入对话框 -->
    <FieldNameDialog
      :visible="fieldNameDialogVisible"
      :dataset="currentDataset"
      :field="currentField"
      @save="handleFieldNameSave"
      @close="fieldNameDialogVisible = false"
    />

    <!-- 右键菜单 -->
    <ContextMenu ref="contextMenu" />
  </div>
</template>

<style scoped>
.tree {
  a {
    text-decoration: none;
    margin-left: 4px;
    color: #000;
  }
}
</style>
