<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { testConnection } from '@/api/designer'
import { showAlert } from '@/utils/comnon'
import { setDirty } from '@/utils/table'

defineOptions({ name: 'DatasourceDialog' })

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

const form = ref<any>(null)
const oldName = ref<string | null>(null)
const formData = reactive({
  dsName: '',
  username: '',
  password: '',
  driver: '',
  url: '',
})

function validateDsName(rule: any, value: string, callback: (error?: Error) => void) {
  if (!value) {
    callback(new Error(t('dialog.datasource.nameTip')))
  }
  else if (checkDuplicateName(value)) {
    callback()
  }
  else {
    callback(new Error(`${t('dialog.datasource.datasource')}[${value}]${t('dialog.datasource.existTip')}`))
  }
}

const rules = reactive({
  dsName: [{
    required: true,
    validator: validateDsName,
    trigger: 'blur',
  }],
  username: [{
    required: true,
    message: t('dialog.datasource.usernameTip'),
    trigger: 'blur',
  }],
  password: [{
    required: true,
    message: t('dialog.datasource.passwordTip'),
    trigger: 'blur',
  }],
  driver: [{
    required: true,
    message: t('dialog.datasource.driverTip'),
    trigger: 'blur',
  }],
  url: [{
    required: true,
    message: t('dialog.datasource.urlTip'),
    trigger: 'blur',
  }],
})

watch(() => props.visible, (newVal) => {
  if (newVal) {
    if (props.datasource) {
      fillForm(props.datasource)
    }

    resetForm()
  }
})

function resetForm() {
  form.value && form.value.resetFields()
  oldName.value = null
}

function fillForm(ds: any) {
  if (ds) {
    oldName.value = ds.name
    formData.dsName = ds.name
    formData.username = ds.username || ''
    formData.password = ds.password || ''
    formData.driver = ds.driver || ''
    formData.url = ds.url || ''
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

function validateForm(): Promise<boolean> {
  return new Promise((resolve) => {
    form.value.validate((valid: boolean) => {
      resolve(valid)
    })
  })
}

function checkDuplicateName(name: string) {
  if (!oldName.value || name !== oldName.value) {
    for (const source of props.datasources) {
      if (source.name === name) {
        return false
      }
    }
  }

  return true
}

async function doTestConnection(showSuccessTips: boolean): Promise<boolean> {
  const valid = await validateForm()
  if (!valid) {
    return false
  }

  const fd = new FormData()
  fd.append('username', formData.username)
  fd.append('password', formData.password)
  fd.append('driver', formData.driver)
  fd.append('url', formData.url)

  try {
    const _data = await testConnection(fd)
    if (_data.result && showSuccessTips) {
      showAlert(t('dialog.datasource.testSuccess'))
    }

    return true
  }
  catch (error: any) {
    console.error('Error testing connection:', error)
    if (error.msg) {
      showAlert(t('dialog.datasource.failTip') + t('colon') + error.msg, { useHTMLString: true })
    }
    else {
      showAlert(t('dialog.datasource.failTip'))
    }
  }

  return false
}

async function save() {
  const valid = await validateForm()
  if (!valid) {
    return
  }

  const success = await doTestConnection(false)
  if (success) {
    emit('save', {
      name: formData.dsName,
      username: formData.username,
      password: formData.password,
      driver: formData.driver,
      url: formData.url,
      oldName: oldName.value,
      type: 'jdbc',
    })
    setDirty()
    closeDialog()
  }
}
</script>

<template>
  <UDialog
    :title="$t('dialog.datasource.title')"
    width="800px"
    :visible="visible"
    @close="closeDialog"
  >
    <div class="dialog-content">
      <u-form ref="form" :model="formData" :rules="rules" :label-width="120">
        <u-form-item :label="$t('dialog.datasource.name')" prop="dsName">
          <u-input v-model="formData.dsName" style="width: 600px" />
        </u-form-item>

        <u-form-item :label="$t('dialog.datasource.username')" prop="username">
          <u-input v-model="formData.username" style="width: 600px" />
        </u-form-item>

        <u-form-item :label="$t('dialog.datasource.password')" prop="password">
          <u-input v-model="formData.password" type="password" style="width: 600px" />
        </u-form-item>

        <u-form-item :label="$t('dialog.datasource.driver')" prop="driver">
          <u-input v-model="formData.driver" style="width: 600px" />
        </u-form-item>

        <u-form-item :label="$t('dialog.datasource.url')" prop="url">
          <u-input v-model="formData.url" style="width: 600px" />
        </u-form-item>
      </u-form>
    </div>

    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click="doTestConnection(true)">{{ $t('dialog.datasource.test') }}</u-button>
        <u-button @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
