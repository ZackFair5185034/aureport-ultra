<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { setDirty } from '@/utils/table'

defineOptions({ name: 'ChartDataConfig' })

const props = withDefaults(defineProps<{
  selectedDataset?: string
  selectedCategoryProperty?: string
  selectedXProperty?: string
  selectedYProperty?: string
  selectedRProperty?: string
  showRProperty?: boolean
}>(), {
  selectedDataset: '',
  selectedCategoryProperty: '',
  selectedXProperty: '',
  selectedYProperty: '',
  selectedRProperty: '',
  showRProperty: true,
})
const emit = defineEmits<{
  (e: 'update-dataset', value: any): void
}>()
const { t } = useI18n()
const store = useReportStore()

const availableFields = ref<any[]>([])
const localDataset = ref('')
const localCategoryProperty = ref('')
const localXProperty = ref('')
const localYProperty = ref('')
const localRProperty = ref('')

const context = computed(() => store.context || {})

const datasources = computed(() => {
  if (!context.value?.reportDef?.datasources) {
    return []
  }
  return context.value.reportDef.datasources
})

const availableDatasets = computed(() => {
  if (!datasources.value)
    return []
  const datasets: any[] = []
  for (const ds of datasources.value) {
    const dsDatasets = ds.datasets || []
    for (const dataset of dsDatasets) {
      datasets.push(dataset)
    }
  }

  return datasets
})

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

watch(() => props.selectedDataset, (newVal) => {
  localDataset.value = newVal
})
watch(() => props.selectedCategoryProperty, (newVal) => {
  localCategoryProperty.value = newVal
})
watch(() => props.selectedXProperty, (newVal) => {
  localXProperty.value = newVal
})
watch(() => props.selectedYProperty, (newVal) => {
  localYProperty.value = newVal
})
watch(() => props.selectedRProperty, (newVal) => {
  localRProperty.value = newVal
})

watch(() => localDataset.value, () => {
  updateAvailableFields()
})

watch(datasources, () => {
  updateAvailableFields()
}, { immediate: true })

onMounted(() => {
  updateAvailableFields()
  initLocalData()
})

function initLocalData() {
  localDataset.value = props.selectedDataset || ''
  localCategoryProperty.value = props.selectedCategoryProperty || ''
  localXProperty.value = props.selectedXProperty || ''
  localYProperty.value = props.selectedYProperty || ''
  localRProperty.value = props.selectedRProperty || ''
}

function updateAvailableFields() {
  if (!localDataset.value || !datasources.value) {
    availableFields.value = []
    return
  }

  let fields: any[] = []
  for (const ds of datasources.value) {
    const datasets = ds.datasets || []
    for (const dataset of datasets) {
      if (dataset.name === localDataset.value) {
        fields = dataset.fields || []
        break
      }
    }

    if (fields.length > 0)
      break
  }

  nextTick(() => {
    availableFields.value = fields
  })
}

function handleDatasetChange() {
  localCategoryProperty.value = ''
  localXProperty.value = ''
  localYProperty.value = ''
  localRProperty.value = ''

  emit('update-dataset', {
    datasetName: localDataset.value,
    categoryProperty: '',
    xProperty: '',
    yProperty: '',
    rProperty: '',
  })

  setDirty()
}

function handleCategoryPropertyChange() {
  emit('update-dataset', { categoryProperty: localCategoryProperty.value })
  setDirty()
}

function handleXPropertyChange() {
  emit('update-dataset', { xProperty: localXProperty.value })
  setDirty()
}

function handleYPropertyChange() {
  emit('update-dataset', { yProperty: localYProperty.value })
  setDirty()
}

function handleRPropertyChange() {
  emit('update-dataset', { rProperty: localRProperty.value })
  setDirty()
}
</script>

<template>
  <div class="chart-dataset-bob">
    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('chart.dataset')">
        <u-select
          v-model="localDataset"
          :clearable="true"
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
          v-model="localCategoryProperty"
          :clearable="true"
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

      <u-form-item class="property-label" :label="t('chart.xProperty')">
        <u-select
          v-model="localXProperty"
          :clearable="true"
          @change="handleXPropertyChange"
        >
          <u-option
            v-for="option in fieldOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.yProperty')">
        <u-select
          v-model="localYProperty"
          :clearable="true"
          @change="handleYPropertyChange"
        >
          <u-option
            v-for="option in fieldOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item v-if="showRProperty" class="property-label" :label="t('chart.rProperty')">
        <u-select
          v-model="localRProperty"
          :clearable="true"
          @change="handleRPropertyChange"
        >
          <u-option
            v-for="option in fieldOptions"
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
.chart-dataset-bob {
  margin-top: 10px;
}
</style>
