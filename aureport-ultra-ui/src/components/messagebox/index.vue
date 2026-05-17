<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

defineOptions({ name: 'UMessageBox' })

const props = withDefaults(defineProps<{
  visible?: boolean
  title?: string
  message?: string
  type?: 'alert' | 'confirm' | 'prompt'
  useHTMLString?: boolean
  zIndex?: number
  callback?: (value?: string) => void
}>(), {
  visible: false,
  title: '',
  message: '',
  type: 'alert',
  useHTMLString: false,
  zIndex: 20100,
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'submit': [value?: string]
  'cancel': []
}>()

const currentValue = ref('')
const show = ref(props.visible)

watch(() => props.visible, (val: boolean) => {
  show.value = val
  if (val) {
    currentValue.value = ''
  }
})

function handleClose() {
  show.value = false
  emit('update:visible', false)
}

function handleSubmit() {
  show.value = false
  emit('update:visible', false)
  emit('submit', currentValue.value)
  if (props.callback) {
    props.callback(currentValue.value)
  }
}

function handleCancel() {
  show.value = false
  emit('update:visible', false)
  emit('cancel')
}

function handleCloseByEsc(event: KeyboardEvent) {
  if (event.keyCode === 27 && show.value) {
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
    class="u-messagebox"
    :class="{ 'u-messagebox-show': show }"
    :style="{ 'z-index': zIndex }"
  >
    <transition name="messagebox-fade">
      <div v-show="show" class="u-messagebox-wrap">
        <div class="u-messagebox-title">
          <span>{{ title }}</span>
          <i class="u-messagebox-close iconfont icon-close" @click="handleClose" />
        </div>
        <div v-if="type === 'prompt'" class="u-messagebox-prompt">
          <p v-if="useHTMLString" v-html="message" />
          <p v-else>
            <slot>{{ message }}</slot>
          </p>
          <div class="u-messagebox-input">
            <UInput v-model="currentValue" style="width: 100%" />
          </div>
        </div>
        <div v-else class="u-messagebox-content">
          <i v-show="type === 'confirm'" class="u-messagebox-icon iconfont icon-warning" />
          <div v-if="useHTMLString" class="u-messagebox-text" v-html="message" />
          <div v-else class="u-messagebox-text">
            <slot>{{ message }}</slot>
          </div>
        </div>
        <div class="u-messagebox-footer">
          <UButton v-show="type !== 'alert'" type="info" style="margin-right: 5px" @click="handleCancel">取消</UButton>
          <UButton @click="handleSubmit">确定</UButton>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.u-messagebox {
  z-index: 2000;
  position: fixed;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0);
  visibility: hidden;
  transition: all 0.2s;
}

.u-messagebox-show {
  background-color: rgba(0, 0, 0, 0.5);
  visibility: visible;
}

.u-messagebox-wrap {
  position: fixed;
  left: calc(50% - 200px);
  top: 35%;
  width: 400px;
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
}

.u-messagebox-title {
  position: relative;
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
  padding: 10px 15px 10px 10px;
  background-color: #00554a;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
}

.u-messagebox-close {
  position: absolute;
  right: 5px;
  width: 20px;
  height: 20px;
  line-height: 20px;
  cursor: pointer;
  color: #fff;
}

.u-messagebox-close:hover {
  color: #ddd;
}

.u-messagebox-content {
  padding: 15px;
  line-height: 18px;
  min-height: 36px;
  display: flex;
}

.u-messagebox-text {
  font-size: 14px;
}

.u-messagebox-icon {
  position: relative;
  top: -2px;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  margin-right: 10px;
  font-size: 24px;
  color: #e6a23c;
}

.u-messagebox-footer {
  padding: 0 15px 15px 15px;
  text-align: right;
}

.u-messagebox-prompt {
  padding: 10px 15px;
}

.u-messagebox-prompt p {
  line-height: 1.5;
  font-size: 14px;
  margin-bottom: 10px;
}
</style>
