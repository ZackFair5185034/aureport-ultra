<template>
  <UDialog
    :title="t('dialog.customGroup.title')"
    width="800px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="custom-group-dialog">
      <div class="form-group">
        <!-- 分组项管理 -->
        <div class="group-items-section">
          <div class="button-group">
            <u-button
                type="info"
                icon="icon-plus-circle"
                :title="t('dialog.customGroup.addGroup')"
                @click="addItem"
            >
            </u-button>
            <u-button
                type="info"
                icon="icon-delete"
                :title="t('dialog.customGroup.deleteGroup')"
                @click="deleteItem"
            >
            </u-button>
            <u-button
                type="info"
                icon="icon-edit"
                :title="t('dialog.customGroup.editGroup')"
                @click="editItem"
            >
            </u-button>
          </div>

          <div style="margin-top: 5px" >
            <select
                v-model="selectedItemIndex"
                size="15"
                class="form-control group-select"
                @change="onSelectedItemChange"
            >
              <option v-for="(item, index) in localGroupItems" :key="index" :value="index">
                {{ item.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- 条件管理 -->
        <div class="conditions-section" v-show="selectedItemIndex !== null && selectedItemIndex !== -1">
          <div class="condition-header">
            <label>{{ t('dialog.customGroup.groupCondition') }}：</label>
            <div class="button-group">
              <u-button
                  type="info"
                  icon="icon-plus-circle"
                  :title="t('dialog.customGroup.addCondition')"
                  @click="addCondition"
              >
              </u-button>
              <u-button
                  type="info"
                  icon="icon-delete"
                  :title="t('dialog.customGroup.delTitle')"
                  @click="deleteCondition"
              >
              </u-button>
              <u-button
                  type="info"
                  icon="icon-edit"
                  :title="t('dialog.customGroup.editTip')"
                  @click="editCondition"
              >
              </u-button>
            </div>
          </div>
          <select
            v-model="selectedConditionIndex"
            size="13"
            class="form-control condition-select"
          >
            <option v-for="(condition, index) in currentConditions" :key="index" :value="index">
              {{ formatConditionText(condition, index) }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- GroupItemDialog 组件 -->
    <GroupItemDialog
      v-model:visible="groupItemDialogVisible"
      :group-item="groupItemDialogItem"
      :operation="groupItemDialogOperation"
      @saveAfter="handleGroupItemSave"
    />

    <!-- ConditionDialog 组件 -->
    <ConditionDialog
      v-model:visible="conditionDialogVisible"
      :fields="conditionDialogFields"
      :condition="conditionDialogCondition"
      :conditions="conditionDialogConditions"
      @saveAfter="handleConditionSave"
    />

    <!-- 底部按钮 -->
    <template #footer>
      <div style="text-align: right">
        <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert, showConfirm } from '@/utils/comnon'
import { deepCopy } from '@/components/utils/index'
import GroupItemDialog from '@/views/report/designer/resource-panel/property-panel/dataset-value-editor/dataset-config/custom-group-item-dialog/index.vue'
import ConditionDialog from '@/views/report/designer/resource-panel/property-panel/dataset-value-editor/dataset-config/condition-dialog/index.vue'

defineOptions({ name: 'CustomGroupDialog' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  groupItems?: any[]
  visible?: boolean
  fields?: any[] | null
}>(), {
  groupItems: () => [],
  visible: false,
  fields: null
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'close'): void
  (e: 'save', value: any[]): void
}>()

const localGroupItems = ref<any[]>([])
const selectedItemIndex = ref<number | null>(null)
const selectedConditionIndex = ref<number | null>(null)
const currentConditionIndex = ref<number | null>(null)
const currentItemIndex = ref<number | null>(null)
const conditionDialogVisible = ref(false)
const conditionDialogFields = ref<any[]>([])
const conditionDialogCondition = ref<any>(null)
const conditionDialogConditions = ref<any[]>([])
const groupItemDialogVisible = ref(false)
const groupItemDialogItem = ref<any>(null)
const groupItemDialogOperation = ref('add')

const currentConditions = computed(() => {
  if (selectedItemIndex.value === null || selectedItemIndex.value === -1) {
    return []
  }
  return localGroupItems.value[selectedItemIndex.value].conditions || []
})

watch(() => props.groupItems, (newVal) => {
  localGroupItems.value = deepCopy(newVal)
}, { immediate: true, deep: true })

watch(() => props.visible, (newVal) => {
  if (newVal) {
    localGroupItems.value = deepCopy(props.groupItems)
    selectedItemIndex.value = null
    selectedConditionIndex.value = null
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function generateId(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

function handleOk() {
  emit('save', localGroupItems.value)
  handleClose()
}

function handleClose() {
  emit('update:visible', false)
  emit('close')
}

function addItem() {
  const newItem = { name: '', conditions: [] }
  groupItemDialogItem.value = newItem
  groupItemDialogOperation.value = 'add'
  groupItemDialogVisible.value = true
}

function deleteItem() {
  if (selectedItemIndex.value === null || selectedItemIndex.value === -1) {
    showAlert(t('dialog.customGroup.deleteTip'))
    return
  }

  const item = localGroupItems.value[selectedItemIndex.value]
  showConfirm(`${t('dialog.customGroup.deleteConfirm')}[${item.name}]?`).then(() => {
    localGroupItems.value.splice(selectedItemIndex.value!, 1)
    selectedItemIndex.value = null
    selectedConditionIndex.value = null
  })
}

function editItem() {
  if (selectedItemIndex.value === null || selectedItemIndex.value === -1) {
    showAlert(t('dialog.customGroup.modTip'))
    return
  }

  const item = localGroupItems.value[selectedItemIndex.value]
  groupItemDialogItem.value = item
  groupItemDialogOperation.value = 'edit'
  groupItemDialogVisible.value = true
}

function onSelectedItemChange() {
  selectedConditionIndex.value = null
}

function addCondition() {
  if (selectedItemIndex.value === null || selectedItemIndex.value === -1) {
    showAlert(t('dialog.customGroup.selectTip'))
    return
  }

  const currentItem = localGroupItems.value[selectedItemIndex.value]
  const conditions = currentItem.conditions || []

  conditionDialogConditions.value = conditions
  currentConditionIndex.value = -1
  currentItemIndex.value = selectedItemIndex.value
  conditionDialogFields.value = props.fields || []
  conditionDialogCondition.value = null
  conditionDialogVisible.value = true
}

function editCondition() {
  if (selectedConditionIndex.value === null || selectedConditionIndex.value === -1) {
    showAlert(t('dialog.customGroup.editConditionTip'))
    return
  }

  if (selectedItemIndex.value === null || selectedItemIndex.value === -1) {
    showAlert(t('dialog.customGroup.selectTip'))
    return
  }

  const currentItem = localGroupItems.value[selectedItemIndex.value]
  const conditions = currentItem.conditions || []
  const condition = conditions[selectedConditionIndex.value]

  conditionDialogConditions.value = conditions
  currentConditionIndex.value = selectedConditionIndex.value
  currentItemIndex.value = selectedItemIndex.value
  conditionDialogFields.value = props.fields || []
  conditionDialogCondition.value = condition
  conditionDialogVisible.value = true
}

function handleGroupItemSave(data: any) {
  if (data.operation === 'add') {
    localGroupItems.value.push(data.groupItem)
  }
}

function handleConditionSave(conditionData: any) {
  if (currentItemIndex.value === null || currentItemIndex.value === -1) {
    return
  }

  const currentItem = localGroupItems.value[currentItemIndex.value]
  const conditions = currentItem.conditions || []

  if (conditionData.isEdit && currentConditionIndex.value !== null && currentConditionIndex.value >= 0) {
    const condition = conditions[currentConditionIndex.value]
    if (condition) {
      condition.left = conditionData.left
      condition.operation = conditionData.operation
      condition.op = conditionData.operation
      condition.right = conditionData.right
      condition.join = conditionData.join
    }
  } else {
    const condition = {
      left: conditionData.left,
      operation: conditionData.operation,
      op: conditionData.operation,
      right: conditionData.right,
      join: conditionData.join,
      id: generateId()
    }
    conditions.push(condition)
  }
}

function deleteCondition() {
  if (selectedConditionIndex.value === null || selectedConditionIndex.value === -1) {
    showAlert(t('dialog.customGroup.delConditionTip'))
    return
  }

  if (selectedItemIndex.value === null || selectedItemIndex.value === -1) {
    showAlert(t('dialog.customGroup.selectTip'))
    return
  }

  const currentItem = localGroupItems.value[selectedItemIndex.value]
  const conditions = currentItem.conditions || []

  conditions.splice(selectedConditionIndex.value, 1)
  selectedConditionIndex.value = null
}

function formatConditionText(condition: any, index: number) {
  const op = condition.operation || condition.op
  let text = `${condition.left} ${op} ${condition.right}`

  if (index > 0 && condition.join) {
    text = `${condition.join} ${text}`
  }

  return text
}

function handleKeydown(e: KeyboardEvent) {
  if (props.visible) {
    if (e.key === 'Escape') {
      handleClose()
    }
  }
}
</script>

<style scoped>
.custom-group-dialog {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.form-group {
  display: flex;
  height: 100%;
}

.group-items-section {
  width: 250px;
  margin-right: 20px;
}

.conditions-section {
  flex: 1;
}

.group-select{
  width: 200px;
  height: 285px;
  display: inline-block;
  outline: none
}

.condition-select{
  height: 250px;
  outline: none;
}

.condition-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.condition-header label {
  margin-right: 10px;
}

.u-button + .u-button{
  margin-left: 5px;
}
</style>
