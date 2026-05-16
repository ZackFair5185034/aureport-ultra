<template>
  <UDialog
    :title="t('dialog.urlParam.title')"
    width="660px"
    :visible="visible"
    :z-index="20000"
    @close="handleClose"
  >
    <div class="dialog-content">
       <div class="top-button">
           <u-button
             type="info"
             @click="handleAdd"
             :title="t('dialog.urlParam.add')"
             icon="icon-plus-circle"
           />
       </div>
      <table class="data-table" style="margin-top: 5px">
        <thead>
          <tr style="background-color: #eeeeee; height: 30px;">
            <td style="width: 150px;"><span>{{ t('dialog.urlParam.name') }}</span></td>
            <td style="width: 350px;"><span>{{ t('dialog.urlParam.expr') }}</span></td>
            <td style="width: 100px;"><span>{{ t('dialog.urlParam.op') }}</span></td>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(param, index) in displayParameters" :key="index" style="height: 30px;">
           <td><span>{{ param.name }}</span></td>
           <td><span>{{ param.value }}</span></td>
           <td>
              <u-button
                  type="info"
                  icon="icon-edit"
                  :title="t('dialog.urlParam.edit')"
                  @click="handleEdit(param)"
                  style="border: none">
              </u-button>
              <u-button
                  type="info"
                  icon="icon-delete"
                  :title="t('dialog.urlParam.delete')"
                  @click="handleDelete(param, index)"
                  style="border: none;color: red">
              </u-button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- URL参数项对话框 -->
    <URLParameterItemDialog
      :visible="itemDialogVisible"
      :param-item="currentParamItem"
      :operation="currentOperation"
      @update:visible="val => itemDialogVisible = val"
      @saveAfter="handleSaveAfter"
    />

  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { showConfirm } from '@/utils/comnon'
import URLParameterItemDialog from '@/views/report/designer/resource-panel/property-panel/url-parameter-dialog/url-parameter-item-dialog/index.vue'

defineOptions({ name: 'URLParameterDialog' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  visible?: boolean
  parameters?: any[]
}>(), {
  visible: false,
  parameters: () => []
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saveAfter', value: { paramItem: any, operation: string }): void
  (e: 'parameters-change', value: any[]): void
}>()

const itemDialogVisible = ref(false)
const currentParamItem = ref<any>(null)
const currentOperation = ref('add')

const displayParameters = computed(() => props.parameters || [])

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleAdd() {
  currentParamItem.value = { name: '', value: '' }
  currentOperation.value = 'add'
  itemDialogVisible.value = true
}

function handleEdit(param: any) {
  currentParamItem.value = param
  currentOperation.value = 'edit'
  itemDialogVisible.value = true
}

function handleSaveAfter({ paramItem, operation }: { paramItem: any, operation: string }) {
  if (operation === 'add') {
    emit('parameters-change', [...props.parameters, paramItem])
  } else if (operation === 'edit' && currentParamItem.value) {
    currentParamItem.value.name = paramItem.name
    currentParamItem.value.value = paramItem.value
    emit('parameters-change', [...props.parameters])
  }
  emit('saveAfter', { paramItem, operation })
}

function handleDelete(param: any, index: number) {
  showConfirm(t('dialog.urlParam.delTip')).then(() => {
    const newParameters = [...props.parameters]
    newParameters.splice(index, 1)
    emit('parameters-change', newParameters)
  })
}

function handleClose() {
  emit('update:visible', false)
}

function handleKeydown(e: KeyboardEvent) {
  if (props.visible) {
    if (e.key === 'Escape') {
      handleClose()
    }
  }
}
</script>

<style scoped>
.top-button{
  display: flex;
  justify-content: end;
}
</style>
