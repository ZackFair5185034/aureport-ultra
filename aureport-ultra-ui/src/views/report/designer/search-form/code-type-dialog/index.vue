<template>
  <div>
    <u-dialog
      v-model:visible="dialogVisible"
      width="500px"
      @open="onOpen"
      @close="onClose"
    >
      <u-row :gutter="15">
        <u-form
          ref="uForm"
          :model="formData"
          :rules="rules"
          size="medium"
          :label-width="100"
        >
          <u-col :span="24">
            <u-form-item :label="t('searchForm.generateType')" prop="type">
              <u-radio-group v-model="formData.type" :button="true">
                <u-radio
                  v-for="(item, index) in typeOptions"
                  :key="index"
                  :label="item.value"
                  :disabled="item.disabled"
                >
                  {{ item.label }}
                </u-radio>
              </u-radio-group>
            </u-form-item>
            <u-form-item v-if="showFileName" :label="t('searchForm.fileName')" prop="fileName">
              <u-input v-model="formData.fileName" :placeholder="t('searchForm.enterFileName')" clearable />
            </u-form-item>
          </u-col>
        </u-form>
      </u-row>

      <template #footer>
        <div style="text-align: right">
          <u-button @click="close" type="info" style="margin-right: 10px;">
            {{ t('searchForm.cancel') }}
          </u-button>
          <u-button type="primary" @click="handleConfirm">
            {{ t('searchForm.confirm') }}
          </u-button>
        </div>
      </template>
    </u-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, defineOptions, defineProps, defineEmits } from 'vue'
import { useI18n } from 'vue-i18n'

defineOptions({
  name: 'CodeTypeDialog',
  inheritAttrs: false
})

const props = defineProps<{
  showFileName: boolean
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'confirm', data: { fileName?: string; type: string }): void
  (e: 'update:visible', val: boolean): void
}>()

const { t } = useI18n()

const dialogVisible = ref(false)
const formData = reactive<{ fileName: string | undefined; type: string }>({
  fileName: undefined,
  type: 'file'
})

const rules = {
  fileName: [
    {
      required: true as const,
      message: t('searchForm.enterFileName'),
      trigger: 'blur' as const
    }
  ],
  type: [
    {
      required: true as const,
      message: t('searchForm.generateType') + '不能为空',
      trigger: 'change' as const
    }
  ]
}

const typeOptions = computed(() => [
  { label: t('searchForm.page'), value: 'file', disabled: false },
  { label: t('searchForm.dialog'), value: 'dialog', disabled: false }
])

watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal
  },
  { immediate: true }
)

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

const uForm = ref(null)

function onOpen() {
  if (props.showFileName) {
    formData.fileName = `${+new Date()}.vue`
  }
}

function onClose() {
  // noop
}

function close() {
  dialogVisible.value = false
}

function handleConfirm() {
  ;(uForm.value as any)?.validate((valid: boolean) => {
    if (!valid) return
    emit('confirm', { ...formData })
    close()
  })
}
</script>
