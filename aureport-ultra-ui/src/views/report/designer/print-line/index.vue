<script setup lang="ts">

import { useReportStore } from '@/stores/report'

defineOptions({ name: 'PrintLine' })

const store = useReportStore()

const printLine = ref<HTMLDivElement | null>(null)

const context = computed(() => store.context || null)

const lineStyle = reactive({
  height: '0px',
  width: '0px',
  borderLeft: 'dashed 1px #999999',
  position: 'absolute' as const,
  left: '300pt',
  top: '35px',
  zIndex: 10,
})

function updateLineHeight() {
  const height = window.innerHeight - 90
  lineStyle.height = `${height}px`
}

function refresh() {
  const paper = (context.value as Record<string, any>)?.reportDef?.paper
  if (!paper)
    return
  const orientation = paper.orientation
  let width = paper.width
  if (orientation === 'landscape') {
    width = paper.height
  }

  const actualWidth = width - paper.leftMargin - paper.rightMargin + 38
  lineStyle.left = `${actualWidth}pt`
}

onMounted(() => {
  window.addEventListener('resize', updateLineHeight)
  updateLineHeight()
  refresh()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateLineHeight)
})
</script>

<template>
  <div
    ref="printLine"
    title="打印线"
    class="ureport-right-hr-for-print"
    :style="lineStyle"
  />
</template>

<style scoped>
</style>
