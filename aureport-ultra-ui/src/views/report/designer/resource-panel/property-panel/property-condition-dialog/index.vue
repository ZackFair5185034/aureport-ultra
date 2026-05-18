<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { setDirty } from '@/utils/table'
import ConditionConfig from '@/views/report/designer/resource-panel/property-panel/property-condition-dialog/condition-config/index.vue'
import ConditionContent from '@/views/report/designer/resource-panel/property-panel/property-condition-dialog/condition-content/index.vue'
import ConditionItem from '@/views/report/designer/resource-panel/property-panel/property-condition-dialog/condition-item/index.vue'

defineOptions({ name: 'ConditionBody' })

const props = withDefaults(defineProps<{
  visible?: boolean
  datasetName?: string
  conditionPropertyItems?: any[]
  propertyConditions?: any[]
}>(), {
  visible: false,
  datasetName: '',
  conditionPropertyItems: () => [],
  propertyConditions: () => [],
})
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saveAfter', value: any[]): void
  (e: 'item-added', value: any): void
  (e: 'item-updated', value: any): void
  (e: 'item-deleted', value: number): void
}>()
const { t } = useI18n()
const store = useReportStore()
const context = computed(() => store.context)

const propGroup = ref<HTMLFieldSetElement | null>(null)
const selectedItem = ref<any>(null)
const selectedItemIndex = ref(-1)
const showPropertyGroup = ref(false)
const localPropertyConditions = ref<any[]>([])
const localDatasetName = ref('')
const currentConditions = ref<any[]>([])
const resetConditionSelection = ref(true)

watch(() => props.propertyConditions, (newVal) => {
  localPropertyConditions.value = [...newVal]
}, { deep: true, immediate: true })

watch(() => props.visible, (newVal) => {
  if (newVal) {
    localDatasetName.value = props.datasetName
    localPropertyConditions.value.splice(0)
    for (const item of props.conditionPropertyItems) {
      localPropertyConditions.value.push(item)
    }

    if (localPropertyConditions.value.length > 0) {
      selectFirstItem()
    }
    else {
      clearSelection()
    }
  }
})

function onItemAdded(newItem: any) {
  localPropertyConditions.value.push(newItem)
  emit('item-added', newItem)
  setDirty()
}

function onItemUpdated(item: any) {
  const index = localPropertyConditions.value.findIndex(existingItem => existingItem.id === item.id)
  if (index !== -1) {
    localPropertyConditions.value[index] = item
  }

  emit('item-updated', item)
  setDirty()
}

function onItemDeleted(index: number) {
  if (index >= 0 && index < localPropertyConditions.value.length) {
    const deletedItem = localPropertyConditions.value[index]
    localPropertyConditions.value.splice(index, 1)

    emit('item-deleted', index)

    if (selectedItem.value && selectedItem.value.id === deletedItem.id) {
      if (localPropertyConditions.value.length > 0) {
        nextTick(() => {
          selectedItemIndex.value = 0
        })
      }
      else {
        selectedItem.value = null
        selectedItemIndex.value = -1
        showPropertyGroup.value = false
        currentConditions.value = []
        resetConditionSelection.value = true
      }
    }

    setDirty()
  }
}

function onItemSelected(item: any) {
  selectedItem.value = item

  if (!item) {
    showPropertyGroup.value = false
    currentConditions.value = []
    resetConditionSelection.value = true
    return
  }

  showPropertyGroup.value = true

  if (!item.conditions) {
    item.conditions = []
  }

  currentConditions.value = [...item.conditions]
  resetConditionSelection.value = false
  setDirty()
}

function onPropertyChanged(updatedItem: any) {
  if (updatedItem) {
    const index = localPropertyConditions.value.findIndex(item => item.name === updatedItem.name)
    if (index !== -1) {
      const currentConditionsData = selectedItem.value ? selectedItem.value.conditions : []

      localPropertyConditions.value[index] = updatedItem

      if (currentConditionsData && currentConditionsData.length > 0) {
        localPropertyConditions.value[index].conditions = currentConditionsData
      }
    }
  }

  setDirty()
}

function onConditionAdded(newCondition: any) {
  if (selectedItem.value) {
    if (!selectedItem.value.conditions) {
      selectedItem.value.conditions = []
    }

    selectedItem.value.conditions.push(newCondition)
    currentConditions.value = [...selectedItem.value.conditions]
  }

  setDirty()
}

function onConditionUpdated(updatedCondition: any) {
  if (selectedItem.value && selectedItem.value.conditions) {
    const index = selectedItem.value.conditions.findIndex((c: any) => c.id === updatedCondition.id)
    if (index !== -1) {
      selectedItem.value.conditions.splice(index, 1, updatedCondition)
      currentConditions.value = [...selectedItem.value.conditions]
    }
  }

  setDirty()
}

function onConditionDeleted(condition: any) {
  if (selectedItem.value && selectedItem.value.conditions) {
    const index = selectedItem.value.conditions.findIndex((c: any) => c.id === condition.id)
    if (index !== -1) {
      selectedItem.value.conditions.splice(index, 1)
      currentConditions.value = [...selectedItem.value.conditions]
    }
  }

  setDirty()
}

function onItemIndexChanged(index: number) {
  selectedItemIndex.value = index
}

function selectFirstItem() {
  if (localPropertyConditions.value.length > 0) {
    selectedItemIndex.value = 0
  }
}

function clearSelection() {
  selectedItem.value = null
  selectedItemIndex.value = -1
  showPropertyGroup.value = false
  currentConditions.value = []
  resetConditionSelection.value = true
}

function handleClose() {
  emit('update:visible', false)
}

function handleOk() {
  emit('update:visible', false)

  const conditionsToReturn = localPropertyConditions.value.map((item) => {
    return structuredClone(item)
  })

  emit('saveAfter', conditionsToReturn)
}
</script>

<template>
  <UDialog
    :title="t('dialog.propCondition.title')"
    width="1200px"
    top="50px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="condition-body-container">
      <fieldset class="fieldset-small">
        <legend class="legend-style">{{ t('dialog.propCondition.config') }}</legend>
        <ConditionItem
          :property-conditions="localPropertyConditions"
          :selected-item-index="selectedItemIndex"
          @item-added="onItemAdded"
          @item-updated="onItemUpdated"
          @item-deleted="onItemDeleted"
          @item-selected="onItemSelected"
          @item-index-changed="onItemIndexChanged"
        />
      </fieldset>

      <fieldset class="fieldset-medium">
        <legend class="legend-style">{{ t('dialog.propCondition.conditionConfig') }}</legend>
        <ConditionContent
          :property-conditions="localPropertyConditions"
          :selected-item="selectedItem"
          :dataset-name="localDatasetName"
          :conditions="currentConditions"
          :reset-selection="resetConditionSelection"
          @condition-added="onConditionAdded"
          @condition-updated="onConditionUpdated"
          @condition-deleted="onConditionDeleted"
        />
      </fieldset>

      <fieldset
        v-show="showPropertyGroup"
        ref="propGroup"
        class="fieldset-large"
      >
        <legend class="legend-style">{{ t('dialog.propCondition.propConfig') }}</legend>
        <ConditionConfig
          :item="selectedItem"
          @property-changed="onPropertyChanged"
        />
      </fieldset>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click.stop="handleClose">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
.condition-body-container {
  padding: 10px;
  height: 560px;
  overflow: auto;
}

.fieldset-small {
  padding: 10px;
  border: solid 1px #dddddd;
  border-radius: 8px;
  width: 160px;
  display: inline-block;
}

.fieldset-medium {
  padding: 10px;
  border: solid 1px #dddddd;
  border-radius: 8px;
  width: 325px;
  display: inline-block;
  vertical-align: top;
  margin-left: 10px;
}

.fieldset-large {
  padding: 10px;
  border: solid 1px #dddddd;
  border-radius: 8px;
  width: 550px;
  display: inline-block;
  vertical-align: top;
  margin-left: 10px;
}

.legend-style {
  width: auto;
  margin-bottom: 1px;
  border-bottom: none;
  font-size: inherit;
  color: #4b4b4b;
}
</style>
