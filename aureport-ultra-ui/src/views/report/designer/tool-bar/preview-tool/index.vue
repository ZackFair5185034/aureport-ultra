<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { savePreviewFile } from '@/api/designer/index'
import { createNavigator, getLibMode } from '@/lib/navigator'
import { useReportStore } from '@/stores/report'
import { showAlert } from '@/utils/comnon'
import { tableToXml } from '@/utils/table'

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
  fileName = fileName ? `${fileName}.ureport.xml` : 'p'

  savePreviewFile(fileName, content)
    .then(() => {
      navigator.value.openPreview({
        reportPath: fileName,
        mode: 'preview',
      }, true)
    })
    .catch((error: any) => {
      console.error('预览失败:', error)
      if (error.msg) {
        showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
      }
      else {
        showAlert(t('tools.preview.previewFail'))
      }
    })
}
</script>

<template>
  <u-button
    :title="$t('tools.preview.view')"
    class="tool-button"
    icon="icon-preview"
    @click="handleClick"
  />
</template>

<style scoped>

</style>
