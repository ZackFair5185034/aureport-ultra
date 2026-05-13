<template>
  <div>
    <div class="form-group form-group-hf-desc">
      {{ $t('dialog.setting.hfdesc') }}
    </div>

    <div>
      <label>{{ $t('dialog.setting.header') }}：</label>
      <u-button
          class="btn-hf-setting"
          @click="handleOpenHeaderFontDialog">
        {{ $t('dialog.setting.fontStyleSetting') }}
      </u-button>

      <span class="span-hf-margin">
        <span>{{ $t('dialog.setting.headerMargin') }}：</span>
      </span>
      <div class="u-inline">
        <u-input-number
          :value="headerMargin"
          @change="handleHeaderMarginChange"
        />
      </div>
    </div>

    <div class="form-group" style="margin-top:10px">
      <label class="label-align-top">{{ $t('dialog.setting.hfLeft') }}：</label>
      <textarea
        ref="leftHeader"
        :value="localHeader.left"
        class="form-control editor-textarea"
        @change="handleHeaderLeftChange"
      ></textarea>

      <span class="span-align-top">{{ $t('dialog.setting.hfCenter') }}：</span>
      <textarea
        ref="centerHeader"
        :value="localHeader.center"
        class="form-control editor-textarea"
        @change="handleHeaderCenterChange"
      ></textarea>

      <span class="span-align-top">{{ $t('dialog.setting.hfRight') }}：</span>
      <textarea
        ref="rightHeader"
        :value="localHeader.right"
        class="form-control editor-textarea"
        @change="handleHeaderRightChange"
      ></textarea>
    </div>

    <div class="div-footer-section">
      <label>{{ $t('dialog.setting.footer') }}：</label>
      <u-button
          class="btn-hf-setting"
          @click="handleOpenFooterFontDialog">
        {{ $t('dialog.setting.fontStyleSetting') }}
      </u-button>

      <span class="span-hf-margin">
        <span>{{ $t('dialog.setting.footerMargin') }}：</span>
      </span>
      <div class="u-inline">
        <u-input-number
          :value="footerMargin"
          @change="handleFooterMarginChange"
        />
      </div>
    </div>

    <div class="form-group" style="margin-top:10px">
      <label class="label-align-top">{{ $t('dialog.setting.hfLeft') }}：</label>
      <textarea
        ref="leftFooter"
        :value="localFooter.left"
        class="form-control editor-textarea"
        @change="handleFooterLeftChange"
      ></textarea>

      <span class="span-align-top">{{ $t('dialog.setting.hfCenter') }}：</span>
      <textarea
        ref="centerFooter"
        :value="localFooter.center"
        class="form-control editor-textarea"
        @change="handleFooterCenterChange"
      ></textarea>

      <span class="span-align-top">{{ $t('dialog.setting.hfRight') }}：</span>
      <textarea
        ref="rightFooter"
        :value="localFooter.right"
        class="form-control editor-textarea"
        @change="handleFooterRightChange"
      ></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { pointToMM, mmToPoint } from '@/utils/table.js'

defineOptions({ name: 'HeaderFooterSettings' })

const emit = defineEmits<{
  (e: 'open-header-font-dialog'): void
  (e: 'open-footer-font-dialog'): void
  (e: 'update:header', value: any): void
  (e: 'update:footer', value: any): void
  (e: 'header-margin-change'): void
  (e: 'footer-margin-change'): void
  (e: 'header-footer-change'): void
}>()

const props = withDefaults(defineProps<{
  header?: any
  footer?: any
}>(), {
  header: () => ({}),
  footer: () => ({})
})

const { t } = useI18n()

const localHeader = ref({ ...props.header })
const localFooter = ref({ ...props.footer })

const leftHeader = ref<HTMLTextAreaElement | null>(null)
const centerHeader = ref<HTMLTextAreaElement | null>(null)
const rightHeader = ref<HTMLTextAreaElement | null>(null)
const leftFooter = ref<HTMLTextAreaElement | null>(null)
const centerFooter = ref<HTMLTextAreaElement | null>(null)
const rightFooter = ref<HTMLTextAreaElement | null>(null)

const headerMargin = computed(() => pointToMM(localHeader.value.margin))
const footerMargin = computed(() => pointToMM(localFooter.value.margin))

watch(() => props.header, (newVal) => {
  localHeader.value = { ...newVal }
  nextTick(() => { setHeaderEditorStyles() })
}, { deep: true })

watch(() => props.footer, (newVal) => {
  localFooter.value = { ...newVal }
  nextTick(() => { setFooterEditorStyles() })
}, { deep: true })

onMounted(() => {
  setHeaderEditorStyles()
  setFooterEditorStyles()
})

function handleOpenHeaderFontDialog() { emit('open-header-font-dialog') }
function handleOpenFooterFontDialog() { emit('open-footer-font-dialog') }

function handleHeaderMarginChange(value: number) {
  if (!isNaN(value)) {
    emit('update:header', { ...localHeader.value, margin: mmToPoint(value) })
    emit('header-margin-change')
  }
}

function handleFooterMarginChange(value: number) {
  if (!isNaN(value)) {
    emit('update:footer', { ...localFooter.value, margin: mmToPoint(value) })
    emit('footer-margin-change')
  }
}

function handleHeaderLeftChange(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:header', { ...localHeader.value, left: target.value })
  emit('header-footer-change')
}

function handleHeaderCenterChange(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:header', { ...localHeader.value, center: target.value })
  emit('header-footer-change')
}

function handleHeaderRightChange(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:header', { ...localHeader.value, right: target.value })
  emit('header-footer-change')
}

function handleFooterLeftChange(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:footer', { ...localFooter.value, left: target.value })
  emit('header-footer-change')
}

function handleFooterCenterChange(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:footer', { ...localFooter.value, center: target.value })
  emit('header-footer-change')
}

function handleFooterRightChange(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:footer', { ...localFooter.value, right: target.value })
  emit('header-footer-change')
}

function setHeaderEditorStyles() {
  const editors = [leftHeader.value, centerHeader.value, rightHeader.value]
  editors.forEach(editor => {
    if (editor) applyEditorStyle(editor, localHeader.value)
  })
}

function setFooterEditorStyles() {
  const editors = [leftFooter.value, centerFooter.value, rightFooter.value]
  editors.forEach(editor => {
    if (editor) applyEditorStyle(editor, localFooter.value)
  })
}

function applyEditorStyle(editor: HTMLTextAreaElement, style: any) {
  editor.style.fontFamily = style.fontFamily
  editor.style.fontSize = style.fontSize + 'pt'
  editor.style.color = `rgb(${style.forecolor})`
  editor.style.fontWeight = style.bold && style.bold !== 'false' ? 'bold' : 'normal'
  editor.style.fontStyle = style.italic && style.italic !== 'false' ? 'italic' : 'normal'
  editor.style.textDecoration = style.underline && style.underline !== 'false' ? 'underline' : 'none'
}
</script>

<style scoped>
.form-group-hf-desc {
  margin: 0 5px 10px 5px;
  color: #999999;
  font-size: 12px;
}

.editor-textarea {
  font-size: 10pt;
  padding: 5px;
  display: inline-block;
  width: 140px;
  height: 80px;
}

.label-align-top {
  vertical-align: top;
}

.span-align-top {
  margin-left: 15px;
  vertical-align: top;
}

.span-hf-margin {
  margin-left: 10px;
}

.btn-hf-setting {
  margin-left: 10px;
}

.div-footer-section {
  margin-top: 10px;
}
</style>
