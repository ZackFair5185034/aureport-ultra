<template>
  <div>
    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('property.dataset.dataset')" style="margin-top: 10px">
        <u-select
            v-model="internalSelectedDataset"
            :clearable="true"
            style="width:250px"
            @change="handleDatasetChange"
        >
          <u-option
              v-for="option in datasetOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.dataset.property')">
        <u-select
            v-model="internalSelectedProperty"
            :clearable="true"
            style="width:250px"
            @change="handlePropertyChange"
        >
          <u-option
              v-for="option in propertyOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.dataset.aggregateType')">
        <u-select
            v-model="internalSelectedAggregate"
            :clearable="true"
            style="width:250px"
            @change="handleAggregateChange"
        >
          <u-option
              v-for="option in aggregateOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
          />
        </u-select>
        <u-button
            style="margin-left: 5px"
            v-show="internalSelectedAggregate === 'customgroup'"
            @click="handleCustomGroupConfig"
        >
          {{ t('property.dataset.configCustomGroup') }}
        </u-button>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.dataset.nestProperty')" v-show="internalSelectedAggregate === 'iterate'">
        <u-input
            v-model="internalNestProperty"
            :clearable="true"
            style="width:250px"
            :placeholder="t('property.dataset.nestPropertyTip')"
            @change="handleNestPropertyChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.dataset.sortType')" v-show="internalShowSortOptions">
        <u-radio-group v-model="internalSelectedSort" @change="handleSortChange">
          <u-radio
              v-for="option in sortOptions"
              :key="option.value"
              :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.dataset.expand')" v-show="internalShowExpandOptions">
        <u-radio-group :value="internalSelectedExpand" @change="handleExpandChange">
          <u-radio
              v-for="option in expandOptions"
              :key="option.value"
              :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.dataset.lineHeight')">
        <u-input-number
            :placeholder="t('property.dataset.lineHeightTip')"
            v-model="internalLineHeight"
            @change="handleLineHeightChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.base.newLineCompute')">
        <u-radio-group v-model="internalWrapCompute" @change="handleWrapComputeChange">
          <u-radio
              v-for="option in wrapComputeOptions"
              :key="option.value"
              :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.base.format')">
        <vue-simple-suggest
            :value="internalFormat"
            :list="suggestionList"
            :filter-by-query="true"
            :placeholder="t('property.base.formatTip')"
            class="simple-suggest"
            @input="handleFormatChange"
        ></vue-simple-suggest>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.base.fillBlank')">
        <u-radio-group v-model="internalFillBlankRows" @change="handleFillBlankRowsChange">
          <u-radio
              v-for="option in fillBlankRowsOptions"
              :key="option.value"
              :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.base.rowTimes')" v-show="internalFillBlankRows === 'default'">
        <u-input-number
            v-model="internalMultiple"
            @change="handleMultipleChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.base.conditionProp')">
        <u-button
            type="info"
            size="mini"
            icon="icon-filter"
            @click="handleConditionPropertyConfig"
        >
          {{ t('property.base.configCondition') }}
        </u-button>
      </u-form-item>
    </u-form>

    <!-- 自定义分组对话框组件 -->
    <CustomGroupDialog
      v-model:visible="customGroupDialogVisible"
      :group-items="groupItems"
      :fields="customGroupDialogFields"
      @save="handleCustomGroupSave"
    />

    <!-- 属性条件对话框组件 -->
    <PropertyConditionDialog
        v-model:visible="propertyConditionDialogVisible"
        :dataset-name="propertyConditionDialogDatasetName"
        :condition-property-items="propertyConditionDialogItems"
        @saveAfter="handlePropertyConditionSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import PropertyConditionDialog from '@/views/report/designer/resource-panel/property-panel/property-condition-dialog/index.vue'
import CustomGroupDialog from '@/views/report/designer/resource-panel/property-panel/dataset-value-editor/dataset-config/custom-group-dialog/index.vue'
import { setDirty } from '@/utils/table.js'
import { showAlert } from '@/utils/comnon.js'
import { deepCopy } from '@/components/utils/index.js'
import VueSimpleSuggest from 'vue-simple-suggest'
import 'vue-simple-suggest/dist/styles.css'

defineOptions({ name: 'DatasetConfigTab' })

const { t } = useI18n()
const store = useReportStore()
const context = computed(() => store.context)
const datasources = computed(() => context.value!.reportDef.datasources || [])

const props = withDefaults(defineProps<{
  datasets?: any[]
  currentFields?: any[]
  groupItems?: any[]
  selectedDataset?: string
  selectedProperty?: string
  selectedAggregate?: string
  selectedSort?: string
  selectedExpand?: string
  lineHeight?: string | number
  wrapCompute?: string
  format?: string
  fillBlankRows?: string
  multiple?: number
  showSortOptions?: boolean
  showExpandOptions?: boolean
  conditionPropertyItems?: any[]
  selectedNestProperty?: string
}>(), {
  datasets: () => [],
  currentFields: () => [],
  groupItems: () => [],
  selectedDataset: '',
  selectedProperty: '',
  selectedAggregate: 'select',
  selectedSort: 'none',
  selectedExpand: 'None',
  lineHeight: 10,
  wrapCompute: 'custom',
  format: '',
  fillBlankRows: 'custom',
  multiple: 0,
  showSortOptions: true,
  showExpandOptions: true,
  conditionPropertyItems: () => [],
  selectedNestProperty: ''
})

const emit = defineEmits<{
  (e: 'update:selectedDataset', value: string): void
  (e: 'update:selectedProperty', value: string): void
  (e: 'update:selectedAggregate', value: string): void
  (e: 'update:selectedSort', value: string): void
  (e: 'update:selectedExpand', value: string): void
  (e: 'update:lineHeight', value: string | number): void
  (e: 'update:wrapCompute', value: string): void
  (e: 'update:format', value: string): void
  (e: 'update:fillBlankRows', value: string): void
  (e: 'update:multiple', value: number): void
  (e: 'update:showSortOptions', value: boolean): void
  (e: 'update:showExpandOptions', value: boolean): void
  (e: 'update:conditionPropertyItems', value: any[]): void
  (e: 'update:selectedNestProperty', value: string): void
  (e: 'dataset-change', value: string): void
  (e: 'property-change', value: string): void
  (e: 'aggregate-change', value: any): void
  (e: 'sort-change', value: string): void
  (e: 'expand-change', value: string): void
  (e: 'line-height-change', value: string | number): void
  (e: 'wrap-compute-change', value: string): void
  (e: 'format-change', value: string): void
  (e: 'fill-blank-rows-change', value: string): void
  (e: 'multiple-change', value: number): void
  (e: 'condition-property-items-change', value: any[]): void
  (e: 'update-custom-group', value: any[]): void
}>()

const internalSelectedDataset = ref('')
const internalSelectedProperty = ref('')
const internalSelectedAggregate = ref('select')
const internalSelectedSort = ref('none')
const internalSelectedExpand = ref('None')
const internalLineHeight = ref<string | number>(10)
const internalWrapCompute = ref('custom')
const internalFormat = ref('')
const internalFillBlankRows = ref('custom')
const internalMultiple = ref(0)
const internalShowSortOptions = ref(true)
const internalShowExpandOptions = ref(true)
const internalNestProperty = ref('')
const isInitialized = ref(false)
const propertyConditionDialogVisible = ref(false)
const propertyConditionDialogDatasetName = ref('')
const propertyConditionDialogItems = ref<any[]>([])
const customGroupDialogVisible = ref(false)
const customGroupDialogFields = ref<any[] | null>(null)
const suggestionList = ref<string[]>([
  "yyyy/MM/dd", "yyyy/MM", "yyyy-MM", "yyyy",
  "yyyy-MM-dd HH:mm:ss", "yyyy年MM月dd日 HH:mm:ss",
  "yyyy-MM-dd", "yyyy年MM月dd日", "HH:mm", "HH:mm:ss",
  "#.##", "#.00", "##.##%", "##.00%", "##,###.##",
  "￥##,###.##", "$##,###.##", "0.00E00", "##0.0E0"
])

const datasetOptions = computed(() =>
  props.datasets.map((dataset: any) => ({
    value: dataset.name,
    label: dataset.name
  }))
)

const propertyOptions = computed(() =>
  props.currentFields.map((field: any) => ({
    value: field.name,
    label: field.name
  }))
)

const aggregateOptions = computed(() => [
  { value: 'select', label: t('property.dataset.select') },
  { value: 'group', label: t('property.dataset.group') },
  { value: 'customgroup', label: t('property.dataset.customGroup') },
  { value: 'iterate', label: t('property.dataset.iterate') },
  { value: 'sum', label: t('property.dataset.sum') },
  { value: 'count', label: t('property.dataset.count') },
  { value: 'max', label: t('property.dataset.max') },
  { value: 'min', label: t('property.dataset.min') },
  { value: 'avg', label: t('property.dataset.avg') }
])

const sortOptions = computed(() => [
  { value: 'none', label: t('property.dataset.notSort') },
  { value: 'asc', label: t('property.dataset.asc') },
  { value: 'desc', label: t('property.dataset.desc') }
])

const expandOptions = computed(() => [
  { value: 'Down', label: t('property.dataset.down') },
  { value: 'Right', label: t('property.dataset.right') },
  { value: 'None', label: t('property.dataset.noneExpand') }
])

const wrapComputeOptions = computed(() => [
  { value: 'default', label: t('property.base.open') },
  { value: 'custom', label: t('property.base.close') }
])

const fillBlankRowsOptions = computed(() => [
  { value: 'default', label: t('property.base.open') },
  { value: 'custom', label: t('property.base.close') }
])

watch(() => props.selectedDataset, (val) => { internalSelectedDataset.value = val })
watch(() => props.selectedProperty, (val) => { internalSelectedProperty.value = val })
watch(() => props.selectedAggregate, (val) => { internalSelectedAggregate.value = val })
watch(() => props.selectedSort, (val) => { internalSelectedSort.value = val })
watch(() => props.selectedExpand, (val) => { internalSelectedExpand.value = val })
watch(() => props.lineHeight, (val) => { internalLineHeight.value = val })
watch(() => props.wrapCompute, (val) => { internalWrapCompute.value = val })
watch(() => props.format, (val) => { internalFormat.value = val })
watch(() => props.fillBlankRows, (val) => { internalFillBlankRows.value = val })
watch(() => props.multiple, (val) => { internalMultiple.value = val })
watch(() => props.showSortOptions, (val) => { internalShowSortOptions.value = val })
watch(() => props.showExpandOptions, (val) => { internalShowExpandOptions.value = val })
watch(() => props.selectedNestProperty, (val) => { internalNestProperty.value = val })

initData()

function initData() {
  internalSelectedDataset.value = props.selectedDataset
  internalSelectedProperty.value = props.selectedProperty
  internalSelectedAggregate.value = props.selectedAggregate
  internalSelectedSort.value = props.selectedSort
  internalSelectedExpand.value = props.selectedExpand
  internalLineHeight.value = props.lineHeight
  internalWrapCompute.value = props.wrapCompute
  internalFormat.value = props.format
  internalFillBlankRows.value = props.fillBlankRows
  internalMultiple.value = props.multiple
  internalShowSortOptions.value = props.showSortOptions
  internalShowExpandOptions.value = props.showExpandOptions
  internalNestProperty.value = props.selectedNestProperty
}

onMounted(() => {
  nextTick(() => {
    isInitialized.value = true
  })
})

function handleDatasetChange(value: string) {
  internalSelectedDataset.value = value
  emit('update:selectedDataset', internalSelectedDataset.value)
  emit('dataset-change', internalSelectedDataset.value)
}

function handlePropertyChange() {
  emit('update:selectedProperty', internalSelectedProperty.value)
  emit('property-change', internalSelectedProperty.value)
}

function handleAggregateChange() {
  if (internalSelectedAggregate.value === 'sum' || internalSelectedAggregate.value === 'count' ||
      internalSelectedAggregate.value === 'max' || internalSelectedAggregate.value === 'min' ||
      internalSelectedAggregate.value === 'avg') {
    internalShowSortOptions.value = false
    internalShowExpandOptions.value = false
  } else {
    internalShowSortOptions.value = true
    internalShowExpandOptions.value = true
  }

  emit('update:selectedAggregate', internalSelectedAggregate.value)
  emit('update:showSortOptions', internalShowSortOptions.value)
  emit('update:showExpandOptions', internalShowExpandOptions.value)
  emit('aggregate-change', {
    aggregate: internalSelectedAggregate.value,
    showSortOptions: internalShowSortOptions.value,
    showExpandOptions: internalShowExpandOptions.value
  })
}

function handleSortChange(value: string) {
  internalSelectedSort.value = value
  emit('update:selectedSort', internalSelectedSort.value)
  emit('sort-change', internalSelectedSort.value)
}

function handleExpandChange(value: string) {
  internalSelectedExpand.value = value
  emit('update:selectedExpand', internalSelectedExpand.value)
  emit('expand-change', internalSelectedExpand.value)
}

function handleLineHeightChange(value: string | number) {
  internalLineHeight.value = value
  emit('update:lineHeight', internalLineHeight.value)
  emit('line-height-change', internalLineHeight.value)
}

function handleWrapComputeChange() {
  emit('update:wrapCompute', internalWrapCompute.value)
  emit('wrap-compute-change', internalWrapCompute.value)
}

function handleFormatChange(value: string) {
  if (!isInitialized.value) return
  internalFormat.value = value
  emit('update:format', internalFormat.value)
  emit('format-change', internalFormat.value)
}

function handleFillBlankRowsChange() {
  emit('update:fillBlankRows', internalFillBlankRows.value)
  emit('fill-blank-rows-change', internalFillBlankRows.value)
}

function handleMultipleChange() {
  if (!isInitialized.value) return
  emit('update:multiple', internalMultiple.value)
  emit('multiple-change', internalMultiple.value)
}

function handleConditionPropertyConfig() {
  const conditionPropertyItems = props.conditionPropertyItems
    ? deepCopy(props.conditionPropertyItems)
    : []

  propertyConditionDialogDatasetName.value = internalSelectedDataset.value
  propertyConditionDialogItems.value = conditionPropertyItems
  propertyConditionDialogVisible.value = true
}

function handlePropertyConditionSave(propertyConditions: any[]) {
  const updatedConditions = deepCopy(propertyConditions)
  emit('update:conditionPropertyItems', updatedConditions)
  emit('condition-property-items-change', updatedConditions)
  setDirty()
}

function handleNestPropertyChange() {
  emit('update:selectedNestProperty', internalNestProperty.value)
  setDirty()
}

function handleCustomGroupConfig() {
  const fields = _buildFields()
  if (fields) {
    customGroupDialogFields.value = fields
    customGroupDialogVisible.value = true
  }
  setDirty()
}

function handleCustomGroupSave(groupItems: any[]) {
  emit('update-custom-group', groupItems)
  setDirty()
}

function _buildFields(): any[] | null {
  let fields: any[] = []
  if (internalSelectedDataset.value === '') {
    showAlert(t('property.dataset.bindDatasetTip'))
    return null
  }
  for (let ds of datasources.value) {
    let datasets = ds.datasets || []
    for (let dataset of datasets) {
      if (dataset.name === internalSelectedDataset.value) {
        fields = dataset.fields || []
        break
      }
    }
    if (fields.length > 0) {
      break
    }
  }
  return fields
}
</script>

<style scoped>
.simple-suggest :deep(.default-input){
  width: 250px !important;
  height: 35px;
  display: inline-block;
}
</style>
