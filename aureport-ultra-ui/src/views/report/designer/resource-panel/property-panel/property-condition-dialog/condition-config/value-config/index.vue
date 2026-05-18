<script setup lang="ts">
import VueSimpleSuggest from '@ffrosch/vue-simple-suggest'
import { onBeforeMount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
/// @ts-expect-error JS module
import configOptions from '../constants/config-options.js'
import '@ffrosch/vue-simple-suggest/style.css'

defineOptions({ name: 'ValueConfig' })

const props = withDefaults(defineProps<{
  cellStyle?: any
  newValue?: string
}>(), {
  cellStyle: () => ({}),
  newValue: '',
})

const emit = defineEmits<{
  (e: 'value-change', value: any): void
}>()

const { t } = useI18n()

const newValueChecked = ref(false)
const localNewValue = ref('')
const formatChecked = ref(false)
const format = ref('')
const suggestionList = ref<any[]>([])

onBeforeMount(() => {
  suggestionList.value = configOptions.getSuggestionList()
})

watch(() => props.cellStyle, (newVal) => {
  loadValueProperties(newVal)
}, { immediate: true, deep: true })

watch(() => props.newValue, (newVal) => {
  if (newVal != null && newVal !== '') {
    newValueChecked.value = true
    if (newVal !== localNewValue.value) {
      localNewValue.value = newVal
    }
  }
  else if (newVal === null) {
    newValueChecked.value = false
    localNewValue.value = ''
  }
}, { immediate: true })

function loadValueProperties(cellStyle: any) {
  if (!cellStyle)
    return
  formatChecked.value = cellStyle.format != null
  format.value = formatChecked.value ? cellStyle.format : ''
}

function onNewValueChange() {
  if (newValueChecked.value) {
    emit('value-change', { type: 'newValue', checked: true, value: localNewValue.value || '' })
  }
  else {
    emit('value-change', { type: 'newValue', checked: false, value: null })
  }
}

function onNewValueInputChange() {
  if (newValueChecked.value) {
    emit('value-change', { type: 'newValue', checked: true, value: localNewValue.value })
  }
}

function onFormatChange() {
  emit('value-change', { type: 'format', checked: formatChecked.value, value: formatChecked.value ? format.value : null })
}

function onFormatInputChange() {
  if (formatChecked.value) {
    emit('value-change', { type: 'format', checked: true, value: format.value })
  }
}
</script>

<template>
  <div>
    <u-checkbox-group>
      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="newValueChecked" @change="onNewValueChange">
            {{ t('dialog.propCondition.newValue') }}
          </u-checkbox>
        </div>
        <span v-show="newValueChecked" style="margin-left: 10px;">
          <div class="u-inline">
            <u-input
              v-model="localNewValue"
              :placeholder="t('dialog.propCondition.newValuePlaceholder')"
              style="width: 268px;"
              @change="onNewValueInputChange"
            />
          </div>
        </span>
      </div>

      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="formatChecked" @change="onFormatChange">
            {{ t('dialog.propCondition.format') }}
          </u-checkbox>
        </div>
        <span v-show="formatChecked" style="margin-left: 10px;">
          <VueSimpleSuggest
            v-model="format"
            :list="suggestionList"
            :filter-by-query="true"
            class="simple-suggest"
            style="display: inline-block"
            @update:model-value="onFormatInputChange"
          />
        </span>
      </div>
    </u-checkbox-group>
  </div>
</template>

<style scoped>
.simple-suggest :deep(.default-input) {
  width: 268px !important;
  height: 35px;
}
</style>
