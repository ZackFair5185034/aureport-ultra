<script setup lang="ts">
import type { FormItemContext } from '../form-item/index.vue'
import { computed, inject, nextTick, onBeforeUnmount, onMounted, provide, reactive, ref, watch } from 'vue'
import { LoadingDirective as vLoading } from '../loading/instance'
import { debounce } from '../utils'

defineOptions({ name: 'USelect' })

const props = withDefaults(defineProps<{
  modelValue?: unknown
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  size?: 'mini' | 'small' | 'medium' | 'large'
  filterable?: boolean
  filterMethod?: (value: string) => void
  loading?: boolean
  remote?: boolean
  remoteMethod?: (value: string) => void
  multiple?: boolean
  multipleLimit?: number
}>(), {
  placeholder: '请选择',
  disabled: false,
  clearable: false,
  size: 'medium',
  filterable: false,
  loading: false,
  remote: false,
  multiple: false,
  multipleLimit: 0,
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  'change': [value: unknown]
  'visible-change': [value: boolean]
  'focus': [value: FocusEvent]
  'blur': [value: FocusEvent]
  'clear': []
  'remove-tag': [value: unknown]
}>()

const formItemContext = inject<FormItemContext | undefined>('formItemContext')

const options = ref<OptionInstance[]>([])
const visible = ref(false)
const currentLabel = ref('')
const currentValue = ref<unknown>('')
const currentTags = ref<OptionInstance[]>([])
const panelHeight = ref(0)
const hasOptions = ref(true)
const currentPlaceholder = ref('')
const onHover = ref(false)
const optionsWidth = ref<number | null>(null)

const uSelectRef = ref<HTMLElement | null>(null)
const multiPanelRef = ref<HTMLElement | null>(null)
const multiInputRef = ref<HTMLInputElement | null>(null)

const placeholderLabel = computed(() => currentPlaceholder.value || props.placeholder)

export interface OptionInstance {
  value: unknown
  label: string
  disabled: boolean
  selected: boolean
  choose: boolean
  visible: boolean
  multi: boolean
}

export interface SelectContext {
  onOptionAdd: (option: OptionInstance) => void
  onOptionSelect: (option: OptionInstance) => void
  onOptionRemove: (option: OptionInstance) => void
}

const selectContext: SelectContext = {
  onOptionAdd(child) {
    options.value.push(child)
    syncValue(props.modelValue)
    setMultiOptionStyle(props.multiple)
  },
  onOptionSelect(child) {
    if (props.multiple) {
      handleMultiChoose(child)
    }
    else {
      handleChoose(child)
    }
  },
  onOptionRemove(child) {
    const idx = options.value.indexOf(child)
    if (idx !== -1)
      options.value.splice(idx, 1)
  },
}
provide('selectContext', selectContext)

function updateOptionsWidth() {
  if (uSelectRef.value) {
    optionsWidth.value = uSelectRef.value.offsetWidth
  }
}

function handleMultiClick() {
  if (props.disabled)
    return
  visible.value = !visible.value
  if (props.filterable && visible.value) {
    nextTick(() => {
      multiInputRef.value?.focus()
    })
  }
}

function handleClick() {
  visible.value = !visible.value
}

function handleChoose(opt: OptionInstance) {
  for (const d of options.value) (d.selected = false)
  opt.selected = true
  currentValue.value = opt.value
  currentLabel.value = opt.label
  visible.value = false
  emit('update:modelValue', currentValue.value)
  emit('change', currentValue.value)
}

function handleMultiChoose(opt: OptionInstance) {
  if (props.multipleLimit > 0 && currentTags.value.length >= props.multipleLimit && !opt.choose)
    return
  opt.choose = !opt.choose
  if (props.filterable) {
    multiInputRef.value?.focus()
  }

  if (opt.choose) {
    currentTags.value.push(opt)
  }
  else {
    const idx = currentTags.value.indexOf(opt)
    if (idx !== -1)
      currentTags.value.splice(idx, 1)
  }
}

function addCloseEvent(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!uSelectRef.value)
    return
  if (!uSelectRef.value.contains(target) && visible.value) {
    visible.value = false
  }
}

function setHoverAttr(event: MouseEvent) {
  onHover.value = event.type === 'mouseenter'
}

function handleClear() {
  currentLabel.value = ''
  currentValue.value = ''
  for (const d of options.value) (d.selected = false)
  emit('update:modelValue', null)
  emit('change', null)
  emit('clear')
}

function handleInput(e: Event) {
  const target = e.target as HTMLInputElement
  currentLabel.value = target.value
  debounce(() => {
    if (props.remote && typeof props.remoteMethod === 'function') {
      props.remoteMethod(currentLabel.value)
    }
    else if (props.filterMethod && typeof props.filterMethod === 'function') {
      props.filterMethod(currentLabel.value)
    }
    else {
      filterOptionsByValue(currentLabel.value)
    }
  }, 333, 'u-select-input')
}

function filterOptionsByValue(value: string) {
  for (const cell of options.value) {
    const label = cell.label || ''
    cell.visible = label.includes(value)
  }

  setNoDataVisible()
}

function handleInputFocus(e: FocusEvent) {
  if (props.filterable) {
    currentPlaceholder.value = currentLabel.value
    currentLabel.value = ''
  }

  emit('focus', e)
}

function handleInputBlur(e: FocusEvent) {
  if (props.filterable) {
    currentLabel.value = currentPlaceholder.value
    currentPlaceholder.value = ''
  }

  emit('blur', e)
  setTimeout(() => {
    if (visible.value)
      return
    filterOptionsByValue('')
  }, 250)
}

function setNoDataVisible() {
  hasOptions.value = props.filterable ? options.value.some(d => d.visible) : options.value.length > 0
}

function setMultiOptionStyle(multi: boolean) {
  for (const d of options.value) (d.multi = multi)
}

function handleTagClose(opt: OptionInstance) {
  if (props.disabled)
    return
  opt.choose = false
  emit('remove-tag', opt.value)
  const idx = currentTags.value.indexOf(opt)
  if (idx !== -1)
    currentTags.value.splice(idx, 1)
}

function syncValue(value: unknown) {
  if (props.multiple) {
    __syncMultiValues(value)
  }
  else {
    __syncSimpleValue(value)
  }

  formItemContext?.onFieldChange()
}

function __syncSimpleValue(value: unknown) {
  let found = false
  for (const d of options.value) {
    if (d.value === value) {
      d.selected = true
      currentValue.value = value
      currentLabel.value = d.label
      found = true
    }
    else {
      d.selected = false
    }
  }

  if (!found) {
    currentValue.value = value
    currentLabel.value = String(value ?? '')
  }
}

function __syncMultiValues(values: unknown) {
  const vals = (values as unknown[]) || []
  for (const d of options.value) {
    if (vals.includes(d.value)) {
      d.choose = true
      if (!currentTags.value.some(tag => tag.value === d.value)) {
        currentTags.value.push(d)
      }
    }
    else {
      d.choose = false
      const idx = currentTags.value.findIndex(tag => tag.value === d.value)
      if (idx !== -1)
        currentTags.value.splice(idx, 1)
    }
  }
}

watch(() => currentTags.value.length, () => {
  nextTick(() => {
    setTimeout(() => {
      if (multiPanelRef.value) {
        panelHeight.value = multiPanelRef.value.clientHeight
      }
    }, 10)
  })
  const values = currentTags.value.reduce<unknown[]>((total, cell) => {
    if (cell.choose) {
      total.push(cell.value)
    }

    return total
  }, [])
  emit('update:modelValue', values)
  emit('change', values)
})

watch(() => props.modelValue, (newVal) => {
  syncValue(newVal)
}, { immediate: true })

watch(visible, (newVal) => {
  emit('visible-change', newVal)
  if (newVal) {
    updateOptionsWidth()
  }
})

watch(() => props.multiple, (value) => {
  setMultiOptionStyle(value)
}, { immediate: true })

onMounted(() => {
  document.addEventListener('click', addCloseEvent)
  updateOptionsWidth()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', addCloseEvent)
})
</script>

<template>
  <div ref="uSelectRef" class="u-select">
    <div
      class="u-select-inner"
      @mouseenter="setHoverAttr"
      @mouseleave="setHoverAttr"
    >
      <div
        v-if="multiple"
        ref="multiPanelRef"
        class="u-select-multi"
        :class="{
          'u-select-multi-disabled': disabled,
          [`u-select-multi-${size}`]: true,
        }"
        @click.self="handleMultiClick"
      >
        <UTag
          v-for="item in currentTags"
          :key="item.value"
          size="mini"
          type="text"
          closeable
          disableTransitions
          @close="handleTagClose(item)"
        >
          {{ item.label }}
        </UTag>
        <input
          v-if="filterable"
          ref="multiInputRef"
          type="text"
          class="u-select-multi-input"
          :placeholder="disabled ? '' : '请输入'"
          :disabled="disabled"
          @click.self="handleMultiClick"
          @input="handleInput"
          @focus="handleInputFocus"
          @blur="handleInputBlur"
        />
      </div>
      <input
        class="u-select-inner-input"
        :class="{
          ['u-select-inner-input-select']: visible,
          ['u-select-inner-input-disabled']: disabled,
          [`u-select-inner-input-size-${size}`]: true,
        }"
        :style="{ 'min-height': panelHeight ? `${panelHeight}px` : undefined }"
        type="text"
        :placeholder="currentTags.length > 0 ? '' : placeholderLabel"
        :disabled="disabled"
        :value="multiple ? '' : currentLabel"
        :readonly="!filterable"
        @click="handleClick"
        @input="handleInput"
        @focus="handleInputFocus"
        @blur="handleInputBlur"
      />
      <i
        v-show="!(clearable && currentValue && onHover)"
        class="u-select-inner-icon iconfont icon-down"
        :class="{
          'u-select-inner-icon-focus': visible,
          [`u-select-inner-icon-size-${size}`]: true,
        }"
      />
      <span
        v-show="clearable && currentValue && onHover"
        class="u-select-inner-icon"
        @click="handleClear"
      >
        <i class="iconfont icon-close" />
      </span>
    </div>
    <transition name="fade-bottom">
      <div
        v-show="visible"
        v-loading="filterable && loading"
        class="u-select-options"
        :style="{
          top: panelHeight ? `${panelHeight + 6}px` : undefined,
          width: optionsWidth ? `${optionsWidth}px` : undefined,
        }"
      >
        <slot />
        <div
          v-show="!$slots.default || !hasOptions"
          class="u-select-options-no-data"
        >
          无数据
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.u-select {
  position: relative;
  width: 220px;
  display: inline-block;
}

.u-select-inner {
  position: relative;
}

.u-select-inner-input {
  cursor: pointer;
  background-color: #fff;
  background-image: none;
  border-radius: 4px;
  border: 1px solid #d8d8d8;
  box-sizing: border-box;
  color: #606266;
  display: inline-block;
  font-size: 13px;
  height: 36px;
  line-height: 34px;
  outline: 0;
  padding: 0 30px 0 15px;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  width: 100%;
}

.u-select-inner-input-select {
  border-color: #00554a;
}

.u-select-inner-input-disabled {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}

.u-select-inner-icon {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  right: 3px;
  width: 25px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #bbbcc0;
}

.u-select-inner-icon-focus {
  transform: translateY(-50%) rotate(180deg);
  color: #00554a;
}

.u-select-inner-input-size-large {
  height: 40px;
  line-height: 38px;
}

.u-select-inner-icon-size-large {
  height: 38px;
  line-height: 38px;
}

.u-select-inner-input-size-small {
  font-size: 12px;
  height: 32px;
  line-height: 30px;
}

.u-select-inner-icon-size-small {
  height: 30px;
  line-height: 30px;
}

.u-select-inner-input-size-mini {
  font-size: 12px;
  height: 28px;
  line-height: 26px;
}

.u-select-inner-icon-size-mini {
  height: 26px;
  line-height: 26px;
}

.u-select-options {
  max-height: 160px;
  transform-origin: center top;
  z-index: 2367;
  position: absolute;
  top: 42px;
  left: 0;
  border: solid 1px #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
  margin: 5px 0;
  overflow-x: hidden;
  overflow-y: auto;
}

.u-select-options::-webkit-scrollbar-track-piece {
  background: #f8f8f8;
}

.u-select-options::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.u-select-options::-webkit-scrollbar-thumb:hover {
  background-color: #bbb;
}

.u-select-options::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 10px;
}

.u-select-options-no-data {
  padding: 5px 0;
  line-height: 32px;
  margin: 0;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.u-select-multi {
  cursor: pointer;
  box-sizing: border-box;
  position: absolute;
  left: 0;
  top: 0;
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  padding: 0 30px 4px 10px;
}

.u-select-multi-disabled {
  cursor: not-allowed !important;
}

.u-select-multi .u-tag {
  margin: 4px 4px 0 0;
}

.u-select-multi-input {
  display: inline-block;
  width: 80px;
  border: none;
  outline: 0;
  height: 20px;
  line-height: 20px;
  padding: 0;
  margin-top: 4px;
}

.u-select-multi-large {
  padding: 6px 30px 10px 10px;
}

.u-select-multi-small {
  padding: 2px 30px 6px 10px;
}

.u-select-multi-medium {
  padding: 4px 30px 8px 10px;
}
</style>

<style>
::-webkit-input-placeholder {
  color: #bbbcc0;
}

:-moz-placeholder {
  color: #bbbcc0;
}

::-moz-placeholder {
  color: #bbbcc0;
}

:-ms-input-placeholder {
  color: #bbbcc0;
}
</style>
