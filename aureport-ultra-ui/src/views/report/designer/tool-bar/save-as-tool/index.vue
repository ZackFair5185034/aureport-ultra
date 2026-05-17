<script setup lang="ts">
import { computed, ref } from 'vue'
import { useReportStore } from '@/stores/report'

defineOptions({ name: 'SaveAsTool' })

const store = useReportStore()
const visible = ref(false)

const context = computed(() => store.context)

function handleSaveAfter(fullFile: string) {
  window.location.replace(`?reportPath=${fullFile}`)
}
</script>

<template>
  <u-button
    :title="$t('tools.save.saveAs')"
    class="tool-button"
    icon="icon-save-as"
    @click="visible = true"
  >
    <SaveDialog
      :visible="visible"
      @update:visible="visible = $event"
      @saveAfter="handleSaveAfter"
    />
  </u-button>
</template>

<style scoped>
</style>
