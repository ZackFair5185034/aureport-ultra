<script setup lang="ts">
import { v1 as uuidv1 } from 'uuid'
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { buildFields as apiBuildFields } from '@/api/designer/index'
import { deepCopy } from '@/components/utils/index'
import { useReportStore } from '@/stores/report'
import { showAlert, showConfirm } from '@/utils/comnon'
import { addCell, getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager'
import SqlDatasetDialog from '@/views/report/designer/resource-panel/datasource-panel/sql-dataset-dialog/index.vue'
import ContextMenu from '../context-menu/index.vue'
import FieldNameDialog from '../field-name-dialog/index.vue'

defineOptions({ name: 'BuildinTree' })

const props = withDefaults(defineProps<{
  name?: string
  datasets?: any[]
}>(), {
  name: '',
  datasets: () => [],
})
const emit = defineEmits<{
  (e: 'remove', name: string): void
  (e: 'update-datasource', data: any): void
}>()
const { t } = useI18n()
const store = useReportStore()

const id = `buildin_${uuidv1()}`
const datasourceExpanded = ref(true)
const datasetExpanded = ref<Record<number, boolean>>({})
const localDatasets = ref<any[]>(props.datasets)
const currentDataset = ref<any>(null)
const fieldNameDialogVisible = ref(false)
const sqlDatasetDialogVisible = ref(false)
const currentDbInfo = ref<any>(null)
const currentDatasetData = ref<any>(null)
const contextMenu = ref<any>(null)

const context = computed(() => store.context)

watch(() => props.datasets, (newDatasets) => {
  localDatasets.value = newDatasets || []
}, { deep: true })

onMounted(() => {
  if (localDatasets.value && localDatasets.value.length > 0) {
    for (let i = 0; i < localDatasets.value.length; i++) {
      datasetExpanded.value[i] = true
    }
  }
})

function toggleDatasource() {
  datasourceExpanded.value = !datasourceExpanded.value
}

function toggleDataset(index: number) {
  datasetExpanded.value[index] = !datasetExpanded.value[index]
}

function showDatasourceContextMenu(event: MouseEvent) {
  const items = [
    { key: 'add', name: t('tree.addDataset'), icon: 'add' },
    { key: 'delete', name: t('tree.delete'), icon: 'delete' },
  ]

  contextMenu.value.show(event, items, (key: string) => {
    handleDatasourceMenuAction(key)
  })
}

function handleDatasourceMenuAction(key: string) {
  if (key === 'add') {
    addDatasetAction()
  }
  else if (key === 'delete') {
    deleteDatasourceAction()
  }
}

function addDatasetAction() {
  currentDbInfo.value = {
    type: 'buildin',
    name: props.name,
  }
  currentDatasetData.value = { parameters: [] }
  sqlDatasetDialogVisible.value = true
}

function deleteDatasourceAction() {
  showConfirm(`${t('tree.delConfirm')}[${props.name}]？`).then(() => {
    emit('remove', props.name)
  })
}

function showDatasetContextMenu(event: MouseEvent, dataset: any, index: number) {
  const items = [
    { key: 'addField', name: t('tree.addField'), icon: 'add' },
    { key: 'edit', name: t('tree.edit'), icon: 'edit' },
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
    { key: 'refresh', name: t('tree.refresh'), icon: 'loading' },
  ]

  contextMenu.value.show(event, items, (key: string) => {
    handleDatasetMenuAction(key, dataset, index)
  })
}

function handleDatasetMenuAction(key: string, dataset: any, index: number) {
  switch (key) {
    case 'addField': {
      addFieldAction(dataset)

      break
    }

    case 'delete': {
      deleteDatasetAction(dataset, index)

      break
    }

    case 'edit': {
      editDatasetAction(dataset, index)

      break
    }

    case 'refresh': {
      refreshDatasetAction(dataset, index)

      break
    }
  // No default
  }
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

    const newDatasets = deepCopy(localDatasets.value)
    const targetDataset = newDatasets.find((ds: any) => ds.name === dataset.name)
    if (!targetDataset.fields) {
      targetDataset.fields = []
    }

    const exists = targetDataset.fields.some((field: any) => field.name === fieldName)
    if (exists) {
      showAlert(t('tree.fieldExist'))
      return
    }

    const field = { name: fieldName }
    targetDataset.fields.push(field)
    emit('update-datasource', {
      name: props.name,
      oldName: props.name,
      datasets: newDatasets,
    })
  }
}

function editDatasetAction(dataset: any, index: number) {
  currentDbInfo.value = {
    type: 'buildin',
    name: props.name,
  }
  currentDatasetData.value = dataset
  sqlDatasetDialogVisible.value = true
}

function deleteDatasetAction(dataset: any, index: number) {
  showConfirm(`${t('tree.delDatasetConfirm')}[${dataset.name}]?`).then(() => {
    const newDatasets = deepCopy(localDatasets.value)
    newDatasets.splice(index, 1)
    delete datasetExpanded.value[index]
    emit('update-datasource', {
      name: props.name,
      oldName: props.name,
      datasets: newDatasets,
    })
  })
}

function refreshDatasetAction(dataset: any, index: number) {
  dataset.fields = null
  buildFields(dataset, index)
}

function showFieldContextMenu(event: MouseEvent, dataset: any, field: any, fieldIndex: number) {
  const items = [
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
  ]

  contextMenu.value.show(event, items, (key: string) => {
    handleFieldMenuAction(key, dataset, field, fieldIndex)
  })
}

function handleFieldMenuAction(key: string, dataset: any, field: any, fieldIndex: number) {
  if (key === 'delete') {
    deleteFieldAction(dataset, field, fieldIndex)
  }
}

function deleteFieldAction(dataset: any, field: any, fieldIndex: number) {
  showConfirm(`${t('tree.delFieldConfirm')}[${field.name}]？`).then(() => {
    const newDatasets = deepCopy(localDatasets.value)
    const targetDataset = newDatasets.find((ds: any) => ds.name === dataset.name)
    if (targetDataset && targetDataset.fields) {
      targetDataset.fields.splice(fieldIndex, 1)
      emit('update-datasource', {
        name: props.name,
        oldName: props.name,
        datasets: newDatasets,
      })
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

  const parameters = {
    sql: dataset.sql,
    parameters: JSON.stringify(dataset.parameters),
    name: props.name,
    type: 'buildin',
  }

  try {
    const fields = await apiBuildFields(parameters)
    const newDatasets = deepCopy(localDatasets.value)
    const targetDataset = newDatasets.find((ds: any) => ds.name === dataset.name)
    if (targetDataset) {
      targetDataset.fields = fields
      emit('update-datasource', {
        name: props.name,
        oldName: props.name,
        datasets: newDatasets,
      })
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

  if (cellDef.value.type !== 'dataset') {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value = { type: 'dataset', conditions: [] }
    addCell(newCellDef)
  }

  const newCellDef = deepCopy(cellDef)
  newCellDef.expand = 'Down'
  const value = newCellDef.value
  value.aggregate = 'group'
  value.datasetName = dataset.name
  value.property = field.name
  value.order = 'none'

  let text = `${value.datasetName}.${value.aggregate}(`
  const prop = value.property
  text += `${prop})`
  hot.setDataAtCell(rowIndex, colIndex, text)

  setCell(rowIndex, colIndex, newCellDef)

  hot.render()

  if (hot.hooks) {
    hot.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)
  }
}

function handleSqlDatasetSave(nameVal: string, oldName: string, sql: string, parameters: any[]) {
  const newDatasets = deepCopy(localDatasets.value)

  let dataset = newDatasets.find((d: any) => d.name === oldName)
  if (dataset) {
    dataset.name = nameVal
    dataset.sql = sql
    dataset.parameters = parameters
    dataset.fields = null
  }
  else {
    dataset = { name: nameVal, sql, parameters }
    newDatasets.push(dataset)
  }

  emit('update-datasource', {
    name: props.name,
    oldName: props.name,
    datasets: newDatasets,
  })
  buildFields(dataset, -1)
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
          <i class="iconfont icon-share" />
          <a href="###" class="ds_name">{{ name }}</a>
        </span>

        <!-- 数据集列表 -->
        <ul
          v-show="datasourceExpanded"
          class="node-list"
        >
          <li
            v-for="(dataset, index) in localDatasets"
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

    <SqlDatasetDialog
      :visible="sqlDatasetDialogVisible"
      :db="currentDbInfo"
      :datasetData="currentDatasetData"
      @save="handleSqlDatasetSave"
      @close="sqlDatasetDialogVisible = false"
    />

    <!-- 字段名输入对话框 -->
    <FieldNameDialog
      :visible="fieldNameDialogVisible"
      :dataset="currentDataset"
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
