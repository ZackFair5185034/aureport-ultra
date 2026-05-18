<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

defineOptions({ name: 'UDialog' })

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  width?: string
  top?: string
  zIndex?: number
  showClose?: boolean
  closeOnPressEscape?: boolean
  closeOnClickModal?: boolean
  appendToBody?: boolean
  beforeClose?: (done: () => void) => void
}>(), {
  title: '标题',
  width: '50%',
  top: '15vh',
  zIndex: 20000,
  showClose: true,
  closeOnPressEscape: true,
  closeOnClickModal: false,
  appendToBody: true,
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'open': []
  'close': []
  'opend': []
  'closed': []
}>()

const rendered = ref(false)
const dialogRef = ref<HTMLElement | null>(null)

watch(() => props.visible, (val) => {
  if (val) {
    rendered.value = true
    emit('open')
  }
  else {
    emit('close')
  }
})

function handleClose() {
  if (typeof props.beforeClose === 'function') {
    props.beforeClose(() => {
      emit('update:visible', false)
      emit('close')
    })
  }
  else {
    emit('update:visible', false)
    emit('close')
  }
}

function handleClickMask() {
  if (props.closeOnClickModal) {
    handleClose()
  }
}

function handleCloseByEsc(event: KeyboardEvent) {
  if (event.keyCode === 27 && props.closeOnPressEscape && props.visible) {
    handleClose()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleCloseByEsc)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleCloseByEsc)
})
</script>

<template>
  <div
    ref="dialogRef"
    class="u-dialog"
    :class="{ 'u-dialog-show': visible }"
    :style="{ zIndex }"
    @click.self="handleClickMask"
  >
    <transition
      name="msgbox-fade"
      @after-enter="emit('opend')"
      @after-leave="emit('closed')"
    >
      <div v-show="visible" class="u-dialog-wrap" :style="{ width, marginTop: top }">
        <div class="u-dialog-header">
          <slot name="title">{{ title }}</slot>
          <i
            v-if="showClose"
            class="iconfont icon-close u-dialog-close"
            @click.stop="handleClose"
          />
        </div>
        <div v-if="rendered" class="u-dialog-body">
          <slot />
        </div>
        <div v-if="$slots.footer" class="u-dialog-footer">
          <slot name="footer" />
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.u-dialog {
  position: fixed;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0);
  visibility: hidden;
  transition: all 0.2s;
  overflow: auto;
}

.u-dialog-show {
  background-color: rgba(0, 0, 0, 0.5);
  visibility: visible;
}

.u-dialog-wrap {
  margin: 15vh auto 0;
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
  color: #333;
}

.u-dialog-header {
  position: relative;
  padding: 12px 16px;
  line-height: 20px;
  color: #fff;
  border-radius: 6px 6px 0 0;
  background-color: #00554a;
  font-size: 15px;
}

.u-dialog-close {
  position: absolute;
  right: 12px;
  top: 12px;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  cursor: pointer;
  color: #fff;
  opacity: 0.8;
}

.u-dialog-close:hover {
  opacity: 1;
}

.u-dialog-body {
  padding: 20px;
}

.u-dialog-footer {
  padding: 0 20px 20px;
  text-align: right;
}

.msgbox-fade-enter-active,
.msgbox-fade-leave-active {
  transition: all 0.3s ease;
}

.msgbox-fade-enter-from,
.msgbox-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
