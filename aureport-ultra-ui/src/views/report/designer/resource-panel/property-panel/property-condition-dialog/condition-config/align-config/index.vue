<script setup lang="ts">
import { onBeforeMount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
/// @ts-expect-error JS module
import configOptions from '../constants/config-options.js'

defineOptions({ name: 'AlignConfig' })

const props = withDefaults(defineProps<{
  cellStyle?: any
}>(), {
  cellStyle: () => ({}),
})

const emit = defineEmits<{
  (e: 'align-change', value: any): void
}>()

const { t } = useI18n()

const alignChecked = ref(false)
const align = ref('')
const alignScope = ref('cell')
const valignChecked = ref(false)
const valign = ref('')
const valignScope = ref('cell')
const alignOptions = ref<any[]>([])
const valignOptions = ref<any[]>([])
const scopeOptions = ref<any[]>([])

onBeforeMount(() => {
  alignOptions.value = configOptions.getAlignOptions(t)
  valignOptions.value = configOptions.getValignOptions(t)
  scopeOptions.value = configOptions.getScopeOptions(t)
})

watch(() => props.cellStyle, (newVal) => {
  loadAlignProperties(newVal)
}, { immediate: true, deep: true })

function loadAlignProperties(cellStyle: any) {
  if (!cellStyle)
    return

  alignChecked.value = !!(cellStyle.align && cellStyle.align !== '')
  align.value = alignChecked.value ? cellStyle.align : ''
  alignScope.value = cellStyle.alignScope || 'cell'

  valignChecked.value = !!(cellStyle.valign && cellStyle.valign !== '')
  valign.value = valignChecked.value ? cellStyle.valign : ''
  valignScope.value = cellStyle.valignScope || 'cell'
}

function onAlignChange() {
  emit('align-change', { type: 'align', checked: alignChecked.value, value: alignChecked.value ? 'center' : null, scope: alignChecked.value ? 'cell' : null })
}

function onAlignValueChange() {
  emit('align-change', { type: 'align', checked: alignChecked.value, value: align.value, scope: alignScope.value })
}

function onAlignScopeChange() {
  emit('align-change', { type: 'align', checked: alignChecked.value, value: align.value, scope: alignScope.value })
}

function onValignChange() {
  emit('align-change', { type: 'valign', checked: valignChecked.value, value: valignChecked.value ? 'middle' : null, scope: valignChecked.value ? 'cell' : null })
}

function onValignValueChange() {
  emit('align-change', { type: 'valign', checked: valignChecked.value, value: valign.value, scope: valignScope.value })
}

function onValignScopeChange() {
  emit('align-change', { type: 'valign', checked: valignChecked.value, value: valign.value, scope: valignScope.value })
}
</script>

<template>
  <div>
    <u-checkbox-group>
      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="alignChecked" @change="onAlignChange">
            {{ t('dialog.propCondition.align') }}
          </u-checkbox>
        </div>
        <span v-show="alignChecked" style="margin-left: 10px">
          <div class="u-inline">
            <u-select
              v-model="align"
              :clearable="true"
              style="width: 120px"
              @change="onAlignValueChange"
            >
              <u-option
                v-for="option in alignOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
          <span style="margin-left: 15px;">{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="alignScope"
              :clearable="true"
              style="width: 120px"
              @change="onAlignScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>

      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="valignChecked" @change="onValignChange">
            {{ t('dialog.propCondition.valign') }}
          </u-checkbox>
        </div>
        <span v-show="valignChecked" style="margin-left: 10px">
          <div class="u-inline">
            <u-select
              v-model="valign"
              :clearable="true"
              style="width: 120px"
              @change="onValignValueChange"
            >
              <u-option
                v-for="option in valignOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
          <span style="margin-left: 15px;">{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="valignScope"
              :clearable="true"
              style="width: 120px"
              @change="onValignScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>
    </u-checkbox-group>
  </div>
</template>
