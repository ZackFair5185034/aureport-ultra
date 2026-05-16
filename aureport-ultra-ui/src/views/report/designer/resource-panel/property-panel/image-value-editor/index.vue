<template>
  <div class="image-value-editor" ref="container">

    <div class="property-quote">
      {{ t('property.image.config') }}
    </div>

    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('property.image.width') + '(px)'">
        <u-input-number
          :placeholder="t('property.image.widthPlaceholder')"
          v-model="width"
          @change="handleWidthChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.image.height') + '(px)'">
        <u-input-number
          :placeholder="t('property.image.heightPlaceholder')"
          v-model="height"
          @change="handleHeightChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.image.source')">
        <u-select
          v-model="source"
          :clearable="true"
          style="width: 250px"
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

      <u-form-item class="property-label" :label="t('property.image.expand')" v-show="source === 'expression'">
        <u-radio-group
          v-model="expand"
          @change="handleExpandChange"
        >
          <u-radio
            v-for="option in [
              { value: 'Down', label: t('property.image.down') },
              { value: 'Right', label: t('property.image.right') },
              { value: 'None', label: t('property.image.noneExpand') }
            ]"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item class="property-label" :label="t('property.image.p')" v-show="source === 'text'">
        <u-input
          :title="t('property.image.tip')"
          :placeholder="t('property.image.tip')"
          style="width: 250px;"
          v-model="path"
          @change="handlePathChange"
        />
      </u-form-item>

      <div v-show="source === 'expression'">
        <u-form-item class="property-label" :label="t('property.image.expr')">
        </u-form-item>
        <div style="border: solid 1px #eeeeee;">
          <textarea ref="codeEditor"></textarea>
        </div>
      </div>
    </u-form>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, watch, nextTick, onBeforeUnmount, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import CodeMirror from 'codemirror'
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/lint/lint.js'
import { setDirty } from '@/utils/table'
import { scriptValidation } from '@/api/designer/index'
import { showAlert } from '@/utils/comnon'
import { deepCopy } from '@/components/utils/index'
import { getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager'

defineOptions({ name: 'ImageValueEditor' })

const { t } = useI18n()
const store = useReportStore()
const context = computed(() => store.context)

const props = withDefaults(defineProps<{
  rowIndex?: number
  colIndex?: number
  row2Index?: number
  col2Index?: number
}>(), {
  rowIndex: 0,
  colIndex: 0,
  row2Index: 0,
  col2Index: 0
})

const container = ref<HTMLDivElement | null>(null)
const codeEditor = ref<HTMLTextAreaElement | null>(null)

const codeMirror = ref<any>(null)
const initialized = ref(false)
const width = ref('')
const height = ref('')
const source = ref('text')
const path = ref('')
const expand = ref('None')

const sourceOptions = computed(() => [
  { value: 'text', label: t('property.image.path') },
  { value: 'expression', label: t('property.image.expr') }
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
  if (!textarea) return

  codeMirror.value = CodeMirror.fromTextArea(textarea, {
    mode: 'javascript',
    lineNumbers: true,
    gutters: ['CodeMirror-linenumbers', 'CodeMirror-lint-markers'],
    lint: {
      getAnnotations: buildScriptLintFunction(),
      async: true
    }
  })

  nextTick(() => {
    if (codeMirror.value) {
      codeMirror.value.refresh()
    }
  })
  codeMirror.value.setSize('auto', '120px')

  codeMirror.value.on('change', (cm: any) => {
    if (initialized.value) {
      return
    }
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
  initialized.value = true

  const currentCellDef = getCell(props.rowIndex, props.colIndex)
  if (!currentCellDef || !currentCellDef.value) return

  width.value = currentCellDef.value.width || ''
  height.value = currentCellDef.value.height || ''
  source.value = currentCellDef.value.source || 'text'

  path.value = ''
  if (source.value === 'text') {
    path.value = currentCellDef.value.value || ''
  } else {
    if (codeMirror.value) {
      let valueToSet = currentCellDef.value.value || ''
      if (valueToSet === 'undefined') {
        valueToSet = ''
      }
      codeMirror.value.setValue(valueToSet)
    }
  }

  expand.value = currentCellDef.expand || 'None'

  nextTick(() => {
    if (source.value === 'expression' && !codeMirror.value) {
      initCodeEditor()
    } else if (source.value === 'expression' && codeMirror.value) {
      let valueToSet = currentCellDef.value.value || ''
      if (valueToSet === 'undefined') {
        valueToSet = ''
      }
      codeMirror.value.setValue(valueToSet)
    }
    initialized.value = false
  })
}

function buildScriptLintFunction() {
  return async (text: string, updateLinting: Function, options: any, editor: any) => {
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
        for (let item of result) {
          item.from = { line: item.line - 1 }
          item.to = { line: item.line - 1 }
        }
        updateLinting(editor, result)
      } else {
        updateLinting(editor, [])
      }
    } catch (error) {
      console.error('Script validation error:', error)
      showAlert(t('property.base.syntaxError'))
    }
  }
}

function handleWidthChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.width = width.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }
  setDirty()
}

function handleHeightChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.height = height.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }
  setDirty()
}

function handleSourceChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.source = source.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }

  if (source.value === 'expression' && !codeMirror.value) {
    nextTick(() => {
      initCodeEditor()
    })
  }

  setDirty()
}

function handlePathChange() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (cellDef && cellDef.value) {
    const newCellDef = deepCopy(cellDef)
    newCellDef.value.value = path.value
    setCell(props.rowIndex, props.colIndex, newCellDef)
  }
  setDirty()
}

function handleExpandChange(expandVal: string) {
  const hot = TableManager.get()
  if (!hot) return
  expand.value = expandVal
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue
      const type = cellDef.value.type
      if (type === 'dataset' || type === 'expression' || type === 'image') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.expand = expandVal
        setCell(i, j, newCellDef)
      }
    }
  }
  hot.render()
  setDirty()
}
</script>

<style scoped>
</style>
