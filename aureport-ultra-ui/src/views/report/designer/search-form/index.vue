<template>
  <div class="container">
    <div class="left-board">
      <div class="logo-wrapper">
        <div class="logo">
          <img :src="logo" alt="logo"> Form Generator
        </div>
      </div>
      <div class="left-scrollbar">
        <div class="components-list">
          <div class="components-title">
            {{ t('searchForm.inputComponents') }}
          </div>
          <draggable
            class="components-draggable"
            :list="inputComponents"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent"
            draggable=".components-item"
            :sort="false"
            @end="onEnd"
          >
            <div
              v-for="(element, index) in inputComponents" :key="index" class="components-item"
              @click="addComponent(element)"
            >
              <div class="components-body">
                {{ element.label }}
              </div>
            </div>
          </draggable>
          <div class="components-title">
            {{ t('searchForm.selectComponents') }}
          </div>
          <draggable
            class="components-draggable"
            :list="selectComponents"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent"
            draggable=".components-item"
            :sort="false"
            @end="onEnd"
          >
            <div
              v-for="(element, index) in selectComponents"
              :key="index"
              class="components-item"
              @click="addComponent(element)"
            >
              <div class="components-body">
                {{ element.label }}
              </div>
            </div>
          </draggable>
          <div class="components-title">
            {{ t('searchForm.layoutComponents') }}
          </div>
          <draggable
            class="components-draggable" :list="layoutComponents"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }" :clone="cloneComponent"
            draggable=".components-item" :sort="false" @end="onEnd"
          >
            <div
              v-for="(element, index) in layoutComponents" :key="index" class="components-item"
              @click="addComponent(element)"
            >
              <div class="components-body">
                {{ element.label }}
              </div>
            </div>
          </draggable>
        </div>
      </div>
    </div>

    <div class="center-board">
      <div class="action-bar">
        <u-button icon="icon-cloud-download" type="text" @click="download">
          {{ t('searchForm.exportVueFile') }}
        </u-button>
        <u-button class="copy-btn-main" icon="icon-copy" type="text" @click="copy">
          {{ t('searchForm.copyCode') }}
        </u-button>
        <u-button class="delete-btn" icon="icon-delete" type="text" @click="empty">
          {{ t('searchForm.clear') }}
        </u-button>
      </div>
      <div class="center-scrollbar">
        <u-row class="center-board-row" :gutter="formConf.gutter">
          <u-form
            :size="formConf.size"
            :label-position="formConf.labelPosition"
            :disabled="formConf.disabled"
            :label-width="formConf.labelWidth"
          >
            <draggable class="drawing-board" :list="drawingList" :animation="340" group="componentsGroup">
              <draggable-item
                v-for="(element, index) in drawingList"
                :key="element.renderKey"
                :drawing-list="drawingList"
                :element="element"
                :index="index"
                :active-id="activeId"
                :form-conf="formConf"
                @activeItem="activeFormItem"
                @copyItem="drawingItemCopy"
                @deleteItem="drawingItemDelete"
              />
            </draggable>
            <div v-show="!drawingList.length" class="empty-info">
              {{ t('searchForm.dragComponents') }}
            </div>
          </u-form>
        </u-row>
      </div>
    </div>

    <right-panel
      :active-data="activeData"
      :form-conf="formConf"
      :show-field="!!drawingList.length"
      @tag-change="tagChange"
    />

    <code-type-dialog
      v-model:visible="dialogVisible"
      title="选择生成类型"
      :show-file-name="showFileName"
      @confirm="generate"
    />
    <input id="copyNode" type="hidden">
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, watch, nextTick, onMounted, onBeforeUnmount, defineProps, defineExpose, defineOptions } from 'vue'
import { useI18n } from 'vue-i18n'
import draggable from 'vuedraggable'
import beautifier from 'js-beautify'
import ClipboardJS from 'clipboard'

import RightPanel from './right-panel/index.vue'
import CodeTypeDialog from './code-type-dialog/index.vue'
import DraggableItem from './draggable-item/index.vue'

import { inputComponents, selectComponents, layoutComponents, formConf as importedFormConf } from './utils/config'
import { beautifierConf, titleCase } from './utils'
import { makeUpHtml, vueTemplate, vueScript, cssStyle } from './utils/html'
import { makeUpJs } from './utils/js'
import { makeUpCss } from './utils/css'
import { drawingDefaultValue, initDrawingDefaultValue, cleanDrawingDefaultValue } from './utils/drawingDefault'
import logo from '@/assets/images/form-designer/logo.png'
import { showAlert, showConfirm } from '@/utils/comnon.js'
import { deepCopy } from '@/components/utils'

defineOptions({
  name: 'SearchForm'
})
const props = withDefaults(defineProps<{
  searchFormConfig?: any
}>(), {
  searchFormConfig: () => ({})
})

const { t } = useI18n()

// Module-level variables (shared across instances, matching Vue 2 behavior)
let oldActiveId: any
let tempActiveData: any
let clipboard: any = null

// beforeCreate logic
initDrawingDefaultValue()

// created logic
document.body.ondrop = (event: DragEvent) => {
  event.preventDefault()
  event.stopPropagation()
}

// --- Reactive state ---
const idGlobal = ref(100)
const formConf = ref(importedFormConf)
const labelWidth = ref(100)
const drawingList = ref(drawingDefaultValue)
const drawingData = ref({})
const activeId = ref(drawingDefaultValue[0]?.formId)
const drawerVisible = ref(false)
const formData = ref({})
const dialogVisible = ref(false)
const generateConf = ref<any>(null)
const showFileName = ref(false)
const activeData = ref(drawingDefaultValue[0])

// --- Watchers ---
watch(activeId, (val) => {
  oldActiveId = val
}, { immediate: true })

watch(() => props.searchFormConfig, (val: any) => {
  if (val) {
    const { fields, ...rest } = val
    formConf.value = deepCopy(rest)
    drawingList.value = deepCopy(fields)
  }
}, { immediate: true })

// --- Lifecycle ---
onMounted(() => {
  clipboard = new ClipboardJS('#copyNode', {
    text: () => {
      const codeStr = generateCode()
      showAlert('代码已复制到剪切板，可粘贴。')
      return codeStr
    }
  })
  clipboard.on('error', () => {
    showAlert('代码复制失败')
  })
})

onBeforeUnmount(() => {
  if (clipboard) {
    clipboard.destroy()
  }
})

// --- Methods ---
function activeFormItem(element: any) {
  activeData.value = element
  activeId.value = element.formId
}

function onEnd(obj: any) {
  if (obj.from !== obj.to) {
    activeData.value = tempActiveData
    activeId.value = idGlobal.value
  }
}

function addComponent(item: any) {
  const clone = cloneComponent(item)
  drawingList.value.push(clone)
  activeFormItem(clone)
}

function cloneComponent(origin: any) {
  const clone = JSON.parse(JSON.stringify(origin))
  clone.formId = ++idGlobal.value
  clone.span = importedFormConf.span
  clone.renderKey = +new Date()
  if (!clone.layout) clone.layout = 'colFormItem'
  if (clone.layout === 'colFormItem') {
    clone.vModel = `field${idGlobal.value}`
    clone.placeholder !== undefined && (clone.placeholder += clone.label)
    tempActiveData = clone
  } else if (clone.layout === 'rowFormItem') {
    delete clone.label
    clone.componentName = `row${idGlobal.value}`
    clone.gutter = formConf.value.gutter
    tempActiveData = clone
  }
  return tempActiveData
}

function AssembleFormData() {
  formData.value = {
    fields: JSON.parse(JSON.stringify(drawingList.value)),
    ...formConf.value
  }
}

function generate(data: any) {
  const func = (generateExec as any)[`exec${titleCase(operationType.value)}`]
  generateConf.value = data
  func && func(data)
}

const generateExec: Record<string, (data: any) => void> = {
  execRun(data: any) {
    AssembleFormData()
    drawerVisible.value = true
  },
  execDownload(data: any) {
    const codeStr = generateCode()
    const blob = new Blob([codeStr], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = data.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  },
  execCopy() {
    document.getElementById('copyNode')?.click()
  }
}

const operationType = ref('')

function empty() {
  showConfirm('确定要清空所有组件吗？', { type: 'warning' }).then(() => {
    drawingList.value = []
    cleanDrawingDefaultValue()
  })
}

function drawingItemCopy(item: any, parent: any[]) {
  let clone = JSON.parse(JSON.stringify(item))
  clone = createIdAndKey(clone)
  parent.push(clone)
  activeFormItem(clone)
}

function createIdAndKey(item: any): any {
  item.formId = ++idGlobal.value
  item.renderKey = +new Date()
  if (item.layout === 'colFormItem') {
    item.vModel = `field${idGlobal.value}`
  } else if (item.layout === 'rowFormItem') {
    item.componentName = `row${idGlobal.value}`
  }
  if (Array.isArray(item.children)) {
    item.children = item.children.map((childItem: any) => createIdAndKey(childItem))
  }
  return item
}

function drawingItemDelete(index: number, parent: any[]) {
  parent.splice(index, 1)
  nextTick(() => {
    const len = drawingList.value.length
    if (len) {
      activeFormItem(drawingList.value[len - 1])
    }
  })
}

function generateCode(): string {
  const { type } = generateConf.value
  AssembleFormData()
  const script = vueScript(makeUpJs(formData.value, type))
  const html = vueTemplate(makeUpHtml(formData.value, type))
  const css = cssStyle(makeUpCss(formData.value))
  return beautifier.html(html + script + css, beautifierConf.html)
}

function download() {
  dialogVisible.value = true
  showFileName.value = true
  operationType.value = 'download'
}

function run() {
  dialogVisible.value = true
  showFileName.value = false
  operationType.value = 'run'
}

function copy() {
  dialogVisible.value = true
  showFileName.value = false
  operationType.value = 'copy'
}

function tagChange(newTag: any) {
  newTag = cloneComponent(newTag)
  newTag.vModel = activeData.value.vModel
  newTag.formId = activeId.value
  newTag.span = activeData.value.span
  delete activeData.value.tag
  delete activeData.value.tagIcon
  delete activeData.value.document
  Object.keys(newTag).forEach(key => {
    if (activeData.value[key] !== undefined
      && typeof activeData.value[key] === typeof newTag[key]) {
      newTag[key] = activeData.value[key]
    }
  })
  activeData.value = newTag
  updateDrawingList(newTag, drawingList.value)
}

function updateDrawingList(newTag: any, list: any[]) {
  const index = list.findIndex((item: any) => item.formId === activeId.value)
  if (index > -1) {
    list.splice(index, 1, newTag)
  } else {
    list.forEach((item: any) => {
      if (Array.isArray(item.children)) updateDrawingList(newTag, item.children)
    })
  }
}

function generateTemplate(): string {
  generateConf.value = {
    fileName: '',
    type: 'file'
  }
  return generateCode()
}

defineExpose({
  generateTemplate
})
</script>

<style lang="scss">
:root {
  --dialog-height: 560px;
}
.editor-tabs{
  background: #121315;
}

.left-scrollbar .u-scrollbar__wrap {
  box-sizing: border-box;
  overflow-x: hidden !important;
  margin-bottom: 0 !important;
}
.center-tabs{
  ul{
    width: 100%;

    li{
      width: 50%;
      border: none !important;
      padding: 0.785em 0 !important;
      text-align: center;
    }
  }
}

.center-tabs.u-tabs.tabs .nav.top li.active{
  border-bottom: 1px solid rgba(34,36,38,.15) !important;
}

.reg-item{
  padding: 12px 6px;
  background: #f8f8f8;
  position: relative;
  border-radius: 4px;
  .close-btn{
    position: absolute;
    right: -6px;
    top: -6px;
    display: block;
    width: 16px;
    height: 16px;
    line-height: 16px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 50%;
    color: #fff;
    text-align: center;
    z-index: 1;
    cursor: pointer;
    font-size: 12px;
    &:hover{
      background: rgba(210, 23, 23, 0.5)
    }
  }
  & + .reg-item{
    margin-top: 18px;
  }
}
.action-bar{
  & .u-button+.u-button {
    margin-left: 15px;
  }
  & i {
    font-size: 20px;
    vertical-align: middle;
    position: relative;
    top: -1px;
  }
}

.custom-tree-node{
  width: 100%;
  font-size: 14px;
  .node-operation{
    float: right;
  }
  i[class*="el-icon"] + i[class*="el-icon"]{
    margin-left: 6px;
  }
  .u-icon-plus{
    color: #409EFF;
  }
  .u-icon-delete{
    color: #157a0c;
  }
}

.left-scrollbar .u-scrollbar__view{
  overflow-x: hidden;
}

.container {
  position: relative;
  width: 100%;
  height: 100%;
}

.components-list {
  padding: 8px;
  box-sizing: border-box;
  height: 100%;
  .components-item {
    display: inline-block;
    width: 48%;
    margin: 1%;
    transition: transform 0ms !important;
  }
}
.components-draggable{
  padding-bottom: 20px;
}
.components-title{
  font-size: 14px;
  color: #222;
  margin: 6px 2px;
}

.components-body {
  padding: 8px 10px;
  background: #f6f7ff;
  font-size: 12px;
  cursor: move;
  border: 1px dashed #f6f7ff;
  border-radius: 3px;
  &:hover {
    border: 1px dashed #787be8;
    color: #787be8;
  }
}

.left-board {
  width: 260px;
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
}
.left-scrollbar{
  height: var(--dialog-height);
  overflow: hidden;
}
.center-scrollbar {
  border-left: 1px solid #f1e8e8;
  border-right: 1px solid #f1e8e8;
  box-sizing: border-box;
}
.center-board {
  width: auto;
  margin: 0 350px 0 260px;
  box-sizing: border-box;
}
.empty-info{
  position: absolute;
  top: 46%;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 18px;
  color: #ccb1ea;
  letter-spacing: 4px;
}
.action-bar{
  position: relative;
  height: 42px;
  text-align: right;
  padding: 0 15px;
  box-sizing: border-box;
  border: 1px solid #f1e8e8;
  border-top: none;
  border-left: none;
  .delete-btn{
    color: #F56C6C;
  }
}
.logo-wrapper{
  position: relative;
  height: 42px;
  background: #fff;
  border-bottom: 1px solid #f1e8e8;
  box-sizing: border-box;
}
.logo{
  position: absolute;
  left: 12px;
  top: 6px;
  line-height: 30px;
  color: #00afff;
  font-weight: 600;
  font-size: 17px;
  white-space: nowrap;
  > img{
    width: 30px;
    height: 30px;
    vertical-align: top;
  }
  .github{
    display: inline-block;
    vertical-align: sub;
    margin-left: 15px;
    > img{
      height: 22px;
    }
  }
}

.center-board-row {
  padding: 12px 12px 15px 12px;
  box-sizing: border-box;
  overflow-x: hidden;
  & > .u-form {
    height: 100%;
    width: 100%;
  }
}
.drawing-board {
  height: var(--dialog-height);
  overflow-y: scroll;
  overflow-x: hidden;
  position: relative;
  .components-body {
    padding: 0;
    margin: 0;
    font-size: 0;
  }
  .sortable-ghost {
    position: relative;
    display: block;
    overflow: hidden;
    &::before {
      content: " ";
      position: absolute;
      left: 0;
      right: 0;
      top: 0;
      height: 3px;
      background: rgb(89, 89, 223);
      z-index: 2;
    }
  }
  .components-item.sortable-ghost {
    width: 100%;
    height: 60px;
    background-color: #f6f7ff;
  }
  .active-from-item {
    & > .u-form-item{
      background: #f6f7ff;
      border-radius: 6px;
    }
    & > .drawing-item-copy, & > .drawing-item-delete{
      display: initial;
    }
    & > .component-name{
      color: #409EFF;
    }
  }
  .u-form-item{
    margin-bottom: 15px;
  }
}
.drawing-item{
  position: relative;
  cursor: move;
  &.unfocus-bordered:not(.activeFromItem) > div:first-child  {
    border: 1px dashed #ccc;
  }
  .u-form-item{
    padding: 12px 10px;
  }
}
.drawing-row-item{
  position: relative;
  cursor: move;
  box-sizing: border-box;
  border: 1px dashed #ccc;
  border-radius: 3px;
  padding: 0 2px;
  margin-bottom: 15px;
  .drawing-row-item {
    margin-bottom: 2px;
  }
  .u-col{
    margin-top: 22px;
  }
  .u-form-item{
    margin-bottom: 0;
  }
  .drag-wrapper{
    min-height: 80px;
  }
  &.active-from-item{
    border: 1px dashed #409EFF;
  }
  .component-name{
    position: absolute;
    top: 0;
    left: 0;
    font-size: 12px;
    color: #bbb;
    display: inline-block;
    padding: 0 6px;
  }
}
.drawing-item, .drawing-row-item{
  &:hover {
    & > .u-form-item{
      background: #f6f7ff;
      border-radius: 6px;
    }
    & > .drawing-item-copy, & > .drawing-item-delete{
      display: initial;
    }
  }
  & > .drawing-item-copy, & > .drawing-item-delete{
    display: none;
    position: absolute;
    top: -10px;
    width: 22px;
    height: 22px;
    line-height: 22px;
    text-align: center;
    border-radius: 50%;
    font-size: 12px;
    border: 1px solid;
    cursor: pointer;
    z-index: 1;
  }
  & > .drawing-item-copy{
    right: 56px;
    border-color: #409EFF;
    color: #409EFF;
    background: #fff;
    &:hover{
      background: #409EFF;
      color: #fff;
    }
  }
  & > .drawing-item-delete{
    right: 24px;
    border-color: #F56C6C;
    color: #F56C6C;
    background: #fff;
    &:hover{
      background: #F56C6C;
      color: #fff;
    }
  }
}

.u-radio-small-button {
  padding: 0 12px 0 9px !important;
}
</style>
