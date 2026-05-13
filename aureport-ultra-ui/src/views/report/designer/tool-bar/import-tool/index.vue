<template>
  <u-button
      :title="$t('importExcel')"
      class="tool-button"
      icon="icon-cloud-upload"
      @click="visible = true"
  >
    <ImportDialog
      :visible="visible"
      @update:visible="visible = $event"
      @import-success="handleImportSuccess"
    />
  </u-button>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

defineOptions({ name: 'ImportTool' })

const router = useRouter()
const visible = ref(false)

function handleImportSuccess() {
  const routeData = router.resolve({
    name: 'Designer',
  })
  window.open(routeData.href, "_self")
}
</script>

<style scoped>
</style>
