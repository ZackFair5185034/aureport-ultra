<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'
import type { HttpParameter } from '@/types'

defineOptions({ name: 'HttpDatasetDialog' })

const props = withDefaults(defineProps<{
  datasources?: any[]
  protocolType?: string
  visible?: boolean
  dataset?: any
}>(), {
  datasources: () => [],
  protocolType: 'standard',
  visible: false,
  dataset: null,
})

const emit = defineEmits<{
  (e: 'save', name: string, data: any, oldName: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const oldName = ref('')
const name = ref('')
const url = ref('')
const method = ref('GET')
const body = ref('')
const responsePath = ref('')
const requestParameters = ref<HttpParameter[]>([])

watch(() => props.visible, (newVal) => {
  if (newVal) {
    initData()
  }
})

watch(() => props.dataset, (newVal) => {
  if (newVal) {
    initData()
  }
})

function initData() {
  name.value = ''
  url.value = ''
  method.value = 'GET'
  body.value = ''
  responsePath.value = ''
  requestParameters.value = []
  oldName.value = ''

  if (props.dataset) {
    oldName.value = props.dataset.name
    name.value = props.dataset.name
    url.value = props.dataset.url || ''
    method.value = props.dataset.method || 'GET'
    body.value = props.dataset.body || ''
    responsePath.value = props.dataset.responsePath || ''
    requestParameters.value = props.dataset.requestParameters || []
  }
}

function addRequestParameter() {
  requestParameters.value.push({ name: '', value: '' })
}

function removeRequestParameter(index: number) {
  requestParameters.value.splice(index, 1)
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

  const data: any = { name: name.value }

  if (props.protocolType === 'thirdParty') {
    if (!url.value) {
      showAlert(t('dialog.httpDS.urlTip'))
      return
    }
    data.url = url.value
    data.method = method.value
    data.body = body.value || ''
    data.responsePath = responsePath.value || ''
    data.requestParameters = requestParameters.value
  }

  emit('save', name.value, data, oldName.value)
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
    :width="protocolType === 'standard' ? '450px' : '650px'"
    :visible="visible"
    @close="closeDialog"
  >
    <div class="dialog-content">
      <u-form :label-width="120">
        <u-form-item :label="$t('dialog.httpDS.datasetName')">
          <u-input v-model="name" style="width: 280px" />
        </u-form-item>

        <!-- 标准协议：仅需数据集名称 -->
        <template v-if="protocolType === 'standard'">
          <u-form-item :label-width="120">
            <div class="protocol-desc">{{ $t('dialog.httpDS.standardDatasetDesc') }}</div>
          </u-form-item>
        </template>

        <!-- 三方接口：完整配置 -->
        <template v-if="protocolType === 'thirdParty'">
          <u-form-item :label="$t('dialog.httpDS.url')">
            <u-input v-model="url" :placeholder="$t('dialog.httpDS.urlParamTip')" style="width: 480px" />
          </u-form-item>

          <u-form-item :label="$t('dialog.httpDS.method')">
            <u-select v-model="method" style="width: 150px">
              <u-option value="GET" label="GET" />
              <u-option value="POST" label="POST" />
              <u-option value="PUT" label="PUT" />
              <u-option value="DELETE" label="DELETE" />
            </u-select>
          </u-form-item>

          <u-form-item :label="$t('dialog.httpDS.requestParams')">
            <div class="params-editor">
              <div v-for="(param, idx) in requestParameters" :key="idx" class="param-row">
                <u-input v-model="param.name" :placeholder="$t('dialog.httpDS.paramName')" style="width: 180px" />
                <u-input v-model="param.value" :placeholder="$t('dialog.httpDS.paramValue')" style="width: 220px; margin-left: 6px;" />
                <u-button type="text" icon="icon-delete" size="small" @click="removeRequestParameter(idx)" />
              </div>
              <u-button type="primary" size="small" class="add-param-btn" @click="addRequestParameter">{{ $t('dialog.httpDS.addParam') }}</u-button>
            </div>
          </u-form-item>

          <u-form-item v-if="method === 'POST' || method === 'PUT'" :label="$t('dialog.httpDS.body')">
            <u-input v-model="body" type="textarea" :rows="4" :placeholder="$t('dialog.httpDS.bodyPlaceholder')" style="width: 480px" />
          </u-form-item>

          <u-form-item :label="$t('dialog.httpDS.responsePath')">
            <u-input v-model="responsePath" :placeholder="$t('dialog.httpDS.responsePathPlaceholder')" style="width: 400px" />
          </u-form-item>
        </template>
      </u-form>
    </div>

    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="closeDialog">{{ $t('dialog.common.cancel') }}</u-button>
        <u-button type="primary" @click="save">{{ $t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
.dialog-content {
  padding: 10px 20px;
}
.protocol-desc {
  font-size: 12px;
  color: #999;
  line-height: 1.6;
  background: #f9f9f9;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #eee;
  max-width: 380px;
}
.protocol-desc p { margin: 0; }
.params-editor { width: 480px; }
.param-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.add-param-btn {
  margin-top: 4px;
}
</style>
