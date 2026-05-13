<script setup lang="ts">
import { ref, watch, computed, onMounted, onBeforeUnmount } from 'vue'
import { oneOf, debounce } from '../utils'

defineOptions({ name: 'UInput' })

const props = withDefaults(defineProps<{
  modelValue?: string | number
  placeholder?: string
  readonly?: boolean
  disabled?: boolean
  clearable?: boolean
  prefixIcon?: string
  suffixIcon?: string
  type?: string
  size?: 'large' | 'medium' | 'small' | 'mini'
  suggestion?: boolean
  fetchSuggestions?: (value: string, cb: (options: { value: string }[]) => void) => void
}>(), {
  modelValue: '',
  placeholder: '请输入',
  readonly: false,
  disabled: false,
  clearable: false,
  prefixIcon: '',
  suffixIcon: '',
  type: 'text',
  size: 'medium',
  suggestion: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  change: [value: string]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
}>()

const inputRef = ref<HTMLElement | null>(null)
const currentValue = ref(formatValue(props.modelValue))
const panelVisible = ref(false)
const options = ref<{ value: string }[]>([])
const loading = ref(false)

const needPaddingRight = computed(() => props.clearable && !!props.suffixIcon)

watch(() => props.modelValue, (val) => {
  currentValue.value = formatValue(val)
})

function formatValue(value: string | number | undefined): string {
  return value === null || value === undefined ? '' : String(value)
}

function handleInput(e: Event) {
  const value = (e.target as HTMLInputElement).value
  currentValue.value = value
  emit('update:modelValue', value)
  emit('change', value)
  debounce(() => getSuggestions(), 333, 'fetch-suggestions')
}

function handleClean() {
  if (!props.disabled) {
    currentValue.value = ''
    emit('update:modelValue', '')
    emit('change', '')
    getSuggestions()
  }
}

function handleFocus(e: FocusEvent) {
  panelVisible.value = true
  emit('focus', e)
}

function handleBlur(e: FocusEvent) {
  emit('blur', e)
}

function addCloseEvent(event: MouseEvent) {
  const path = event.composedPath()
  if (inputRef.value && !path.includes(inputRef.value)) {
    panelVisible.value = false
  }
}

function getSuggestions() {
  if (props.fetchSuggestions) {
    loading.value = true
    props.fetchSuggestions(currentValue.value, (result) => {
      loading.value = false
      options.value = result
    })
  }
}

function setSuggestion(str: string) {
  currentValue.value = formatValue(str)
  emit('update:modelValue', str)
  emit('change', str)
  panelVisible.value = false
}

onMounted(() => {
  document.addEventListener('click', addCloseEvent)
  getSuggestions()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', addCloseEvent)
})
</script>

<template>
  <div
    ref="inputRef"
    class="u-input"
    :class="{
      'u-input-disabled': disabled,
      [`u-input-${size}`]: true,
    }"
  >
    <input
      class="u-input-inner"
      :class="{
        'u-input-disabled': disabled,
        'u-input--has-prefix': !!prefixIcon,
        'u-input--has-suffix': needPaddingRight || !!suffixIcon,
        [`u-input-inner-${size}`]: true,
      }"
      :type="type"
      :value="currentValue"
      :readonly="readonly"
      :placeholder="placeholder"
      :disabled="disabled"
      @input="handleInput"
      @focus="handleFocus"
      @blur="handleBlur"
    />
    <span v-if="prefixIcon" class="u-input-icon u-input-icon--prefix">
      <i :class="['iconfont', prefixIcon]" />
    </span>
    <span class="u-input-icon u-input-icon--suffix">
      <i
        v-if="clearable && currentValue && !disabled"
        class="iconfont icon-close"
        @click="handleClean"
      />
      <i v-else-if="suffixIcon" :class="['iconfont', suffixIcon]" />
    </span>
    <transition name="fade-bottom">
      <div
        v-if="suggestion && options.length > 0 && panelVisible"
        class="u-input-suggestion"
      >
        <div
          v-for="item in options"
          :key="item.value"
          class="u-input-suggestion-item"
          @click="setSuggestion(item.value)"
        >
          <slot name="item" :item="item">
            {{ item.value }}
          </slot>
        </div>
        <div v-show="loading" class="u-input-suggestion-loading">
          <i class="iconfont icon-loading" />
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.u-input {
  position: relative;
  display: inline-block;
  width: 220px;
  height: 36px;
}

.u-input-disabled { cursor: not-allowed; }

.u-input-inner {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  padding: 7px 10px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  outline: 0;
  font-size: 14px;
  transition: border-color 0.2s;
  background: #fff;
}

.u-input-inner:focus {
  border-color: #00554a;
  box-shadow: 0 0 0 2px rgba(0, 85, 74, 0.1);
}

.u-input-inner:disabled {
  cursor: not-allowed;
  background-color: #f5f7fa;
}

.u-input--has-prefix { padding-left: 30px; }
.u-input--has-suffix { padding-right: 30px; }

.u-input-inner-large { padding: 9px 10px; }
.u-input-inner-medium { padding: 7px 10px; }
.u-input-inner-small { padding: 5px 10px; font-size: 13px; }
.u-input-inner-mini { padding: 3px 10px; font-size: 12px; }

.u-input-large { height: 40px; }
.u-input-medium { height: 36px; }
.u-input-small { height: 32px; }
.u-input-mini { height: 28px; }

.u-input-icon {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  font-size: 14px;
  text-align: center;
  line-height: 16px;
  color: #999;
}

.u-input-icon--prefix { left: 8px; }
.u-input-icon--suffix { right: 8px; }

.u-input-icon--suffix i { cursor: pointer; }
.u-input-icon--suffix i:hover { color: #333; }

.u-input-suggestion {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  width: 100%;
  max-height: 160px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow-x: hidden;
  overflow-y: auto;
  z-index: 2367;
}

.u-input-suggestion-item {
  padding: 0 10px;
  line-height: 28px;
  font-size: 14px;
  cursor: pointer;
}

.u-input-suggestion-item:hover { background-color: #f5f7fa; }

.u-input-suggestion-loading {
  padding: 8px;
  text-align: center;
}

.fade-bottom-enter-active,
.fade-bottom-leave-active { transition: all 0.2s; }
.fade-bottom-enter-from,
.fade-bottom-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
