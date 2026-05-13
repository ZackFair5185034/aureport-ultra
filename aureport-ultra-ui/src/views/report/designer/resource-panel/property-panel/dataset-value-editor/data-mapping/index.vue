<template>
  <div class="data-mapping-tab">
    <u-form :label-width="100" labelPosition="left">
      <div style="padding-top: 10px">
        <div v-if="!showMappingOptions" class="alert alert-info" style="margin-bottom: 10px;">
        </div>

        <u-form-item :label="t('property.dataset.mappingType')" v-show="showMappingOptions">
          <u-radio-group
              v-model="localMappingType"
              @change="handleMappingTypeChange">
            <u-radio
              v-for="option in mappingTypeOptions"
              :key="option.value"
              :label="option.value"
            >
            {{ option.label }}
            </u-radio>
          </u-radio-group>
        </u-form-item>

        <div v-show="showMappingOptions && localMappingType === 'simple'" class="form-group">
          <div class="top-button">
            <u-button
                type="info"
                :title="t('property.dataset.addMappping')"
                @click="handleAddMapping"
                icon="icon-plus-circle"
            >
            </u-button>
          </div>
          <table class="data-table" style="margin-top: 10px">
            <thead>
              <tr style="background-color: #f5f5f5;height: 30px;">
                <td style="width: 130px;"><span>{{ t('property.dataset.realValue') }}</span></td>
                <td style="width: 150px;"><span>{{ t('property.dataset.displayValue') }}</span></td>
                <td style="width: 80px;"><span>{{ t('property.dataset.op') }}</span></td>
              </tr>
            </thead>
            <tbody style="font-size: 12px">
              <tr v-for="(item, index) in localMappingItems" :key="index" style="height: 30px">
                <td><span>{{ item.value }}</span></td>
                <td><span>{{ item.label }}</span></td>
                <td>
                  <u-button
                      type="info"
                      icon="icon-edit"
                      :title="t('dialog.urlParam.edit')"
                      @click="handleEditMapping(index)"
                      style="border: none">
                  </u-button>
                  <u-button
                      type="info"
                      icon="icon-delete"
                      :title="t('dialog.urlParam.delete')"
                      @click="handleDeleteMapping(index)"
                      style="border: none">
                  </u-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-show="showMappingOptions && localMappingType === 'dataset'">
          <u-form-item :label="t('property.dataset.dataset')">
            <u-select
              v-model="localMappingDataset"
              :clearable="true"
              @change="handleMappingDatasetChange"
            >
              <u-option
                v-for="option in datasetOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </u-form-item>

          <u-form-item :label="t('property.dataset.realValueProp')">
            <u-select
              v-model="localMappingKeyProperty"
              :clearable="true"
              @change="handleMappingKeyPropertyChange"
            >
              <u-option
                v-for="option in mappingFieldOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </u-form-item>

          <u-form-item :label="t('property.dataset.displayValueProp')">
            <u-select
              v-model="localMappingValueProperty"
              :clearable="true"
              @change="handleMappingValuePropertyChange"
            >
              <u-option
                v-for="option in mappingFieldOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </u-form-item>
        </div>
      </div>
    </u-form>
    <mapping-dialog
      v-model:visible="dialogVisible"
      :mapping-item="currentMappingItem"
      :operation="dialogOperation"
      @save="handleMappingSave"
    ></mapping-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { setDirty } from '@/utils/table'
import { showAlert, showConfirm } from '@/utils/comnon'
import MappingDialog from '@/views/report/designer/resource-panel/property-panel/dataset-value-editor/mapping-dialog/index.vue'

defineOptions({ name: 'DataMappingTab' })

const { t } = useI18n()
const store = useReportStore()

const props = withDefaults(defineProps<{
  datasets?: any[]
  showMappingOptions?: boolean
  mappingType?: string
  mappingItems?: any[]
  mappingDataset?: string
  mappingKeyProperty?: string
  mappingValueProperty?: string
}>(), {
  datasets: () => [],
  showMappingOptions: false,
  mappingType: 'simple',
  mappingItems: () => [],
  mappingDataset: '',
  mappingKeyProperty: '',
  mappingValueProperty: ''
})

const emit = defineEmits<{
  (e: 'mapping-type-change', value: string): void
  (e: 'mapping-items-change', value: any[]): void
  (e: 'mapping-dataset-change', value: string): void
  (e: 'mapping-key-property-change', value: string): void
  (e: 'mapping-value-property-change', value: string): void
}>()

const mappingFields = ref<any[]>([])
const dialogVisible = ref(false)
const dialogOperation = ref('add')
const currentMappingItem = ref({ value: '', label: '' })
const editingIndex = ref(-1)
const localMappingType = ref(props.mappingType)
const localMappingItems = ref<any[]>([...props.mappingItems])
const localMappingDataset = ref(props.mappingDataset)
const localMappingKeyProperty = ref(props.mappingKeyProperty)
const localMappingValueProperty = ref(props.mappingValueProperty)

const context = computed(() => store.context || {})

// @ts-ignore
const datasources = computed(() => context.value.reportDef?.datasources || [])

const datasetOptions = computed(() =>
  props.datasets.map((dataset: any) => ({
    value: dataset.name,
    label: dataset.name
  }))
)

const mappingFieldOptions = computed(() =>
  mappingFields.value.map((field: any) => ({
    value: field.name,
    label: field.name
  }))
)

const mappingTypeOptions = computed(() => [
  { value: 'simple', label: t('property.dataset.simple') },
  { value: 'dataset', label: t('property.dataset.ds') }
])

watch(() => props.mappingType, (newVal) => {
  localMappingType.value = newVal
  emit('mapping-type-change', newVal)
})

watch(() => props.mappingItems, (newVal) => {
  localMappingItems.value = [...newVal]
  emit('mapping-items-change', newVal)
})

watch(() => props.mappingDataset, (newVal) => {
  localMappingDataset.value = newVal
  emit('mapping-dataset-change', newVal)
})

watch(() => props.mappingKeyProperty, (newVal) => {
  localMappingKeyProperty.value = newVal
  emit('mapping-key-property-change', newVal)
})

watch(() => props.mappingValueProperty, (newVal) => {
  localMappingValueProperty.value = newVal
  emit('mapping-value-property-change', newVal)
})

watch(() => localMappingDataset.value, () => { loadMappingFields() }, { immediate: true })

function loadMappingFields() {
  mappingFields.value = []

  if (localMappingDataset.value) {
    for (let ds of datasources.value) {
      let datasets = ds.datasets || []
      for (let dataset of datasets) {
        if (dataset.name === localMappingDataset.value) {
          mappingFields.value = dataset.fields || []
          break
        }
      }
      if (mappingFields.value.length > 0) {
        break
      }
    }
  }
}

function handleMappingTypeChange() {
  emit('mapping-type-change', localMappingType.value)
  setDirty()
}

function handleAddMapping() {
  currentMappingItem.value = { value: '', label: '' }
  dialogOperation.value = 'add'
  editingIndex.value = -1
  dialogVisible.value = true
}

function handleEditMapping(index: number) {
  const item = localMappingItems.value[index]
  currentMappingItem.value = { value: item.value, label: item.label }
  dialogOperation.value = 'edit'
  editingIndex.value = index
  dialogVisible.value = true
}

function handleMappingSave(data: any) {
  if (dialogOperation.value === 'add') {
    localMappingItems.value.push(data)
  } else {
    if (editingIndex.value >= 0) {
      localMappingItems.value[editingIndex.value] = data
    }
  }
  emit('mapping-items-change', localMappingItems.value)
  setDirty()
}

function handleDeleteMapping(index: number) {
  const item = localMappingItems.value[index]
  showConfirm(t('property.dataset.delConfirm')).then(() => {
    const newMappingItems = [...localMappingItems.value]
    const itemIndex = newMappingItems.indexOf(item)

    if (itemIndex !== -1) {
      newMappingItems.splice(itemIndex, 1)
      localMappingItems.value = newMappingItems
      emit('mapping-items-change', newMappingItems)
      setDirty()
    }
  })
}

function handleMappingDatasetChange() {
  emit('mapping-dataset-change', localMappingDataset.value)
  setDirty()
}

function handleMappingKeyPropertyChange() {
  emit('mapping-key-property-change', localMappingKeyProperty.value)
  setDirty()
}

function handleMappingValuePropertyChange() {
  emit('mapping-value-property-change', localMappingValueProperty.value)
  setDirty()
}
</script>
<style scoped>
.top-button{
  display: flex;
  justify-content: end;
}

</style>
