<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { loadMethods, type MethodInfo } from '@/api/designer/index'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'MethodSelectDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  beanId?: string
}>(), {
  visible: false,
  beanId: '',
})

const emit = defineEmits<{
  (e: 'save', method: string, returnClass?: string | null): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const loading = ref(false)
const methods = ref<MethodInfo[]>([])

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
    methods.value = await loadMethods(props.beanId)
    loading.value = false
  }
  catch (error: any) {
    loading.value = false
    if (error.msg) {
      showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
    }
    else {
      showAlert(`加载方法[${props.beanId}]失败`)
    }
  }
}

function selectMethod(methodItem: MethodInfo) {
  emit('save', methodItem.method, methodItem.returnClass)
  emit('close')
}
</script>

<template>
  <UDialog
    :title="$t('dialog.methodSelect.title')"
    width="600px"
    :visible="visible"
    :loading="loading"
    @close="closeDialog"
  >
    <div v-if="loading" style="padding: 20px; text-align: center;">
      {{ $t('dialog.methodSelect.load') }}
    </div>
    <div v-else>
      <table class="data-table">
        <thead>
          <tr style="background: #f4f4f4; height: 30px;">
            <td><span>{{ $t('dialog.methodSelect.methodName') }}</span></td>
            <td style="width: 40%;"><span>{{ $t('dialog.methodSelect.returnClass') }}</span></td>
            <td style="width: 60px;"><span>{{ $t('dialog.methodSelect.select') }}</span></td>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(methodItem, index) in methods" :key="index" style="height: 35px;">
            <td><span>{{ methodItem.method }}</span></td>
            <td><span style="color: #999; font-size: 12px;">{{ methodItem.returnClass || '-' }}</span></td>
            <td>
              <u-button type="text" icon="icon-hand-up" @click="selectMethod(methodItem)" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click.stop="handleClose">{{ $t('dialog.common.cancel') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
