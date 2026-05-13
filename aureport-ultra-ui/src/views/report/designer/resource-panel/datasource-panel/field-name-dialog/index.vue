<template>
  <UDialog
    :title="$t('tree.addField')"
    width="500px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form ref="form" :model="formData" :rules="rules" :label-width="100">
        <u-form-item :label="$t('tree.fieldName')" prop="fieldName">
          <u-input
            :placeholder="$t('tree.inputTip')"
            v-model="formData.fieldName"
            ref="input"
            @keyup.enter="handleOk"
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer><div style="text-align: right">
      <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ $t('dialog.common.cancel') }}</u-button>
      <u-button @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
    </div></template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'

defineOptions({ name: 'FieldNameDialog' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  visible: boolean
  dataset: any
}>(), {
  visible: false,
  dataset: null
})

const emit = defineEmits<{
  (e: 'save', fieldName: string, dataset: any): void
  (e: 'close'): void
}>()

const form = ref<any>(null)
const formData = reactive({
  fieldName: ''
})
const rules = reactive({
  fieldName: [{
    required: true,
    message: t('tree.inputTip'),
    trigger: 'blur'
  }]
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
  if (props.visible) {
    if (e.key === 'Escape') {
      handleClose()
    }
  }
}
</script>

<style scoped>
</style>
