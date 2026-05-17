<script setup lang="ts">
import { computed, provide, reactive } from 'vue'
import './style/index.css'

defineOptions({ name: 'URow' })

const props = withDefaults(defineProps<{
  type?: string
  align?: string
  justify?: string
  gutter?: number
  className?: string
}>(), {
  gutter: 0,
})

export interface RowContext {
  gutter: number
}

const prefixCls = 'u-row'

const classes = computed(() => [
  {
    [`${prefixCls}`]: !props.type,
    [`${prefixCls}-${props.type}`]: !!props.type,
    [`${prefixCls}-${props.type}-${props.align}`]: !!props.align,
    [`${prefixCls}-${props.type}-${props.justify}`]: !!props.justify,
    [`${props.className}`]: !!props.className,
  },
])

const styles = computed(() => {
  if (props.gutter !== 0) {
    return {
      marginLeft: `${props.gutter / -2}px`,
      marginRight: `${props.gutter / -2}px`,
    }
  }

  return {}
})

const rowContext = reactive<RowContext>({
  get gutter() { return props.gutter },
})
provide('rowContext', rowContext)
</script>

<template>
  <div :class="classes" :style="styles">
    <slot />
  </div>
</template>

<style scoped>
</style>
