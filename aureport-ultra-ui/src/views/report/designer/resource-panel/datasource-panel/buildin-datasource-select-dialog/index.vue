<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { loadBuildinDatasources } from '@/api/designer/index'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'

defineOptions({ name: 'BuildinDatasourceSelectDialog' })

const props = withDefaults(defineProps<{
  datasources?: any[]
  visible?: boolean
}>(), {
  datasources: () => [],
  visible: false,
})

const emit = defineEmits<{
  (e: 'select', data: { name: string, type: string }): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const loading = ref(false)
const buildinDatasources = ref<string[]>([])

watch(() => props.visible, (newVal) => {
  if (newVal) {
    buildinDatasources.value = []
    loading.value = true
    loadBuildinDatasourcesData()
  }
})

async function loadBuildinDatasourcesData() {
  try {
    buildinDatasources.value = await loadBuildinDatasources() as any
    loading.value = false
  }
  catch (error: any) {
    loading.value = false
    if (error.msg) {
      showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
    }
    else {
      showAlert(t('dialog.buildin.loadFail'))
    }
  }
}

function selectDatasource(name: string) {
  for (const ds of props.datasources) {
    if (ds.name === name) {
      showAlert(`${t('dialog.buildin.datasource')}[${name}]${t('dialog.buildin.datasourceExist')}`)
      return
    }
  }

  emit('select', { name, type: 'buildin' })
  setDirty()
  closeDialog()
}

function closeDialog() {
  emit('close')
}
</script>

<template>
  <UDialog
    :title="$t('dialog.buildin.selectDatasource')"
    width="600px"
    :visible="visible"
    @close="closeDialog"
  >
    <div v-if="loading" style="padding: 20px; text-align: center;">
      {{ $t('dialog.buildin.loading') }}
    </div>
    <table v-else class="data-table">
      <thead>
        <tr style="background: #f4f4f4;height: 30px;">
          <td>
            <span>{{ $t('dialog.buildin.datasourceName') }}</span>
          </td>
          <td>
            <span>{{ $t('dialog.buildin.select') }}</span>
          </td>
        </tr>
      </thead>
      <tbody>
        <tr v-for="name in buildinDatasources" :key="name" style="height: 35px;">
          <td>
            <span>{{ name }}</span>
          </td>
          <td>
            <u-button type="text" icon="icon-hand-up" @click="selectDatasource(name)" />
          </td>
        </tr>
      </tbody>
    </table>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="closeDialog">{{ $t('dialog.common.cancel') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
