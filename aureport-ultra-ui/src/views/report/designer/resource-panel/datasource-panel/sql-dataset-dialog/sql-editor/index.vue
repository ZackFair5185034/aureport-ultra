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
      SQL(<span style="color: #999999;font-size: 12px;">{{ $t('dialog.sql.desc') }}：</span>)
      <textarea
        ref="sqlTextarea"
        placeholder="select username,dept_id from employee where dept_id=:deptId"
        class="form-control"
        rows="8"
        cols="30"
        style="width: 660px"
      ></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import CodeMirror from 'codemirror'
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/lint/lint.js'
import { showAlert } from '@/utils/comnon.js'
import { scriptValidation } from '@/api/designer'

defineOptions({ name: 'SqlEditor' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  name: string
  sql: string
}>(), {
  name: '',
  sql: ''
})

const emit = defineEmits<{
  (e: 'sql-change', sql: string): void
  (e: 'dataset-name-change', name: string): void
}>()

const sqlTextarea = ref<HTMLTextAreaElement | null>(null)
const datasetName = ref(props.name)
const codeMirror = ref<any>(null)
const isInternalUpdate = ref(false)

watch(() => props.name, (newVal) => {
  datasetName.value = newVal || ''
  setDatasetName(newVal)
})

watch(() => props.sql, (newVal) => {
  if (isInternalUpdate.value) {
    isInternalUpdate.value = false
    return
  }
  setSql(newVal || '')
})

onMounted(() => {
  nextTick(() => {
    initCodeMirror(props.sql)
  })
})

onBeforeUnmount(() => {
  if (codeMirror.value) {
    codeMirror.value.toTextArea()
    codeMirror.value = null
  }
})

function handleDatasetNameChange() {
  emit('dataset-name-change', getDatasetName())
}

function initCodeMirror(initialSql = '') {
  const textarea = sqlTextarea.value
  if (!textarea) return

  if (codeMirror.value) {
    codeMirror.value.setValue(initialSql || '')
    return
  }

  if (initialSql) {
    textarea.value = initialSql
  }

  codeMirror.value = CodeMirror.fromTextArea(textarea, {
    mode: 'javascript',
    lineNumbers: true,
    gutters: ['CodeMirror-linenumbers', 'CodeMirror-lint-markers'],
    lint: {
      getAnnotations: buildScriptLintFunction(),
      async: true
    },
    lineWrapping: true
  })
  codeMirror.value.setSize('660px', '204px')

  codeMirror.value.on('change', (cm: any, change: any) => {
    if (change.origin !== 'setValue') {
      isInternalUpdate.value = true
      emit('sql-change', getSql())
    }
  })

  if (initialSql) {
    codeMirror.value.setValue(initialSql)
  }
}

function buildScriptLintFunction() {
  return async function (text: string, updateLinting: Function, options: any, editor: any) {
    if (text === '') {
      updateLinting(editor, [])
      return
    }
    if (!text || text === '') {
      return
    }
    const prefix = text.substring(0, 2)
    const suffix = text.substring(text.length - 1, text.length)
    if (prefix === '${' && suffix === '}') {
      text = text.substring(2, text.length - 1)
    } else {
      return
    }

    try {
      const result = await scriptValidation(text) as any[]
      if (result) {
        for (let item of result) {
          item.from = { line: item.line - 1 }
          item.to = { line: item.line - 1 }
        }
        updateLinting(editor, result)
      } else {
        updateLinting(editor, [])
      }
    } catch (error: any) {
      if (error.msg) {
        showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
      } else {
        showAlert(t('dialog.sql.syntaxCheckError'))
      }
      updateLinting(editor, [])
    }
  }
}

function getDatasetName() {
  return datasetName.value
}

function setDatasetName(name: string) {
  datasetName.value = name || ''
}

function getSql() {
  if (codeMirror.value) {
    return codeMirror.value.getValue()
  }
  const textarea = sqlTextarea.value
  if (textarea) {
    return textarea.value
  }
  return ''
}

function setSql(sql: string) {
  if (codeMirror.value) {
    codeMirror.value.setValue(sql || '')
  } else {
    const textarea = sqlTextarea.value
    if (textarea) {
      textarea.value = sql || ''
    }
  }
}
</script>

<style scoped>
.sql-editor-container {
}
</style>
