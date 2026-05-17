<script setup lang="ts">
// @ts-nocheck
import { computed, defineEmits, defineOptions, defineProps, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import draggable from 'vuedraggable'
import { deepCopy } from '@/components/utils'
import { isNumberStr } from '../utils'
import { inputComponents, selectComponents } from '../utils/config'

defineOptions({
  name: 'RightPanel',
})
const props = defineProps<{
  showField: boolean
  activeData: any
  formConf: any
}>()

const emit = defineEmits<{
  (e: 'tag-change', target: any): void
}>()

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
  props.activeData.document || 'https://element.eleme.cn/#/zh-CN/component/installation',
)

const dateTypeOptions = computed(() => [
  { label: t('searchForm.date'), value: 'date' },
  { label: t('searchForm.month'), value: 'month' },
  { label: t('searchForm.year'), value: 'year' },
  { label: t('searchForm.datetime'), value: 'datetime' },
])

const dateOptions = computed(() => {
  if (props.activeData.type !== undefined && props.activeData.tag === 'u-date-picker') {
    return dateTypeOptions.value
  }

  return []
})

const tagList = computed(() => [
  { label: t('searchForm.inputComponents'), options: inputComponents },
  { label: t('searchForm.selectComponents'), options: selectComponents },
])

const layoutTree = computed(() => deepCopy([props.activeData]))

const dateTimeFormat: Record<string, string> = {
  date: 'YYYY-MM-DD',
  month: 'YYYY-MM',
  year: 'YYYY',
  datetime: 'YYYY-MM-DD HH:mm:ss',
}

function addReg() {
  if (!props.activeData.regList) {
    props.activeData.regList = []
  }

  props.activeData.regList.push({
    pattern: '',
    message: '',
  })
}

function addSelectItem() {
  props.activeData.options.push({
    label: '',
    value: '',
  })
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
  if (Array.isArray(props.activeData.defaultValue)) {
    props.activeData.defaultValue = str.split(',').map((val: string) =>
      isNumberStr(val) ? +val : val,
    )
  }
  else if (['true', 'false'].includes(str)) {
    props.activeData.defaultValue = JSON.parse(str)
  }
  else {
    props.activeData.defaultValue = isNumberStr(str) ? +str : str
  }
}

function onSwitchValueInput(val: string, name: string) {
  if (['true', 'false'].includes(val)) {
    props.activeData[name] = JSON.parse(val)
  }
  else {
    props.activeData[name] = isNumberStr(val) ? +val : val
  }
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
  props.activeData.defaultValue = null
  props.activeData.format = val
}

function spanChange(val: number) {
  props.formConf.span = val
}

function multipleChange(val: boolean) {
  props.activeData.defaultValue = val ? [] : ''
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
  props.activeData.min = val || undefined
}

function onCheckboxMaxInput(val: any) {
  props.activeData.max = val || undefined
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
          <u-form-item v-if="activeData.changeTag" :label="t('searchForm.componentType')">
            <u-select
              v-model="activeData.tagIcon"
              :placeholder="t('searchForm.selectComponentType')"
              :style="{width: '100%'}"
              @change="tagChange"
            >
              <template v-for="group in tagList">
                <div v-for="item in group.options" :key="item.label">
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
          <u-form-item v-if="activeData.vModel!==undefined" :label="t('searchForm.fieldName')">
            <u-input v-model="activeData.vModel" :placeholder="t('searchForm.enterFieldName')" />
          </u-form-item>
          <u-form-item v-if="activeData.componentName!==undefined" :label="t('searchForm.componentName')">
            {{ activeData.componentName }}
          </u-form-item>
          <u-form-item v-if="activeData.label!==undefined" :label="t('searchForm.title')">
            <u-input v-model="activeData.label" :placeholder="t('searchForm.enterTitle')" />
          </u-form-item>
          <u-form-item v-if="activeData.placeholder!==undefined" :label="t('searchForm.placeholder')">
          <u-input v-model="activeData.placeholder" :placeholder="t('searchForm.enterPlaceholder')" />
        </u-form-item>

        <u-form-item v-if="activeData.span!==undefined" :label="t('searchForm.formGrid')">
          <u-input-number v-model="activeData.span" :max="24" :min="1" @change="spanChange" />
        </u-form-item>
        <u-form-item v-if="activeData.layout==='rowFormItem'" :label="t('searchForm.gridSpacing')">
          <u-input-number v-model="activeData.gutter" :min="0" :placeholder="t('searchForm.gridSpacing')" />
        </u-form-item>
        <u-form-item v-if="activeData.layout==='rowFormItem'" :label="t('searchForm.layoutMode')">
          <u-radio-group v-model="activeData.type" button>
            <u-radio label="default" size="small" />
            <u-radio label="flex" size="small" />
          </u-radio-group>
        </u-form-item>
        <u-form-item v-if="activeData.justify!==undefined&&activeData.type==='flex'" :label="t('searchForm.horizontalAlignment')">
          <u-select v-model="activeData.justify" :placeholder="t('searchForm.selectHorizontalAlignment')" :style="{width: '100%'}">
            <u-option
              v-for="(item, index) in justifyOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </u-select>
        </u-form-item>
        <u-form-item v-if="activeData.align!==undefined&&activeData.type==='flex'" :label="t('searchForm.verticalAlignment')">
          <u-radio-group v-model="activeData.align" button>
            <u-radio label="top" size="small" />
            <u-radio label="middle" size="small" />
            <u-radio label="bottom" size="small" />
          </u-radio-group>
        </u-form-item>
        <u-form-item v-if="activeData.labelWidth!==undefined" :label="t('searchForm.labelWidth')">
          <u-input-number v-model="activeData.labelWidth" :placeholder="t('searchForm.enterLabelWidth')" />
        </u-form-item>
        <u-form-item v-if="activeData.style&&activeData.style.width!==undefined" :label="t('searchForm.componentWidth')">
          <u-input v-model="activeData.style.width" :placeholder="t('searchForm.enterComponentWidth')" clearable />
        </u-form-item>
        <u-form-item v-if="activeData.vModel!==undefined" :label="t('searchForm.defaultValue')">
          <u-input
            :value="setDefaultValue(activeData.defaultValue)"
            :placeholder="t('searchForm.enterDefaultValue')"
            @input="onDefaultValueInput"
          />
        </u-form-item>
        <u-form-item v-if="activeData.tag==='u-checkbox-group'" :label="t('searchForm.minSelect')">
          <u-input-number
            :value="activeData.min"
            :min="0"
            :placeholder="t('searchForm.minSelect')"
            @input="onCheckboxMinInput"
          />
        </u-form-item>
        <u-form-item v-if="activeData.tag==='u-checkbox-group'" :label="t('searchForm.maxSelect')">
          <u-input-number
            :value="activeData.max"
            :min="0"
            :placeholder="t('searchForm.maxSelect')"
            @input="onCheckboxMaxInput"
          />
        </u-form-item>
        <!-- <u-form-item v-if="activeData.prepend!==undefined" label="前缀">
          <u-input v-model="activeData.prepend" placeholder="请输入前缀" />
        </u-form-item>
        <u-form-item v-if="activeData.append!==undefined" label="后缀">
          <u-input v-model="activeData.append" placeholder="请输入后缀" />
        </u-form-item> -->
          <u-form-item v-if="activeData.min !== undefined" :label="t('searchForm.minValue')">
            <u-input-number v-model="activeData.min" :placeholder="t('searchForm.minValue')" />
          </u-form-item>
          <u-form-item v-if="activeData.max !== undefined" :label="t('searchForm.maxValue')">
            <u-input-number v-model="activeData.max" :placeholder="t('searchForm.maxValue')" />
          </u-form-item>
          <u-form-item v-if="activeData.step !== undefined" :label="t('searchForm.step')">
            <u-input-number v-model="activeData.step" :placeholder="t('searchForm.stepCount')" />
          </u-form-item>
          <!-- <u-form-item v-if="activeData.tag === 'u-input-number'" label="精度">
            <u-input-number v-model="activeData.precision" :min="0" placeholder="精度" />
          </u-form-item> -->
          <!-- <u-form-item v-if="activeData.tag === 'u-input-number'" label="按钮位置">
            <u-radio-group v-model="activeData['controlsPosition']" button>
              <u-radio label="" size="small">
                默认
              </u-radio>
              <u-radio label="right" size="small">
                右侧
              </u-radio>
            </u-radio-group>
          </u-form-item> -->
          <!-- <u-form-item v-if="activeData.maxlength !== undefined" label="最多输入">
            <u-input v-model="activeData.maxlength" placeholder="请输入字符长度">
              <template slot="append">
                个字符
              </template>
            </u-input>
          </u-form-item> -->
          <u-form-item v-if="activeData['activeText'] !== undefined" :label="t('searchForm.activeText')">
            <u-input v-model="activeData['activeText']" :placeholder="t('searchForm.enterActiveText')" />
          </u-form-item>
          <u-form-item v-if="activeData['inactiveText'] !== undefined" :label="t('searchForm.inactiveText')">
            <u-input v-model="activeData['inactiveText']" :placeholder="t('searchForm.enterInactiveText')" />
          </u-form-item>
          <u-form-item v-if="activeData['activeValue'] !== undefined" :label="t('searchForm.activeValue')">
            <u-input
              :value="setDefaultValue(activeData['activeValue'])"
              :placeholder="t('searchForm.enterActiveValue')"
              @input="onSwitchActiveValueInput"
            />
          </u-form-item>
          <u-form-item v-if="activeData['inactiveValue'] !== undefined" :label="t('searchForm.inactiveValue')">
            <u-input
              :value="setDefaultValue(activeData['inactiveValue'])"
              :placeholder="t('searchForm.enterInactiveValue')"
              @input="onSwitchInactiveValueInput"
            />
          </u-form-item>
          <u-form-item
            v-if="activeData.type !== undefined && 'u-date-picker' === activeData.tag"
            :label="t('searchForm.timeType')"
          >
            <u-select
              v-model="activeData.type"
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


          <u-form-item v-if="activeData.format !== undefined" :label="t('searchForm.timeFormat')">
            <u-input
              :value="activeData.format"
              :placeholder="t('searchForm.enterTimeFormat')"
              @input="setTimeFormatValue"
            />
          </u-form-item>
          <template v-if="['u-checkbox-group', 'u-radio-group', 'u-select'].indexOf(activeData.tag) > -1">
            <u-divider>{{ t('searchForm.options') }}</u-divider>
            <draggable
              :list="activeData.options"
              :animation="340"
              group="selectItem"
              handle=".option-drag"
            >
              <div v-for="(item, index) in activeData.options" :key="index" class="select-item">
                <div class="select-line-icon option-drag">
                  <i class="iconfont icon-success" />
                </div>
                <u-input v-model="item.label" :placeholder="t('searchForm.optionName')" size="small" />
                <u-input
                  :placeholder="t('searchForm.optionValue')"
                  size="small"
                  :value="item.value"
                  @input="onOptionValueInput(item, $event)"
                />
                <div class="close-btn select-line-icon" @click="activeData.options.splice(index, 1)">
                  <i class="iconfont icon-delete" />
                </div>
              </div>
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

          <u-form-item v-if="activeData.optionType !== undefined" :label="t('searchForm.optionStyle')">
            <u-radio-group v-model="activeData.optionType" button>
              <u-radio label="default" size="small">
                {{ t('searchForm.default') }}
              </u-radio>
              <u-radio label="button" size="small">
                {{ t('searchForm.button') }}
              </u-radio>
            </u-radio-group>
          </u-form-item>
          <u-form-item
            v-if="activeData.border !== undefined && activeData.optionType === 'default'"
            :label="t('searchForm.bordered')"
          >
            <u-switch v-model="activeData.border" />
          </u-form-item>
          <u-form-item
            v-if="activeData.size !== undefined &&
              (activeData.optionType === 'button' || activeData.border)"
            :label="t('searchForm.optionSize')"
          >
            <u-radio-group v-model="activeData.size" button>
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
          <!-- <u-form-item v-if="activeData['showWordLimit'] !== undefined" label="输入统计">
            <u-switch v-model="activeData['showWordLimit']" />
          </u-form-item> -->
          <!-- <u-form-item v-if="activeData.tag === 'u-input-number'" label="严格步数">
            <u-switch v-model="activeData['stepStrictly']" />
          </u-form-item> -->
          <u-form-item v-if="activeData.clearable !== undefined" :label="t('searchForm.clearable')">
            <u-switch v-model="activeData.clearable" />
          </u-form-item>
          <u-form-item v-if="activeData.showTip !== undefined" :label="t('searchForm.showTip')">
            <u-switch v-model="activeData.showTip" />
          </u-form-item>

          <u-form-item v-if="activeData.readonly !== undefined" :label="t('searchForm.readonly')">
            <u-switch v-model="activeData.readonly" />
          </u-form-item>
          <u-form-item v-if="activeData.disabled !== undefined" :label="t('searchForm.disabled')">
            <u-switch v-model="activeData.disabled" />
          </u-form-item>
          <!-- <u-form-item v-if="activeData.tag === 'u-select'" label="是否可搜索">
            <u-switch v-model="activeData.filterable" />
          </u-form-item> -->
          <u-form-item v-if="activeData.tag === 'u-select'" :label="t('searchForm.multiple')">
            <u-switch v-model="activeData.multiple" @change="multipleChange" />
          </u-form-item>
          <u-form-item v-if="activeData.required !== undefined" :label="t('searchForm.required')">
            <u-switch v-model="activeData.required" />
          </u-form-item>

          <template v-if="activeData.layoutTree">
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
            <u-input v-model="formConf.formRef" :placeholder="t('searchForm.enterFormName')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.formModel')">
            <u-input v-model="formConf.formModel" :placeholder="t('searchForm.enterFormModel')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.formRules')">
            <u-input v-model="formConf.formRules" :placeholder="t('searchForm.enterFormRules')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.formSize')" button>
            <u-radio-group v-model="formConf.size" button>
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
            <u-radio-group v-model="formConf.labelPosition" button>
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
            <u-input-number v-model="formConf.labelWidth" :placeholder="t('searchForm.labelWidth')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.gutter')">
            <u-input-number v-model="formConf.gutter" :min="0" :placeholder="t('searchForm.gutter')" />
          </u-form-item>
          <u-form-item :label="t('searchForm.disableForm')">
            <u-switch v-model="formConf.disabled" />
          </u-form-item>
          <u-form-item :label="t('searchForm.formButtons')">
            <u-switch v-model="formConf.formBtns" />
          </u-form-item>
          <u-form-item :label="t('searchForm.showUnfocusedBorder')">
            <u-switch v-model="formConf.unFocusedComponentBorder" />
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
