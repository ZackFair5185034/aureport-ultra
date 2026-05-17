<script setup lang="ts">
// @ts-nocheck
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils/index'
import { useReportStore } from '@/stores/report'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'
import DataMapping from '@/views/report/designer/resource-panel/property-panel/dataset-value-editor/data-mapping/index.vue'
import DatasetConfig from '@/views/report/designer/resource-panel/property-panel/dataset-value-editor/dataset-config/index.vue'
import FilterCondition from '@/views/report/designer/resource-panel/property-panel/dataset-value-editor/filter-condition/index.vue'

defineOptions({ name: 'DatasetValueEditor' })

const props = withDefaults(defineProps<{
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
}>(), {
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0,
})
const { t } = useI18n()
const store = useReportStore()
const context = computed(() => store.context)

const container = ref<HTMLDivElement | null>(null)
const activeTab = ref('dataset')
const datasets = ref<any[]>([])
const currentFields = ref<any[]>([])
const initialized = ref(false)
const selectedDataset = ref('')
const selectedProperty = ref('')
const selectedAggregate = ref('select')
const selectedSort = ref('none')
const selectedExpand = ref('None')
const lineHeight = ref<string | number>('')
const wrapCompute = ref('custom')
const format = ref('')
const fillBlankRows = ref('custom')
const multiple = ref(0)
const conditions = ref<any[]>([])
const showMappingOptions = ref(false)
const showSortOptions = ref(true)
const showExpandOptions = ref(true)
const mappingType = ref('simple')
const mappingItems = ref<any[]>([])
const mappingDataset = ref('')
const mappingKeyProperty = ref('')
const mappingValueProperty = ref('')
const conditionPropertyItems = ref<any[]>([])
const groupItems = ref<any[]>([])
const selectedNestProperty = ref('')
const groupHead = ref(false)
const groupFoot = ref(false)

watch(() => [props.rowIndex, props.colIndex], () => {
  loadCellData()
}, { immediate: true })

watch(groupHead, () => {
  if (!initialized.value)
    return
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef || !cellDef.value)
        continue
      const newCellDef = deepCopy(cellDef)
      newCellDef.value.groupHead = groupHead.value
      setCell(i, j, newCellDef)
    }
  }

  setDirty()
})

watch(groupFoot, () => {
  if (!initialized.value)
    return
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef || !cellDef.value)
        continue
      const newCellDef = deepCopy(cellDef)
      newCellDef.value.groupFoot = groupFoot.value
      setCell(i, j, newCellDef)
    }
  }

  setDirty()
})

function loadCellData() {
  initialized.value = false

  const cellDef = getCell(props.rowIndex, props.colIndex)

  loadDatasets()
  loadInitialValues(cellDef)

  nextTick(() => {
    initialized.value = true
  })
}

function loadDatasets() {
  datasets.value = []
  const datasources = context.value.reportDef.datasources || []
  for (const ds of datasources) {
    const dsDatasets = ds.datasets || []
    for (const dataset of dsDatasets) {
      datasets.value.push(dataset)
    }
  }
}

function loadInitialValues(cellDef: any) {
  wrapCompute.value = cellDef.cellStyle && cellDef.cellStyle.wrapCompute ? 'default' : 'custom'

  lineHeight.value = cellDef.cellStyle && cellDef.cellStyle.lineHeight ? cellDef.cellStyle.lineHeight : ''

  format.value = cellDef.cellStyle && cellDef.cellStyle.format ? cellDef.cellStyle.format : ''

  if (cellDef.fillBlankRows) {
    fillBlankRows.value = 'default'
    multiple.value = cellDef.multiple || 0
  }
  else {
    fillBlankRows.value = 'custom'
  }

  selectedExpand.value = cellDef.expand ? cellDef.expand : 'None'

  const value = cellDef.value
  if (value) {
    selectedDataset.value = value.datasetName || ''
    selectedProperty.value = value.property || ''
    selectedAggregate.value = value.aggregate || 'select'
    selectedSort.value = value.order || 'none'
    conditions.value = value.conditions || []
    mappingType.value = value.mappingType || 'simple'
    mappingItems.value = value.mappingItems || []
    mappingDataset.value = value.mappingDataset || ''
    mappingKeyProperty.value = value.mappingKeyProperty || ''
    mappingValueProperty.value = value.mappingValueProperty || ''
    selectedNestProperty.value = value.nestProperty || ''
    groupHead.value = value.groupHead || false
    groupFoot.value = value.groupFoot || false
  }

  conditionPropertyItems.value = cellDef.conditionPropertyItems ? [...cellDef.conditionPropertyItems] : []

  groupItems.value = cellDef.value.groupItems ? [...cellDef.value.groupItems] : []

  handleDatasetChange()

  if (selectedAggregate.value === 'sum' || selectedAggregate.value === 'count'
    || selectedAggregate.value === 'max' || selectedAggregate.value === 'min'
    || selectedAggregate.value === 'avg') {
    showSortOptions.value = false
    showExpandOptions.value = false
  }
}

function handleDatasetChange() {
  currentFields.value = []

  if (selectedDataset.value) {
    const datasources = context.value.reportDef.datasources || []
    for (const ds of datasources) {
      const dsDatasets = ds.datasets || []
      for (const dataset of dsDatasets) {
        if (dataset.name === selectedDataset.value) {
          currentFields.value = dataset.fields || []
          break
        }
      }

      if (currentFields.value.length > 0) {
        break
      }
    }
  }

  if (initialized.value) {
    _setDatasetName(selectedDataset.value)
  }
}

function handlePropertyChange() {
  _setProperty(selectedProperty.value)
}

function handleAggregateChange(params: any) {
  if (params && typeof params === 'object') {
    showSortOptions.value = params.showSortOptions
    showExpandOptions.value = params.showExpandOptions
  }
  else {
    if (selectedAggregate.value === 'sum' || selectedAggregate.value === 'count'
      || selectedAggregate.value === 'max' || selectedAggregate.value === 'min'
      || selectedAggregate.value === 'avg') {
      showSortOptions.value = false
      showExpandOptions.value = false
    }
    else {
      showSortOptions.value = true
      showExpandOptions.value = true
    }
  }

  showMappingOptions.value = !!(selectedAggregate.value === 'group' || selectedAggregate.value === 'select')

  if (initialized.value) {
    _setAggregate(selectedAggregate.value)
  }
}

function handleSortChange() {
  _setOrder(selectedSort.value)
}

function handleExpandChange() {
  _setExpand(selectedExpand.value)
}

function handleLineHeightChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.cellStyle) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.cellStyle.lineHeight = lineHeight.value

    const hot = TableManager.get()
    if (hot) {
      const td = hot.getCell(props.rowIndex, props.colIndex)
      if (td) {
        td.style.lineHeight = lineHeight.value === '' ? '' : (lineHeight.value as string)
        hot.render()
      }
    }

    for (let i = props.rowIndex; i <= props.row2Index; i++) {
      for (let j = props.colIndex; j <= props.col2Index; j++) {
        const originalCellDef = getCell(i, j)
        if (originalCellDef) {
          const updatedCellDef = deepCopy(originalCellDef)
          updatedCellDef.cellStyle.lineHeight = lineHeight.value
          setCell(i, j, updatedCellDef)
        }
      }
    }

    setDirty()
  }
}

function handleWrapComputeChange() {
  const wrapComputeValue = wrapCompute.value === 'default'
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const newCellDef = deepCopy(cellDef)
      if (!newCellDef.cellStyle) {
        newCellDef.cellStyle = {}
      }

      newCellDef.cellStyle.wrapCompute = wrapComputeValue
      setCell(i, j, newCellDef)
    }
  }

  setDirty()
}

function handleFormatChange() {
  const hot = TableManager.get()
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const newCellDef = deepCopy(cellDef)
      if (!newCellDef.cellStyle) {
        newCellDef.cellStyle = {}
      }

      newCellDef.cellStyle.format = format.value
      setCell(i, j, newCellDef)
    }
  }

  setDirty()
}

function handleFillBlankRowsChange() {
  const fillBlankRowsValue = fillBlankRows.value === 'default'
  _setFillBlankRows(fillBlankRowsValue)
}

function handleMultipleChange() {
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const newCellDef = deepCopy(cellDef)
      newCellDef.multiple = multiple.value
      setCell(i, j, newCellDef)
    }
  }

  setDirty()
}

function handleConditionPropertyItemsChange(conditionPropertyItems: any[]) {
  conditionPropertyItems.value = conditionPropertyItems

  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const originalCellDef = getCell(i, j)
      if (originalCellDef) {
        const updatedCellDef = deepCopy(originalCellDef)
        updatedCellDef.conditionPropertyItems = conditionPropertyItems
        setCell(i, j, updatedCellDef)
      }
    }
  }

  setDirty()
}

function handleUpdateFilterConditions(conditions: any[]) {
  conditions.value = conditions

  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const originalCellDef = getCell(i, j)
      if (originalCellDef) {
        const updatedCellDef = deepCopy(originalCellDef)
        updatedCellDef.value.conditions = conditions
        setCell(i, j, updatedCellDef)
      }
    }
  }
}

function handleUpdateCustomGroup(groupItems: any[]) {
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const originalCellDef = getCell(i, j)
      if (originalCellDef) {
        const updatedCellDef = deepCopy(originalCellDef)
        updatedCellDef.value.groupItems = groupItems
        setCell(i, j, updatedCellDef)
      }
    }
  }
}

function _updateTableData() {
  const hot = TableManager.get()
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const value = cellDef.value
      const valueType = value.type
      let data = ''
      switch (valueType) {
        case 'simple': {
          data = value.value

          break
        }

        case 'dataset': {
          let text = `${value.datasetName}.${value.aggregate}(`
          if (value.aggregate === 'iterate') {
            text += value.nestProperty || ''
            text += ')'
            if (value.property) {
              text += `.${value.property}`
            }
          }
          else {
            text += `${value.property})`
          }

          data = text

          break
        }

        case 'expression': {
          data = value.value

          break
        }
      // No default
      }

      if (hot) {
        hot.setDataAtCell(cellDef.rowNumber - 1, cellDef.columnNumber - 1, data)
      }
    }
  }
}

function _setDatasetName(datasetName: string) {
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const valueType = cellDef.value.type
      if (valueType === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.datasetName = datasetName
        setCell(i, j, newCellDef)
      }
    }
  }

  _updateTableData()
  setDirty()
}

function _setProperty(property: string) {
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const valueType = cellDef.value.type
      if (valueType === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.property = property
        setCell(i, j, newCellDef)
      }
    }
  }

  _updateTableData()
  setDirty()
}

function _setAggregate(aggregate: string) {
  let none = false
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const valueType = cellDef.value.type
      if (valueType === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.aggregate = aggregate
        if (aggregate === 'sum' || aggregate === 'count' || aggregate === 'max'
          || aggregate === 'min' || aggregate === 'avg') {
          newCellDef.value.order = 'none'
          newCellDef.expand = 'None'
          none = true
        }

        setCell(i, j, newCellDef)
      }
    }
  }

  if (none) {
    selectedSort.value = 'none'
    selectedExpand.value = 'None'
  }

  _updateTableData()
  const hot = TableManager.get()
  if (hot) {
    hot.render()
  }

  setDirty()
}

function _setOrder(order: string) {
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const valueType = cellDef.value.type
      if (valueType === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.order = order
        setCell(i, j, newCellDef)
      }
    }
  }

  setDirty()
}

function _setExpand(expand: string) {
  const originalCellDef = getCell(props.rowIndex, props.colIndex)
  if (originalCellDef) {
    const updatedCellDef = deepCopy(originalCellDef)
    updatedCellDef.expand = expand
    setCell(props.rowIndex, props.colIndex, updatedCellDef)
  }

  const hot = TableManager.get()
  if (hot) {
    hot.render()
  }

  setDirty()
}

function _setFillBlankRows(value: boolean) {
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const newCellDef = deepCopy(cellDef)
      newCellDef.fillBlankRows = value
      if (!newCellDef.multiple) {
        newCellDef.multiple = 0
      }

      setCell(i, j, newCellDef)
    }
  }

  setDirty()
}

function handleNestPropertyChange() {
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      const valueType = cellDef.value.type
      if (valueType === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.nestProperty = selectedNestProperty.value
        setCell(i, j, newCellDef)
      }
    }
  }

  setDirty()
}

function _setMappingType(mappingType: string) {
  mappingType.value = mappingType
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      if (cellDef.value.type === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.mappingType = mappingType
        setCell(i, j, newCellDef)
      }
    }
  }

  setDirty()
}

function _setMappingItems(mappingItems: any[]) {
  mappingItems.value = mappingItems
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      if (cellDef.value.type === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.mappingItems = mappingItems
        setCell(i, j, newCellDef)
      }
    }
  }

  setDirty()
}

function _setMappingDataset(mappingDataset: string) {
  mappingDataset.value = mappingDataset
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      if (cellDef.value.type === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.mappingDataset = mappingDataset
        setCell(i, j, newCellDef)
      }
    }
  }

  setDirty()
}

function _setMappingKeyProperty(mappingKeyProperty: string) {
  mappingKeyProperty.value = mappingKeyProperty
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      if (cellDef.value.type === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.mappingKeyProperty = mappingKeyProperty
        setCell(i, j, newCellDef)
      }
    }
  }

  setDirty()
}

function _setMappingValueProperty(mappingValueProperty: string) {
  mappingValueProperty.value = mappingValueProperty
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef)
        continue
      if (cellDef.value.type === 'dataset') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.value.mappingValueProperty = mappingValueProperty
        setCell(i, j, newCellDef)
      }
    }
  }

  setDirty()
}
</script>

<template>
  <div ref="container" class="dataset-value-editor">
    <u-tabs v-model="activeTab" type="button">
      <u-tab-pane :label="t('property.dataset.datasetConfig')" index="dataset">
        <DatasetConfig
          :datasets="datasets"
          :current-fields="currentFields"
          :group-items="groupItems"
          :selected-dataset="selectedDataset"
          :selected-property="selectedProperty"
          :selected-aggregate="selectedAggregate"
          :selected-sort="selectedSort"
          :selected-expand="selectedExpand"
          :line-height="lineHeight"
          :wrap-compute="wrapCompute"
          :format="format"
          :fill-blank-rows="fillBlankRows"
          :multiple="multiple"
          :show-sort-options="showSortOptions"
          :show-expand-options="showExpandOptions"
          :condition-property-items="conditionPropertyItems"
          :selected-nest-property="selectedNestProperty"
          :group-head="groupHead"
          :group-foot="groupFoot"
          @update:selectedDataset="val => selectedDataset = val"
          @update:selectedProperty="val => selectedProperty = val"
          @update:selectedAggregate="val => selectedAggregate = val"
          @update:selectedSort="val => selectedSort = val"
          @update:selectedExpand="val => selectedExpand = val"
          @update:lineHeight="val => lineHeight = val"
          @update:wrapCompute="val => wrapCompute = val"
          @update:format="val => format = val"
          @update:fillBlankRows="val => fillBlankRows = val"
          @update:multiple="val => multiple = val"
          @update:showSortOptions="val => showSortOptions = val"
          @update:showExpandOptions="val => showExpandOptions = val"
          @update:conditionPropertyItems="val => conditionPropertyItems = val"
          @update:selectedNestProperty="val => selectedNestProperty = val"
          @update:groupHead="val => groupHead = val"
          @update:groupFoot="val => groupFoot = val"
          @nest-property-change="handleNestPropertyChange"
          @dataset-change="handleDatasetChange"
          @property-change="handlePropertyChange"
          @aggregate-change="handleAggregateChange"
          @sort-change="handleSortChange"
          @expand-change="handleExpandChange"
          @line-height-change="handleLineHeightChange"
          @wrap-compute-change="handleWrapComputeChange"
          @format-change="handleFormatChange"
          @fill-blank-rows-change="handleFillBlankRowsChange"
          @multiple-change="handleMultipleChange"
          @condition-property-items-change="handleConditionPropertyItemsChange"
          @update-custom-group="handleUpdateCustomGroup"
        />
      </u-tab-pane>

      <u-tab-pane :label="t('property.dataset.filterCondition')" index="condition">
        <FilterCondition
          :selected-dataset="selectedDataset"
          :conditions="conditions"
          :current-fields="currentFields"
          @update:conditions="val => conditions = val"
          @update-filter-conditions="handleUpdateFilterConditions"
        />
      </u-tab-pane>

      <u-tab-pane :label="t('property.dataset.mapping')" index="mapping">
        <DataMapping
          :datasets="datasets"
          :show-mapping-options="showMappingOptions"
          :mapping-type="mappingType"
          :mapping-items="mappingItems"
          :mapping-dataset="mappingDataset"
          :mapping-key-property="mappingKeyProperty"
          :mapping-value-property="mappingValueProperty"
          @mapping-type-change="_setMappingType"
          @mapping-items-change="_setMappingItems"
          @mapping-dataset-change="_setMappingDataset"
          @mapping-key-property-change="_setMappingKeyProperty"
          @mapping-value-property-change="_setMappingValueProperty"
        />
      </u-tab-pane>
    </u-tabs>
  </div>
</template>

<style scoped>
</style>
