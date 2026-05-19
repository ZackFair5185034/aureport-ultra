<script setup lang="ts">
import CodeMirror from 'codemirror'

import { useI18n } from 'vue-i18n'
import { scriptValidation } from '@/api/designer/index'
import { deepCopy } from '@/components/utils/index'
import { useReportStore } from '@/stores/report'
import { showAlert } from '@/utils/comnon'
import { getCell, setCell } from '@/utils/contextActions'
import { setDirty } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/lint/lint.js'

defineOptions({ name: 'ZxingValueEditor' })

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
const context = computed(() => store.context)

const container = ref<HTMLDivElement | null>(null)
const codeEditor = ref<HTMLTextAreaElement | null>(null)

const codeMirror = ref<any>(null)
const width = ref(100)
const height = ref(100)
const format = ref('QR_CODE')
const source = ref('text')
const textValue = ref('')
const expand = ref('None')
const showFormat = ref(true)

const formatOptions = computed(() => [
  { value: 'AZTEC', label: 'AZTEC' },
  { value: 'CODABAR', label: 'CODABAR' },
  { value: 'CODE_39', label: 'CODE_39' },
  { value: 'CODE_93', label: 'CODE_93' },
  { value: 'CODE_128', label: 'CODE_128' },
  { value: 'DATA_MATRIX', label: 'DATA_MATRIX' },
  { value: 'EAN_8', label: 'EAN_8' },
  { value: 'EAN_13', label: 'EAN_13' },
  { value: 'ITF', label: 'ITF' },
  { value: 'PDF_417', label: 'PDF_417' },
  { value: 'UPC_E', label: 'UPC_E' },
  { value: 'UPC_A', label: 'UPC_A' },
])

const sourceOptions = computed(() => [
  { value: 'text', label: t('property.zxing.text') },
  { value: 'expression', label: t('property.zxing.expr') },
])

watch(() => [props.rowIndex, props.colIndex], () => {
  loadCellData()
}, { immediate: true })

onBeforeUnmount(() => {
  if (codeMirror.value) {
    codeMirror.value.toTextArea()
    codeMirror.value = null
  }
})

function initCodeEditor() {
  const textarea = codeEditor.value
  if (!textarea)
    return

  codeMirror.value = CodeMirror.fromTextArea(textarea, {
    mode: 'javascript',
    lineNumbers: true,
    gutters: ['CodeMirror-linenumbers', 'CodeMirror-lint-markers'],
    lint: {
      getAnnotations: buildScriptLintFunction(),
      async: true,
    },
    lineWrapping: true,
    viewportMargin: Infinity,
    indentWithTabs: false,
    tabSize: 2,
    smartIndent: true,
    cursorScrollMargin: 10,
  })

  nextTick(() => {
    if (codeMirror.value) {
      codeMirror.value.refresh()
    }
  })
  codeMirror.value.setSize('auto', '120px')

  codeMirror.value.on('change', (cm: any) => {
    const expr = cm.getValue()
    if (expr === 'undefined' || expr === undefined || expr === null) {
      return
    }

    const cellDef = getCell(props.rowIndex, props.colIndex)
    if (cellDef && cellDef.value) {
      const newCellDef = deepCopy(cellDef)
      newCellDef.value.value = expr
      setCell(props.rowIndex, props.colIndex, newCellDef)
    }

    setDirty()
  })

  loadCellData()
}

function loadCellData() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef) return

  width.value = cellDef.value.width || 100
  height.value = cellDef.value.height || 100
  format.value = cellDef.value.format || 'QR_CODE'
  source.value = cellDef.value.source || 'text'
  textValue.value = cellDef.value.value || ''
  expand.value = cellDef.expand || 'None'
  showFormat.value = cellDef.value.category !== 'qrcode'

  if (source.value === 'expression') {
    nextTick(() => {
      if (codeMirror.value) {
        let valueToSet = cellDef.value.value || ''
        if (valueToSet === 'undefined') {
          valueToSet = ''
        }

        codeMirror.value.setValue(valueToSet)
        codeMirror.value.refresh()
      }
      else {
        initCodeEditor()
      }
    })
  }
}

async function lintCallback(text: string, updateLinting: (editor: any, annotations: any[]) => void, _options: any, editor: any) {
  if (text === '') {
    updateLinting(editor, [])
    return
  }

  if (!text || text === '') {
    return
  }

  try {
    const result = await scriptValidation(text)
    if (result) {
      for (const item of result as any[]) {
        item.from = { line: item.line - 1 }
        item.to = { line: item.line - 1 }
      }

      updateLinting(editor, result as any[])
    }
    else {
      updateLinting(editor, [])
    }
  }
  catch {
    showAlert(t('property.base.syntaxError'))
  }
}

function buildScriptLintFunction() {
  return lintCallback
}

function handleWidthChange() {
  if (isNaN(width.value)) {
    showAlert(t('property.zxing.numberTip'))
    return
  }

  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.width = width.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
    const hot = TableManager.get()
    if (hot) {
      hot.render()
    }

    setDirty()
  }
}

function handleHeightChange() {
  if (isNaN(height.value)) {
    showAlert(t('property.zxing.numberTip'))
    return
  }

  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.height = height.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
    const hot = TableManager.get()
    if (hot) {
      hot.render()
    }

    setDirty()
  }
}

function handleFormatChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.format = format.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
    setDirty()
  }
}

function handleSourceChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.source = source.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
    setDirty()

    if (source.value === 'expression') {
      nextTick(() => {
        if (codeMirror.value) {
          const currentCellDef = getCell(props.rowIndex, props.colIndex)
          codeMirror.value.setValue(currentCellDef?.value?.value || '')
          codeMirror.value.refresh()
        }
        else {
          initCodeEditor()
        }
      })
    }
  }
}

function handleTextChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.value = textValue.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
    setDirty()
  }
}

function handleExpandChange(expandVal: string) {
  const hot = TableManager.get()
  if (!hot)
    return
  expand.value = expandVal

  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.expand = expandVal
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }

  hot.render()
  setDirty()
}
</script>

<template>
  <div ref="container" class="zxing-value-editor">
    <div class="property-quote">
      {{ t('property.zxing.config') }}
    </div>

    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('property.zxing.width')">
        <u-input-number
          v-model="width"
          @change="handleWidthChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.zxing.height')">
        <u-input-number
          v-model="height"
          @change="handleHeightChange"
        />
      </u-form-item>

      <u-form-item v-show="showFormat" class="property-label" :label="t('property.zxing.format')">
        <u-select
          v-model="format"
          :clearable="true"
          @change="handleFormatChange"
        >
          <u-option
            v-for="option in formatOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.zxing.source')">
        <u-select
          v-model="source"
          :clearable="true"
          @change="handleSourceChange"
        >
          <u-option
            v-for="option in sourceOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item v-show="source === 'expression'" class="property-label" :label="t('property.zxing.expand')">
        <u-radio-group
          v-model="expand"
          @change="handleExpandChange"
        >
          <u-radio
            v-for="option in [
              { value: 'Down', label: t('property.zxing.down') },
              { value: 'Right', label: t('property.zxing.right') },
              { value: 'None', label: t('property.zxing.noneExpand') },
            ]"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item v-show="source === 'text'" class="property-label" :label="t('property.zxing.text1')">
        <u-input
          v-model="textValue"
          style="width: 250px;"
          @change="handleTextChange"
        />
      </u-form-item>

      <u-form-item v-show="source === 'expression'" class="property-label" :label="t('property.zxing.expr')">
        <div style="border: solid 1px #eeeeee;">
          <textarea ref="codeEditor" />
        </div>
      </u-form-item>
    </u-form>
  </div>
</template>

<style scoped>
</style>
