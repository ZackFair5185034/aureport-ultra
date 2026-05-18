<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'URLParameterItemDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  paramItem?: any | null
  operation?: string
}>(), {
  visible: false,
  paramItem: null,
  operation: 'add',
})

const emit = defineEmits<{
  (e: 'saveAfter', value: { paramItem: any, operation: string }): void
  (e: 'update:visible', value: boolean): void
}>()

const { t } = useI18n()

const form = ref<any>(null)
const nameInput = ref<any>(null)
const valueInput = ref<any>(null)
const name = ref('')
const value = ref('')
const localParamItem = ref<any>(null)

const title = computed(() =>
  props.operation === 'add' ? t('dialog.paramItem.add') : t('dialog.paramItem.edit'),
)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    name.value = props.paramItem?.name || ''
    value.value = props.paramItem?.value || ''
    localParamItem.value = props.paramItem ? { ...props.paramItem } : null
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleOk() {
  if (name.value === '' || value.value === '') {
    showAlert(t('dialog.paramItem.tip'))
    return
  }

  if (localParamItem.value) {
    localParamItem.value.name = name.value
    localParamItem.value.value = value.value
  }
  else {
    localParamItem.value = {
      name: name.value,
      value: value.value,
    }
  }

  emit('saveAfter', {
    paramItem: localParamItem.value,
    operation: props.operation,
  })

  handleClose()
}

function handleClose() {
  emit('update:visible', false)

  setTimeout(() => {
    name.value = ''
    value.value = ''
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
    :title="title"
    width="500px"
    :visible="visible"
    :z-index="20010"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form ref="form" :label-width="80">
        <u-form-item :label="t('dialog.paramItem.name')">
          <u-input
            ref="nameInput"
            v-model="name"
            style="width: 350px;"
            @keyup.enter="handleOk"
          />
        </u-form-item>

        <u-form-item :label="t('dialog.paramItem.expr')">
          <u-input
            ref="valueInput"
            v-model="value"
            style="width: 350px;"
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
