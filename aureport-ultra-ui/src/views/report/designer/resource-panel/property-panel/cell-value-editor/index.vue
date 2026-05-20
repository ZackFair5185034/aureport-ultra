<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils/index'
import { showAlert } from '@/utils/comnon'
import { getCell, getCellName, setCell } from '@/utils/contextActions'
import { setDirty } from '@/utils/table'
import TableManager from '@/views/report/designer/edit-table/manager'
import URLParameterDialog from '@/views/report/designer/resource-panel/property-panel/url-parameter-dialog/index.vue'

defineOptions({ name: 'CellValueEditor' })

const props = withDefaults(defineProps<{
  showParentGroup?: boolean
  showRendererGroup?: boolean
  showLinkGroup?: boolean
  showTypeGroup?: boolean
  rowIndex?: number
  colIndex?: number
}>(), {
  showParentGroup: false,
  showRendererGroup: false,
  showLinkGroup: false,
  showTypeGroup: false,
  rowIndex: 0,
  colIndex: 0,
})

const emit = defineEmits<{
  (e: 'select-renderer'): void
  (e: 'cell-type-change', value: string): void
}>()

const { t } = useI18n()

const parentGroup = ref<HTMLDivElement | null>(null)
const rendererGroup = ref<HTMLDivElement | null>(null)
const urlParameterDialogVisible = ref(false)
const leftParentCellNameOptions = ref<any[]>([])
const leftParentRowNumberOptions = ref<number[]>([])
const topParentCellNameOptions = ref<any[]>([])
const topParentRowNumberOptions = ref<number[]>([])
const leftParentType = ref('default')
const topParentType = ref('default')
const leftParentCellName = ref('')
const leftParentRowNumber = ref('')
const topParentCellName = ref('')
const topParentRowNumber = ref('')
const rendererBean = ref('')
const linkUrl = ref('')
const linkTarget = ref('_blank')
const cellType = ref('simple')
const linkParameters = ref<any[]>([])

const parentTypeOptions = computed(() => [
  { label: t('property.prop.default'), value: 'default' },
  { label: t('property.prop.custom'), value: 'custom' },
])

const leftParentRowNumberOptionsFormatted = computed(() =>
  leftParentRowNumberOptions.value.map(num => ({
    label: String(num),
    value: num.toString(),
  })),
)

const topParentRowNumberOptionsFormatted = computed(() =>
  topParentRowNumberOptions.value.map(num => ({
    label: String(num),
    value: num.toString(),
  })),
)

const linkTargetOptions = computed(() => [
  { label: t('property.prop.newWindow'), value: '_blank' },
  { label: t('property.prop.currentWindow'), value: '_self' },
  { label: t('property.prop.parentWindow'), value: '_parent' },
  { label: t('property.prop.topWindow'), value: '_top' },
])

const cellTypeOptions = computed(() => [
  { label: t('property.prop.text'), value: 'simple' },
  { label: t('property.prop.expr'), value: 'expression' },
  { label: t('property.prop.dataset'), value: 'dataset' },
  { label: t('property.prop.image'), value: 'image' },
  { label: t('property.prop.slash'), value: 'slash' },
  { label: t('property.prop.qrcode'), value: 'qrcode' },
  { label: t('property.prop.barcode'), value: 'barcode' },
  { label: t('property.prop.chart'), value: 'chart' },
])

watch(() => [props.rowIndex, props.colIndex], () => {
  buildParentCellNameOptions()
  buildParentRowNumberOptions()
  updateLinkParameters()
})

onMounted(() => {
  buildParentCellNameOptions()
  buildParentRowNumberOptions()
  updateLinkParameters()
})

function updateLinkParameters() {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  linkParameters.value = cellDef && cellDef.linkParameters ? cellDef.linkParameters : []
}

function buildParentCellNameOptions() {
  const hot = TableManager.get()
  const countCols = hot.countCols()
  const cellDef = getCell(props.rowIndex, props.colIndex)

  leftParentCellNameOptions.value = [{ value: 'root', label: t('property.prop.none') }]
  topParentCellNameOptions.value = [{ value: 'root', label: t('property.prop.none') }]

  for (let j = 0; j < countCols; j++) {
    const name = getCellName(null, j)
    leftParentCellNameOptions.value.push({ value: name, label: name })
    topParentCellNameOptions.value.push({ value: name, label: name })
  }

  if (cellDef && cellDef.leftParentCellName) {
    leftParentType.value = 'custom'
    const name = cellDef.leftParentCellName
    if (name === 'root') {
      leftParentCellName.value = 'root'
      leftParentRowNumber.value = ''
    }
    else {
      const data = parseCellName(name)
      leftParentCellName.value = data.name
      leftParentRowNumber.value = data.num
    }
  }
  else {
    leftParentType.value = 'default'
    if (props.colIndex === 0) {
      leftParentCellName.value = 'root'
      leftParentRowNumber.value = ''
    }
    else {
      let row = props.rowIndex
      let col = props.colIndex - 1
      const hot = TableManager.get()
      const td = hot.getCell(row, col)
      if (isCellHidden(td)) {
        const mergeCells = hot.getSettings().mergeCells
        for (const item of mergeCells) {
          const rowStart = item.row
          const rowspan = item.rowspan
          const colStart = item.col
          const colspan = item.colspan
          const rowEnd = rowStart + rowspan - 1
          const colEnd = colStart + colspan - 1
          if (row >= rowStart && row <= rowEnd && col >= colStart && col <= colEnd) {
            row = rowStart
            col = colStart
            break
          }
        }
      }

      const cellName = getCellName(row, col)
      const data = parseCellName(cellName)
      leftParentCellName.value = data.name
      leftParentRowNumber.value = data.num
    }
  }

  if (cellDef && cellDef.topParentCellName) {
    topParentType.value = 'custom'
    const name = cellDef.topParentCellName
    if (name === 'root') {
      topParentCellName.value = 'root'
      topParentRowNumber.value = ''
    }
    else {
      const data = parseCellName(name)
      topParentCellName.value = data.name
      topParentRowNumber.value = data.num
    }
  }
  else {
    topParentType.value = 'default'
    if (props.rowIndex === 0) {
      topParentCellName.value = 'root'
      topParentRowNumber.value = ''
    }
    else {
      let row = props.rowIndex - 1
      let col = props.colIndex
      const hot = TableManager.get()
      const td = hot.getCell(row, col)
      if (isCellHidden(td)) {
        const mergeCells = hot.getSettings().mergeCells
        for (const item of mergeCells) {
          const rowStart = item.row
          const rowspan = item.rowspan
          const colStart = item.col
          const colspan = item.colspan
          const rowEnd = rowStart + rowspan - 1
          const colEnd = colStart + colspan - 1
          if (row >= rowStart && row <= rowEnd && col >= colStart && col <= colEnd) {
            row = rowStart
            col = colStart
            break
          }
        }
      }

      const cellName = getCellName(row, col)
      const data = parseCellName(cellName)
      topParentCellName.value = data.name
      topParentRowNumber.value = data.num
    }
  }

  rendererBean.value = cellDef && cellDef.cellStyle && cellDef.cellStyle.renderer ? cellDef.cellStyle.renderer : ''

  if (cellDef) {
    linkUrl.value = cellDef.linkUrl || ''
    linkTarget.value = cellDef.linkTargetWindow || '_blank'
  }
  else {
    linkUrl.value = ''
    linkTarget.value = '_blank'
  }

  if (cellDef && cellDef.value) {
    const type = cellDef.value.type || 'simple'
    cellType.value = type === 'zxing' ? (cellDef.value.category || '') : type
  }
  else {
    cellType.value = 'simple'
  }
}

function buildParentRowNumberOptions() {
  const hot = TableManager.get()
  const countRows = hot.countRows()

  leftParentRowNumberOptions.value = []
  topParentRowNumberOptions.value = []

  for (let j = 0; j < countRows; j++) {
    leftParentRowNumberOptions.value.push(j + 1)
    topParentRowNumberOptions.value.push(j + 1)
  }
}

function handleLeftParentTypeChange(value: string) {
  if (value === 'default') {
    setParentCell(null, true)
  }
}

function handleLeftParentCellNameChange(value: string) {
  if (value === 'root') {
    setParentCell('root', true)
  }
  else {
    const num = leftParentRowNumber.value
    if (value !== '' && num !== '') {
      setParentCell(value + num.toString(), true)
    }
  }
}

function handleLeftParentRowNumberChange(value: string) {
  const name = leftParentCellName.value
  if (name === 'root') {
    setParentCell('root', true)
  }
  else {
    if (name !== '' && value !== '') {
      setParentCell(name + value.toString(), true)
    }
  }
}

function handleTopParentTypeChange(value: string) {
  if (value === 'default') {
    setParentCell(null, false)
  }
}

function handleTopParentCellNameChange(value: string) {
  if (value === 'root') {
    setParentCell('root', false)
  }
  else {
    const num = topParentRowNumber.value
    if (value !== '' && num !== '') {
      setParentCell(value + num.toString(), false)
    }
  }
}

function handleTopParentRowNumberChange(value: string) {
  const name = topParentCellName.value
  if (name === 'root') {
    setParentCell('root', false)
  }
  else {
    if (name !== '' && value !== '') {
      setParentCell(name + value.toString(), false)
    }
  }
}

function setParentCell(parentCellName: string | null, isLeft: boolean) {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef)
    return
  const newCellDef = deepCopy(cellDef)
  if (isLeft) {
    newCellDef.leftParentCellName = parentCellName ?? undefined
  }
  else {
    newCellDef.topParentCellName = parentCellName ?? undefined
  }

  setCell(props.rowIndex, props.colIndex, newCellDef)
  setDirty()
}

function isCellHidden(td: any): boolean {
  return td && td.style && td.style.display === 'none'
}

function parseCellName(cellName: string): { name: string, num: string } {
  let pos = -1
  for (let i = 0; i < cellName.length; i++) {
    const char = cellName.charAt(i)
    const num = parseInt(char)
    if (!isNaN(num)) {
      pos = i
      break
    }
  }

  const name = cellName.slice(0, Math.max(0, pos))
  const num = cellName.slice(pos)
  return { name, num: num.toString() }
}

function handleRendererChange(value: string) {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef)
    return
  const newCellDef = deepCopy(cellDef)
  if (!newCellDef.cellStyle) {
    newCellDef.cellStyle = {}
  }

  newCellDef.cellStyle!.renderer = value
  setCell(props.rowIndex, props.colIndex, newCellDef)
  setDirty()
}

function handleSelectRenderer() {
  emit('select-renderer')
}

function handleLinkUrlChange(value: string) {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef)
    return
  const newCellDef = deepCopy(cellDef)
  newCellDef.linkUrl = value
  setCell(props.rowIndex, props.colIndex, newCellDef)
  setDirty()
}

function handleLinkTargetChange(value: string) {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef)
    return
  const newCellDef = deepCopy(cellDef)
  newCellDef.linkTargetWindow = value
  setCell(props.rowIndex, props.colIndex, newCellDef)
  setDirty()
}

function handleUrlParameterConfig() {
  if (!linkUrl.value || linkUrl.value === '') {
    showAlert(t('property.prop.urlTip'))
    return
  }

  urlParameterDialogVisible.value = true
}

function handleLinkParametersChange(value: any[]) {
  const cellDef = getCell(props.rowIndex, props.colIndex)
  if (!cellDef)
    return
  const newCellDef = deepCopy(cellDef)
  newCellDef.linkParameters = value || []
  setCell(props.rowIndex, props.colIndex, newCellDef)
  setDirty()
  linkParameters.value = value || []
}

function handleCellTypeChange(value: string) {
  emit('cell-type-change', value)
}
</script>

<template>
  <div class="cell-value-editor">
    <u-form :label-width="100" labelPosition="left">
      <!-- 父单元格配置 -->
      <div v-show="showParentGroup" ref="parentGroup">
        <u-form-item class="property-label parent-cell" :label="t('property.prop.leftParent')">
          <u-radio-group
            v-model="leftParentType"
            @change="handleLeftParentTypeChange"
          >
            <u-radio
              v-for="option in parentTypeOptions"
              :key="option.value"
              :label="option.value"
            >
              {{ option.label }}
            </u-radio>
          </u-radio-group>
        </u-form-item>
        <u-form-item class="property-label">
          <u-select
            v-model="leftParentCellName"
            :clearable="true"
            :disabled="leftParentType !== 'custom'"
            style="width: 100px"
            @change="handleLeftParentCellNameChange"
          >
            <u-option
              v-for="option in leftParentCellNameOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
          <u-select
            v-model="leftParentRowNumber"
            :clearable="true"
            :disabled="leftParentType !== 'custom' || leftParentCellName === 'root'"
            style="margin-left:10px;width: 100px"
            @change="handleLeftParentRowNumberChange"
          >
            <u-option
              v-for="option in leftParentRowNumberOptionsFormatted"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item class="property-label parent-cell" :label="t('property.prop.topParent')">
          <u-radio-group
            v-model="topParentType"
            @change="handleTopParentTypeChange"
          >
            <u-radio
              v-for="option in parentTypeOptions"
              :key="option.value"
              :label="option.value"
            >
              {{ option.label }}
            </u-radio>
          </u-radio-group>
        </u-form-item>
        <u-form-item class="property-label">
          <u-select
            v-model="topParentCellName"
            :disabled="topParentType !== 'custom'"
            :clearable="true"
            style="width: 100px"
            @change="handleTopParentCellNameChange"
          >
            <u-option
              v-for="option in topParentCellNameOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
          <u-select
            v-model="topParentRowNumber"
            :clearable="true"
            :disabled="topParentType !== 'custom' || topParentCellName === 'root'"
            style="margin-left:10px;width: 100px"
            @change="handleTopParentRowNumberChange"
          >
            <u-option
              v-for="option in topParentRowNumberOptionsFormatted"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>
      </div>

      <!-- 渲染器配置 -->
      <div v-show="showRendererGroup" ref="rendererGroup" class="form-group" style="margin-bottom:6px">
        <label>{{ t('property.prop.renderBean') }}：</label>
        <div class="input-group" style="width: 290px;display: inline-block;height: 22px;">
          <div class="u-inline">
            <u-input
              v-model="rendererBean"
              style="width: 250px"
              @change="handleRendererChange"
            />
          </div>
          <span class="input-group-btn">
            <u-button @click="handleSelectRenderer">
              {{ t('property.prop.selectBean') }}
            </u-button>
          </span>
        </div>
      </div>

      <!-- 链接配置 -->
      <div v-show="showLinkGroup">
        <div class="property-quote">
          {{ t('property.prop.linkConfig') }}
        </div>

        <u-form-item class="property-label" :label="t('property.prop.linkUrl')">
          <u-input
            v-model="linkUrl"
            :placeholder="t('property.prop.urlExpressionSupport') + t('property.prop.urlExpressionExample')"
            style="width: 250px;"
            @change="handleLinkUrlChange"
          />
        </u-form-item>

        <u-form-item class="property-label" :label="t('property.prop.target')">
          <u-select
            v-model="linkTarget"
            :clearable="true"
            style="width: 120px"
            @change="handleLinkTargetChange"
          >
            <u-option
              v-for="option in linkTargetOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>

          <u-button
            type="primary"
            style="margin-left: 10px;"
            @click="handleUrlParameterConfig"
          >
            {{ t('property.prop.urlParameterConfig') }}
          </u-button>
        </u-form-item>
      </div>

      <!-- 单元格类型 -->
      <u-form-item v-show="showTypeGroup" class="property-label" :label="t('property.prop.cellType')">
        <u-select
          v-model="cellType"
          :clearable="true"
          style="width: 250px"
          @change="handleCellTypeChange"
        >
          <u-option
            v-for="option in cellTypeOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>
    </u-form>
    <!-- URL参数对话框 -->
    <URLParameterDialog
      v-show="urlParameterDialogVisible"
      v-model:visible="urlParameterDialogVisible"
      :parameters="linkParameters || []"
      @parameters-change="handleLinkParametersChange"
    />
  </div>
</template>

<style scoped>
.cell-value-editor {
  width: 100%;
}

.parent-cell {
  margin-bottom: 0 !important;
}
</style>
