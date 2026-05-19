<script setup lang="ts">

import { useI18n } from 'vue-i18n'

defineOptions({ name: 'PagingSettings' })

const props = withDefaults(defineProps<{
  paper?: any
}>(), {
  paper: () => ({}),
})

const emit = defineEmits<{
  (e: 'update:paper', value: any): void
  (e: 'paging-mode-change'): void
  (e: 'fix-rows-change', value: number): void
}>()

const { t } = useI18n()

const localPaper = ref({ ...props.paper })

const pagingModeOptions = computed(() => [
  { value: 'fitpage', label: t('dialog.setting.auto') },
  { value: 'fixrows', label: t('dialog.setting.fixRows') },
])

watch(() => props.paper, (newVal) => {
  localPaper.value = { ...newVal }
}, { deep: true })

function handlePagingModeChange(value: string) {
  emit('update:paper', { ...localPaper.value, pagingMode: value })
  emit('paging-mode-change')
}

function handleFixRowsChange(value: number) {
  emit('update:paper', { ...localPaper.value, fixRows: value })
  emit('fix-rows-change', value)
}
</script>

<template>
  <div>
    <div class="form-group">
      <label>{{ $t('dialog.setting.pagingType') }}：</label>
      <div class="u-inline">
        <u-radio-group
          :value="localPaper.pagingMode"
          @change="handlePagingModeChange"
        >
          <u-radio
            v-for="option in pagingModeOptions"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </div>
    </div>

    <div
      v-show="localPaper.pagingMode === 'fixrows'"
      class="form-group"
    >
      <label>{{ $t('dialog.setting.rowsPerPage') }}：</label>
      <div class="u-inline">
        <u-input-number
          :value="localPaper.fixRows"
          :min="1"
          @change="handleFixRowsChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
