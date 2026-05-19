<script setup lang="ts">

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
const editorValue = ref(props.sql)
const editorRef = ref<any>(null)

defineExpose({
  setSqlContent(sql: string) {
    editorValue.value = sql || ''
  },
  refreshEditor() {
    // handled by automaticLayout
  },
})

watch(() => props.name, (newVal) => {
  datasetName.value = newVal || ''
})

watch(() => props.sql, (newVal) => {
  if (newVal !== editorValue.value) {
    editorValue.value = newVal || ''
  }
})

function handleEditorChange(value: string) {
  emit('sql-change', value)
}

function handleDatasetNameChange() {
  emit('dataset-name-change', datasetName.value)
}

function handleFormatSql() {
  const sql = editorValue.value
  if (!sql) return
  try {
    const formatted = formatSql(sql, { language: 'sql', tabWidth: 2, useTabs: false })
    editorValue.value = formatted
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
        <u-button type="info" size="mini" icon="icon-font-code" @click="handleFormatSql" />
      </div>
      <Editor
        ref="editorRef"
        v-model="editorValue"
        :options="{
          language: 'sql',
          theme: 'vs',
          fontSize: 13,
          lineNumbers: 'on',
          roundedSelection: true,
          scrollBeyondLastLine: false,
          automaticLayout: true,
          wordWrap: 'on',
          minimap: { enabled: false },
          scrollbar: {
            verticalScrollbarSize: 8,
            horizontalScrollbarSize: 8,
          },
        }"
        style="width:660px;height:204px;"
        @change="handleEditorChange"
      />
    </div>
  </div>
</template>

<style scoped>
.sql-editor-container {
}
</style>