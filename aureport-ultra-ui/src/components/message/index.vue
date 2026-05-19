<script setup lang="ts">

defineOptions({ name: 'UMessage' })

export interface MessageItem {
  message: string
  showClose?: boolean
  useHTMLString?: boolean
  type: 'info' | 'success' | 'warning' | 'error'
  top: number
  key: string
}

const messageList = ref<MessageItem[]>([])
const offsetTop = ref(20)

function add(params: {
  message: string
  duration?: number
  showClose?: boolean
  type?: string
  useHTMLString?: boolean
}) {
  const key = `i-need-key-by-random-${Math.random()}`
  const { duration, message, showClose, useHTMLString, type } = params

  messageList.value.push({
    message,
    showClose: showClose || false,
    useHTMLString: useHTMLString || false,
    type: (type as MessageItem['type']) || 'info',
    top: offsetTop.value,
    key,
  })

  nextTick(() => {
    const el = document.querySelector(`[data-key="${key}"]`) as HTMLElement | null
    if (el) {
      offsetTop.value += el.clientHeight + 20
    }
  })

  if (duration !== 0) {
    setTimeout(() => {
      remove(key)
    }, duration || 3000)
  }
}

function remove(key: string) {
  const index = messageList.value.findIndex(cell => cell.key === key)
  if (index === -1)
    return

  const el = document.querySelector(`[data-key="${key}"]`) as HTMLElement | null
  const height = el ? el.clientHeight + 20 : 0
  offsetTop.value -= height

  messageList.value.splice(index, 1)
  for (const cell of messageList.value) {
    if (messageList.value.indexOf(cell) >= index) {
      cell.top -= height
    }
  }
}

defineExpose({ add, remove })
</script>

<template>
  <div class="u-message">
    <transition-group name="slide">
      <div
        v-for="item in messageList"
        :key="item.key"
        :data-key="item.key"
        class="u-message-cell"
        :class="[`u-message-cell-${item.type}`]"
        :style="{ top: `${item.top}px` }"
      >
        <i class="u-message-cell-icon iconfont" :class="[`icon-${item.type}`]" />

        <div
          v-if="item.useHTMLString"
          class="u-message-cell-text"
          v-html="item.message"
        />
        <div v-else class="u-message-cell-text">
          {{ item.message }}
        </div>
        <i
          v-if="item.showClose"
          class="u-message-cell-close iconfont icon-close"
          @click="remove(item.key)"
        />
      </div>
    </transition-group>
  </div>
</template>

<style scoped>
.u-message {
  z-index: 1000;
  position: absolute;
  left: 0;
  top: 0;
}

.u-message-cell {
  position: fixed;
  left: calc(50% - 200px);
  top: 40px;
  box-sizing: border-box;
  width: 400px;
  padding: 15px 10px 15px 15px;
  background-color: #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
}

.u-message-cell-icon {
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  margin-right: 10px;
}

.u-message-cell-text {
  font-size: 14px;
  margin-right: 10px;
}

.u-message-cell-close {
  width: 20px;
  height: 20px;
  line-height: 20px;
  margin-left: auto;
  cursor: pointer;
  color: #888;
}

.u-message-cell-close:hover {
  color: #000;
}

.u-message-cell-success {
  background-color: #eef8e9;
  border-color: #67c23a;
  color: #67c23a;
}
.u-message-cell-warning {
  background-color: #faedda;
  border-color: #e6a23c;
  color: #e6a23c;
}
.u-message-cell-error {
  background-color: #fde3e3;
  border-color: #f56c6c;
  color: #f56c6c;
}
</style>
