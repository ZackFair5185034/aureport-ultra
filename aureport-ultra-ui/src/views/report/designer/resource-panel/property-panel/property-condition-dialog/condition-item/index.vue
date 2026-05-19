<script setup lang="ts">
import { v1 as uuid } from 'uuid'

import { useI18n } from 'vue-i18n'
import { showAlert, showConfirm } from '@/utils/comnon'
import { setDirty } from '@/utils/table'
import PropertyConditionItemDialog from '@/views/report/designer/resource-panel/property-panel/property-condition-dialog/condition-item-dialog/index.vue'

defineOptions({ name: 'ConditionItem' })

const props = withDefaults(defineProps<{
  propertyConditions?: any[]
  selectedItemIndex?: number
}>(), {
  propertyConditions: () => [],
  selectedItemIndex: -1,
})

const emit = defineEmits<{
  (e: 'item-selected', value: any): void
  (e: 'item-added', value: any): void
  (e: 'item-updated', value: any): void
  (e: 'item-deleted', value: number): void
  (e: 'item-index-changed', value: number): void
}>()

const { t } = useI18n()

const itemSelect = ref<HTMLSelectElement | null>(null)
const selectedItem = ref<any>(null)
const dialogVisible = ref(false)
const currentConditionItem = ref<any>(null)
const currentOperation = ref('add')

watch(() => props.selectedItemIndex, (newVal) => {
  if (newVal < 0 || newVal >= props.propertyConditions.length) {
    selectedItem.value = null
    emit('item-selected', null)
  }
  else {
    selectedItem.value = props.propertyConditions[newVal]
    emit('item-selected', selectedItem.value)
  }
}, { immediate: true })

function addItem() {
  const newItem = { name: '', id: uuid() }
  currentConditionItem.value = newItem
  currentOperation.value = 'add'
  dialogVisible.value = true
}

function editItem() {
  if (props.selectedItemIndex < 0 || props.selectedItemIndex >= props.propertyConditions.length) {
    showAlert(t('dialog.propCondition.editTip'))
    return
  }

  const item = props.propertyConditions[props.selectedItemIndex]
  currentConditionItem.value = item
  currentOperation.value = 'edit'
  dialogVisible.value = true
}

function deleteItem() {
  if (props.selectedItemIndex < 0 || props.selectedItemIndex >= props.propertyConditions.length) {
    showAlert(t('dialog.propCondition.delTip'))
    return
  }

  const item = props.propertyConditions[props.selectedItemIndex]
  const itemName = item.name || ''

  showConfirm(`${t('dialog.propCondition.delConfirm')}[${itemName}]?`).then(() => {
    emit('item-deleted', props.selectedItemIndex)
    setDirty()
  })
}

function onItemSelectChange(event: Event) {
  const newIndex = parseInt((event.target as HTMLSelectElement).value)
  emit('item-index-changed', newIndex)
  setDirty()
}

function handleSaveAfter({ item, operation }: { item: any, operation: string }) {
  if (operation === 'add') {
    if (currentConditionItem.value) {
      currentConditionItem.value.name = item.name
      emit('item-added', currentConditionItem.value)
      const newIndex = props.propertyConditions.length - 1
      emit('item-index-changed', newIndex)
    }
  }
  else if (operation === 'edit' && currentConditionItem.value) {
    currentConditionItem.value.name = item.name
    emit('item-updated', currentConditionItem.value)
  }

  setDirty()
}

function handleDialogClose() {
  dialogVisible.value = false
  setTimeout(() => {
    currentConditionItem.value = null
    currentOperation.value = 'add'
  }, 300)
}
</script>

<template>
  <div>
    <div>
      <u-button
        type="info"
        icon="icon-plus-circle"
        :title="t('dialog.propCondition.addItem')"
        @click="addItem"
      />
      <u-button
        type="info"
        icon="icon-edit"
        :title="t('dialog.propCondition.editItem')"
        @click="editItem"
      />
      <u-button
        type="info"
        icon="icon-delete"
        :title="t('dialog.propCondition.delItem')"
        @click="deleteItem"
      />
    </div>

    <div style="margin-top: 10px;">
      <select
        ref="itemSelect"
        size="10"
        class="form-control item-select"
        :value="selectedItemIndex"
        @change="onItemSelectChange"
      >
        <option
          v-for="(item, index) in propertyConditions"
          :key="item.id"
          :value="index"
        >
          {{ item.name }}
        </option>
      </select>
    </div>

    <PropertyConditionItemDialog
      :visible="dialogVisible"
      :conditionItem="currentConditionItem"
      :operation="currentOperation"
      :propertyConditions="propertyConditions"
      @saveAfter="handleSaveAfter"
      @close="handleDialogClose"
    />
  </div>
</template>

<style scoped>
.u-button + .u-button {
  margin-left: 5px;
}

.item-select {
  height: 500px;
  outline: none;
}
</style>
