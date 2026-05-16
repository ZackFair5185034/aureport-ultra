<template>
  <UDialog
    top="20px"
    :title="$t('dialog.searchForm.title')"
    width="1200px"
    :visible="visible"
    @close="handleClose"
  >
    <div class="search-form-dialog-content">
      <search-form :searchFormConfig="searchFormConfig" ref="searchFormDesigner"></search-form>
    </div>
    <template #footer><div style="text-align: right">
      <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ $t('dialog.common.cancel') }}</u-button>
      <u-button @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
    </div></template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import SearchForm from "@/views/report/designer/search-form/index.vue"
import { deepClone } from "@/views/report/designer/search-form/utils"
import { updateReportDef } from '@/utils/contextActions'

defineOptions({ name: 'SearchFormDialog' })

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const props = withDefaults(defineProps<{
  visible?: boolean
}>(), {
  visible: false
})

const { t } = useI18n()
const store = useReportStore()

const searchFormDesigner = ref<any>(null)
const searchFormConfig = ref<any>(null)

const context = computed(() => store.context)

watch(() => props.visible, (newVal) => {
  if (newVal && context.value && context.value.reportDef && context.value.reportDef.searchForm) {
    searchFormConfig.value = deepClone(context.value.reportDef.searchForm)
  }
})

function buildData() {
  if (!searchFormDesigner.value) return
  searchFormDesigner.value.AssembleFormData()
  const formData = searchFormDesigner.value.formData
  const newReportDef = deepClone(context.value!.reportDef)
  newReportDef.searchForm = deepClone(formData)
  updateReportDef(newReportDef)
}

function handleClose() {
  emit('update:visible', false)
}

function handleOk() {
  buildData()
  emit('update:visible', false)
}
</script>

<style scoped>
.search-form-dialog-content {
  height: 600px;
  padding: 0;
}
</style>
