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
import { beautifierConf, titleCase } from './utils'
import { inputComponents as _ic, layoutComponents as _lc, selectComponents as _sc, formConf as importedFormConf } from './utils/config'
import { makeUpCss } from './utils/css'
import { cleanDrawingDefaultValue, drawingDefaultValue, initDrawingDefaultValue } from './utils/drawingDefault'
import { cssStyle, makeUpHtml, vueScript, vueTemplate } from './utils/html'
import { makeUpJs } from './utils/js'

// Wrap in ref for vuedraggable v4 v-model compatibility
const inputComponents = ref(_ic)
const selectComponents = ref(_sc)
const layoutComponents = ref(_lc)

const { t } = useI18n()

const activeData: any = ref({})
const tempActiveData: any = null
let oldActiveId: any = null

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

function onEnd() {}

function cloneComponent(origin: any) {
  const clone = deepCopy(origin)
  clone.renderKey = `${Date.now()}${Math.random()}`
  return clone
}

function addComponent(element: any) {
  const clone = deepCopy(element)
  clone.renderKey = `${Date.now()}${Math.random()}`
  drawingList.value.push(clone)
  activeId.value = clone.renderKey
  activeData.value = clone
}

function activeFormItem(element: any) {
  activeId.value = element.renderKey
  activeData.value = element
}

function drawingItemCopy(element: any) {
  const clone = deepCopy(element)
  clone.renderKey = `${Date.now()}${Math.random()}`
  drawingList.value.push(clone)
  activeId.value = clone.renderKey
  activeData.value = clone
}

function drawingItemDelete(element: any) {
  drawingList.value = drawingList.value.filter((item: any) => item.renderKey !== element.renderKey)
  if (activeId.value === element.renderKey) {
    activeId.value = ''
    activeData.value = {}
  }
}

function generate(type: string, fileName: string) {
  const html = makeUpHtml(drawingList.value, formConf.value)
  const script = makeUpJs(drawingList.value, formConf.value, generateType.value)
  const css = makeUpCss(formConf.value)
  const result = vueTemplate(html + script + css)
  if (cliEvent.value === 'copy') {
    const successful = navigator.clipboard.writeText(result)
    if (successful) {
      showAlert(t('searchForm.codeCopied'), 'success')
    }
  }
}

watch(activeId, (val) => {
  oldActiveId = val
  activeData.value = val ? drawingList.value.find((item: any) => item.renderKey === val) : {}
})

onMounted(() => {
  // @ts-expect-error ClipboardJS is loaded from CDN
  window.ClipboardJS = ClipboardJS
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
      :show-file-name="showFileName"
      @confirm="generate"
    />
    <CodeTypeDialog
      v-model="dialogVisible"
      :show-file-name="showFileName"
      @confirm="generate"
    />
    <input id="copyNode" type="hidden" />
  </div>
</template>
