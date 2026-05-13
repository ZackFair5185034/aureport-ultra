<template>
  <u-button
      :title="$t('tools.preview.view')"
      class="tool-button"
      icon="icon-preview"
      @click="handleClick"
  >
  </u-button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { useRouter, useRoute } from 'vue-router'
import { tableToXml } from '@/utils/table.js'
import { showAlert } from '@/utils/comnon.js'
import { savePreviewFile } from '@/api/designer/index.js'
import { createNavigator, getLibMode } from '@/lib/navigator'

defineOptions({ name: 'PreviewTool' })

const { t } = useI18n()
const store = useReportStore()
const router = useRouter()
const route = useRoute()

const context = computed(() => store.context)
const isLibMode = computed(() => getLibMode())
const navigator = computed(() => createNavigator({ $router: router, $route: route }))

function handleClick() {
  const content = tableToXml(context.value)
  let fileName = store.fileName
  if (fileName) {
    fileName = fileName + ".ureport.xml"
  } else {
    fileName = 'p'
  }

  savePreviewFile(fileName, content)
    .then(() => {
      navigator.value.openPreview({
        reportPath: fileName,
        mode: 'preview'
      }, true)
    })
    .catch((error: any) => {
      console.error('预览失败:', error)
      if (error.msg) {
        showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
      } else {
        showAlert(t('tools.preview.previewFail'))
      }
    })
}
</script>

<style scoped>

</style>
