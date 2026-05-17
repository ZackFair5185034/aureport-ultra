<script setup lang="ts">
import { v1 as uuid } from 'uuid'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'
import ConditionContentDialog from '../condition-content-dialog/index.vue'

defineOptions({ name: 'ConditionContent' })

const props = withDefaults(defineProps<{
  propertyConditions?: any[]
  selectedItem?: any
  datasetName?: string
  conditions?: any[]
  resetSelection?: boolean
}>(), {
  propertyConditions: () => [],
  selectedItem: null,
  datasetName: '',
  conditions: () => [],
  resetSelection: true,
})
const emit = defineEmits<{
  (e: 'condition-added', value: any): void
  (e: 'condition-updated', value: any): void
  (e: 'condition-deleted', value: any): void
  (e: 'condition-selected', value: any): void
}>()
const { t } = useI18n()
const store = useReportStore()
const context = computed(() => store.context || {})
// @ts-ignore
const datasources = computed(() => context.value.reportDef?.datasources || [])

const conditionList = ref<HTMLSelectElement | null>(null)
const selectedConditionIndex = ref(-1)
const isAddingCondition = ref(false)
const dialogVisible = ref(false)
const dialogFields = ref<any[]>([])
const dialogCondition = ref<any>(null)
const dialogConditions = ref<any[]>([])

watch(() => props.resetSelection, (newVal) => {
  if (newVal) {
    selectedConditionIndex.value = -1
  }
})

watch(() => props.conditions, (newVal) => {
  if (newVal) {
    selectedConditionIndex.value = -1
  }
}, { immediate: true })

watch(() => props.selectedItem, () => {}, { immediate: true })

function getConditionText(condition: any): string {
  let text = `${condition.left} ${condition.operation} ${condition.right}`
  if (condition.type === 'property' && (!condition.left || condition.left === '')) {
    text = `${t('dialog.propCondition.currentValue')} ${condition.operation} ${condition.right || condition.expr}`
  }

  if (condition.join && props.conditions.indexOf(condition) > 0) {
    text = `${condition.join} ${text}`
  }

  return text
}

function addCondition() {
  if (!props.selectedItem) {
    showAlert(t('dialog.propCondition.selectItem'))
    return
  }

  const fields = buildFields()
  const conditions = props.selectedItem.conditions || []

  isAddingCondition.value = true
  dialogFields.value = fields
  dialogCondition.value = null
  dialogConditions.value = conditions
  dialogVisible.value = true
}

function editCondition() {
  if (selectedConditionIndex.value < 0 || selectedConditionIndex.value >= props.conditions.length) {
    showAlert(t('dialog.propCondition.editConditionTip'))
    return
  }

  if (!props.selectedItem) {
    showAlert(t('dialog.propCondition.selectConditionItem'))
    return
  }

  const fields = buildFields()
  const condition = props.conditions[selectedConditionIndex.value]
  const conditions = props.selectedItem.conditions || []

  isAddingCondition.value = false
  dialogFields.value = fields
  dialogCondition.value = condition
  dialogConditions.value = conditions
  dialogVisible.value = true
}

function handleSaveAfter(type: string, left: string, op: string, right: string, join?: string) {
  if (!props.selectedItem)
    return

  if (isAddingCondition.value) {
    const newCondition = {
      type,
      left,
      operation: op,
      right,
      join,
      id: uuid(),
    }
    emit('condition-added', newCondition)
    isAddingCondition.value = false
  }
  else {
    if (selectedConditionIndex.value >= 0 && selectedConditionIndex.value < props.conditions.length) {
      const condition = props.conditions[selectedConditionIndex.value]
      const updatedCondition = {
        ...condition,
        type,
        left,
        operation: op,
        right,
        join,
      }
      emit('condition-updated', updatedCondition)
    }

    isAddingCondition.value = false
  }

  setDirty()
}

function deleteCondition() {
  if (selectedConditionIndex.value < 0 || selectedConditionIndex.value >= props.conditions.length) {
    showAlert(t('dialog.propCondition.delConditionTip'))
    return
  }

  if (!props.selectedItem) {
    showAlert(t('dialog.propCondition.selectDelCondition'))
    return
  }

  const condition = props.conditions[selectedConditionIndex.value]
  emit('condition-deleted', condition)
  selectedConditionIndex.value = -1
  setDirty()
}

function buildFields(): any[] {
  let fields: any[] = []
  if (!props.datasetName || props.datasetName === '') {
    return fields
  }

  for (const ds of datasources.value) {
    const datasets = ds.datasets || []
    for (const dataset of datasets) {
      if (dataset.name === props.datasetName) {
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

function onConditionSelectChange() {
  if (selectedConditionIndex.value >= 0 && selectedConditionIndex.value < props.conditions.length) {
    const selectedCondition = props.conditions[selectedConditionIndex.value]
    emit('condition-selected', selectedCondition)
  }
}
</script>

<template>
  <div>
    <div class="top-button">
      <u-button
        type="info"
        icon="icon-plus-circle"
        :title="t('dialog.propCondition.addValue')"
        @click="addCondition"
      />
      <u-button
        type="info"
        icon="icon-edit"
        :title="t('dialog.propCondition.editConditionItem')"
        @click="editCondition"
      />
      <u-button
        type="info"
        icon="icon-delete"
        :title="t('dialog.propCondition.delCondition')"
        @click="deleteCondition"
      />
    </div>

    <div style="margin-top: 10px;">
      <select
        ref="conditionList"
        v-model="selectedConditionIndex"
        class="form-control condition-select"
        size="100"
        @change="onConditionSelectChange"
      >
        <option
          v-for="(condition, index) in conditions"
          :key="condition.id"
          :value="index"
        >
          {{ getConditionText(condition) }}
        </option>
      </select>
    </div>

    <ConditionContentDialog
      :visible="dialogVisible"
      :dialog-fields="dialogFields"
      :dialog-condition="dialogCondition"
      :dialog-conditions="dialogConditions"
      @saveAfter="handleSaveAfter"
      @close="dialogVisible = false"
    />
  </div>
</template>

<style scoped>
.u-button + .u-button {
  margin-left: 5px;
}

.top-button {
  display: flex;
  justify-content: end;
}

.condition-select {
  height: 500px;
  padding: 3px;
  outline: none;
}
</style>
