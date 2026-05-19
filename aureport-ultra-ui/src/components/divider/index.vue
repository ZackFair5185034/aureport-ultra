<script setup lang="ts">

defineOptions({ name: 'UDivider' })

const props = withDefaults(defineProps<{
  type?: 'horizontal' | 'vertical'
  align?: 'left' | 'right' | 'center'
  dashed?: boolean
}>(), {
  type: 'horizontal',
  align: 'center',
  dashed: false,
})

const classes = computed(() => [
  'u-divider',
  `u-divider-${props.type}`,
  { 'u-divider-dashed': props.dashed },
])

const slotClasses = computed(() => [
  'u-divider-inner-text',
  `is-${props.align}`,
])
</script>

<template>
  <div :class="classes">
    <span v-if="$slots.default" :class="slotClasses">
      <slot />
    </span>
  </div>
</template>

<style scoped>
.u-divider {
  position: relative;
}

.u-divider-horizontal {
  display: block;
  height: 1px;
  width: 100%;
  margin: 24px 0;
}

.u-divider-vertical {
  position: relative;
  margin: 0 8px;
  display: inline-block;
  height: 1em;
  width: 1px;
  vertical-align: middle;
  top: -0.06em;
}

.u-divider-dashed {
  background: 0 0;
}

.u-divider-inner-text {
  position: absolute;
  background-color: #fff;
  padding: 0 16px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.u-divider-inner-text.is-left {
  left: 24px;
  transform: translateY(-50%);
}

.u-divider-inner-text.is-center {
  left: 50%;
  transform: translate(-50%) translateY(-50%);
}

.u-divider-inner-text.is-right {
  right: 24px;
  transform: translateY(-50%);
}
</style>
