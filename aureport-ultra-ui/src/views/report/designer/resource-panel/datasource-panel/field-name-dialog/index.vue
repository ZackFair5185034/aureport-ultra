<script setup lang="ts">
/* eslint-disable vue/no-unused-refs */

import { useI18n } from 'vue-i18n'

defineOptions({ name: 'FieldNameDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  dataset?: any
  field?: any
  fields?: any[]
}>(), {
  visible: false,
  dataset: null,
  field: null,
  fields: () => [],
})

const emit = defineEmits<{
  (e: 'save', fieldName: string, dataset: any, label?: string, parentPath?: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const _form = ref<any>(null)
const form = _form
const formData = reactive({
  fieldName: '',
  label: '',
  parentPath: '',
})

const parentPathOptions = computed(() => {
  const options: { value: string; label: string }[] = [
    { value: '', label: '(根目录)' },
  ]
  function walk(fields: any[], prefix: string) {
    for (const f of fields) {
      const path = prefix ? `${prefix}.${f.name}` : f.name
      options.push({ value: path, label: path })
      if (f.children?.length) {
        walk(f.children, path)
      }
    }
  }
  if (!props.field) {
    walk(props.fields, '')
  }
  return options
})
const rules = reactive({
  fieldName: [{
    required: true,
    message: t('tree.inputTip'),
    trigger: 'blur',
  }],
})

watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm()
    formData.parentPath = ''
    if (props.field) {
      formData.fieldName = props.field.name || ''
      formData.label = props.field.label || ''
    }
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function resetForm() {
  form.value && form.value.resetFields()
}

function validateForm(): Promise<boolean> {
  return new Promise((resolve) => {
    form.value.validate((valid: boolean) => {
      resolve(valid)
    })
  })
}

async function handleOk() {
  const valid = await validateForm()
  if (!valid) {
    return
  }

  emit('save', formData.fieldName.trim(), props.dataset, formData.label.trim() || undefined, formData.parentPath || undefined)
  emit('close')
}

function handleClose() {
  emit('close')
  setTimeout(() => {
    resetForm()
  }, 300)
}

function handleKeydown(e: KeyboardEvent) {
  if (props.visible && e.key === 'Escape') {
    handleClose()
  }
}
</script>

<template>
  <UDialog
    :title="$t('tree.addField')"
    width="500px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form ref="_form" :model="formData" :rules="rules" :label-width="100">
        <u-form-item v-if="!field" :label="$t('tree.parentField')" prop="parentPath">
          <u-select
            v-model="formData.parentPath"
            :clearable="true"
            class="parent-path-select"
          >
            <u-option
              v-for="option in parentPathOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
              :title="option.label"
            />
          </u-select>
        </u-form-item>
        <u-form-item :label="$t('tree.fieldName')" prop="fieldName">
          <u-input
            ref="_input"
            v-model="formData.fieldName"
            :placeholder="$t('tree.inputTip')"
            @keyup.enter="handleOk"
          />
        </u-form-item>
        <u-form-item :label="$t('tree.fieldLabel')" prop="label">
          <u-input
            v-model="formData.label"
            :placeholder="$t('tree.fieldLabelTip')"
            @keyup.enter="handleOk"
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click.stop="handleClose">{{ $t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
.parent-path-select {
  width: 100%;
}
.parent-path-select :deep(.u-select-selection) {
  white-space: nowrap;
}
.parent-path-select :deep(.u-select-selection .u-select-placeholder),
.parent-path-select :deep(.u-select-selection .u-select-selected-value) {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}
</style>
