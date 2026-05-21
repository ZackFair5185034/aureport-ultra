<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'
import { httpStandardProxy, type ReportBeanInfo, type MethodInfo } from '@/api/designer/index'

defineOptions({ name: 'HttpStandardDatasetDialog' })

const props = withDefaults(defineProps<{
  datasources?: any[]
  visible?: boolean
  dataset?: any
  datasourceName?: string
  host?: string
  hostType?: string
  serviceName?: string
}>(), {
  datasources: () => [],
  visible: false,
  dataset: null,
  datasourceName: '',
  host: '',
  hostType: 'manual',
  serviceName: '',
})

const emit = defineEmits<{
  (e: 'save', name: string, beanMethod: string, clazz: string, oldName: string, beanId: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const displayMethod = computed(() => {
  if (method.value && clazz.value) {
    return `${method.value} : ${clazz.value}`
  }
  return method.value
})

const oldName = ref('')
const name = ref('')
const method = ref('')
const clazz = ref('')
const beanOptions = ref<ReportBeanInfo[]>([])
const selectedBeanId = ref('')
const methods = ref<MethodInfo[]>([])
const loadingBeans = ref(false)
const loadingMethods = ref(false)
const methodDialogVisible = ref(false)

watch(() => props.visible, (newVal) => {
  if (newVal) {
    initData()
    fetchBeans()
  }
})

watch(() => props.dataset, (newVal) => {
  if (newVal) {
    initData()
  }
})

watch(selectedBeanId, (newVal) => {
  if (newVal) {
    fetchMethods(newVal)
  } else {
    methods.value = []
  }
  method.value = ''
  clazz.value = ''
})

async function fetchBeans() {
  loadingBeans.value = true
  try {
    const result = await httpStandardProxy({
      datasourceName: props.datasourceName,
      endpoint: 'report-beans',
      host: props.host,
      hostType: props.hostType,
      serviceName: props.serviceName,
    })
    beanOptions.value = (result || []) as ReportBeanInfo[]
  } catch {
    beanOptions.value = []
  } finally {
    loadingBeans.value = false
  }
}

async function fetchMethods(beanId: string) {
  loadingMethods.value = true
  try {
    const result = await httpStandardProxy({
      datasourceName: props.datasourceName,
      endpoint: 'datasource/loadMethods',
      beanId,
      host: props.host,
      hostType: props.hostType,
      serviceName: props.serviceName,
    })
    methods.value = (result || []) as MethodInfo[]
  } catch {
    methods.value = []
  } finally {
    loadingMethods.value = false
  }
}

function openMethodDialog() {
  if (!selectedBeanId.value) {
    showAlert(t('dialog.springDS.beanTip'))
    return
  }
  methodDialogVisible.value = true
}

function initData() {
  name.value = ''
  method.value = ''
  clazz.value = ''
  oldName.value = ''
  selectedBeanId.value = ''
  methods.value = []

  if (props.dataset) {
    oldName.value = props.dataset.name
    name.value = props.dataset.name
    method.value = props.dataset.beanMethod || props.dataset.method || ''
    clazz.value = props.dataset.clazz || ''
    selectedBeanId.value = props.dataset.beanId || ''
  }
}

function selectMethod(methodItem: MethodInfo) {
  method.value = methodItem.method
  if (methodItem.returnClass) {
    clazz.value = methodItem.returnClass
  } else {
    clazz.value = ''
  }
  if (!name.value) {
    name.value = methodItem.method
  }
  methodDialogVisible.value = false
}

function validateName(): boolean {
  let check = false
  if (!oldName.value || name.value !== oldName.value) {
    check = true
  }
  if (check) {
    for (const datasource of props.datasources) {
      const datasets = datasource.datasets
      if (!datasets || !Array.isArray(datasets)) continue
      for (const dataset of datasets) {
        if (dataset.name === name.value) {
          showAlert(`${name.value} ${t('dialog.bean.datasetExist')}`)
          return false
        }
      }
    }
  }
  return true
}

function save() {
  if (!name.value) {
    showAlert(t('dialog.httpDS.nameTip'))
    return
  }
  if (!validateName()) return

  emit('save', name.value, method.value, clazz.value, oldName.value, selectedBeanId.value)
  setDirty()
  closeDialog()
}

function closeDialog() {
  emit('close')
}
</script>

<template>
  <UDialog
    :title="$t('dialog.httpDS.datasetConfig')"
    width="780px"
    :visible="visible"
    @close="closeDialog"
  >
    <div class="dialog-content">
      <u-form :label-width="120">
        <u-form-item :label="$t('dialog.bean.datasetName')">
          <u-input v-model="name" style="width: 600px" />
        </u-form-item>

        <u-form-item :label="$t('dialog.springDS.bean')">
          <u-select v-model="selectedBeanId" :clearable="true" style="width: 600px" :placeholder="$t('dialog.springDS.beanTip')" :loading="loadingBeans">
            <u-option
              v-for="option in beanOptions"
              :key="option.beanId"
              :value="option.beanId"
              :label="`${option.name}`"
            >
              <div class="bean-option">
                <span class="bean-option__name">{{ option.name }}</span>
                <span class="bean-option__class">{{ option.className }}</span>
              </div>
            </u-option>
          </u-select>
        </u-form-item>

        <u-form-item :label="$t('dialog.bean.methodName')">
          <div class="method-input-row">
            <u-input :model-value="displayMethod" readonly :placeholder="$t('dialog.bean.methodParameters')" style="width: 500px" />
            <u-button type="primary" @click="openMethodDialog">{{ $t('dialog.methodSelect.select') }}</u-button>
          </div>
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="closeDialog">{{ $t('dialog.common.cancel') }}</u-button>
        <u-button type="primary" @click="save">{{ $t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>

  <UDialog
    :title="$t('dialog.bean.methodName')"
    width="600px"
    :visible="methodDialogVisible"
    @close="methodDialogVisible = false"
  >
    <div class="method-dialog-content">
      <div v-if="loadingMethods" class="method-loading">{{ $t('dialog.methodSelect.load') }}</div>
      <div v-else-if="methods.length > 0" class="method-table-wrap">
        <table class="method-table">
          <thead>
            <tr>
              <td>{{ $t('dialog.methodSelect.methodName') }}</td>
              <td style="width: 40%;">{{ $t('dialog.methodSelect.returnClass') }}</td>
              <td style="width: 60px;">{{ $t('dialog.methodSelect.select') }}</td>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(methodItem, index) in methods" :key="index">
              <td>{{ methodItem.method }}</td>
              <td><span class="return-class">{{ methodItem.returnClass || '-' }}</span></td>
              <td>
                <u-button type="text" icon="icon-hand-up" @click="selectMethod(methodItem)" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="method-empty">{{ $t('dialog.bean.methodParameters') }}</div>
    </div>
  </UDialog>
</template>

<style scoped>
.dialog-content {
  padding: 20px;
}
.bean-option {
  display: flex;
  flex-direction: column;
  line-height: 1.5;
  padding: 2px 0;
}
.bean-option__name {
  font-weight: 500;
  font-size: 13px;
}
.bean-option__class {
  font-size: 11px;
  color: #999;
  word-break: break-all;
}
.method-table {
  width: 100%;
  border-collapse: collapse;
}
.method-table thead td {
  background: #f4f4f4;
  height: 30px;
  padding: 0 8px;
  font-size: 13px;
}
.method-table tbody td {
  padding: 4px 8px;
  font-size: 13px;
  border-bottom: 1px solid #f0f0f0;
}
.return-class {
  color: #999;
  font-size: 12px;
}
.method-loading, .method-empty {
  padding: 12px;
  text-align: center;
  color: #999;
}
.method-section {
  width: 100%;
}
.method-table-wrap {
  max-height: 280px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  margin-bottom: 8px;
}
.method-table-wrap .method-table thead td {
  position: sticky;
  top: 0;
  z-index: 1;
}
.selected-method {
  display: flex;
  align-items: center;
  gap: 8px;
}
.method-input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.method-dialog-content {
  padding: 16px 20px;
}
.return-class-tag {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 3px;
}
</style>
