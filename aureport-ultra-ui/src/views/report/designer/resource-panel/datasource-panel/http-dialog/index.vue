<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'
import { listServices } from '@/api/designer'

defineOptions({ name: 'HttpDialog' })

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
const protocolType = ref('standard')
const hostType = ref('manual')
const host = ref('')
const serviceName = ref('')
const baseUrl = ref('')
const oldName = ref<string | null>(null)
const services = ref<string[]>([])
const headerList = ref<{ key: string, value: string }[]>([])

watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm()
    if (props.datasource) {
      fillForm(props.datasource)
    }
    // 对话框打开时如果已经是 discovery 模式，立即加载服务列表
    if (hostType.value === 'discovery') {
      loadServices()
    }
  }
})

watch(hostType, (val) => {
  if (val === 'discovery') {
    loadServices()
  }
})

async function loadServices() {
  try {
    services.value = await listServices()
  } catch {
    services.value = []
  }
}

function parseHeaders(json: string): { key: string, value: string }[] {
  if (!json) return []
  try {
    const obj = JSON.parse(json)
    return Object.entries(obj).map(([key, value]) => ({ key, value: String(value) }))
  } catch {
    return []
  }
}

function serializeHeaders(list: { key: string, value: string }[]): string {
  const obj: Record<string, string> = {}
  for (const item of list) {
    if (item.key) {
      obj[item.key] = item.value
    }
  }
  return Object.keys(obj).length > 0 ? JSON.stringify(obj) : ''
}

function addHeader() {
  headerList.value.push({ key: '', value: '' })
}

function removeHeader(index: number) {
  headerList.value.splice(index, 1)
}

function resetForm() {
  dsName.value = ''
  protocolType.value = 'standard'
  hostType.value = 'manual'
  host.value = ''
  serviceName.value = ''
  baseUrl.value = ''
  headerList.value = []
  oldName.value = null
}

function fillForm(ds: any) {
  if (ds) {
    oldName.value = ds.name
    dsName.value = ds.name
    protocolType.value = ds.protocolType || 'standard'
    hostType.value = ds.hostType || 'manual'
    host.value = ds.host || ''
    serviceName.value = ds.serviceName || ''
    baseUrl.value = ds.baseUrl || ''
    headerList.value = parseHeaders(ds.headers || '')
  }
}

function saveData() {
  if (dsName.value === '') {
    showAlert(t('dialog.httpDS.nameTip'))
    return
  }

  if (protocolType.value === 'standard') {
    if (hostType.value === 'manual' && !host.value) {
      showAlert(t('dialog.httpDS.hostTip'))
      return
    }
    if (hostType.value === 'discovery' && !serviceName.value) {
      showAlert(t('dialog.httpDS.serviceNameTip'))
      return
    }
  } else if (protocolType.value === 'thirdParty' && !baseUrl.value) {
    showAlert(t('dialog.httpDS.urlTip'))
    return
  }

  let check = false
  if (!oldName.value || dsName.value !== oldName.value) {
    check = true
  }

  if (check) {
    for (const source of props.datasources) {
      if (source.name === dsName.value) {
        showAlert(`${t('dialog.httpDS.ds')}[${dsName.value}]${t('dialog.httpDS.exist')}`)
        return
      }
    }
  }

  emit('save', {
    name: dsName.value,
    protocolType: protocolType.value,
    hostType: hostType.value,
    host: host.value,
    serviceName: serviceName.value,
    baseUrl: baseUrl.value,
    headers: serializeHeaders(headerList.value),
    type: 'http',
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
    :title="$t('dialog.httpDS.title')"
    width="550px"
    :visible="visible"
    :z-index="20000"
    @close="closeDialog"
  >
    <u-form>
      <u-form-item :label="$t('dialog.httpDS.name')" :label-width="120" required>
        <u-input v-model="dsName" />
      </u-form-item>
      <u-form-item :label="$t('dialog.httpDS.protocol')" :label-width="120">
        <u-select v-model="protocolType" style="width: 280px">
          <u-option value="standard" :label="$t('dialog.httpDS.standardProtocol')" />
          <u-option value="thirdParty" :label="$t('dialog.httpDS.thirdPartyProtocol')" />
        </u-select>
      </u-form-item>

      <!-- 标准协议配置 -->
      <template v-if="protocolType === 'standard'">
        <u-form-item :label="$t('dialog.httpDS.hostType')" :label-width="120">
          <u-radio-group v-model="hostType" direction="horizontal">
            <u-radio :label="'manual'">{{ $t('dialog.httpDS.hostManual') }}</u-radio>
            <u-radio :label="'discovery'">{{ $t('dialog.httpDS.hostDiscovery') }}</u-radio>
          </u-radio-group>
        </u-form-item>

        <u-form-item v-if="hostType === 'manual'" :label="$t('dialog.httpDS.host')" :label-width="120" required>
          <u-input v-model="host" :placeholder="'http://192.168.1.100:8080'" />
        </u-form-item>

        <u-form-item v-if="hostType === 'discovery'" :label="$t('dialog.httpDS.serviceName')" :label-width="120" required>
          <u-select v-model="serviceName" style="width: 280px" :placeholder="$t('dialog.httpDS.serviceNamePlaceholder')">
            <u-option v-for="svc in services" :key="svc" :value="svc" :label="svc" />
          </u-select>
        </u-form-item>
      </template>

      <!-- 三方接口配置 -->
      <u-form-item v-if="protocolType === 'thirdParty'" :label="$t('dialog.httpDS.baseUrl')" :label-width="120" required>
        <u-input v-model="baseUrl" :placeholder="$t('dialog.httpDS.urlPlaceholder')" />
      </u-form-item>

      <u-form-item v-if="protocolType === 'thirdParty'" :label="$t('dialog.httpDS.headers')" :label-width="120">
        <div class="headers-editor">
          <div v-for="(header, idx) in headerList" :key="idx" class="header-row">
            <u-input v-model="header.key" :placeholder="$t('dialog.httpDS.paramName')" style="width: 160px" />
            <u-input v-model="header.value" :placeholder="$t('dialog.httpDS.paramValue')" style="width: 160px; margin-left: 6px;" />
            <u-button type="text" icon="icon-delete" size="small" @click="removeHeader(idx)" />
          </div>
          <u-button type="primary" size="small" class="add-header-btn" @click="addHeader">{{ $t('dialog.httpDS.addParam') }}</u-button>
        </div>
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
.headers-editor { width: 380px; }
.header-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.add-header-btn {
  margin-top: 4px;
}
</style>
