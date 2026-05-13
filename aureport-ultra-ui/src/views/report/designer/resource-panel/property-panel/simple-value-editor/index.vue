<template>
  <div class="simple-value-editor">

    <div class="property-quote">
      {{ t('property.simple.config') }}
    </div>

    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('property.simple.lineHeight')">
        <u-input-number
            v-model="lineHeight"
            @change="onLineHeightChange"
            :placeholder="t('property.simple.tip')"
        />
      </u-form-item>
      <u-form-item class="property-label" :label="t('property.simple.content')">
        <textarea
          v-model="content"
          @input="onContentChange"
          style="width: 220px"
          class="form-control"
          rows="3">
        </textarea>
      </u-form-item>
    </u-form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { setDirty } from '@/utils/table'
import { deepCopy } from '@/components/utils'
import { setCell, getCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager.js'

defineOptions({ name: 'SimpleValueEditor' })

const { t } = useI18n()
const store = useReportStore()

const props = withDefaults(defineProps<{
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
}>(), {
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0
})

const content = ref('')
const lineHeight = ref('')

function loadCellData() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef) {
    content.value = ''
    lineHeight.value = ''
    return
  }

  if (cellDef && cellDef.value && cellDef.value.value !== undefined) {
    content.value = cellDef.value.value
  } else {
    content.value = ''
  }

  if (cellDef && cellDef.cellStyle && cellDef.cellStyle.lineHeight !== undefined) {
    // @ts-ignore
    lineHeight.value = cellDef.cellStyle.lineHeight
  } else {
    lineHeight.value = ''
  }
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

    // @ts-ignore
    newCellDef.cellStyle.lineHeight = lineHeight.value

    const hot = TableManager.get()
    if (hot) {
      const td = hot.getCell(props.rowIndex, props.colIndex)
      if (td) {
        if (lineHeight.value === '') {
          td.style.lineHeight = ''
        } else {
          td.style.lineHeight = lineHeight.value
        }
        hot.render()
      }
    }

    setDirty()
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }
}

watch(() => props.rowIndex, () => { loadCellData() }, { immediate: true })
watch(() => props.colIndex, () => { loadCellData() }, { immediate: true })

onMounted(() => { loadCellData() })
</script>

<style scoped>
textarea:focus {
  outline: none;
}
</style>
