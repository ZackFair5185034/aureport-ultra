<script setup lang="ts">
import { ref } from 'vue'

defineOptions({ name: 'OpenTool' })

const visible = ref(false)
</script>

<template>
  <u-button
    :title="$t('openFile')"
    class="tool-button"
    icon="icon-folder"
    @click="visible = true"
  >
    <OpenDialog
      :visible="visible"
      @update:visible="visible = $event"
    />
  </u-button>
</template>

<style scoped>
</style>
