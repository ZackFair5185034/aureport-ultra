<script setup lang="ts">
/* eslint-disable vue/no-unused-refs */
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

defineOptions({ name: 'FieldNameDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  dataset?: any
}>(), {
  visible: false,
  dataset: null,
})

const emit = defineEmits<{
  (e: 'save', fieldName: string, dataset: any): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const _form = ref<any>(null)
const formData = reactive({
  fieldName: '',
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

  emit('save', formData.fieldName.trim(), props.dataset)
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
        <u-form-item :label="$t('tree.fieldName')" prop="fieldName">
          <u-input
            ref="_input"
            v-model="formData.fieldName"
            :placeholder="$t('tree.inputTip')"
            @keyup.enter="handleOk"
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="handleClose">{{ $t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
