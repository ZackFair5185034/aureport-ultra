<template>
  <u-button
      :title="$t('tools.save.save')"
      class="tool-button"
      icon="icon-save2"
      @click="handleClick"
  >
    <SaveDialog
      :visible="visible"
      @update:visible="visible = $event"
      @saveAfter="handleSaveAfter"
    />
  </u-button>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { showAlert } from '@/utils/comnon'
import { resetDirty, tableToXml } from '@/utils/table'
import { saveReportFile } from '@/api/designer/index'

defineOptions({ name: 'SaveTool' })

const { t } = useI18n()
const store = useReportStore()
const visible = ref(false)

const getSaveStatus = computed(() => store.saveStatus)
const getFileName = computed(() => store.fileName)
const context = computed(() => store.context)

function handleClick() {
  if (!getSaveStatus.value) {
    visible.value = true
    return
  }

  const content = tableToXml(context.value)
  const fullFileName = getFileName.value + ".ureport.xml"

  saveReportFile(fullFileName, content)
    .then(() => {
      showAlert(t('tools.save.successSave'))
      resetDirty()
    })
    .catch((error: any) => {
      console.error('保存失败:', error)
      if (error.msg) {
        showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
      } else {
        showAlert(t('tools.save.failSave'))
      }
    })
}

function handleSaveAfter(fullFile: string) {
  window.location.replace("?reportPath=" + fullFile)
}
</script>

<style scoped>
</style>
