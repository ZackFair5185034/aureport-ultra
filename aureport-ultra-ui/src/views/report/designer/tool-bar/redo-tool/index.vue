<template>
  <u-button
      :title="$t('tools.redo.redo')"
      class="tool-button"
      icon="icon-redo"
      @click="handleClick"
  >
  </u-button>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { undoManager } from '@/utils/table.js'
import { showAlert } from '@/utils/comnon.js'

defineOptions({ name: 'RedoTool' })

const { t } = useI18n()

function handleClick() {
  if (undoManager.hasRedo()) {
    undoManager.redo()
  } else {
    showAlert(t('tools.redo.noRedo'))
  }
}
</script>

<style scoped>
</style>
