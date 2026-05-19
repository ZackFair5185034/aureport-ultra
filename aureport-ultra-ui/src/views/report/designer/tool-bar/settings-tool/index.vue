<script setup lang="ts">

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

<template>
  <u-button
    :title="$t('setting')"
    class="tool-button"
    icon="icon-settings"
    @click="handleClick"
  />
  <SettingsDialog
    :visible="dialogVisible"
    @update:visible="dialogVisible = $event"
    @close="handleDialogClose"
    @ok="handleDialogOk"
  />
</template>

<style scoped>
</style>
