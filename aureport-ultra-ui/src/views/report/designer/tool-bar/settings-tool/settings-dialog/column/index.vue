<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { mmToPoint, pointToMM } from '@/utils/table'

defineOptions({ name: 'ColumnSettings' })

const props = withDefaults(defineProps<{
  paper?: any
}>(), {
  paper: () => ({}),
})

const emit = defineEmits<{
  (e: 'update:paper', value: any): void
  (e: 'column-enabled-change'): void
  (e: 'column-count-change'): void
  (e: 'column-margin-change'): void
}>()

const { t } = useI18n()

const localPaper = ref({ ...props.paper })

const columnMargin = computed(() => pointToMM(localPaper.value.columnMargin))

const columnCountOptions = computed(() => {
  const options = []
  for (let i = 1; i <= 9; i++) {
    options.push({
      value: i + 1,
      label: `${i + 1}${t('dialog.setting.columnUnit')}`,
    })
  }

  return options
})

const columnEnabledOptions = computed(() => [
  { value: false, label: t('dialog.setting.disable') },
  { value: true, label: t('dialog.setting.enable') },
])

watch(() => props.paper, (newVal) => {
  localPaper.value = { ...newVal }
}, { deep: true })

function handleColumnEnabledChange(value: boolean) {
  emit('update:paper', { ...localPaper.value, columnEnabled: value })
  emit('column-enabled-change')
}

function handleColumnCountChange(value: number) {
  emit('update:paper', { ...localPaper.value, columnCount: value })
  emit('column-count-change')
}

function handleColumnMarginChange(value: number) {
  if (!isNaN(value)) {
    emit('update:paper', { ...localPaper.value, columnMargin: mmToPoint(value) })
    emit('column-margin-change')
  }
}
</script>

<template>
  <div>
    <div class="div-col-desc">{{ $t('dialog.setting.colDesc') }}</div>

    <div class="form-group form-group-col-enabled">
      <label>{{ $t('dialog.setting.column') }}：</label>
      <div class="u-inline">
        <u-radio-group
          :value="localPaper.columnEnabled"
          @change="handleColumnEnabledChange"
        >
          <u-radio
            v-for="option in columnEnabledOptions"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </div>
    </div>

    <div class="form-group form-group-col-config">
      <label>{{ $t('dialog.setting.columnCount') }}：</label>
      <div class="u-inline">
        <u-select
          :value="localPaper.columnCount"
          :disabled="!localPaper.columnEnabled"
          @change="handleColumnCountChange"
        >
          <u-option
            v-for="option in columnCountOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </div>

      <span class="span-col-margin">
        <label>{{ $t('dialog.setting.columnMargin') }}：</label>
      </span>
      <div class="u-inline">
        <u-input-number
          :value="columnMargin"
          :disabled="!localPaper.columnEnabled"
          @change="handleColumnMarginChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.div-col-desc {
  margin: 0 5px 10px 5px;
  color: #999999;
  font-size: 12px;
}

.form-group-col-enabled {
  margin-top: 8px;
}

.form-group-col-config {
  margin-top: 1px;
  display: inline-block;
}

.span-col-margin {
  margin-left: 20px;
}
</style>
