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
          ></i>
          <i class="iconfont icon-database"></i>
          <a href="###" class="ds_name">{{ name }}</a>
        </span>

        <!-- 数据集列表 -->
        <ul
          v-show="datasourceExpanded"
          class="node-list"
        >
          <li
            v-for="(dataset, index) in datasets"
            :key="dataset.name + '_' + index"
          >
            <!-- 数据集节点 -->
            <span
              :id="'dataset_' + dataset.name + '_' + index"
              @click="toggleDataset(index)"
              @contextmenu.prevent.stop="showDatasetContextMenu($event, dataset, index)"
            >
              <i
                class="iconfont"
                :class="datasetExpanded[index] ? 'icon-minus-circle' : 'icon-plus-circle'"
                style="margin-right:2px"
              ></i>
              <i class="iconfont icon-sqlds"></i>
              <a href="###" class="dataset_name">{{ dataset.name }}</a>
            </span>

            <!-- 字段列表 -->
            <ul
              v-show="datasetExpanded[index]"
              style="padding-left: 22px;"
            >
              <li
                v-for="(field, fieldIndex) in dataset.fields"
                :key="field.name + '_' + fieldIndex"
              >
                <span
                  :id="'field_' + dataset.name + '_' + field.name + '_' + fieldIndex"
                  :title="$t('tree.doubleClick')"
                  @dblclick="handleFieldDoubleClick(dataset, field)"
                  @contextmenu.prevent.stop="showFieldContextMenu($event, dataset, field, fieldIndex)"
                >
                  <i class="iconfont icon-property"></i>
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
      ref="fieldNameDialog"
      :visible="fieldNameDialogVisible"
      :dataset="currentDataset"
      @save="handleFieldNameSave"
      @close="fieldNameDialogVisible = false"
    />

    <!-- 右键菜单 -->
    <ContextMenu ref="contextMenu" />
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, watch, onMounted } from 'vue'
import { useReportStore } from '@/stores/report'
import { useI18n } from 'vue-i18n'
import { v1 as uuidv1 } from 'uuid'
import { showAlert, showConfirm } from '@/utils/comnon.js'
import SqlDatasetDialog from '@/views/report/designer/resource-panel/datasource-panel/sql-dataset-dialog/index.vue'
import DatasourceDialog from '@/views/report/designer/resource-panel/datasource-panel/datasource-dialog/index.vue'
import FieldNameDialog from '../field-name-dialog/index.vue'
import ContextMenu from '../context-menu/index.vue'
import { buildJdbcFields } from '@/api/designer/index.js'
import { deepCopy } from '@/components/utils/index.js'
import { getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager.js'

defineOptions({ name: 'DatabaseTree' })

const { t } = useI18n()
const store = useReportStore()

const props = defineProps<{
  ds: any
  datasources: any[]
}>()

const emit = defineEmits<{
  (e: 'remove', name: string): void
  (e: 'update-datasource', data: any): void
}>()

const type = 'jdbc'
const id = uuidv1()
const name = ref(props.ds.name)
const username = ref(props.ds.username)
const password = ref(props.ds.password)
const driver = ref(props.ds.driver)
const url = ref(props.ds.url)
const datasets = ref<any[]>(props.ds.datasets || [])
const datasourceExpanded = ref(true)
const datasetExpanded = ref<Record<number, boolean>>({})
const currentDataset = ref<any>(null)
const datasourceDialogVisible = ref(false)
const currentDatasource = ref<any>(null)
const fieldNameDialogVisible = ref(false)
const sqlDatasetDialogVisible = ref(false)
const currentDbInfo = ref<any>(null)
const currentDatasetData = ref<any>(null)
const contextMenu = ref<any>(null)
const datasourceDialogRef = ref<any>(null)

const context = computed(() => store.context)

watch(() => props.ds, (newDs) => {
  if (newDs) {
    name.value = newDs.name
    username.value = newDs.username
    password.value = newDs.password
    driver.value = newDs.driver
    url.value = newDs.url
    datasets.value = newDs.datasets || []
    initDatasetExpanded()
  }
}, { deep: true })

onMounted(() => {
  initDatasetExpanded()
})

function initDatasetExpanded() {
  datasets.value.forEach((dataset, index) => {
    datasetExpanded.value[index] = true
    if (!dataset.fields) {
      buildFields(dataset, index)
    }
  })
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
    { key: 'delete', name: t('tree.del'), icon: 'delete' }
  ]

  if (contextMenu.value) {
    contextMenu.value.show(event, items, (key: string) => {
      if (key === 'add') {
        addDatasetAction()
      } else if (key === 'edit') {
        editDatasourceAction()
      } else if (key === 'delete') {
        deleteDatasourceAction()
      }
    })
  } else {
    console.error('contextMenu ref not found')
  }
}

function showDatasetContextMenu(event: MouseEvent, dataset: any, index: number) {
  const items = [
    { key: 'add', name: t('tree.addField'), icon: 'add' },
    { key: 'edit', name: t('tree.edit'), icon: 'edit' },
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
    { key: 'refresh', name: t('tree.refresh'), icon: 'loading' }
  ]

  if (contextMenu.value) {
    contextMenu.value.show(event, items, (key: string) => {
      if (key === 'add') {
        addFieldAction(dataset)
      } else if (key === 'edit') {
        editDatasetAction(dataset, index)
      } else if (key === 'delete') {
        deleteDatasetAction(dataset, index)
      } else if (key === 'refresh') {
        refreshDatasetAction(dataset, index)
      }
    })
  } else {
    console.error('contextMenu ref not found')
  }
}

function showFieldContextMenu(event: MouseEvent, dataset: any, field: any, fieldIndex: number) {
  const items = [
    { key: 'delete', name: t('tree.del'), icon: 'delete' }
  ]

  contextMenu.value.show(event, items, (key: string) => {
    if (key === 'delete') {
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
    type: type
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
  showConfirm(t('tree.delConfirm') + `[${name.value}]？`).then(() => {
    emit('remove', name.value)
  })
}

function addFieldAction(dataset: any) {
  currentDataset.value = dataset
  fieldNameDialogVisible.value = true
}

function handleFieldNameSave(fieldName: string, dataset: any) {
  if (fieldName) {
    if (!dataset.fields) {
      dataset.fields = []
    }

    const exists = dataset.fields.some((field: any) => field.name === fieldName)
    if (exists) {
      showAlert(t('tree.fieldExist'))
      return
    }

    const field = { name: fieldName }
    dataset.fields.push(field)
  }
}

function addDatasetAction() {
  currentDbInfo.value = {
    name: name.value,
    username: username.value,
    password: password.value,
    driver: driver.value,
    url: url.value,
    type: type,
    datasources: props.datasources
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
    type: type,
    datasources: props.datasources
  }
  currentDatasetData.value = dataset
  sqlDatasetDialogVisible.value = true
}

function deleteDatasetAction(dataset: any, index: number) {
  showConfirm(t('tree.delDatasetConfirm') + `[${dataset.name}]?`).then(() => {
    datasets.value.splice(index, 1)
    delete datasetExpanded.value[index]
  })
}

function refreshDatasetAction(dataset: any, index: number) {
  dataset.fields = null
  buildFields(dataset, index)
}

function deleteFieldAction(dataset: any, field: any, fieldIndex: number) {
  showConfirm(t('tree.delFieldConfirm') + `[${field.name}]?`).then(() => {
    if (dataset.fields) {
      dataset.fields.splice(fieldIndex, 1)
    }
  })
}

function handleFieldDoubleClick(dataset: any, field: any) {
  _buildClickEvent(dataset, field, context.value)
}

async function buildFields(dataset: any, index: number) {
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
    type: 'jdbc'
  }

  try {
    const fields = await buildJdbcFields(params)
    dataset.fields = fields
  } catch (error: any) {
    if (error.msg) {
      showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
    } else {
      showAlert(t('tree.loadFieldFail'))
    }
  }
}

function handleSqlDatasetSave(nameVal: string, oldName: string, sql: string, parameters: any[]) {
  const datasourceData = {
    name: name.value,
    oldName: name.value,
    username: username.value,
    password: password.value,
    driver: driver.value,
    url: url.value,
    type: type,
    datasets: datasets.value.map(dataset => ({ ...dataset }))
  }

  let dataset = datasourceData.datasets.find((d: any) => d.name === oldName)
  if (dataset) {
    dataset.name = nameVal
    dataset.sql = sql
    dataset.parameters = parameters
    dataset.fields = null
  } else {
    dataset = { name: nameVal, sql, parameters }
    datasourceData.datasets.push(dataset)
  }
  emit('update-datasource', datasourceData)
  buildFields(dataset, -1)
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
  const cellDef = getCell(rowIndex, colIndex)

  const oldCellDef = deepCopy(cellDef)

  let newCellDef: any
  if (cellDef.value.type !== 'dataset') {
    newCellDef = {
      value: { type: 'dataset', conditions: [] },
      rowNumber: cellDef.rowNumber,
      columnNumber: cellDef.columnNumber,
      cellStyle: cellDef.cellStyle
    }
  } else {
    newCellDef = deepCopy(cellDef)
  }

  newCellDef.expand = 'Down'
  const value = newCellDef.value
  value.aggregate = 'group'
  value.datasetName = dataset.name
  value.property = field.name
  value.order = 'none'

  let text = value.datasetName + '.' + value.aggregate + '('
  const prop = value.property
  text += prop + ')'

  setCell(rowIndex, colIndex, newCellDef)
  hot.setDataAtCell(rowIndex, colIndex, text)

  if (window.setDirty) {
    window.setDirty()
  }

  hot.render()

  if (window.Handsontable && window.Handsontable.hooks) {
    window.Handsontable.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)
  }

  if (window.undoManager) {
    window.undoManager.add({
      redo: () => {
        const currentCellDef = getCell(rowIndex, colIndex)
        let redoCellDef: any
        if (currentCellDef.value.type !== 'dataset') {
          redoCellDef = {
            value: { type: 'dataset', conditions: [] },
            rowNumber: currentCellDef.rowNumber,
            columnNumber: currentCellDef.columnNumber,
            cellStyle: currentCellDef.cellStyle
          }
        } else {
          redoCellDef = deepCopy(currentCellDef)
        }
        redoCellDef.expand = 'Down'
        const redoValue = redoCellDef.value
        redoValue.aggregate = 'group'
        redoValue.datasetName = dataset.name
        redoValue.property = field.name
        redoValue.order = 'none'

        let redoText = redoValue.datasetName + '.' + redoValue.aggregate + '('
        redoText += redoValue.property + ')'
        setCell(rowIndex, colIndex, redoCellDef)
        hot.setDataAtCell(rowIndex, colIndex, redoText)
        if (window.setDirty) window.setDirty()
        hot.render()
        if (window.Handsontable && window.Handsontable.hooks) {
          window.Handsontable.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)
        }
      },
      undo: () => {
        setCell(rowIndex, colIndex, oldCellDef)
        const val = oldCellDef.value
        let text = val.value || ''
        if (val.type === 'dataset') {
          text = val.datasetName + '.' + val.aggregate + '('
          text += val.property + ')'
        }
        hot.setDataAtCell(rowIndex, colIndex, text)
        if (window.setDirty) window.setDirty()
        hot.render()
        if (window.Handsontable && window.Handsontable.hooks) {
          window.Handsontable.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)
        }
      }
    })
  }
}
</script>
<style scoped>
.tree{
  a {
    text-decoration: none;
    margin-left: 4px;
    color: #000;
  }
}
</style>
