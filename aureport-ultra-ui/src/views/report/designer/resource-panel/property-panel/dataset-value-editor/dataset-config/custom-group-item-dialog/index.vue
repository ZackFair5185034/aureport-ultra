<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'GroupItemDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  groupItem?: any
  operation?: string
}>(), {
  visible: false,
  groupItem: null,
  operation: 'add',
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saveAfter', value: any): void
}>()

const { t } = useI18n()

const name = ref('')

const dialogTitle = computed(() =>
  props.operation === 'add'
    ? t('dialog.groupItem.addItem')
    : t('dialog.groupItem.editItem'),
)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    name.value = props.groupItem?.name || ''
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleOk() {
  if (!name.value.trim()) {
    showAlert(t('dialog.groupItem.nameTip'))
    return
  }

  const updatedGroupItem = props.groupItem ? { ...props.groupItem, name: name.value } : null

  emit('saveAfter', {
    operation: props.operation,
    groupItem: updatedGroupItem,
  })

  handleClose()
}

function handleClose() {
  emit('update:visible', false)
}

function handleClosed() {
  name.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (props.visible && e.key === 'Escape') {
    handleClose()
  }
}
</script>

<template>
  <UDialog
    :title="dialogTitle"
    width="400px"
    :visible="visible"
    :z-index="20000"
    @close="handleClose"
    @closed="handleClosed"
  >
    <div class="dialog-content">
      <u-form :label-width="80">
        <u-form-item :label="t('dialog.groupItem.name')">
          <u-input
            v-model="name"
            style="width:240px;"
            @keyup.enter="handleOk"
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click.stop="handleClose">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
