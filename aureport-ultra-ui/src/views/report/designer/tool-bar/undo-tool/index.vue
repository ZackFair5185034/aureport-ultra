<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { undoManager } from '@/utils/table'

defineOptions({ name: 'UndoTool' })

const { t } = useI18n()

function handleClick() {
  if (undoManager.hasUndo()) {
    undoManager.undo()
  }
  else {
    showAlert(t('tools.undo.noUndo'))
  }
}
</script>

<template>
  <u-button
    :title="$t('tools.undo.undo')"
    class="tool-button"
    icon="icon-undo"
    @click="handleClick"
  />
</template>

<style scoped>
</style>
