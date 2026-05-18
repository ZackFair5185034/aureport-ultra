<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'

defineOptions({ name: 'SpringDialog' })

const props = withDefaults(defineProps<{
  datasources?: any[]
  visible?: boolean
  datasource?: any
}>(), {
  datasources: () => [],
  visible: false,
  datasource: null,
})

const emit = defineEmits<{
  (e: 'save', data: any): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const dsName = ref('')
const beanId = ref('')
const oldName = ref<string | null>(null)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm()
    if (props.datasource) {
      fillForm(props.datasource)
    }
  }
})

function resetForm() {
  dsName.value = ''
  beanId.value = ''
  oldName.value = null
}

function fillForm(ds: any) {
  if (ds) {
    oldName.value = ds.name
    dsName.value = ds.name
    beanId.value = ds.beanId
  }
}

function saveData() {
  if (dsName.value === '') {
    showAlert(t('dialog.springDS.nameTip'))
    return
  }

  if (beanId.value === '') {
    showAlert(t('dialog.springDS.beanTip'))
    return
  }

  let check = false
  if (!oldName.value || dsName.value !== oldName.value) {
    check = true
  }

  if (check) {
    for (const source of props.datasources) {
      if (source.name === dsName.value) {
        showAlert(`${t('dialog.springDS.ds')}[${dsName.value}]${t('dialog.springDS.exist')}`)
        return
      }
    }
  }

  emit('save', {
    name: dsName.value,
    beanId: beanId.value,
    type: 'spring',
    datasets: [],
    oldName: oldName.value,
  })
  closeDialog()
  setDirty()
}

function closeDialog() {
  emit('close')
}
</script>

<template>
  <UDialog
    :title="$t('dialog.springDS.title')"
    width="500px"
    :visible="visible"
    :z-index="20000"
    @close="closeDialog"
  >
    <u-form>
      <u-form-item :label="$t('dialog.springDS.name')" :label-width="120">
        <u-input v-model="dsName" />
      </u-form-item>
      <u-form-item :label="$t('dialog.springDS.bean')" :label-width="120">
        <u-input v-model="beanId" />
      </u-form-item>
    </u-form>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="closeDialog">{{ $t('dialog.common.cancel') }}</u-button>
        <u-button @click="saveData">{{ $t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
