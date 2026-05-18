<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils'
import { useReportStore } from '@/stores/report'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'RichTextEditor' })

const props = withDefaults(defineProps<{
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
}>(), {
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0,
})
const { t } = useI18n()
const store = useReportStore()

const content = ref('')
const editorRef = ref<HTMLElement | null>(null)

// 状态追踪
const isBold = ref(false)
const isItalic = ref(false)
const isUnderline = ref(false)

function loadCellData() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef) {
    content.value = ''
    updateEditorContent('')
    return
  }

  if (cellDef && cellDef.value && cellDef.value.value !== undefined) {
    content.value = cellDef.value.value
    updateEditorContent(content.value)
    updateToolbarState()
  }
  else {
    content.value = ''
    updateEditorContent('')
  }
}

function updateEditorContent(html: string) {
  if (editorRef.value) {
    editorRef.value.innerHTML = html || ''
  }
}

function updateToolbarState() {
  if (editorRef.value) {
    isBold.value = document.queryCommandState('bold')
    isItalic.value = document.queryCommandState('italic')
    isUnderline.value = document.queryCommandState('underline')
  }
}

function toggleBold() {
  document.execCommand('bold', false)
  isBold.value = document.queryCommandState('bold')
  onContentChange()
}

function toggleItalic() {
  document.execCommand('italic', false)
  isItalic.value = document.queryCommandState('italic')
  onContentChange()
}

function toggleUnderline() {
  document.execCommand('underline', false)
  isUnderline.value = document.queryCommandState('underline')
  onContentChange()
}

function setColor(color: string) {
  document.execCommand('foreColor', false, color)
  onContentChange()
}

function setHighlight(color: string) {
  document.execCommand('hiliteColor', false, color)
  onContentChange()
}

function onContentChange() {
  const html = editorRef.value?.innerHTML || ''
  content.value = html

  const cellDef = getCell(props.rowIndex, props.colIndex)
  const newCellDef = deepCopy(cellDef)

  if (newCellDef) {
    if (!newCellDef.value) {
      newCellDef.value = { type: 'richtext', value: '' }
    }

    newCellDef.value.type = 'richtext'
    newCellDef.value.value = html
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }

  // 更新 Handsontable 显示
  const hot = TableManager.get()
  if (hot && props.rowIndex !== null && props.colIndex !== null) {
    // 富文本在 Handsontable 中显示纯文本
    const plainText = editorRef.value?.textContent || ''
    hot.setDataAtCell(props.rowIndex, props.colIndex, plainText)
  }

  setDirty()
}

// 监听选择变化，更新工具栏状态
function handleSelectionChange() {
  updateToolbarState()
}

onMounted(() => {
  loadCellData()
  document.addEventListener('selectionchange', handleSelectionChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('selectionchange', handleSelectionChange)
})

watch(
  () => props.rowIndex,
  () => { loadCellData() },
)
watch(
  () => props.colIndex,
  () => { loadCellData() },
)
</script>

<template>
  <div class="rich-text-editor">
    <div class="property-quote">
      {{ t('property.richtext.config') }}
    </div>

    <div class="toolbar">
      <button class="tool-btn" :class="{ active: isBold }" title="加粗" @click="toggleBold">
        <b>B</b>
      </button>
      <button class="tool-btn" :class="{ active: isItalic }" title="斜体" @click="toggleItalic">
        <i>I</i>
      </button>
      <button class="tool-btn" :class="{ active: isUnderline }" title="下划线" @click="toggleUnderline">
        <u>U</u>
      </button>
      <span class="separator" />
      <button class="tool-btn" title="红色" @click="setColor('#FF0000')">
        <span style="color: #FF0000">A</span>
      </button>
      <button class="tool-btn" title="绿色" @click="setColor('#00AA00')">
        <span style="color: #00AA00">A</span>
      </button>
      <button class="tool-btn" title="蓝色" @click="setColor('#0066CC')">
        <span style="color: #0066CC">A</span>
      </button>
      <span class="separator" />
      <button class="tool-btn" title="黄色背景" @click="setHighlight('#FFFF00')">
        <span style="background: #FFFF00">A</span>
      </button>
      <button class="tool-btn" title="浅绿背景" @click="setHighlight('#90EE90')">
        <span style="background: #90EE90">A</span>
      </button>
    </div>

    <div ref="editorRef" class="editor-content" contenteditable="true" @input="onContentChange" @blur="onContentChange" />

    <div class="tip">
      {{ t('property.richtext.tip') }}
    </div>
  </div>
</template>

<style scoped>
.rich-text-editor {
  padding: 8px;
}

.property-quote {
  color: #666;
  font-size: 12px;
  margin-bottom: 10px;
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  padding: 6px;
  background: #fafafa;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.tool-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #d0d0d0;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.tool-btn:hover {
  background: #f0f0f0;
  border-color: #b0b0b0;
}

.tool-btn.active {
  background: #e0e0e0;
  border-color: #a0a0a0;
}

.separator {
  width: 1px;
  height: 20px;
  background: #d0d0d0;
  margin: 0 4px;
}

.editor-content {
  min-height: 80px;
  max-height: 150px;
  overflow-y: auto;
  padding: 8px;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  background: #fff;
  font-size: 14px;
  line-height: 1.5;
}

.editor-content:focus {
  outline: none;
  border-color: #409eff;
}

.editor-content:empty::before {
  content: attr(data-placeholder);
  color: #aaa;
}

.editor-content b {
  font-weight: bold;
}

.editor-content i {
  font-style: italic;
}

.editor-content u {
  text-decoration: underline;
}

.tip {
  margin-top: 8px;
  color: #999;
  font-size: 11px;
}
</style>
