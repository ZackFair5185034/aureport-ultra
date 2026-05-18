<script setup lang="ts">
import ClipboardJS from 'clipboard'
import beautifier from 'js-beautify'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import draggable from 'vuedraggable'

import logo from '@/assets/images/form-designer/logo.png'
import { deepCopy } from '@/components/utils'
import { showAlert, showConfirm } from '@/utils/comnon'

import CodeTypeDialog from './code-type-dialog/index.vue'
import DraggableItem from './draggable-item/index.vue'
import RightPanel from './right-panel/index.vue'

defineOptions({
  name: 'SearchFormDesigner',
})

const props = defineProps<{
  searchFormConfig?: any
}>()
import { beautifierConf, titleCase } from '@/views/report/designer/search-form/utils'
import { inputComponents as _ic, layoutComponents as _lc, selectComponents as _sc, formConf as importedFormConf } from '@/views/report/designer/search-form/utils/config'
import { makeUpCss } from '@/views/report/designer/search-form/utils/css'
import { cleanDrawingDefaultValue, drawingDefaultValue, initDrawingDefaultValue } from '@/views/report/designer/search-form/utils/drawingDefault'
import { cssStyle, makeUpHtml, vueScript, vueTemplate } from '@/views/report/designer/search-form/utils/html'
import { makeUpJs } from '@/views/report/designer/search-form/utils/js'

// Wrap in ref for vuedraggable v4 v-model compatibility
const inputComponents = ref(_ic)
const selectComponents = ref(_sc)
const layoutComponents = ref(_lc)

const { t } = useI18n()

const activeData: any = ref({})
const tempActiveData: any = ref(null)
let oldActiveId: any = null
let idGlobal = 100

initDrawingDefaultValue()

const formConf = ref(deepCopy(importedFormConf))

const drawingList = ref(deepCopy(drawingDefaultValue))
const activeId = ref('')

const dialogVisible = ref(false)
const showFileName = ref('form-generator.vue')

const generateType = ref('vue')
const cliEvent = ref('copy')

function copy() {
  showAlert(t('searchForm.codeCopied'), 'success')
}

function download() {
  dialogVisible.value = true
}

function empty() {
  showConfirm(t('searchForm.clearAllConfirm')).then(() => {
    drawingList.value = []
    cleanDrawingDefaultValue()
    activeId.value = ''
    activeData.value = {}
  }).catch(() => {})
}

function onEnd(obj: any, a: any) {
  if (obj.from !== obj.to) {
    activeData.value = tempActiveData.value
    activeId.value = String(idGlobal)
  }
}

function cloneComponent(origin: any) {
  const clone = deepCopy(origin)
  clone.formId = ++idGlobal
  clone.span = formConf.value.span
  clone.renderKey = `${Date.now()}${Math.random()}`
  if (!clone.layout)
    clone.layout = 'colFormItem'
  if (clone.layout === 'colFormItem') {
    clone.vModel = `field${idGlobal}`
    if (clone.placeholder !== undefined)
      clone.placeholder += clone.label
    tempActiveData.value = clone
  }
  else if (clone.layout === 'rowFormItem') {
    delete clone.label
    clone.componentName = `row${idGlobal}`
    tempActiveData.value = clone
  }
  return tempActiveData.value
}

function addComponent(element: any) {
  const clone = cloneComponent(element)
  drawingList.value.push(clone)
  activeFormItem(clone)
}

function activeFormItem(element: any) {
  activeId.value = element.formId
  activeData.value = element
}

function drawingItemCopy(element: any, parent: any[]) {
  const clone = deepCopy(element)
  clone.formId = ++idGlobal
  clone.renderKey = `${Date.now()}${Math.random()}`
  if (clone.layout === 'colFormItem') {
    clone.vModel = `field${idGlobal}`
  }
  else if (clone.layout === 'rowFormItem') {
    clone.componentName = `row${idGlobal}`
  }
  if (Array.isArray(clone.children)) {
    clone.children = clone.children.map((child: any) => createIdAndKey(child))
  }
  parent.push(clone)
  activeFormItem(clone)
}

function createIdAndKey(item: any) {
  item.formId = ++idGlobal
  item.renderKey = `${Date.now()}${Math.random()}`
  if (item.layout === 'colFormItem') {
    item.vModel = `field${idGlobal}`
  }
  else if (item.layout === 'rowFormItem') {
    item.componentName = `row${idGlobal}`
  }
  if (Array.isArray(item.children)) {
    item.children = item.children.map((child: any) => createIdAndKey(child))
  }
  return item
}

function drawingItemDelete(index: number, parent: any[]) {
  parent.splice(index, 1)
  if (drawingList.value.length > 0) {
    const lastItem = drawingList.value[drawingList.value.length - 1]
    activeId.value = String(lastItem.formId)
    activeData.value = lastItem
  }
  else {
    activeId.value = ''
    activeData.value = {}
  }
}

function tagChange(newTag: any) {
  const target = deepCopy(newTag)
  target.vModel = activeData.value.vModel
  target.formId = activeId.value
  target.span = activeData.value.span
  delete target.tag
  delete target.tagIcon
  delete target.document
  Object.keys(target).forEach((key) => {
    if (activeData.value[key] !== undefined && typeof activeData.value[key] === typeof target[key]) {
      target[key] = activeData.value[key]
    }
  })
  activeData.value = target
  updateDrawingList(target, drawingList.value)
}

function updateDrawingList(newTag: any, list: any[]) {
  const index = list.findIndex((item: any) => item.formId === activeId.value)
  if (index > -1) {
    list.splice(index, 1, newTag)
  }
  else {
    list.forEach((item) => {
      if (Array.isArray(item.children))
        updateDrawingList(newTag, item.children)
    })
  }
}

async function generate(type: string, fileName: string) {
  const html = makeUpHtml(drawingList.value, formConf.value)
  const script = makeUpJs(drawingList.value, formConf.value, generateType.value)
  const css = makeUpCss(formConf.value)
  const result = vueTemplate(html + script + css)
  if (cliEvent.value === 'copy') {
    await navigator.clipboard.writeText(result)
    showAlert(t('searchForm.codeCopied'), 'success')
  }
}

function handleConfirm(data: { type: string, fileName?: string }) {
  generate(data.type, data.fileName || 'form-generator.vue')
}

watch(activeId, (val) => {
  oldActiveId = val
  activeData.value = val ? drawingList.value.find((item: any) => item.formId === val) : {}
})

watch(() => props.searchFormConfig, (val: any) => {
  if (val) {
    const { fields, ...rest } = val
    formConf.value = deepCopy(rest)
    drawingList.value = deepCopy(fields)
  }
}, { immediate: true })

onMounted(() => {
  window.ClipboardJS = ClipboardJS as any
  // 防止 firefox 下拖拽会新打开一个选项卡
  document.body.ondrop = (event: DragEvent) => {
    event.preventDefault()
    event.stopPropagation()
  }
  document.addEventListener('keydown', (e: any) => {
    if (e.key === 's' && (e.ctrlKey || e.metaKey)) {
      e.preventDefault()
      showAlert(t('searchForm.saved'), 'success')
    }
  })
})

onBeforeUnmount(() => {})

defineExpose({
  getFormData: () => drawingList.value,
})
</script>

<template>
  <div class="container">
    <div class="left-board">
      <div class="logo-wrapper">
        <div class="logo">
          <img :src="logo" alt="logo" /> Form Generator
        </div>
      </div>
      <div class="left-scrollbar">
        <div class="components-list">
          <div class="components-title">
            {{ t('searchForm.inputComponents') }}
          </div>
          <draggable
            v-model="inputComponents"
            class="components-draggable"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent"
            item-key="tag"
            tag="div"
            :sort="false"
          >
            <template #item="{ element }">
              <div class="components-item" @click="addComponent(element)">
                <div class="components-body">
                  {{ element.label }}
                </div>
              </div>
            </template>
          </draggable>
          <div class="components-title">
            {{ t('searchForm.selectComponents') }}
          </div>
          <draggable
            v-model="selectComponents"
            class="components-draggable"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent"
            item-key="tag"
            tag="div"
            :sort="false"
          >
            <template #item="{ element }">
              <div class="components-item" @click="addComponent(element)">
                <div class="components-body">
                  {{ element.label }}
                </div>
              </div>
            </template>
          </draggable>
          <div class="components-title">
            {{ t('searchForm.layoutComponents') }}
          </div>
          <draggable
            v-model="layoutComponents"
            class="components-draggable"
            :group="{ name: 'componentsGroup', pull: 'clone', put: false }"
            :clone="cloneComponent"
            item-key="tag"
            tag="div"
            :sort="false"
          >
            <template #item="{ element }">
              <div class="components-item" @click="addComponent(element)">
                <div class="components-body">
                  {{ element.label }}
                </div>
              </div>
            </template>
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
            <draggable
              v-model="drawingList"
              class="drawing-board"
              :animation="340"
              group="componentsGroup"
              item-key="renderKey"
              tag="div"
            >
              <template #item="{ element, index }">
                <DraggableItem
                  :drawing-list="drawingList"
                  :element="element"
                  :index="index"
                  :active-id="activeId"
                  :form-conf="formConf"
                  @active-item="activeFormItem"
                  @copy-item="drawingItemCopy"
                  @delete-item="drawingItemDelete"
                />
              </template>
            </draggable>
            <div v-show="!drawingList.length" class="empty-info">
              {{ t('searchForm.dragComponents') }}
            </div>
          </u-form>
        </u-row>
      </div>
    </div>

    <RightPanel
      :active-data="activeData"
      :form-conf="formConf"
      :show-field="!!drawingList.length"
      @confirm="handleConfirm"
      @tag-change="tagChange"
      @update:active-data="(val: any) => { if (activeData && val) Object.assign(activeData, val) }"
      @update:form-conf="(val: any) => { if (formConf && val) Object.assign(formConf, val) }"
    />
    <CodeTypeDialog
      :visible="dialogVisible"
      :show-file-name="showFileName"
      @confirm="handleConfirm"
      @update:visible="val => dialogVisible = val"
    />
    <input id="copyNode" type="hidden" />
  </div>
</template>

<style>
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
  box-sizing: border-box;;
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
  padding-top: 15px;
  box-sizing: border-box;
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
