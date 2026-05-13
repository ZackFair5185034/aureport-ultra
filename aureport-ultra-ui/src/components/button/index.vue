<script setup lang="ts">
import { oneOf } from '../utils'

defineOptions({ name: 'UButton', inheritAttrs: false })

const props = withDefaults(defineProps<{
  nativeType?: 'button' | 'submit' | 'reset'
  type?: 'primary' | 'info' | 'success' | 'warning' | 'text' | 'error'
  disabled?: boolean
  size?: 'large' | 'medium' | 'small' | 'mini'
  icon?: string
  round?: boolean
  circle?: boolean
  loading?: boolean
}>(), {
  nativeType: 'button',
  type: 'primary',
  disabled: false,
  size: 'medium',
  round: false,
  circle: false,
  loading: false,
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

function handleClick(event: MouseEvent) {
  emit('click', event)
}
</script>

<template>
  <button
    class="u-btn"
    v-bind="$attrs"
    :type="nativeType"
    :disabled="disabled || loading"
    :class="[
      `u-btn-${type}`,
      `u-btn-size-${size}`,
      {
        'u-btn-disabled': disabled,
        [`u-btn-round`]: round,
        [`u-btn-circle`]: circle,
        'is-loading': loading,
      },
    ]"
    @click="handleClick"
  >
    <i v-if="loading" class="iconfont icon-loading u-btn-loading-icon" />
    <i v-else-if="icon" :class="['iconfont', icon]" />
    <slot />
  </button>
</template>

<style scoped>
.u-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 1px solid transparent;
  border-radius: 4px;
  outline: 0;
  padding: 0 12px;
  height: 36px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  user-select: none;
  box-sizing: border-box;
}

.u-btn:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}

.u-btn-primary { background-color: #00554a; border-color: #00554a; color: #fff; }
.u-btn-primary:hover { background-color: rgba(0, 85, 74, 0.8); }
.u-btn-primary:active { background-color: rgba(0, 85, 74, 0.9); }
.u-btn-primary.u-btn-disabled { background-color: rgba(0, 85, 74, 0.5); border-color: rgba(0, 85, 74, 0.5); cursor: not-allowed; }

.u-btn-info { background-color: #fff; border-color: #ddd; color: #5e6d82; }
.u-btn-info:hover { color: #00554a; border-color: rgba(0, 85, 74, 0.5); }
.u-btn-info.u-btn-disabled { color: rgba(94, 109, 130, 0.5); cursor: not-allowed; }

.u-btn-success { background-color: #67c23a; border-color: #67c23a; color: #fff; }
.u-btn-success:hover { background-color: rgba(103, 194, 58, 0.8); }
.u-btn-success.u-btn-disabled { background-color: rgba(103, 194, 58, 0.5); cursor: not-allowed; }

.u-btn-warning { background-color: #e6a23c; border-color: #e6a23c; color: #fff; }
.u-btn-warning:hover { background-color: rgba(230, 162, 60, 0.8); }
.u-btn-warning.u-btn-disabled { background-color: rgba(230, 162, 60, 0.5); cursor: not-allowed; }

.u-btn-error { background-color: #f56c6c; border-color: #f56c6c; color: #fff; }
.u-btn-error:hover { background-color: rgba(245, 108, 108, 0.8); }
.u-btn-error.u-btn-disabled { background-color: rgba(245, 108, 108, 0.5); cursor: not-allowed; }

.u-btn-text { background-color: transparent; border-color: transparent; color: #00554a; }
.u-btn-text:hover { color: rgba(0, 85, 74, 0.7); }
.u-btn-text.u-btn-disabled { color: rgba(0, 85, 74, 0.5); cursor: not-allowed; }

.u-btn-size-large { height: 40px; padding: 0 15px; }
.u-btn-size-medium { height: 36px; }
.u-btn-size-small { height: 32px; font-size: 13px; padding: 0 10px; }
.u-btn-size-mini { height: 28px; font-size: 12px; padding: 0 8px; }

.u-btn-round { border-radius: 18px; }
.u-btn-size-large.u-btn-round { border-radius: 20px; }
.u-btn-size-small.u-btn-round { border-radius: 16px; }
.u-btn-size-mini.u-btn-round { border-radius: 14px; }

.u-btn-circle { min-width: 36px; border-radius: 50%; padding: 0; }
.u-btn-size-large.u-btn-circle { min-width: 40px; }
.u-btn-size-small.u-btn-circle { min-width: 32px; }
.u-btn-size-mini.u-btn-circle { min-width: 28px; }

.u-btn-loading-icon {
  display: inline-block;
  animation: u-btn-spin 2s linear infinite;
}

@keyframes u-btn-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
