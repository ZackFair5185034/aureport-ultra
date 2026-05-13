<script setup lang="ts">
import { computed, inject, onMounted, onBeforeUnmount } from 'vue'
import type { TabsContext } from './index.vue'

defineOptions({ name: 'UTabPane' })

const props = defineProps<{
  label: string
  index: string
}>()

const tabsContext = inject<TabsContext>('tabsContext')

onMounted(() => {
  tabsContext?.registerPane({ label: props.label, index: props.index })
})

onBeforeUnmount(() => {
  tabsContext?.unregisterPane({ label: props.label, index: props.index })
})

const visible = computed(() => tabsContext?.activeValue === props.index)
</script>

<template>
  <div v-show="visible" class="pane">
    <slot />
  </div>
</template>

<style scoped>
</style>
