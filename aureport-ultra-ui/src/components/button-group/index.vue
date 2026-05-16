<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

defineOptions({ name: 'ButtonGroup' })

interface MenuItem {
  text: string
  icon?: string
  iconStyle?: Record<string, string>
  class?: string
  disabled?: boolean
  action?: (item: MenuItem) => void
}

const props = withDefaults(defineProps<{
  iconClass?: string
  iconStyle?: Record<string, string>
  buttonText?: string
  showText?: boolean
  buttonStyle?: Record<string, string>
  title?: string
  customClass?: string
  hasDropdown?: boolean
  menuItems?: MenuItem[]
  maxMenuHeight?: number
}>(), {
  iconClass: '',
  iconStyle: () => ({ color: '#0e90d2' }),
  buttonText: '',
  showText: false,
  buttonStyle: () => ({ border: 'none' }),
  title: '',
  customClass: '',
  hasDropdown: true,
  menuItems: () => [],
  maxMenuHeight: 300,
})

const emit = defineEmits<{
  'button-click': []
  'dropdown-toggle': [value: boolean]
  'dropdown-close': []
  'menu-item-click': [item: MenuItem]
}>()

const isDropdownOpen = ref(false)
const dropdown = ref<HTMLElement | null>(null)
const mainButton = ref<HTMLElement | null>(null)

function toggleDropdown() {
  if (!props.hasDropdown) {
    emit('button-click')
    return
  }
  isDropdownOpen.value = !isDropdownOpen.value
  emit('dropdown-toggle', isDropdownOpen.value)
}

function closeDropdown() {
  isDropdownOpen.value = false
  emit('dropdown-close')
}

function handleClickOutside(event: MouseEvent) {
  if (mainButton.value && mainButton.value instanceof HTMLElement && !mainButton.value.contains(event.target as Node)) {
    closeDropdown()
  }
}

function handleMenuItemClick(item: MenuItem) {
  if (item.disabled) return
  emit('menu-item-click', item)
  if (item.action && typeof item.action === 'function') {
    item.action(item)
  }
  closeDropdown()
}

onMounted(() => {
  if (props.hasDropdown) {
    document.addEventListener('click', handleClickOutside)
  }
})

onBeforeUnmount(() => {
  if (props.hasDropdown) {
    document.removeEventListener('click', handleClickOutside)
  }
})
</script>

<template>
  <div class="dropdown-buttons" :class="customClass">
    <UButton
      ref="mainButton"
      type="info"
      :style="buttonStyle"
      :title="title"
      :icon="iconClass"
      @click="toggleDropdown"
    >
      <span v-if="showText" class="button-text">{{ buttonText }}</span>
      <span v-if="hasDropdown" class="caret"></span>
    </UButton>
    <ul
      v-if="hasDropdown"
      ref="dropdown"
      class="dropdown-menu"
      role="menu"
      :style="{ display: isDropdownOpen ? 'block' : 'none', maxHeight: maxMenuHeight + 'px', overflowY: 'auto' }"
    >
      <li v-for="(item, index) in menuItems" :key="index" :class="item.class">
        <a href="javascript:void(0)" @click="handleMenuItemClick(item)" style="text-decoration: none">
          <i v-if="item.icon" :class="item.icon" :style="item.iconStyle"></i> {{ item.text }}
        </a>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.dropdown-buttons {
  position: relative;
  display: inline-block;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 1000;
  display: none;
  min-width: 160px;
  max-width: 300px;
  padding: 5px 0;
  margin: 2px 0 0;
  list-style: none;
  background-color: #fff;
  border: 1px solid #ccc;
  border: 1px solid rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.175);
  background-clip: padding-box;
}

.dropdown-menu > li > a {
  display: block;
  padding: 3px 20px;
  clear: both;
  font-weight: normal;
  line-height: 1.42857143;
  color: #333;
  white-space: nowrap;
}

.dropdown-menu > li > a:hover,
.dropdown-menu > li > a:focus {
  color: #262626;
  text-decoration: none;
  background-color: #f5f5f5;
}

.dropdown-menu > .disabled > a,
.dropdown-menu > .disabled > a:hover,
.dropdown-menu > .disabled > a:focus {
  color: #777;
}

.dropdown-menu > .disabled > a:hover,
.dropdown-menu > .disabled > a:focus {
  text-decoration: none;
  background-color: transparent;
  background-image: none;
  cursor: not-allowed;
}

.caret {
  display: inline-block;
  width: 0;
  height: 0;
  margin-left: 8px;
  vertical-align: middle;
  border-top: 4px dashed;
  border-top: 4px solid \9;
  border-right: 4px solid transparent;
  border-left: 4px solid transparent;
}

.button-text {
  margin-left: 5px;
}
</style>
