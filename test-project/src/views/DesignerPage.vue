<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 从 URL query 读取报表路径，如 /index?reportPath=file:test.ureport.xml
const reportPath = (route.query.reportPath as string) || 'classpath:template/template.ureport.xml'

function onWindowNavigate(e: Event) {
  const detail = (e as CustomEvent).detail
  if (detail.target === 'Preview' && detail.params?.reportPath) {
    router.push({
      path: '/preview',
      query: { reportPath: detail.params.reportPath },
    })
  }
}

function onSave(e: Event) {
  console.log('save:', (e as CustomEvent).detail)
}

function onError(e: Event) {
  console.error('error:', (e as CustomEvent).detail)
}

onMounted(() => {
  window.addEventListener('aureport-navigate', onWindowNavigate)
})

onBeforeUnmount(() => {
  window.removeEventListener('aureport-navigate', onWindowNavigate)
})
</script>

<template>
  <div class="designer-page">
    <aureport-designer
      :report-path="reportPath"
      locale="zh"
      @save="onSave"
      @error="onError"
    />
  </div>
</template>

<style scoped>
.designer-page {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>
