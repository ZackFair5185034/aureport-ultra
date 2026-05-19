<script setup lang="ts">
import { v1 as uuidv1 } from 'uuid'

import { useI18n } from 'vue-i18n'
import { buildClass } from '@/api/designer/index'
import { deepCopy } from '@/components/utils/index'
import { useReportStore } from '@/stores/report'
import { showAlert, showConfirm } from '@/utils/comnon'
import { addCell, getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager'
import BeanMethodDialog from '@/views/report/designer/resource-panel/datasource-panel/bean-method-dialog/index.vue'
import SpringDialog from '@/views/report/designer/resource-panel/datasource-panel/spring-dialog/index.vue'
import ContextMenu from '../context-menu/index.vue'
import FieldNameDialog from '../field-name-dialog/index.vue'

defineOptions({ name: 'SpringTree' })

const props = withDefaults(defineProps<{
  name?: string
  datasets?: any[]
  datasources?: any[]
  beanId?: string
}>(), {
  name: '',
  datasets: () => [],
  datasources: () => [],
  beanId: '',
})
const emit = defineEmits<{
  (e: 'remove', name: string): void
  (e: 'update-datasource', data: any): void
  (e: 'update-datasets', datasets: any[]): void
}>()
const { t } = useI18n()
const store = useReportStore()

const id = `spring_${uuidv1()}`
const datasourceExpanded = ref(true)
const datasetExpanded = ref<Record<number, boolean>>({})
const localName = ref(props.name)
const localBeanId = ref(props.beanId)
const localDatasets = ref<any[]>(props.datasets)
const currentDataset = ref<any>(null)
const currentField = ref<any>(null)
const beanMethodDialogVisible = ref(false)
const springDialogVisible = ref(false)
const currentSpringDatasource = ref<any>(null)
const fieldNameDialogVisible = ref(false)
const contextMenu = ref<any>(null)
const springDialog = ref<any>(null)

const context = computed(() => store.context)

onMounted(() => {
  if (localDatasets.value && localDatasets.value.length > 0) {
    for (let i = 0; i < localDatasets.value.length; i++) {
      datasetExpanded.value[i] = true
    }
  }
})

watch(() => props.name, (newName) => {
  localName.value = newName
})

watch(() => props.beanId, (newBeanId) => {
  localBeanId.value = newBeanId
})

watch(() => props.datasets, (newDatasets) => {
  localDatasets.value = newDatasets || []
}, { deep: true })

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
  contextMenu.value.show(event, items, (key: string) => {
    handleDatasourceMenuAction(key)
  })
}

function handleDatasourceMenuAction(key: string) {
  switch (key) {
    case 'add': {
      addDatasetAction()

      break
    }

    case 'delete': {
      deleteDatasourceAction()

      break
    }

    case 'edit': {
      editDatasourceAction()

      break
    }
  // No default
  }
}

function addDatasetAction() {
  currentDataset.value = null
  beanMethodDialogVisible.value = true
}

function deleteDatasourceAction() {
  showConfirm(`${t('tree.delConfirm')}[${props.name}]？`).then(() => {
    emit('remove', props.name)
  })
}

function editDatasourceAction() {
  currentSpringDatasource.value = {
    name: localName.value,
    beanId: localBeanId.value,
  }
  springDialogVisible.value = true
}

function showDatasetContextMenu(event: MouseEvent, dataset: any, index: number) {
  const items = [
    { key: 'add', name: t('tree.addField'), icon: 'add' },
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
    case 'add': {
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
  currentField.value = null
  fieldNameDialogVisible.value = true
}

function handleFieldNameSave(fieldName: string, dataset: any, label?: string) {
  if (fieldName) {
    const newDatasets = deepCopy(localDatasets.value)
    const targetDataset = newDatasets.find((d: any) => d.name === dataset.name)

    if (!targetDataset.fields) {
      targetDataset.fields = []
    }

    const editField = currentField.value
    if (editField) {
      const oldField = targetDataset.fields.find((f: any) => f.name === editField.name)
      if (oldField) {
        const nameConflict = editField.name !== fieldName
          && targetDataset.fields.some((f: any) => f.name === fieldName)
        if (nameConflict) {
          showAlert(t('tree.fieldExist'))
          return
        }
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
    emit('update-datasets', newDatasets)
  }
}

function editDatasetAction(dataset: any, index: number) {
  currentDataset.value = dataset
  beanMethodDialogVisible.value = true
}

function handleBeanMethodSave(nameVal: string, method: string, clazz: string, oldName: string) {
  const newDatasets = deepCopy(localDatasets.value)

  if (oldName && oldName !== '') {
    const index = newDatasets.findIndex((dataset: any) => dataset.name === oldName)
    if (index !== -1) {
      const dataset = newDatasets[index]
      const originalClazz = dataset.clazz || ''

      dataset.name = nameVal
      dataset.method = method
      dataset.clazz = clazz

      if (clazz !== originalClazz) {
        if (!clazz || clazz === '') {
          dataset.fields = []
        }
        else {
          buildFields(dataset, index, false, newDatasets)
        }
      }

      emit('update-datasets', newDatasets)
      return
    }
  }

  const dataset = { name: nameVal, method, clazz, fields: [] }
  newDatasets.push(dataset)
  const newIndex = newDatasets.length - 1
  datasetExpanded.value[newIndex] = true

  if (clazz && clazz !== '') {
    buildFields(dataset, newIndex, false, newDatasets)
  }

  emit('update-datasets', newDatasets)
}

function deleteDatasetAction(dataset: any, index: number) {
  showConfirm(`${t('tree.delDatasetConfirm')}[${dataset.name}]?`).then(() => {
    const newDatasets = deepCopy(localDatasets.value)
    newDatasets.splice(index, 1)
    delete datasetExpanded.value[index]
    emit('update-datasets', newDatasets)
  })
}

function refreshDatasetAction(dataset: any, index: number) {
  const newDatasets = deepCopy(localDatasets.value)
  const targetDataset = newDatasets.find((d: any) => d.name === dataset.name)
  buildFields(targetDataset, index, true, newDatasets)
}

function showFieldContextMenu(event: MouseEvent, dataset: any, field: any, fieldIndex: number) {
  const items = [
    { key: 'edit', name: t('tree.edit'), icon: 'edit' },
    { key: 'delete', name: t('tree.del'), icon: 'delete' },
  ]

  contextMenu.value.show(event, items, (key: string) => {
    handleFieldMenuAction(key, dataset, field, fieldIndex)
  })
}

function handleFieldMenuAction(key: string, dataset: any, field: any, fieldIndex: number) {
  switch (key) {
    case 'edit': {
      editFieldAction(dataset, field)
      break
    }
    case 'delete': {
      deleteFieldAction(dataset, field, fieldIndex)
      break
    }
  // No default
  }
}

function deleteFieldAction(dataset: any, field: any, fieldIndex: number) {
  showConfirm(`${t('tree.delFieldConfirm')}[${field.name}]?`).then(() => {
    const newDatasets = deepCopy(localDatasets.value)
    const targetDataset = newDatasets.find((d: any) => d.name === dataset.name)
    if (targetDataset.fields) {
      targetDataset.fields.splice(fieldIndex, 1)
      emit('update-datasets', newDatasets)
    }
  })
}

function editFieldAction(dataset: any, field: any) {
  currentDataset.value = dataset
  currentField.value = field
  fieldNameDialogVisible.value = true
}

function handleFieldDoubleClick(dataset: any, field: any) {
  _buildClickEvent(dataset, field, context.value)
}

async function toggleFieldChildren(field: any) {
  field._expanded = !field._expanded
}

function initFieldExpandState(fields: any[]) {
  if (!fields) return
  for (const f of fields) {
    if (f.children && f.children.length > 0) {
      f._expanded = true
      initFieldExpandState(f.children)
    }
  }
}

async function buildFields(dataset: any, index: number, refresh = false, newDatasets: any[] | null = null) {
  const defaultFields = dataset.fields

  if (!refresh && defaultFields) {
    if (newDatasets) {
      initFieldExpandState(defaultFields)
      emit('update-datasets', newDatasets)
    }
    return
  }

  try {
    const response = await buildClass(dataset.clazz)
    initFieldExpandState(response)
    dataset.fields = response
    if (newDatasets) {
      emit('update-datasets', newDatasets)
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

  let newCellDef: any = deepCopy(cellDef)

  if (newCellDef.value.type === 'dataset') {
    setCell(rowIndex, colIndex, newCellDef)
  }
  else {
    newCellDef = {
      value: { type: 'dataset', conditions: [] },
      rowNumber: cellDef.rowNumber,
      columnNumber: cellDef.columnNumber,
      cellStyle: cellDef.cellStyle,
    }
    addCell(newCellDef)
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
  hot.setDataAtCell(rowIndex, colIndex, text)

  hot.render()

  if (hot.hooks) {
    hot.hooks.run(hot, 'afterSelectionEnd', rowIndex, colIndex, endRow, endCol)
  }
}

function handleSpringDatasourceSave(datasourceData: any) {
  localName.value = datasourceData.name
  localBeanId.value = datasourceData.beanId
  emit('update-datasource', datasourceData)
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
          <i class="iconfont icon-leaf" />
          <a href="###" class="ds_name">{{ localName }}</a>
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
              <template v-for="(field, fieldIndex) in dataset.fields" :key="`${field.name}_${fieldIndex}`">
                <li v-if="field.children && field.children.length > 0">
                  <span
                    :id="`field_${dataset.name}_${field.name}_${fieldIndex}`"
                    @click="toggleFieldChildren(field)"
                  >
                    <i
                      class="iconfont"
                      :class="field._expanded ? 'icon-minus-circle' : 'icon-plus-circle'"
                      style="margin-right:2px"
                    />
                    <i class="iconfont icon-property" />
                    <a href="###">{{ field.label ? `${field.name} (${field.label})` : field.name }}</a>
                  </span>
                  <ul v-show="field._expanded" style="padding-left: 22px;">
                    <li
                      v-for="(child, childIndex) in field.children"
                      :key="`${field.name}_${child.name}_${childIndex}`"
                    >
                      <span
                        :title="$t('tree.doubleClick')"
                        @dblclick="handleFieldDoubleClick(dataset, child)"
                        @contextmenu.prevent.stop="showFieldContextMenu($event, dataset, child, fieldIndex)"
                      >
                        <i class="iconfont icon-property" />
                        <a href="###">{{ child.label ? `${child.name} (${child.label})` : child.name }}</a>
                      </span>
                    </li>
                  </ul>
                </li>
                <li v-else>
                  <span
                    :id="`field_${dataset.name}_${field.name}_${fieldIndex}`"
                    :title="$t('tree.doubleClick')"
                    @dblclick="handleFieldDoubleClick(dataset, field)"
                    @contextmenu.prevent.stop="showFieldContextMenu($event, dataset, field, fieldIndex)"
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

    <!-- Bean方法配置对话框 -->
    <BeanMethodDialog
      :visible="beanMethodDialogVisible"
      :dataset="currentDataset"
      :datasources="datasources"
      :beanId="localBeanId"
      @save="handleBeanMethodSave"
      @close="beanMethodDialogVisible = false"
    />

    <!-- Spring数据源配置对话框 -->
    <SpringDialog
      ref="springDialog"
      :datasources="datasources"
      :visible="springDialogVisible"
      :datasource="currentSpringDatasource"
      @close="springDialogVisible = false"
      @save="handleSpringDatasourceSave"
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
