<template>
  <div class="u-date-picker">
    <DatePicker
      ref="datePickerRef"
      v-model:value="innerValue"
      v-bind="attrs"
      :type="type"
      :range="range"
      :format="format"
      :value-type="valueType as 'date' | 'timestamp' | 'format' | string"
      :default-value="defaultValue"
      :lang="lang"
      :placeholder="placeholder"
      :editable="editable"
      :clearable="clearable"
      :confirm="confirm"
      :confirm-text="confirmText"
      :multiple="multiple"
      :disabled="disabled"
      :disabled-date="disabledDate"
      :disabled-time="disabledTime"
      :append-to-body="appendToBody"
      :inline="inline"
      :input-class="inputClass"
      :input-attr="inputAttr"
      :open="open"
      :default-panel="defaultPanel"
      :popup-style="popupStyle"
      :popup-class="popupClass"
      :shortcuts="shortcuts"
      :title-format="titleFormat"
      :partial-update="partialUpdate"
      :range-separator="rangeSeparator"
      :show-week-number="showWeekNumber"
      :hour-step="hourStep"
      :minute-step="minuteStep"
      :second-step="secondStep"
      :hour-options="hourOptions"
      :minute-options="minuteOptions"
      :second-options="secondOptions"
      :show-hour="showHour"
      :show-minute="showMinute"
      :show-second="showSecond"
      :use12h="use12h"
      :show-time-header="showTimeHeader"
      :time-title-format="timeTitleFormat"
      :time-picker-options="timePickerOptions"
      :prefix-class="prefixClass"
      :scroll-duration="scrollDuration"
      @change="handleChange"
      @open="handleOpen"
      @close="handleClose"
      @confirm="handleConfirm"
      @clear="handleClear"
      @input-error="handleInputError"
      @focus="handleFocus"
      @blur="handleBlur"
      @pick="handlePick"
      @calendar-change="handleCalendarChange"
      @panel-change="handlePanelChange"
    >
      <template v-if="$slots['icon-calendar']" #icon-calendar>
        <slot name="icon-calendar"></slot>
      </template>
      <template v-if="$slots['icon-clear']" #icon-clear>
        <slot name="icon-clear"></slot>
      </template>
      <template v-if="$slots.input" #input>
        <slot name="input"></slot>
      </template>
      <template v-if="$slots.header" #header>
        <slot name="header"></slot>
      </template>
      <template v-if="$slots.footer" #footer>
        <slot name="footer"></slot>
      </template>
      <template v-if="$slots.sidebar" #sidebar>
        <slot name="sidebar"></slot>
      </template>
    </DatePicker>
  </div>
</template>

<script setup lang="ts">
import { ref, useAttrs, useSlots, inject } from 'vue'
import DatePicker from 'vue-datepicker-next'
import 'vue-datepicker-next/index.css'
import 'vue-datepicker-next/locale/zh-cn'
import 'vue-datepicker-next/locale/en'
import type { FormItemContext } from '../form-item/index.vue'

defineOptions({ name: 'UDatePicker' })

const props = withDefaults(defineProps<{
  modelValue?: Date | string | number | (Date | string | number)[]
  type?: string
  range?: boolean
  format?: string
  valueType?: string
  defaultValue?: Date | string | number
  lang?: Record<string, unknown> | null
  placeholder?: string
  editable?: boolean
  clearable?: boolean
  confirm?: boolean
  confirmText?: string
  multiple?: boolean
  disabled?: boolean
  disabledDate?: (date: Date) => boolean
  disabledTime?: (date: Date | Date[]) => boolean
  appendToBody?: boolean
  inline?: boolean
  inputClass?: string
  inputAttr?: Record<string, unknown>
  open?: boolean | null
  defaultPanel?: string | null
  popupStyle?: Record<string, string>
  popupClass?: string
  shortcuts?: unknown[]
  titleFormat?: string
  partialUpdate?: boolean
  rangeSeparator?: string
  showWeekNumber?: boolean
  hourStep?: number
  minuteStep?: number
  secondStep?: number
  hourOptions?: number[] | null
  minuteOptions?: number[] | null
  secondOptions?: number[] | null
  showHour?: boolean | null
  showMinute?: boolean | null
  showSecond?: boolean | null
  use12h?: boolean | null
  showTimeHeader?: boolean
  timeTitleFormat?: string
  timePickerOptions?: Record<string, unknown> | null
  prefixClass?: string
  scrollDuration?: number
}>(), {
  modelValue: undefined,
  type: 'date',
  range: false,
  format: 'YYYY-MM-DD',
  valueType: 'format',
  defaultValue: () => new Date(),
  lang: null,
  placeholder: '',
  editable: true,
  clearable: true,
  confirm: false,
  confirmText: 'OK',
  multiple: false,
  disabled: false,
  appendToBody: true,
  inline: false,
  inputClass: 'mx-input',
  inputAttr: () => ({}),
  open: null,
  defaultPanel: null,
  popupStyle: () => ({}),
  popupClass: '',
  shortcuts: () => [],
  titleFormat: 'YYYY-MM-DD',
  partialUpdate: false,
  rangeSeparator: ' ~ ',
  showWeekNumber: false,
  hourStep: 1,
  minuteStep: 1,
  secondStep: 1,
  hourOptions: null,
  minuteOptions: null,
  secondOptions: null,
  showHour: null,
  showMinute: null,
  showSecond: null,
  use12h: null,
  showTimeHeader: false,
  timeTitleFormat: 'YYYY-MM-DD',
  timePickerOptions: null,
  prefixClass: 'mx',
  scrollDuration: 100,
})

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
  'change': [value: unknown, type?: unknown]
  'open': [event: unknown]
  'close': []
  'confirm': [date: unknown]
  'clear': []
  'input-error': [value: unknown]
  'focus': []
  'blur': []
  'pick': [date: unknown]
  'calendar-change': [date: unknown, oldDate: unknown, type: unknown]
  'panel-change': [type: unknown, oldType: unknown]
}>()

const formItemContext = inject<FormItemContext | undefined>('formItemContext', undefined)

const datePickerRef = ref<{ openPopup: () => void; closePopup: () => void; clearDate: () => void } | null>(null)
const innerValue = ref(props.modelValue)
const attrs = useAttrs()
const $slots = useSlots()

function handleChange(date: unknown, type?: unknown) {
  innerValue.value = date as Date | string | number | (Date | string | number)[] | undefined
  emit('update:modelValue', date)
  emit('change', date, type)
  formItemContext?.onFieldChange()
}

function handleOpen(event: unknown) {
  emit('open', event)
}

function handleClose() {
  emit('close')
}

function handleConfirm(date: unknown) {
  emit('confirm', date)
}

function handleClear() {
  emit('clear')
}

function handleInputError(value: unknown) {
  emit('input-error', value)
}

function handleFocus() {
  emit('focus')
}

function handleBlur() {
  emit('blur')
  formItemContext?.onFieldBlur()
}

function handlePick(date: unknown) {
  emit('pick', date)
}

function handleCalendarChange(date: unknown, oldDate: unknown, type: unknown) {
  emit('calendar-change', date, oldDate, type)
}

function handlePanelChange(type: unknown, oldType: unknown) {
  emit('panel-change', type, oldType)
}

function openPopup() {
  datePickerRef.value?.openPopup()
}

function closePopup() {
  datePickerRef.value?.closePopup()
}

function clearDate() {
  datePickerRef.value?.clearDate()
}

defineExpose({ openPopup, closePopup, clearDate })
</script>

<style scoped>
.u-date-picker {
  display: inline-block;
}
</style>
