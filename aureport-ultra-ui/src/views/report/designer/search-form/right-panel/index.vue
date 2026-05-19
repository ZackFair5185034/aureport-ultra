<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import draggable from 'vuedraggable'
import { deepCopy } from '@/components/utils'
import { isNumberStr } from '@/views/report/designer/search-form/utils'
import { inputComponents, selectComponents } from '@/views/report/designer/search-form/utils/config'

defineOptions({
  name: 'RightPanel',
})
const props = withDefaults(defineProps<{
  showField?: boolean
  activeData: any
  formConf: any
}>(), {
  showField: true,
})

const emit = defineEmits<{
  (e: 'tag-change', target: any): void
  (e: 'update:activeData', value: any): void
  (e: 'update:formConf', value: any): void
}>()

// Local state to avoid mutating props directly
const localActiveData = ref(deepCopy(props.activeData))
const localFormConf = ref(deepCopy(props.formConf))

// Flags to break the emit → parent update → prop sync → emit loop
let syncingActiveData = false
let syncingFormConf = false

watch(() => props.activeData, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    syncingActiveData = true
    localActiveData.value = deepCopy(newVal)
    nextTick(() => { syncingActiveData = false })
  }
}, { deep: true })

watch(localActiveData, (newVal) => {
  if (syncingActiveData) return
  if (newVal && Object.keys(newVal).length > 0) {
    emit('update:activeData', deepCopy(newVal))
  }
}, { deep: true })

watch(() => props.formConf, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    syncingFormConf = true
    localFormConf.value = deepCopy(newVal)
    nextTick(() => { syncingFormConf = false })
  }
}, { deep: true })

watch(localFormConf, (newVal) => {
  if (syncingFormConf) return
  if (newVal && Object.keys(newVal).length > 0) {
    emit('update:formConf', deepCopy(newVal))
  }
}, { deep: true })

const { t } = useI18n()

const currentTab = ref('field')

const justifyOptions = [
  { label: 'start', value: 'start' },
  { label: 'end', value: 'end' },
  { label: 'center', value: 'center' },
  { label: 'space-around', value: 'space-around' },
  { label: 'space-between', value: 'space-between' },
]

const documentLink = computed(() =>
  localActiveData.value.document || 'https://element.eleme.cn/#/zh-CN/component/installation',
)

const dateTypeOptions = computed(() => [
  { label: t('searchForm.date'), value: 'date' },
  { label: t('searchForm.month'), value: 'month' },
  { label: t('searchForm.year'), value: 'year' },
  { label: t('searchForm.datetime'), value: 'datetime' },
])

const dateOptions = computed(() => {
  if (localActiveData.value.type !== undefined && localActiveData.value.tag === 'u-date-picker') {
    return dateTypeOptions.value
  }

  return []
})

const tagList = computed(() => [
  { label: t('searchForm.inputComponents'), options: inputComponents },
  { label: t('searchForm.selectComponents'), options: selectComponents },
])

const layoutTree = computed(() => deepCopy([localActiveData.value]))

const dateTimeFormat: Record<string, string> = {
  date: 'YYYY-MM-DD',
  month: 'YYYY-MM',
  year: 'YYYY',
  datetime: 'YYYY-MM-DD HH:mm:ss',
}

function addReg() {
  if (!localActiveData.value.regList) {
    localActiveData.value.regList = []
  }

  localActiveData.value.regList.push({
    pattern: '',
    message: '',
  })
  emit('update:activeData', localActiveData.value)
}

function addSelectItem() {
  localActiveData.value.options.push({
    label: '',
    value: '',
  })
  emit('update:activeData', localActiveData.value)
}

function addNode(data: any) {
  // currentNode is not used functionally; kept for compatibility
}

function onOptionValueInput(item: any, val: string) {
  item.value = isNumberStr(val) ? +val : val
}

function setDefaultValue(val: any): string {
  if (Array.isArray(val)) {
    return val.join(',')
  }

  if (['string', 'number'].includes(typeof val)) {
    return String(val)
  }

  if (typeof val === 'boolean') {
    return String(val)
  }

  return val
}

function onDefaultValueInput(str: string) {
  if (Array.isArray(localActiveData.value.defaultValue)) {
    localActiveData.value.defaultValue = str.split(',').map((val: string) =>
      isNumberStr(val) ? +val : val,
    )
  }
  else if (['true', 'false'].includes(str)) {
    localActiveData.value.defaultValue = JSON.parse(str)
  }
  else {
    localActiveData.value.defaultValue = isNumberStr(str) ? +str : str
  }
  emit('update:activeData', localActiveData.value)
}

function onSwitchValueInput(val: string, name: string) {
  if (['true', 'false'].includes(val)) {
    localActiveData.value[name] = JSON.parse(val)
  }
  else {
    localActiveData.value[name] = isNumberStr(val) ? +val : val
  }
  emit('update:activeData', localActiveData.value)
}

function onSwitchActiveValueInput(val: string) {
  onSwitchValueInput(val, 'activeValue')
}

function onSwitchInactiveValueInput(val: string) {
  onSwitchValueInput(val, 'inactiveValue')
}

function setTimeFormatValue(val: string) {
  setTimeValue(val)
}

function setTimeValue(val: string, _type?: string) {
  localActiveData.value.defaultValue = null
  localActiveData.value.format = val
  emit('update:activeData', localActiveData.value)
}

function spanChange(val: number) {
  localFormConf.value.span = val
  emit('update:formConf', localFormConf.value)
}

function multipleChange(val: boolean) {
  localActiveData.value.defaultValue = val ? [] : ''
  emit('update:activeData', localActiveData.value)
}

function dateTypeChange(val: string) {
  setTimeValue(dateTimeFormat[val] || val, val)
}

function tagChange(tagIcon: string) {
  let target = inputComponents.find((item: any) => item.tagIcon === tagIcon)
  if (!target)
    target = selectComponents.find((item: any) => item.tagIcon === tagIcon)
  emit('tag-change', target)
}

function onCheckboxMinInput(val: any) {
  localActiveData.value.min = val || undefined
  emit('update:activeData', localActiveData.value)
}

function onCheckboxMaxInput(val: any) {
  localActiveData.value.max = val || undefined
  emit('update:activeData', localActiveData.value)
}
</script>

<template>
  <!-- eslint-disable -->
  <div class="right-board">
    <u-tabs v-model="currentTab" type="text" class="center-tabs">
      <u-tab-pane :label="t('searchForm.componentProperties')" index="field" />
      <u-tab-pane :label="t('searchForm.formProperties')" index="form" />
    </u-tabs>

    <div class="field-box">
      <div class="right-scrollbar">
        <!-- 组件属性 -->
        <u-form v-show="currentTab==='field' && showField" size="small" :label-width="90">
          <u-form-item v-if="localActiveData.changeTag" :label="t('searchForm.componentType')">
            <u-select
              v-model="localActiveData.tagIcon"
              :placeholder="t('searchForm.selectComponentType')"
              :style="{width: '100%'}"
              @change="tagChange"
            >
              <template v-for="group in tagList">
                <div v-for="(item, idx) in group.options" :key="idx">
                  <u-option
                    :label="item.label"
                    :value="item.tagIcon"
                  >
                    {{ item.label }}
                  </u-option>
                </div>
              </template>
            </u-select>
          </u-form-item>
          <u-form-item v-if="localActiveData.vModel!==undefined" :label="t('searchForm.fieldName')">
            <u-input v-model="localActiveData.vModel" :placeholder="t('searchForm.enterFieldName')" />
          </u-form-item>
          <u-form-item v-if="localActiveData.componentName!==undefined" :label="t('searchForm.componentName')">
            {{ localActiveData.componentName }}
          </u-form-item>
          <u-form-item v-if="localActiveData.label!==undefined" :label="t('searchForm.title')">
            <u-input v-model="localActiveData.label" :placeholder="t('searchForm.enterTitle')" />
          </u-form-item>
          <u-form-item v-if="localActiveData.placeholder!==undefined" :label="t('searchForm.placeholder')">
          <u-input v-model="localActiveData.placeholder" :placeholder="t('searchForm.enterPlaceholder')" />
        </u-form-item>

        <u-form-item v-if="localActiveData.span!==undefined" :label="t('searchForm.formGrid')">
          <u-input-number v-model="localActiveData.span" :max="24" :min="1" @change="spanChange" />
        </u-form-item>
        <u-form-item v-if="localActiveData.layout==='rowFormItem'" :label="t('searchForm.gridSpacing')">
          <u-input-number v-model="localActiveData.gutter" :min="0" :placeholder="t('searchForm.gridSpacing')" />
        </u-form-item>
        <u-form-item v-if="localActiveData.layout==='rowFormItem'" :label="t('searchForm.layoutMode')">
          <u-radio-group v-model="localActiveData.type" button>
            <u-radio label="default" size="small" />
            <u-radio label="flex" size="small" />
          </u-radio-group>
        </u-form-item>
        <u-form-item v-if="localActiveData.justify!==undefined&&localActiveData.type==='flex'" :label="t('searchForm.horizontalAlignment')">
          <u-select v-model="localActiveData.justify" :placeholder="t('searchForm.selectHorizontalAlignment')" :style="{width: '100%'}">
            <u-option
              v-for="(item, index) in justifyOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </u-select>
        </u-form-item>
        <u-form-item v-if="localActiveData.align!==undefined&&localActiveData.type==='flex'" :label="t('searchForm.verticalAlignment')">
          <u-radio-group v-model="localActiveData.align" button>
            <u-radio label="top" size="small" />
            <u-radio label="middle" size="small" />
            <u-radio label="bottom" size="small" />
          </u-radio-group>
        </u-form-item>
        <u-form-item v-if="localActiveData.labelWidth!==undefined" :label="t('searchForm.labelWidth')">
          <u-input-number v-model="localActiveData.labelWidth" :placeholder="t('searchForm.enterLabelWidth')" />
        </u-form-item>
        <u-form-item v-if="localActiveData.style&&localActiveData.style.width!==undefined" :label="t('searchForm.componentWidth')">
          <u-input v-model="localActiveData.style.width" :placeholder="t('searchForm.enterComponentWidth')" clearable />
        </u-form-item>
        <u-form-item v-if="localActiveData.vModel!==undefined" :label="t('searchForm.defaultValue')">
          <u-input
            :value="setDefaultValue(localActiveData.defaultValue)"
            :placeholder="t('searchForm.enterDefaultValue')"
            @input="onDefaultValueInput"
          />
        </u-form-item>
        <u-form-item v-if="localActiveData.tag==='u-checkbox-group'" :label="t('searchForm.minSelect')">
          <u-input-number
            :value="localActiveData.min"
            :min="0"
            :placeholder="t('searchForm.minSelect')"
            @input="onCheckboxMinInput"
          />
        </u-form-item>
        <u-form-item v-if="localActiveData.tag==='u-checkbox-group'" :label="t('searchForm.maxSelect')">
          <u-input-number
            :value="localActiveData.max"
            :min="0"
            :placeholder="t('searchForm.maxSelect')"
            @input="onCheckboxMaxInput"
          />
        </u-form-item>
          <u-form-item v-if="localActiveData.min !== undefined" :label="t('searchForm.minValue')">
            <u-input-number v-model="localActiveData.min" :placeholder="t('searchForm.minValue')" />
          </u-form-item>
          <u-form-item v-if="localActiveData.max !== undefined" :label="t('searchForm.maxValue')">
            <u-input-number v-model="localActiveData.max" :placeholder="t('searchForm.maxValue')" />
          </u-form-item>
          <u-form-item v-if="localActiveData.step !== undefined" :label="t('searchForm.step')">
            <u-input-number v-model="localActiveData.step" :placeholder="t('searchForm.stepCount')" />
          </u-form-item>
          <u-form-item v-if="localActiveData['activeText'] !== undefined" :label="t('searchForm.activeText')">
            <u-input v-model="localActiveData['activeText']" :placeholder="t('searchForm.enterActiveText')" />
          </u-form-item>
          <u-form-item v-if="localActiveData['inactiveText'] !== undefined" :label="t('searchForm.inactiveText')">
            <u-input v-model="localActiveData['inactiveText']" :placeholder="t('searchForm.enterInactiveText')" />
          </u-form-item>
          <u-form-item v-if="localActiveData['activeValue'] !== undefined" :label="t('searchForm.activeValue')">
            <u-input
              :value="setDefaultValue(localActiveData['activeValue'])"
              :placeholder="t('searchForm.enterActiveValue')"
              @input="onSwitchActiveValueInput"
            />
          </u-form-item>
          <u-form-item v-if="localActiveData['inactiveValue'] !== undefined" :label="t('searchForm.inactiveValue')">
            <u-input
              :value="setDefaultValue(localActiveData['inactiveValue'])"
              :placeholder="t('searchForm.enterInactiveValue')"
              @input="onSwitchInactiveValueInput"
            />
          </u-form-item>
          <u-form-item
            v-if="localActiveData.type !== undefined && 'u-date-picker' === localActiveData.tag"
            :label="t('searchForm.timeType')"
          >
            <u-select
              v-model="localActiveData.type"
              :placeholder="t('searchForm.selectTimeType')"
              :style="{ width: '100%' }"
              @change="dateTypeChange"
            >
              <u-option
                v-for="(item, index) in dateOptions"
                :key="index"
                :label="item.label"
                :value="item.value"
              />
            </u-select>
          </u-form-item>


          <u-form-item v-if="localActiveData.format !== undefined" :label="t('searchForm.timeFormat')">
            <u-input
              :value="localActiveData.format"
              :placeholder="t('searchForm.enterTimeFormat')"
              @input="setTimeFormatValue"
            />
          </u-form-item>
          <template v-if="['u-checkbox-group', 'u-radio-group', 'u-select'].indexOf(localActiveData.tag) > -1">
            <u-divider>{{ t('searchForm.options') }}</u-divider>
            <draggable
              :list="localActiveData.options"
              :animation="340"
              group="selectItem"
              handle=".option-drag"
              item-key="value"
            >
              <template #item="{ element, index }">
                <div class="select-item">
                  <div class="select-line-icon option-drag">
                    <i class="iconfont icon-success" />
                  </div>
                  <u-input v-model="element.label" :placeholder="t('searchForm.optionName')" size="small" />
                  <u-input
                    :placeholder="t('searchForm.optionValue')"
                    size="small"
                    :value="element.value"
                    @input="onOptionValueInput(element, $event)"
                  />
                  <div class="close-btn select-line-icon" @click="localActiveData.options.splice(index, 1)">
                    <i class="iconfont icon-delete" />
                  </div>
                </div>
              </template>
            </draggable>
            <div style="margin-left: 20px;">
              <u-button
                style="padding-bottom: 0"
                icon="u-icon-circle-plus-outline"
                type="text"
                @click.prevent="addSelectItem"
              >
                {{ t('searchForm.addOption') }}
              </u-button>
            </div>
            <u-divider />
          </template>

          <u-form-item v-if="localActiveData.optionType !== undefined" :label="t('searchForm.optionStyle')">
            <u-radio-group v-model="localActiveData.optionType" button>
              <u-radio label="default" size="small">
                {{ t('searchForm.default') }}
              </u-radio>
              <u-radio label="button" size="small">
                {{ t('searchForm.button') }}
              </u-radio>
            </u-radio-group>
          </u-form-item>
          <u-form-item
            v-if="localActiveData.border !== undefined && localActiveData.optionType === 'default'"
            :label="t('searchForm.bordered')"
          >
            <u-switch v-model="localActiveData.border" />
          </u-form-item>
          <u-form-item
            v-if="localActiveData.size !== undefined &&
              (localActiveData.optionType === 'button' || localActiveData.border)"
            :label="t('searchForm.optionSize')"
          >
            <u-radio-group v-model="localActiveData.size" button>
              <u-radio label="medium" size="small">
                {{ t('searchForm.medium') }}
              </u-radio>
              <u-radio label="small" size="small">
                {{ t('searchForm.small') }}
              </u-radio>
              <u-radio label="mini" size="small">
                {{ t('searchForm.mini') }}
              </u-radio>
            </u-radio-group>
          </u-form-item>
          <u-form-item v-if="localActiveData.clearable !== undefined" :label="t('searchForm.clearable')">
            <u-switch v-model="localActiveData.clearable" />
          </u-form-item>
          <u-form-item v-if="localActiveData.showTip !== undefined" :label="t('searchForm.showTip')">
            <u-switch v-model="localActiveData.showTip" />
          </u-form-item>

          <u-form-item v-if="localActiveData.readonly !== undefined" :label="t('searchForm.readonly')">
            <u-switch v-model="localActiveData.readonly" />
          </u-form-item>
          <u-form-item v-if="localActiveData.disabled !== undefined" :label="t('searchForm.disabled')">
            <u-switch v-model="localActiveData.disabled" />
          </u-form-item>
          <u-form-item v-if="localActiveData.tag === 'u-select'" :label="t('searchForm.multiple')">
            <u-switch v-model="localActiveData.multiple" @change="multipleChange" />
          </u-form-item>
          <u-form-item v-if="localActiveData.required !== undefined" :label="t('searchForm.required')">
            <u-switch v-model="localActiveData.required" />
          </u-form-item>

          <template v-if="localActiveData.layoutTree">
            <u-divider>{{ t('searchForm.layoutStructureTree') }}</u-divider>
            <u-tree
              :data="layoutTree"
              node-key="renderKey"
            >
              <template #default="{ node, data }">
                <span class="node-label">
                  {{ data.componentName || `${data.label}: ${data.vModel}` }}
                </span>
              </template>
            </u-tree>
          </template>

          <!-- <template v-if="activeData.layout === 'colFormItem' && activeData.tag !== 'u-button'">
            <u-divider>正则校验</u-divider>
            <div
              v-for="(item, index) in activeData.regList"
              :key="index"
              class="reg-item"
            >
              <span class="close-btn" @click="activeData.regList.splice(index, 1)">
                <i class="iconfont icon-close" />
              </span>
              <u-form-item label="表达式">
                <u-input v-model="item.pattern" placeholder="请输入正则" />
              </u-form-item>
              <u-form-item label="错误提示" style="margin-bottom:0">
                <u-input v-model="item.message" placeholder="请输入错误提示" />
              </u-form-item>
            </div>
            <div style="margin-left: 20px">
              <u-button icon="u-icon-circle-plus-outline" type="text" @click.prevent="addReg">
                添加规则
              </u-button>
            </div>
          </template> -->
        </u-form>
        <!-- 表单属性 -->
        <u-form v-show="currentTab === 'form'" size="small" :label-width="90">
          <u-form-item :label="t('searchForm.formName')">
            <u-input v-model="localFormConf.formRef" :placeholder="t('searchForm.enterFormName')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.formModel')">
            <u-input v-model="localFormConf.formModel" :placeholder="t('searchForm.enterFormModel')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.formRules')">
            <u-input v-model="localFormConf.formRules" :placeholder="t('searchForm.enterFormRules')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.formSize')" button>
            <u-radio-group v-model="localFormConf.size" button>
              <u-radio label="medium" size="small">
                {{ t('searchForm.medium') }}
              </u-radio>
              <u-radio label="small" size="small">
                {{ t('searchForm.small') }}
              </u-radio>
              <u-radio label="mini" size="small">
                {{ t('searchForm.mini') }}
              </u-radio>
            </u-radio-group>
          </u-form-item>
          <u-form-item :label="t('searchForm.labelPosition')">
            <u-radio-group v-model="localFormConf.labelPosition" button>
              <u-radio label="left" size="small">
                {{ t('searchForm.leftAlign') }}
              </u-radio>
              <u-radio label="right" size="small">
                {{ t('searchForm.rightAlign') }}
              </u-radio>
              <u-radio label="top" size="small">
                {{ t('searchForm.topAlign') }}
              </u-radio>
            </u-radio-group>
          </u-form-item>
          <u-form-item :label="t('searchForm.labelWidth')">
            <u-input-number v-model="localFormConf.labelWidth" :placeholder="t('searchForm.labelWidth')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.gutter')">
            <u-input-number v-model="localFormConf.gutter" :min="0" :placeholder="t('searchForm.gutter')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.disableForm')">
            <u-switch v-model="localFormConf.disabled" />
          </u-form-item>
          <u-form-item :label="t('searchForm.formButtons')">
            <u-switch v-model="localFormConf.formBtns" />
          </u-form-item>
          <u-form-item :label="t('searchForm.showUnfocusedBorder')">
            <u-switch v-model="localFormConf.unFocusedComponentBorder" />
          </u-form-item>
        </u-form>
      </div>
    </div>

  </div>
</template>

<style scoped lang="scss">
.right-scrollbar {
  padding: 12px 18px 15px 15px;
  height: var(--dialog-height);
}
.right-board {
  width: 350px;
  position: absolute;
  right: 0;
  top: 0;
  padding-top: 3px;
}
.right-board .field-box {
  position: relative;
  height: var(--dialog-height);
  box-sizing: border-box;
  overflow-y: auto;
}
.right-board .u-scrollbar {
  height: 100%;
}
.select-item {
  display: flex;
  border: 1px dashed #fff;
  box-sizing: border-box;
}
.select-item .close-btn {
  cursor: pointer;
  color: #f56c6c;
}
.select-item .u-input + .u-input {
  margin-left: 4px;
}
.select-item + .select-item {
  margin-top: 4px;
}
.select-item.sortable-chosen {
  border: 1px dashed #409eff;
}
.select-line-icon {
  line-height: 32px;
  font-size: 22px;
  padding: 0 4px;
  color: #777;
}
.option-drag {
  cursor: move;
}
.node-label {
  font-size: 14px;
}
</style>
