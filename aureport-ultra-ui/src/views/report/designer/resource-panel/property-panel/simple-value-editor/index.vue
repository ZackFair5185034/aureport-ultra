<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils'
import { useReportStore } from '@/stores/report'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'SimpleValueEditor' })

const props = withDefaults(defineProps<{
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
}>(), {
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0,
})
const { t } = useI18n()
const store = useReportStore()

const content = ref('')
const lineHeight = ref('')

function loadCellData() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef) {
    content.value = ''
    lineHeight.value = ''
    return
  }

  content.value = cellDef && cellDef.value && cellDef.value.value !== undefined ? cellDef.value.value : ''

  lineHeight.value = cellDef?.cellStyle?.lineHeight != null ? String(cellDef.cellStyle.lineHeight) : ''
}

function onContentChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  const newCellDef = deepCopy(cellDef)

  if (newCellDef) {
    if (!newCellDef.value) {
      newCellDef.value = { type: 'simple', value: '' }
    }

    newCellDef.value.type = 'simple'
    newCellDef.value.value = content.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }

  const hot = TableManager.get()
  if (hot && props.rowIndex !== null && props.colIndex !== null) {
    hot.setDataAtCell(props.rowIndex, props.colIndex, content.value)
  }

  setDirty()
}

function onLineHeightChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  const newCellDef = deepCopy(cellDef)

  if (newCellDef) {
    if (!newCellDef.cellStyle) {
      newCellDef.cellStyle = {}
    }

    // @ts-expect-error: lineHeight may not exist on cellStyle
    newCellDef.cellStyle.lineHeight = lineHeight.value

    const hot = TableManager.get()
    if (hot) {
      const td = hot.getCell(props.rowIndex, props.colIndex)
      if (td) {
        td.style.lineHeight = lineHeight.value === '' ? '' : lineHeight.value
        hot.render()
      }
    }

    setDirty()
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }
}

watch(
  () => props.rowIndex,
  () => {
    loadCellData()
  },
  { immediate: true },
)
watch(
  () => props.colIndex,
  () => {
    loadCellData()
  },
  { immediate: true },
)

onMounted(() => {
  loadCellData()
})
</script>

<template>
  <div class="simple-value-editor">
    <div class="property-quote">
      {{ t('property.simple.config') }}
    </div>

    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('property.simple.lineHeight')">
        <u-input-number
          v-model="lineHeight"
          :placeholder="t('property.simple.tip')"
          @change="onLineHeightChange"
        />
      </u-form-item>
      <u-form-item class="property-label" :label="t('property.simple.content')">
        <textarea
          v-model="content"
          style="width: 220px"
          class="form-control"
          rows="3"
          @input="onContentChange"
        />
      </u-form-item>
    </u-form>
  </div>
</template>

<style scoped>
textarea:focus {
  outline: none;
}
</style>
