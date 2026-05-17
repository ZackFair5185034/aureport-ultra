<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'PropertyConditionItemDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  conditionItem?: any | null
  operation?: string
  propertyConditions?: any[]
}>(), {
  visible: false,
  conditionItem: null,
  operation: 'add',
  propertyConditions: () => [],
})

const emit = defineEmits<{
  (e: 'saveAfter', value: { item: any, operation: string }): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const form = ref<any>(null)
const input = ref<any>(null)
const name = ref('')
const localConditionItem = ref<any>(null)

const title = computed(() => {
  if (props.operation === 'add') {
    return t('dialog.conditionItem.add')
  }
  else if (props.operation === 'edit') {
    return t('dialog.conditionItem.edit')
  }

  return t('dialog.conditionItem.title')
})

watch(() => props.visible, (newVal) => {
  if (newVal && props.conditionItem) {
    name.value = props.conditionItem.name || ''
    localConditionItem.value = { ...props.conditionItem }
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
    showAlert(t('dialog.conditionItem.nameTip'))
    return
  }

  const isDuplicate = props.propertyConditions.some((item) => {
    if (props.operation === 'edit' && item === props.conditionItem) {
      return false
    }

    return item.name === name.value
  })

  if (isDuplicate) {
    showAlert(t('dialog.conditionItem.nameExists'))
    return
  }

  if (localConditionItem.value) {
    localConditionItem.value.name = name.value
  }

  emit('saveAfter', {
    item: localConditionItem.value,
    operation: props.operation,
  })

  handleClose()
}

function handleClose() {
  emit('close')
}

function handleKeydown(e: KeyboardEvent) {
  if (props.visible && e.key === 'Escape') {
    handleClose()
  }
}
</script>

<template>
  <UDialog
    :title="title"
    width="500px"
    :visible="visible"
    :z-index="20000"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form ref="form" :label-width="120">
        <u-form-item :label="t('dialog.conditionItem.itemName')">
          <u-input
            ref="input"
            v-model="name"
            :placeholder="t('dialog.conditionItem.nameTip')"
            @keyup.enter="handleOk"
            @click.stop
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="handleClose">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
