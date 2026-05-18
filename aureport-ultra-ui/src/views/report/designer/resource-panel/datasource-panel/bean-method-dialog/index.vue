<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'
import MethodSelectDialog from '@/views/report/designer/resource-panel/datasource-panel/method-select-dialog/index.vue'

defineOptions({ name: 'BeanMethodDialog' })

const props = withDefaults(defineProps<{
  datasources?: any[]
  beanId?: string
  visible?: boolean
  dataset?: any
}>(), {
  datasources: () => [],
  beanId: '',
  visible: false,
  dataset: null,
})

const emit = defineEmits<{
  (e: 'save', name: string, method: string, clazz: string, oldName: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const oldName = ref('')
const name = ref('')
const method = ref('')
const clazz = ref('')
const methodSelectDialogVisible = ref(false)

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
  method.value = ''
  clazz.value = ''
  oldName.value = ''

  if (props.dataset) {
    oldName.value = props.dataset.name
    name.value = props.dataset.name
    method.value = props.dataset.method
    clazz.value = props.dataset.clazz
  }
}

function closeDialog() {
  emit('close')
}

function handleClose() {
  closeDialog()
}

function handleOk() {
  save()
}

function selectMethod(event?: Event) {
  if (event) {
    event.preventDefault()
  }

  methodSelectDialogVisible.value = true
}

function handleMethodSelect(selectedMethod: string) {
  method.value = selectedMethod
}

function validateName(): boolean {
  let check = false
  if (!oldName.value || name.value !== oldName.value) {
    check = true
  }

  if (check) {
    for (const datasource of props.datasources) {
      const datasets = datasource.datasets
      if (!datasets || !Array.isArray(datasets)) {
        continue
      }

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
  if (!validateName()) {
    return
  }

  emit('save', name.value, method.value, clazz.value, oldName.value)
  setDirty()
  closeDialog()
}
</script>

<template>
  <div>
    <!-- Bean方法配置对话框 -->
    <UDialog
      :title="$t('dialog.bean.beanDatasetConfig')"
      width="600px"
      :visible="visible"
      @close="closeDialog"
    >
      <div class="dialog-content">
        <u-form :label-width="120">
          <u-form-item :label="$t('dialog.bean.datasetName')">
            <u-input v-model="name" style="width: 400px" />
          </u-form-item>

          <u-form-item :label="$t('dialog.bean.methodName')">
            <div class="input-group">
              <u-input v-model="method" :placeholder="$t('dialog.bean.methodParameters')" style="width: 300px" />
              <span class="input-group-btn">
                <u-button type="primary" @click.prevent="selectMethod">{{ $t('dialog.bean.selectMethod') }}</u-button>
              </span>
            </div>
          </u-form-item>

          <u-form-item :label="$t('dialog.bean.returnObject')">
            <u-input v-model="clazz" :placeholder="$t('dialog.bean.className')" style="width: 400px" />
          </u-form-item>
        </u-form>
      </div>

      <template #footer>
        <div style="text-align: right">
          <u-button type="info" style="margin-right: 10px;" @click="handleClose">{{ $t('dialog.common.cancel') }}</u-button>
          <u-button type="primary" @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
        </div>
      </template>
    </UDialog>

    <!-- 方法选择对话框 -->
    <MethodSelectDialog
      :visible="methodSelectDialogVisible"
      :beanId="beanId"
      @save="handleMethodSelect"
      @close="methodSelectDialogVisible = false"
    />
  </div>
</template>

<style scoped>
.dialog-content {
  padding: 20px;
}

.input-group {
  display: flex;
  align-items: center;
}

.input-group-btn {
  margin-left: 10px;
}

.btn {
  padding: 6px 12px;
  font-size: 14px;
  border: 1px solid #ccc;
  background-color: #f5f5f5;
  cursor: pointer;
}

.btn:hover {
  background-color: #e6e6e6;
}
</style>
