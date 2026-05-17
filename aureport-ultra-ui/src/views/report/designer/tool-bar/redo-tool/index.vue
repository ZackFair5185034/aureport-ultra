<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { undoManager } from '@/utils/table'

defineOptions({ name: 'RedoTool' })

const { t } = useI18n()

function handleClick() {
  if (undoManager.hasRedo()) {
    undoManager.redo()
  }
  else {
    showAlert(t('tools.redo.noRedo'))
  }
}
</script>

<template>
  <u-button
    :title="$t('tools.redo.redo')"
    class="tool-button"
    icon="icon-redo"
    @click="handleClick"
  />
</template>

<style scoped>
</style>
