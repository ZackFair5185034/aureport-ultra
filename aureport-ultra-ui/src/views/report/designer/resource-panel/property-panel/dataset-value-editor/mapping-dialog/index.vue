<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'MappingDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  mappingItem?: any
  operation?: string
}>(), {
  visible: false,
  mappingItem: () => ({ value: '', label: '' }),
  operation: 'add',
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'save', value: any): void
}>()

const { t } = useI18n()

const localMappingItem = ref({ value: '', label: '' })

const dialogTitle = computed(() =>
  props.operation === 'add' ? t('dialog.mapping.add') : t('dialog.mapping.edit'),
)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    localMappingItem.value = {
      value: props.mappingItem.value || '',
      label: props.mappingItem.label || '',
    }
  }
})

function handleSave() {
  if (localMappingItem.value.value === '' || localMappingItem.value.label === '') {
    showAlert(t('dialog.mapping.tip'))
    return
  }

  emit('save', {
    value: localMappingItem.value.value,
    label: localMappingItem.value.label,
  })

  handleClose()
}

function handleClose() {
  emit('update:visible', false)
  localMappingItem.value = { value: '', label: '' }
}
</script>

<template>
  <UDialog
    :title="dialogTitle"
    width="500px"
    :visible="visible"
    :z-index="10000"
    @close="handleClose"
  >
    <div class="dialog-content">
      <div class="form-group">
        <label>{{ t('dialog.mapping.key') }}：</label>
        <div class="u-inline">
          <u-input
            v-model="localMappingItem.value"
            :placeholder="t('dialog.mapping.keyPlaceholder')"
          />
        </div>
      </div>
      <div class="form-group">
        <label>{{ t('dialog.mapping.value') }}：</label>
        <div class="u-inline">
          <u-input
            v-model="localMappingItem.label"
            :placeholder="t('dialog.mapping.valuePlaceholder')"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click.stop="handleClose">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleSave">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
