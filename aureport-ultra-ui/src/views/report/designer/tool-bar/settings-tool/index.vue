<template>
  <u-button
      :title="$t('setting')"
      class="tool-button"
      icon="icon-settings"
      @click="handleClick"
  >
    <SettingsDialog
        :visible="dialogVisible"
        @close="handleDialogClose"
        @ok="handleDialogOk"
    />
  </u-button>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineOptions({ name: 'SettingsTool' })

const dialogVisible = ref(false)

function handleClick() {
  dialogVisible.value = true
}

function handleDialogClose() {
  dialogVisible.value = false
}

function handleDialogOk() {
  dialogVisible.value = false
}
</script>

<style scoped>
</style>
