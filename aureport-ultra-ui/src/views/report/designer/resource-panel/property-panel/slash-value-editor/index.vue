<template>
  <div class="slash-value-editor">

    <div class="property-quote">
      <span>{{ t('property.slash.config') }}</span>
    </div>

    <u-form :label-width="100" labelPosition="left">

      <u-form-item class="property-label">
        <u-button
            @click="handleRefresh"
            icon="icon-refresh"
            style="float: right"
        >
          {{ t('property.slash.refresh') }}
        </u-button>
      </u-form-item>

      <div v-for="(slash, index) in slashes" :key="index" class="slash-item">

        <u-form-item class="property-label" :label="t('property.slash.name')" style="margin-bottom: 10px">
          <u-input
              v-model="slash.text"
              style="width: 250px"
              @change="handleSlashChange(index)"
          />
        </u-form-item>

        <u-form-item class="property-label" label="Y" style="margin-bottom: 10px">
          <u-input-number
              v-model="slash.y"
              @change="handleSlashChange(index)"
          />
        </u-form-item>

        <u-form-item class="property-label" label="X" style="margin-bottom: 10px">
          <u-input-number
              v-model="slash.x"
              @change="handleSlashChange(index)"
          />
        </u-form-item>

        <u-form-item class="property-label" :label="t('property.slash.angle')" style="margin-bottom: 10px">
          <u-input-number
              v-model="slash.degree"
              @change="handleSlashChange(index)"
          />
        </u-form-item>

      </div>
    </u-form>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { setDirty } from '@/utils/table'
import { deepCopy } from '@/components/utils'
import { setCell, getCell, getContext } from '@/utils/contextActions'
import CrossTabWidget from '@/views/report/designer/edit-table/cross-tab-widget/class.js'

defineOptions({ name: 'SlashValueEditor' })

const { t } = useI18n()

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

const slashes = ref<any[]>([])

function loadSlashes() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value && cellDef.value.slashes) {
    slashes.value = deepCopy(cellDef.value.slashes)
  } else {
    slashes.value = []
  }
}

function handleSlashChange(index: number) {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef || !cellDef.value || !cellDef.value.slashes) return

  const newCellDef = deepCopy(cellDef)
  newCellDef.value.slashes[index] = deepCopy(slashes.value[index])

  setCell(props.rowIndex, props.colIndex, newCellDef)

  const context = getContext()
  if (context) {
    const crossTabWidget = new CrossTabWidget(context, props.rowIndex, props.colIndex, '')
  }

  setDirty()
}

function handleRefresh() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef) return

  const context = getContext()
  if (context) {
    const crossTabWidget = new CrossTabWidget(context, props.rowIndex, props.colIndex, '')
    crossTabWidget.refreshCell()
    crossTabWidget.doDraw()

    loadSlashes()
  }
}

watch(() => props.rowIndex, () => { loadSlashes() }, { immediate: true })
watch(() => props.colIndex, () => { loadSlashes() }, { immediate: true })

onMounted(() => { loadSlashes() })
</script>

<style scoped>
.slash-item{
  margin-top: 22px;
}
</style>
