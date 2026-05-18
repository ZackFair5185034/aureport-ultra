<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { setDirty } from '@/utils/table'

defineOptions({ name: 'ChartDataset' })

const props = withDefaults(defineProps<{
  datasetConfig?: any
}>(), {
  datasetConfig: () => ({}),
})
const emit = defineEmits<{
  (e: 'dataset-change', value: string): void
  (e: 'category-property-change', value: string): void
  (e: 'value-property-change', value: string): void
  (e: 'series-type-change', value: string): void
  (e: 'series-property-change', value: string): void
  (e: 'series-text-change', value: string): void
  (e: 'aggregate-change', value: string): void
}>()
const { t } = useI18n()
const store = useReportStore()

const availableDatasets = ref<any[]>([])
const availableFields = ref<any[]>([])
const localDatasetConfig = reactive({
  datasetName: '',
  categoryProperty: '',
  valueProperty: '',
  seriesType: 'text',
  seriesProperty: '',
  seriesText: '',
  collectType: '',
  format: '',
})

const context = computed(() => store.context || {})

const datasetOptions = computed(() =>
  availableDatasets.value.map((dataset: any) => ({
    value: dataset.name,
    label: dataset.name,
  })),
)

const fieldOptions = computed(() =>
  availableFields.value.map((field: any) => ({
    value: field.name,
    label: field.name,
  })),
)

const aggregateOptions = computed(() => [
  { value: 'select', label: t('chart.select') },
  { value: 'sum', label: t('chart.sum') },
  { value: 'count', label: t('chart.count') },
  { value: 'max', label: t('chart.max') },
  { value: 'min', label: t('chart.min') },
  { value: 'avg', label: t('chart.avg') },
])

watch(() => props.datasetConfig, (newVal) => {
  if (newVal) {
    Object.assign(localDatasetConfig, { ...localDatasetConfig, ...newVal })
  }
}, { deep: true, immediate: true })

watch(() => localDatasetConfig.datasetName, (newVal) => {
  if (newVal) {
    loadAvailableFields()
  }
}, { immediate: true })

onMounted(() => {
  loadAvailableDatasets()
})

function loadAvailableDatasets() {
  availableDatasets.value = []
  const ctx = context.value
  if (!ctx.reportDef)
    return
  for (const ds of ctx.reportDef.datasources) {
    const datasets = ds.datasets || []
    for (const dataset of datasets) {
      availableDatasets.value.push(dataset)
    }
  }
}

function loadAvailableFields() {
  availableFields.value = []
  const datasetName = localDatasetConfig.datasetName
  if (!datasetName)
    return
  const ctx = context.value
  if (!ctx.reportDef)
    return
  for (const ds of ctx.reportDef.datasources) {
    const datasets = ds.datasets || []
    for (const dataset of datasets) {
      if (dataset.name === datasetName) {
        availableFields.value = dataset.fields || []
        break
      }
    }

    if (availableFields.value.length > 0) {
      break
    }
  }
}

function handleDatasetChange(value: string) {
  emit('dataset-change', value)
  setDirty()
}

function handleCategoryPropertyChange(value: string) {
  emit('category-property-change', value)
  setDirty()
}

function handleValuePropertyChange(value: string) {
  emit('value-property-change', value)
  setDirty()
}

function handleSeriesTypeChange(value: string) {
  emit('series-type-change', value)
  setDirty()
}

function handleSeriesPropertyChange(value: string) {
  emit('series-property-change', value)
  setDirty()
}

function handleSeriesTextChange(value: string) {
  emit('series-text-change', value)
  setDirty()
}

function handleAggregateChange(value: string) {
  emit('aggregate-change', value)
  setDirty()
}
</script>

<template>
  <div class="chart-dataset">
    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('chart.dataset')">
        <u-select
          v-model="localDatasetConfig.datasetName"
          :clearable="true"
          style="width: 250px"
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

      <u-form-item class="property-label" :label="t('chart.categoryProperty')">
        <u-select
          v-model="localDatasetConfig.categoryProperty"
          :clearable="true"
          style="width: 250px"
          @change="handleCategoryPropertyChange"
        >
          <u-option
            v-for="option in fieldOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.valueProperty')">
        <u-select
          v-model="localDatasetConfig.valueProperty"
          :clearable="true"
          style="width: 250px"
          @change="handleValuePropertyChange"
        >
          <u-option
            v-for="option in fieldOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.seriesProperty')">
        <u-radio-group v-model="localDatasetConfig.seriesType" @change="handleSeriesTypeChange">
          <u-radio
            v-for="option in [
              { label: t('chart.property'), value: 'property' },
              { label: t('chart.static'), value: 'text' },
            ]"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item v-show="localDatasetConfig.seriesType === 'property'" class="property-label" :label="t('chart.prop')">
        <u-select
          v-model="localDatasetConfig.seriesProperty"
          :clearable="true"
          style="width: 250px"
          @change="handleSeriesPropertyChange"
        >
          <u-option
            v-for="option in fieldOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item v-show="localDatasetConfig.seriesType === 'text'" class="property-label" :label="t('chart.staticValue')">
        <u-input
          v-model="localDatasetConfig.seriesText"
          style="width: 250px;"
          @change="handleSeriesTextChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.aggregate')">
        <u-select
          v-model="localDatasetConfig.collectType"
          :clearable="true"
          style="width: 250px"
          @change="handleAggregateChange"
        >
          <u-option
            v-for="option in aggregateOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>
    </u-form>
  </div>
</template>

<style scoped>
.chart-dataset {
  margin-top: 10px;
}
</style>
