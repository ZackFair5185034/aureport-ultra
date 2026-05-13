<template>
  <div class="expression-value-editor" ref="container" >
    <u-form :label-width="100" labelPosition="left">

      <div class="property-quote">
        {{ t('property.expr.config') }}
      </div>

      <!-- 换行计算选项 -->
      <u-form-item class="property-label" :label="t('property.base.newLineCompute')">
        <u-radio-group
            v-model="wrapCompute"
            @change="handleWrapComputeChange"
        >
          <u-radio
              v-for="option in [
              { value: 'default', label: t('property.base.open') },
              { value: 'custom', label: t('property.base.close') }
            ]"
              :key="option.value"
              :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <!-- 展开选项 -->
      <u-form-item class="property-label" :label="t('property.expr.expand')">
        <u-radio-group
            v-model="expand"
            @change="handleExpandChange"
        >
          <u-radio
              v-for="option in expandOptions"
              :key="option.value"
              :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <!-- 格式化输入框 -->
      <u-form-item class="property-label" :label="t('property.base.format')">
        <vue-simple-suggest
            v-model="format"
            :list="suggestionList"
            :filter-by-query="true"
            :placeholder="t('property.base.formatTip')"
            class="simple-suggest"
            @input="handleFormatChange"
        ></vue-simple-suggest>
      </u-form-item>

      <!-- 条件属性配置 -->
      <u-form-item class="property-label" :label="t('property.base.conditionProp')">
        <u-button
            type="info"
            icon="icon-filter"
            @click="handleConditionPropertyConfig"
        >
          {{ t('property.base.configCondition') }}
        </u-button>
      </u-form-item>

      <!-- 表达式编辑器 -->
      <u-form-item class="property-label" :label="t('property.expr.expr')">
      </u-form-item>
      <div style="border: solid 1px #eeeeee;">
        <textarea ref="codeEditor"></textarea>
      </div>
    </u-form>

    <!-- 条件属性对话框 -->
    <PropertyConditionDialog
        v-model:visible="propertyConditionDialogVisible"
        :dataset-name="propertyConditionDialogDatasetName"
        :condition-property-items="propertyConditionDialogItems"
        @saveAfter="handlePropertyConditionSave"
    />
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
import { setDirty } from '@/utils/table.js'
import { scriptValidation, parseDatasetName } from '@/api/designer/index.js'
import PropertyConditionDialog from '@/views/report/designer/resource-panel/property-panel/property-condition-dialog/index.vue'
import VueSimpleSuggest from 'vue-simple-suggest'
import 'vue-simple-suggest/dist/styles.css'
import { showAlert } from '@/utils/comnon.js'
import { deepCopy } from '@/components/utils/index.js'
import { getCell, setCell } from '@/utils/contextActions'
import TableManager from '@/views/report/designer/edit-table/manager.js'

defineOptions({ name: 'ExpressionValueEditor' })

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
const wrapCompute = ref('default')
const expand = ref('None')
const format = ref('')
const suggestionList = ref<string[]>([
  "yyyy/MM/dd", "yyyy/MM", "yyyy-MM", "yyyy",
  "yyyy-MM-dd HH:mm:ss", "yyyy年MM月dd日 HH:mm:ss",
  "yyyy-MM-dd", "yyyy年MM月dd日", "HH:mm", "HH:mm:ss",
  "#.##", "#.00", "##.##%", "##.00%", "##,###.##",
  "￥##,###.##", "$##,###.##", "0.00E00", "##0.0E0"
])
const loadingCellData = ref(false)
const propertyConditionDialogVisible = ref(false)
const propertyConditionDialogDatasetName = ref('')
const propertyConditionDialogItems = ref<any[]>([])

const expandOptions = computed(() => [
  { value: 'Down', label: t('property.dataset.down') },
  { value: 'Right', label: t('property.dataset.right') },
  { value: 'None', label: t('property.dataset.noneExpand') }
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
    },
    lineWrapping: true,
    viewportMargin: Infinity,
    indentWithTabs: false,
    tabSize: 2,
    smartIndent: true,
    cursorScrollMargin: 10
  })

  nextTick(() => {
    if (codeMirror.value) {
      codeMirror.value.refresh()
    }
  })
  codeMirror.value.setSize('auto', '160px')

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
    const hot = TableManager.get()
    if (hot) {
      hot.setDataAtCell(props.rowIndex, props.colIndex, expr)
    }
    setDirty()
  })

  loadCellData()
}

function loadCellData() {
  if (loadingCellData.value) return

  const cellDef = getCell(props.rowIndex, props.colIndex)

  if (codeMirror.value && cellDef && cellDef.value) {
    loadingCellData.value = true
    let valueToSet = cellDef.value.value || ''
    if (valueToSet === 'undefined') {
      valueToSet = ''
    }
    codeMirror.value.setValue(valueToSet)
    nextTick(() => {
      loadingCellData.value = false
    })
  }

  if (cellDef && cellDef.expand) {
    expand.value = cellDef.expand
  }

  if (cellDef && cellDef.cellStyle && cellDef.cellStyle.format) {
    format.value = cellDef.cellStyle.format
  } else {
    format.value = ''
  }

  if (cellDef && cellDef.cellStyle && cellDef.cellStyle.wrapCompute) {
    wrapCompute.value = 'default'
  } else {
    wrapCompute.value = 'custom'
  }

  nextTick(() => {
    initialized.value = true
    if (!codeMirror.value) {
      initCodeEditor()
    } else {
      codeMirror.value.refresh()
    }
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

function handleExpandChange(expandVal: string) {
  const hot = TableManager.get()
  if (!hot) return
  expand.value = expandVal
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue
      const type = cellDef.value.type
      if (type === 'dataset' || type === 'expression') {
        const newCellDef = deepCopy(cellDef)
        newCellDef.expand = expandVal
        setCell(i, j, newCellDef)
      }
    }
  }
  hot.render()
  setDirty()
}

function handleWrapComputeChange() {
  const wrapComputeValue = wrapCompute.value === 'default'
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue
      const newCellDef = deepCopy(cellDef)
      if (!newCellDef.cellStyle) {
        newCellDef.cellStyle = {}
      }
      newCellDef.cellStyle.wrapCompute = wrapComputeValue
      setCell(i, j, newCellDef)
    }
  }
  setDirty()
}

function handleFormatChange(formatVal: string) {
  const hot = TableManager.get()
  if (!hot) return
  format.value = formatVal
  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      const cellDef = getCell(i, j)
      if (!cellDef) continue
      const newCellDef = deepCopy(cellDef)
      if (!newCellDef.cellStyle) {
        newCellDef.cellStyle = {}
      }
      newCellDef.cellStyle.format = formatVal
      setCell(i, j, newCellDef)
    }
  }
  setDirty()
}

async function handleConditionPropertyConfig() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef) return

  const conditionPropertyItems = cellDef.conditionPropertyItems
      ? deepCopy(cellDef.conditionPropertyItems)
      : []

  let datasetName = ''
  const expr = codeMirror.value ? codeMirror.value.getValue() : ''

  if (expr && expr !== '') {
    try {
      const result = await parseDatasetName(expr)
      datasetName = result.datasetName
    } catch (error) {
      console.error('Parse dataset name error:', error)
    }
  }

  showPropertyConditionDialog(datasetName, conditionPropertyItems)
}

function showPropertyConditionDialog(datasetName: string, conditionPropertyItems: any[]) {
  propertyConditionDialogDatasetName.value = datasetName
  propertyConditionDialogItems.value = conditionPropertyItems
  propertyConditionDialogVisible.value = true
}

function handlePropertyConditionSave(propertyConditions: any[]) {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef) return

  const newCellDef = deepCopy(cellDef)
  newCellDef.conditionPropertyItems = deepCopy(propertyConditions)

  for (let i = props.rowIndex; i <= props.row2Index; i++) {
    for (let j = props.colIndex; j <= props.col2Index; j++) {
      setCell(i, j, newCellDef)
    }
  }
  setDirty()
}
</script>

<style scoped>
.simple-suggest :deep(.default-input){
  display: inline-block !important;
  width: 250px !important;
  height: 35px;
}
</style>
