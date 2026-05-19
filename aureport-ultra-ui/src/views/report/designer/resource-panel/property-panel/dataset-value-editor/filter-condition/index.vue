<script setup lang="ts">
import { v1 as uuidv1 } from 'uuid'

import { useI18n } from 'vue-i18n'
import { showAlert, showConfirm } from '@/utils/comnon'
import { setDirty } from '@/utils/table'
import ConditionDialog from '@/views/report/designer/resource-panel/property-panel/dataset-value-editor/dataset-config/condition-dialog/index.vue'

defineOptions({ name: 'FilterConditionTab' })

const props = withDefaults(defineProps<{
  selectedDataset?: string
  conditions?: any[]
  currentFields?: any[]
}>(), {
  selectedDataset: '',
  conditions: () => [],
  currentFields: () => [],
})

const emit = defineEmits<{
  (e: 'update:conditions', value: any[]): void
  (e: 'update-filter-conditions', value: any[]): void
}>()

const { t } = useI18n()

const selectedConditionIndex = ref(-1)
const currentConditionIndex = ref(-1)
const conditionDialogVisible = ref(false)
const conditionDialogFields = ref<any[]>([])
const conditionDialogCondition = ref<any>(null)

function handleAddCondition() {
  if (!props.selectedDataset) {
    showAlert(t('property.dataset.bindDatasetTip'))
    return
  }

  currentConditionIndex.value = -1
  conditionDialogFields.value = props.currentFields
  conditionDialogCondition.value = null
  conditionDialogVisible.value = true
}

function handleEditCondition() {
  if (selectedConditionIndex.value < 0) {
    showAlert(t('property.dataset.selectFilterConditionTip'))
    return
  }

  currentConditionIndex.value = selectedConditionIndex.value
  const condition = props.conditions[selectedConditionIndex.value]
  conditionDialogFields.value = props.currentFields
  conditionDialogCondition.value = condition
  conditionDialogVisible.value = true
}

function handleConditionSave(conditionData: any) {
  const conditions = [...props.conditions]

  if (conditionData.isEdit && currentConditionIndex.value >= 0) {
    const targetCondition = conditions[currentConditionIndex.value]
    if (targetCondition) {
      targetCondition.left = conditionData.left
      targetCondition.operation = conditionData.operation
      targetCondition.right = conditionData.right
      targetCondition.join = conditionData.join
    }
  }
  else {
    const condition = {
      left: conditionData.left,
      operation: conditionData.operation,
      right: conditionData.right,
      join: conditionData.join,
      id: uuidv1(),
    }
    conditions.push(condition)
  }

  emit('update:conditions', conditions)
  emit('update-filter-conditions', conditions)
  setDirty()
}

function handleDeleteCondition() {
  if (selectedConditionIndex.value < 0) {
    showAlert(t('property.dataset.delFilterConditionTip'))
    return
  }

  const condition = props.conditions[selectedConditionIndex.value]
  showConfirm(t('property.dataset.delConfirm')).then(() => {
    const conditions = [...props.conditions]
    const index = conditions.findIndex((c: any) => c.id === condition.id)

    if (index !== -1) {
      conditions.splice(index, 1)
      emit('update:conditions', conditions)
      emit('update-filter-conditions', conditions)
      selectedConditionIndex.value = -1
      setDirty()
    }
  })
}

function formatConditionText(condition: any) {
  let text = `${condition.left} ${condition.operation} ${condition.right}`
  if (condition.join) {
    text = `${condition.join} ${text}`
  }

  return text
}
</script>

<template>
  <div class="form-group" style="padding-top: 10px">
    <!-- 当没有选择数据集时显示提示 -->
    <div v-if="!selectedDataset" class="empty-tip-container">
      <i class="iconfont icon-warning empty-tip-icon" />
      <div class="empty-tip-content">
        <div class="empty-tip-title">{{ t('property.dataset.noDatasetSelected') }}</div>
        <div class="empty-tip-desc">{{ t('property.dataset.bindDatasetTip') }}</div>
      </div>
    </div>

    <!-- 条件列表和操作按钮 -->
    <div v-show="selectedDataset" class="form-group" style="margin-bottom: 10px;">
      <div class="top-button">
        <u-button
          type="info"
          icon="icon-plus-circle"
          :title="t('property.dataset.addFilterCondition')"
          @click="handleAddCondition"
        />
        <u-button
          type="info"
          icon="icon-edit"
          :title="t('property.dataset.editFilterCondition')"
          @click="handleEditCondition"
        />
        <u-button
          type="info"
          icon="icon-delete"
          :title="t('property.dataset.delFilterCondition')"
          @click="handleDeleteCondition"
        />
      </div>

      <div style="margin-top: 10px;">
        <select
          v-model="selectedConditionIndex"
          class="form-control condition-select"
          size="5"
        >
          <option
            v-for="(condition, index) in conditions"
            :key="condition.id"
            :value="index"
          >
            {{ formatConditionText(condition) }}
          </option>
        </select>
      </div>
    </div>

    <!-- 条件对话框组件 -->
    <ConditionDialog
      v-model:visible="conditionDialogVisible"
      :fields="conditionDialogFields"
      :condition="conditionDialogCondition"
      @saveAfter="handleConditionSave"
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
  height: 100px;
  outline: none;
}

.empty-tip-container {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.empty-tip-container:hover {
  border-color: #217346;
  box-shadow: 0 2px 12px rgba(33, 115, 70, 0.15);
}

.empty-tip-icon {
  flex-shrink: 0;
  font-size: 32px;
  color: #217346;
  margin-right: 16px;
}

.empty-tip-content {
  flex: 1;
  min-width: 0;
}

.empty-tip-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  line-height: 1.4;
}

.empty-tip-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}
</style>
