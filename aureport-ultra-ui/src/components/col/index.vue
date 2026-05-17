<script setup lang="ts">
import type { RowContext } from '../row/index.vue'
import { computed, inject } from 'vue'
import './style/index.css'

defineOptions({ name: 'UCol' })

const props = defineProps<{
  span?: number | string
  order?: number | string
  offset?: number | string
  push?: number | string
  pull?: number | string
  className?: string
  xs?: number | Record<string, number | string>
  sm?: number | Record<string, number | string>
  md?: number | Record<string, number | string>
  lg?: number | Record<string, number | string>
  xl?: number | Record<string, number | string>
  xxl?: number | Record<string, number | string>
}>()

const prefixCls = 'u-col'

const rowContext = inject<RowContext>('rowContext')

const classes = computed(() => {
  const classList: (string | Record<string, boolean>)[] = [
    `${prefixCls}`,
    {
      [`${prefixCls}-span-${props.span}`]: props.span !== undefined,
      [`${prefixCls}-order-${props.order}`]: props.order !== undefined,
      [`${prefixCls}-offset-${props.offset}`]: props.offset !== undefined,
      [`${prefixCls}-push-${props.push}`]: props.push !== undefined,
      [`${prefixCls}-pull-${props.pull}`]: props.pull !== undefined,
      [`${props.className}`]: !!props.className,
    },
  ]

  for (const size of (['xs', 'sm', 'md', 'lg', 'xl', 'xxl'] as const)) {
    const val = props[size]
    if (typeof val === 'number') {
      classList.push(`${prefixCls}-span-${size}-${val}`)
    }
    else if (typeof val === 'object' && val !== null) {
      const sizeProps = val as Record<string, number | string>
      for (const prop of Object.keys(sizeProps)) {
        classList.push(
          prop === 'span'
            ? `${prefixCls}-span-${size}-${sizeProps[prop]}`
            : `${prefixCls}-${size}-${prop}-${sizeProps[prop]}`,
        )
      }
    }
  }

  return classList
})

const styles = computed(() => {
  if (rowContext && rowContext.gutter !== 0) {
    return {
      paddingLeft: `${rowContext.gutter / 2}px`,
      paddingRight: `${rowContext.gutter / 2}px`,
    }
  }

  return {}
})
</script>

<template>
  <div :class="classes" :style="styles">
    <slot />
  </div>
</template>

<style scoped>
.u-row {
  position: relative;
  display: block;
}

.u-row-flex {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
}

.u-row-flex:before,
.u-row-flex:after {
  display: flex;
}

.u-row-flex-start {
  justify-content: flex-start;
}

.u-row-flex-center {
  justify-content: center;
}

.u-row-flex-end {
  justify-content: flex-end;
}

.u-row-flex-space-between {
  justify-content: space-between;
}

.u-row-flex-space-around {
  justify-content: space-around;
}

.u-row-flex-top {
  align-items: flex-start;
}

.u-row-flex-middle {
  align-items: center;
}

.u-row-flex-bottom {
  align-items: flex-end;
}

.u-row:before,
.u-row:after {
  content: '';
  display: table;
}

.u-row:after {
  clear: both;
  visibility: hidden;
  font-size: 0;
  height: 0;
}
</style>
