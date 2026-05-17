<script setup lang="ts">
import { ref } from 'vue'

defineOptions({ name: 'SearchFormSwitchTool' })

const visible = ref(false)
</script>

<template>
  <u-button
    :title="$t('tools.searchFormSwitch')"
    class="tool-button"
    icon="icon-form-design"
    @click="visible = true"
  >
    <search-form-dialog
      :visible="visible"
      @update:visible="visible = $event"
    />
  </u-button>
</template>

<style scoped>
</style>
