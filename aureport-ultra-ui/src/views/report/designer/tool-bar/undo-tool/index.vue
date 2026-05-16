<template>
  <u-button
      :title="$t('tools.undo.undo')"
      class="tool-button"
      icon="icon-undo"
      @click="handleClick"
  >
  </u-button>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { undoManager } from '@/utils/table'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'UndoTool' })

const { t } = useI18n()

function handleClick() {
  if (undoManager.hasUndo()) {
    undoManager.undo()
  } else {
    showAlert(t('tools.undo.noUndo'))
  }
}
</script>

<style scoped>
</style>
