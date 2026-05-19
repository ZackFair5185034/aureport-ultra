<script setup lang="ts">

defineOptions({ name: 'ContextMenu' })

const visible = ref(false)
const x = ref(0)
const y = ref(0)
const items = ref<any[]>([])
const callback = ref<((key: string) => void) | null>(null)
const justShown = ref(false)

function handleDocumentClick(e: MouseEvent) {
  if (justShown.value) {
    justShown.value = false
    return
  }

  if (visible.value) {
    const menuEl = document.querySelector('.context-menu') as HTMLElement | null
    if (menuEl && !menuEl.contains(e.target as Node)) {
      hideMenu()
    }
  }
}

defineExpose({ show, hideMenu })

onMounted(() => {
  document.addEventListener('click', handleDocumentClick, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick, true)
})

function show(event: MouseEvent, menuItems: any[], cb: (key: string) => void) {
  x.value = event.clientX
  y.value = event.clientY
  items.value = menuItems
  callback.value = cb
  visible.value = true
  justShown.value = true

  setTimeout(() => {
    justShown.value = false
  }, 100)

  nextTick(() => {
    // ensure menu doesn't go out of viewport
    setTimeout(() => {
      const menuEl = document.querySelector('.context-menu') as HTMLElement | null
      if (menuEl) {
        const rect = menuEl.getBoundingClientRect()
        const viewportWidth = window.innerWidth
        const viewportHeight = window.innerHeight
        if (rect.right > viewportWidth) {
          x.value = viewportWidth - rect.width - 5
        }

        if (rect.bottom > viewportHeight) {
          y.value = viewportHeight - rect.height - 5
        }
      }
    }, 0)
  })
}

function hideMenu() {
  visible.value = false
}

function handleItemClick(item: any) {
  if (callback.value) {
    callback.value(item.key)
  }

  hideMenu()
}

function getIconClass(icon: string) {
  const iconMap: Record<string, string> = {
    add: 'iconfont icon-plus-circle',
    edit: 'iconfont icon-edit',
    delete: 'iconfont icon-delete',
    loading: 'iconfont icon-refresh',
  }
  return iconMap[icon] || ''
}
</script>

<template>
  <div
    v-if="visible"
    class="context-menu"
    :style="{ left: `${x}px`, top: `${y}px` }"
    @click.stop
  >
    <div
      v-for="(item, index) in items"
      :key="index"
      class="context-menu-item"
      @click="handleItemClick(item)"
    >
      <i v-if="item.icon" class="menu-icon" :class="getIconClass(item.icon)" />
      <span>{{ item.name }}</span>
    </div>
  </div>
</template>

<style scoped>
.context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 10000;
  min-width: 180px;
  padding: 4px 0;
}

.context-menu-item {
  padding: 6px 16px;
  cursor: pointer;
  font-size: 13px;
  color: #333;
  display: flex;
  align-items: center;
  transition: background-color 0.2s;
}

.context-menu-item:hover {
  background-color: #f5f5f5;
}

.menu-icon {
  margin-right: 8px;
  font-size: 14px;
  width: 16px;
  display: inline-block;
  text-align: center;
}
</style>
