<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'ParameterDialog' })

const props = withDefaults(defineProps<{
  visible: boolean
  editData: any
}>(), {
  visible: false,
  editData: null,
})

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'save', name: string, type: string, defaultValue: string): void
}>()

const { t } = useI18n()

const name = ref('')
const type = ref('')
const defaultValue = ref('')
const form = ref<any>(null)

const typeOptions = computed(() => [
  { value: 'String', label: 'String' },
  { value: 'Integer', label: 'Integer' },
  { value: 'Float', label: 'Float' },
  { value: 'Boolean', label: 'Boolean' },
  { value: 'Date', label: 'Date' },
  { value: 'List', label: 'List' },
])

watch(() => props.editData, (newData) => {
  if (newData) {
    name.value = newData.name || ''
    type.value = newData.type || ''
    defaultValue.value = newData.defaultValue || ''
  }
  else {
    name.value = ''
    type.value = 'String'
    defaultValue.value = ''
  }
}, { immediate: true })

watch(() => props.visible, (newVal) => {
  if (newVal) {
    if (props.editData) {
      name.value = props.editData.name || ''
      type.value = props.editData.type || ''
      defaultValue.value = props.editData.defaultValue || ''
    }
    else {
      name.value = ''
      type.value = 'String'
      defaultValue.value = ''
    }
  }
})

function handleClose() {
  emit('update:visible', false)
}

function handleSave() {
  if (!name.value) {
    showAlert(t('dialog.sqlParam.nameTip'))
    return
  }

  if (!type.value) {
    showAlert(t('dialog.sqlParam.datatypeTip'))
    return
  }

  emit('save', name.value, type.value, defaultValue.value)
  emit('update:visible', false)
}
</script>

<template>
  <UDialog
    :title="$t('dialog.sqlParam.title')"
    width="500px"
    :visible="visible"
    :z-index="20000"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form ref="form" :label-width="100">
        <u-form-item :label="$t('dialog.sqlParam.name')">
          <u-input v-model="name" :placeholder="$t('dialog.sqlParam.namePlaceholder')" />
        </u-form-item>

        <u-form-item :label="$t('dialog.sqlParam.datatype')">
          <u-select
            v-model="type"
            :clearable="true"
          >
            <u-option
              v-for="option in typeOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item :label="$t('dialog.sqlParam.defaultValue')">
          <u-input v-model="defaultValue" :placeholder="$t('dialog.sqlParam.tip')" />
        </u-form-item>
      </u-form>
    </div>

    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="handleClose">{{ $t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleSave">{{ $t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
