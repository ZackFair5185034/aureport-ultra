<script setup lang="ts">
import CodeMirror from 'codemirror'
import 'codemirror/lib/codemirror.css'
import 'codemirror/mode/sql/sql.js'

import { useI18n } from 'vue-i18n'
import { format as formatSql } from 'sql-formatter'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'SqlEditor' })

const props = withDefaults(defineProps<{
  name?: string
  sql?: string
}>(), {
  name: '',
  sql: '',
})

const emit = defineEmits<{
  (e: 'sql-change', sql: string): void
  (e: 'dataset-name-change', name: string): void
}>()

const { t } = useI18n()

const datasetName = ref(props.name)
const container = ref<HTMLDivElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
let cm: any = null

defineExpose({
  setSqlContent(sql: string) {
    sql = sql || ''
    if (cm) {
      cm.setValue(sql)
    }
    emit('sql-change', sql)
  },
  refreshEditor() {
    if (cm) {
      cm.refresh()
    }
  },
})

watch(() => props.name, (newVal) => {
  datasetName.value = newVal || ''
})

watch(() => props.sql, (newVal) => {
  if (cm) {
    const val = newVal || ''
    if (val !== cm.getValue()) {
      cm.setValue(val)
    }
  }
})

onMounted(() => {
  const textarea = textareaRef.value
  if (!textarea) return

  cm = CodeMirror.fromTextArea(textarea, {
    mode: 'text/x-mysql',
    lineNumbers: true,
    lineWrapping: true,
    viewportMargin: Infinity,
    indentWithTabs: false,
    tabSize: 2,
    smartIndent: true,
    cursorScrollMargin: 10,
  })
  cm.setSize('100%', '204px')

  if (props.sql) {
    cm.setValue(props.sql)
  }

  cm.on('change', () => {
    const value = cm.getValue()
    emit('sql-change', value)
  })
})

onBeforeUnmount(() => {
  if (cm) {
    cm.toTextArea()
    cm = null
  }
})

function handleDatasetNameChange() {
  emit('dataset-name-change', datasetName.value)
}

function handleFormatSql() {
  if (!cm) return
  const sql = cm.getValue()
  if (!sql) return
  try {
    const formatted = formatSql(sql, { language: 'sql', tabWidth: 2, useTabs: false })
    cm.setValue(formatted)
    emit('sql-change', formatted)
  }
  catch {
    showAlert(t('dialog.sql.formatFail'))
  }
}
</script>

<template>
  <div class="sql-editor-container">
    <div class="row" style="margin: 10px;">
      {{ $t('dialog.sql.datasetName') }}：
      <div class="u-inline">
        <u-input
          v-model="datasetName"
          style="width:500px;"
          @input="handleDatasetNameChange"
        />
      </div>
    </div>

    <div class="row" style="margin:10px;">
      <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px;">
        <span>SQL(<span style="color: #999999;font-size: 12px;">{{ $t('dialog.sql.desc') }}：</span>)</span>
        <i class="iconfont icon-font-code sql-format-btn" @click="handleFormatSql" />
      </div>
      <textarea ref="textareaRef" />
    </div>
  </div>
</template>

<style scoped>
.sql-format-btn {
  font-size: 14px;
  cursor: pointer;
  color: #606266;
  padding: 2px;
  border-radius: 3px;
  transition: all 0.2s;
}
.sql-format-btn:hover {
  color: #00554a;
  background: #e8f4f0;
}

.sql-editor-container :deep(.CodeMirror) {
  border: 1px solid #d8d8d8;
  border-radius: 4px;
  height: auto;
  min-height: 204px;
}
</style>
