<template>
  <UDialog
      :title="$t('dialog.methodSelect.title')"
      width="600px"
      :visible="visible"
      @close="closeDialog"
      :loading="loading"
  >
    <div v-if="loading" style="padding: 20px; text-align: center;">
      {{ $t('dialog.methodSelect.load') }}
    </div>
    <div v-else>
      <table class="data-table">
        <thead>
        <tr style="background: #f4f4f4; height: 30px;">
          <td><span>{{ $t('dialog.methodSelect.methodName') }}</span></td>
          <td><span>{{ $t('dialog.methodSelect.select') }}</span></td>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(methodItem, index) in methods" :key="index" style="height: 35px;">
          <td><span>{{ methodItem }}</span></td>
          <td>
            <u-button type="text" icon="icon-hand-up" @click="selectMethod(methodItem)"></u-button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <template #footer><div style="text-align: right">
      <u-button type="info" @click="handleClose" style="margin-right: 10px;">{{ $t('dialog.common.cancel') }}</u-button>
    </div></template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { loadMethods } from '@/api/designer/index.js'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'MethodSelectDialog' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  visible: boolean
  beanId: string
}>(), {
  visible: false,
  beanId: ''
})

const emit = defineEmits<{
  (e: 'save', method: string): void
  (e: 'close'): void
}>()

const loading = ref(false)
const methods = ref<string[]>([])

watch(() => props.visible, (newVal) => {
  if (newVal && props.beanId) {
    methods.value = []
    loadMethodsData()
  }
})

function closeDialog() {
  emit('close')
}

function handleClose() {
  closeDialog()
}

async function loadMethodsData() {
  loading.value = true
  try {
    methods.value = await loadMethods(props.beanId) as any
    loading.value = false
  } catch (error: any) {
    loading.value = false
    if (error.msg) {
      showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
    } else {
      showAlert(`加载方法[${props.beanId}]失败`)
    }
  }
}

function selectMethod(methodItem: string) {
  emit('save', methodItem)
  emit('close')
}
</script>

<style scoped>
</style>
