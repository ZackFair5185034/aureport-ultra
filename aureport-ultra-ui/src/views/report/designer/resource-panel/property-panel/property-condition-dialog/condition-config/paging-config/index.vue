<script setup lang="ts">
import { onBeforeMount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
// @ts-ignore
import configOptions from '../constants/config-options.js'

defineOptions({ name: 'PagingConfig' })

const props = withDefaults(defineProps<{
  paging?: any
}>(), {
  paging: null,
})

const emit = defineEmits<{
  (e: 'paging-change', value: any): void
}>()

const { t } = useI18n()

const pagingBreakChecked = ref(false)
const pagingPosition = ref('after')
const pagingLine = ref(0)
const pagingPositionOptions = ref<any[]>([])

onBeforeMount(() => {
  pagingPositionOptions.value = configOptions.getPagingPositionOptions(t)
})

watch(() => props.paging, (newVal) => {
  loadPagingProperties(newVal)
}, { immediate: true, deep: true })

function loadPagingProperties(paging: any) {
  pagingBreakChecked.value = !!paging
  if (pagingBreakChecked.value) {
    pagingPosition.value = paging.position || 'after'
    pagingLine.value = paging.line || 0
  }
  else {
    pagingPosition.value = 'after'
    pagingLine.value = 0
  }
}

function onPagingBreakChange() {
  emit('paging-change', {
    checked: pagingBreakChecked.value,
    paging: pagingBreakChecked.value ? { position: pagingPosition.value, line: pagingLine.value } : null,
  })
}

function onPagingPositionChange() {
  if (pagingBreakChecked.value) {
    emit('paging-change', { checked: true, paging: { position: pagingPosition.value, line: pagingLine.value } })
  }
}

function onPagingLineChange() {
  if (pagingBreakChecked.value) {
    emit('paging-change', { checked: true, paging: { position: pagingPosition.value, line: pagingLine.value } })
  }
}
</script>

<template>
  <u-checkbox-group>
    <div class="form-group" style="margin-bottom: 5px;">
      <div class="u-inline">
        <u-checkbox v-model="pagingBreakChecked" @change="onPagingBreakChange">
          {{ t('dialog.propCondition.paging') }}
        </u-checkbox>
      </div>
      <span v-show="pagingBreakChecked" style="margin-left: 10px;">
        <div class="u-inline">
          <u-select
            v-model="pagingPosition"
            :clearable="true"
            @change="onPagingPositionChange"
          >
            <u-option
              v-for="option in pagingPositionOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </div>
        <div class="u-inline" style="margin-left: 10px">
          <u-input-number v-model="pagingLine" @change="onPagingLineChange" />
        </div>
      </span>
    </div>
  </u-checkbox-group>
</template>
