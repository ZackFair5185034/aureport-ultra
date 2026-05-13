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
      <u-form ref="form" :label-width="80">
        <u-form-item :label="t('dialog.groupItem.name')">
          <u-input
            v-model="name"
            ref="nameInput"
            @keyup.enter="handleOk"
            style="width:240px;"
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'GroupItemDialog' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  visible?: boolean
  groupItem?: any
  operation?: string
}>(), {
  visible: false,
  groupItem: null,
  operation: 'add'
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saveAfter', value: any): void
}>()

const name = ref('')

const dialogTitle = computed(() =>
  props.operation === 'add'
    ? t('dialog.groupItem.addItem')
    : t('dialog.groupItem.editItem')
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
    groupItem: updatedGroupItem
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
  if (props.visible) {
    if (e.key === 'Escape') {
      handleClose()
    }
  }
}
</script>
<style scoped>
</style>
