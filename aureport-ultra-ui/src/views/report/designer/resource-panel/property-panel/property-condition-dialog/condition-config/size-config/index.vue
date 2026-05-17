<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

defineOptions({ name: 'SizeConfig' })

const props = withDefaults(defineProps<{
  rowHeight?: number | null
  colWidth?: number | null
}>(), {
  rowHeight: null,
  colWidth: null,
})

const emit = defineEmits<{
  (e: 'size-change', value: any): void
}>()

const { t } = useI18n()

const rowHeightChecked = ref(false)
const localRowHeight = ref(0)
const colWidthChecked = ref(false)
const localColWidth = ref(0)

watch(() => props.rowHeight, (newVal) => {
  loadRowHeight(newVal)
}, { immediate: true })

watch(() => props.colWidth, (newVal) => {
  loadColWidth(newVal)
}, { immediate: true })

function loadRowHeight(rowHeight: any) {
  rowHeightChecked.value = rowHeight !== null && rowHeight !== undefined && rowHeight !== -1
  localRowHeight.value = rowHeightChecked.value ? rowHeight : 0
}

function loadColWidth(colWidth: any) {
  colWidthChecked.value = colWidth !== null && colWidth !== undefined && colWidth !== -1
  localColWidth.value = colWidthChecked.value ? colWidth : 0
}

function onRowHeightChange() {
  emit('size-change', { type: 'rowHeight', checked: rowHeightChecked.value, value: rowHeightChecked.value ? localRowHeight.value : null })
}

function onRowHeightValueChange() {
  if (rowHeightChecked.value) {
    emit('size-change', { type: 'rowHeight', checked: true, value: localRowHeight.value })
  }
}

function onColWidthChange() {
  emit('size-change', { type: 'colWidth', checked: colWidthChecked.value, value: colWidthChecked.value ? localColWidth.value : null })
}

function onColWidthValueChange() {
  if (colWidthChecked.value) {
    emit('size-change', { type: 'colWidth', checked: true, value: localColWidth.value })
  }
}
</script>

<template>
  <div>
    <u-checkbox-group>
      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="rowHeightChecked" @change="onRowHeightChange">
            {{ t('dialog.propCondition.rowHeight') }}
          </u-checkbox>
        </div>
        <span v-show="rowHeightChecked" style="margin-left: 10px;">
          <div class="u-inline">
            <u-input-number v-model="localRowHeight" @change="onRowHeightValueChange" />
          </div>
        </span>
      </div>

      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="colWidthChecked" @change="onColWidthChange">
            {{ t('dialog.propCondition.colWidth') }}
          </u-checkbox>
        </div>
        <span v-show="colWidthChecked" style="margin-left: 10px;">
          <div class="u-inline">
            <u-input-number v-model="localColWidth" @change="onColWidthValueChange" />
          </div>
        </span>
      </div>
    </u-checkbox-group>
  </div>
</template>
