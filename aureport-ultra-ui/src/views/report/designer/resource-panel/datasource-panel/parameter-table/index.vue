<template>
  <div class="parameter-table-container">
    <div class="header-row">
      <div class="text-section">
        {{ $t('dialog.sql.fiterParam') }}
        <span class="text-info">{{ $t('dialog.sql.paramDesc') }}</span>
      </div>
      <u-button
          icon="icon-plus-circle"
          @click="addParameter"
      >
        {{ $t('dialog.paramTable.addParam') }}
      </u-button>
    </div>
    <table class="data-table" style="margin-top: 5px">
      <thead>
        <tr style="background: #f4f4f4;height: 30px;">
          <td><span>{{ $t('dialog.paramTable.paramName') }}</span></td>
          <td><span>{{ $t('dialog.paramTable.paramDatatype') }}</span></td>
          <td><span>{{ $t('dialog.paramTable.defaultValue') }}</span></td>
          <td style="width: 80px;"><span>{{ $t('dialog.paramTable.operator') }}</span></td>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(param, index) in data"
          :key="index"
          style="height: 35px;"
        >
          <td><span>{{ param.name }}</span></td>
          <td><span>{{ param.type }}</span></td>
          <td><span>{{ param.defaultValue }}</span></td>
          <td>
            <u-button
                type="info"
                icon="icon-edit"
                :title="$t('dialog.paramTable.editParam')"
                @click.prevent="editParameter(param, index)"
                style="border: none">
            </u-button>
            <u-button
                type="info"
                icon="icon-delete"
                :title="$t('dialog.paramTable.delParam')"
                @click.prevent="removeParameter(param, index)"
                style="border: none;color: red">
            </u-button>
          </td>
        </tr>
      </tbody>
    </table>
    <ParameterDialog
      :visible="parameterDialogVisible"
      :edit-data="currentEditData"
      @update:visible="parameterDialogVisible = $event"
      @save="handleDialogSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ParameterDialog from '../parameter-dialog/index.vue'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'ParameterTable' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  data: any[]
}>(), {
  data: () => []
})

const emit = defineEmits<{
  (e: 'add-parameter', param: any): void
  (e: 'edit-parameter', index: number, param: any): void
  (e: 'remove-parameter', index: number): void
  (e: 'update'): void
}>()

const currentEditData = ref<any>(null)
const currentIndex = ref(-1)
const parameterDialogVisible = ref(false)

watch(() => props.data, () => {
  // data changes auto-react
}, { deep: true })

function refreshData() {
  emit('update')
}

function handleDialogSave(name: string, type: string, defaultValue: string) {
  if ((currentIndex.value === -1 || props.data[currentIndex.value].name !== name) &&
      props.data.some((param: any) => param.name === name)) {
    showAlert(`参数[${name}]已存在`)
    return
  }

  if (currentIndex.value === -1) {
    const newParam = { name, type, defaultValue }
    emit('add-parameter', newParam)
  } else {
    emit('edit-parameter', currentIndex.value, { name, type, defaultValue })
  }
  emit('update')
}

function addParameter() {
  currentIndex.value = -1
  currentEditData.value = null
  parameterDialogVisible.value = true
}

function editParameter(param: any, index: number) {
  currentIndex.value = index
  currentEditData.value = param
  parameterDialogVisible.value = true
}

function removeParameter(param: any, index: number) {
  emit('remove-parameter', index)
  emit('update')
}
</script>

<style scoped>

.header-row{
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-section{
  flex: 1;
}
</style>
