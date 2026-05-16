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
            v-model="inputComponents"
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
            class="components-draggable"
            v-model="selectComponents"
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
            class="components-draggable"
            v-model="layoutComponents"
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
              class="drawing-board"
              v-model="drawingList"
              :animation="340"
              group="componentsGroup"
              item-key="renderKey"
              tag="div"
            >
              <template #item="{ element, index }">
                <draggable-item
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

    <right-panel
      :active-data="activeData"
      :form-conf="formConf"
      :show-file-name="showFileName"
      @confirm="generate"
    />
    <code-type-dialog
      v-model="dialogVisible"
      :show-file-name="showFileName"
      @confirm="generate"
    />
    <input id="copyNode" type="hidden">
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import draggable from 'vuedraggable'
import beautifier from 'js-beautify'
import ClipboardJS from 'clipboard'

import RightPanel from './right-panel/index.vue'
import CodeTypeDialog from './code-type-dialog/index.vue'
import DraggableItem from './draggable-item/index.vue'

import { inputComponents as _ic, selectComponents as _sc, layoutComponents as _lc, formConf as importedFormConf } from './utils/config'
import { beautifierConf, titleCase } from './utils'
import { makeUpHtml, vueTemplate, vueScript, cssStyle } from './utils/html'
import { makeUpJs } from './utils/js'
import { makeUpCss } from './utils/css'
import { drawingDefaultValue, initDrawingDefaultValue, cleanDrawingDefaultValue } from './utils/drawingDefault'
import logo from '@/assets/images/form-designer/logo.png'
import { showAlert, showConfirm } from '@/utils/comnon'
import { deepCopy } from '@/components/utils'

// Wrap in ref for vuedraggable v4 v-model compatibility
const inputComponents = ref(_ic)
const selectComponents = ref(_sc)
const layoutComponents = ref(_lc)

const { t } = useI18n()

let activeData: any = ref({})
let tempActiveData: any = null
let oldActiveId: any = null

initDrawingDefaultValue()

const formConf = ref(deepCopy(importedFormConf))

const drawingList = ref(deepCopy(drawingDefaultValue))
const activeId = ref('')

const dialogVisible = ref(false)
const showFileName = ref('form-generator.vue')

const generateType = ref('vue')
const cliEvent = ref('copy')

const copy = () => {
  showAlert(t('searchForm.codeCopied'), 'success')
}

const download = () => {
  dialogVisible.value = true
}

const empty = () => {
  showConfirm(t('searchForm.clearAllConfirm')).then(() => {
    drawingList.value = []
    cleanDrawingDefaultValue()
    activeId.value = ''
    activeData.value = {}
  }).catch(() => {})
}

const onEnd = () => {}

const cloneComponent = (origin: any) => {
  const clone = deepCopy(origin)
  clone.renderKey = Date.now() + '' + Math.random()
  return clone
}

const addComponent = (element: any) => {
  const clone = deepCopy(element)
  clone.renderKey = Date.now() + '' + Math.random()
  drawingList.value.push(clone)
  activeId.value = clone.renderKey
  activeData.value = clone
}

const activeFormItem = (element: any) => {
  activeId.value = element.renderKey
  activeData.value = element
}

const drawingItemCopy = (element: any) => {
  const clone = deepCopy(element)
  clone.renderKey = Date.now() + '' + Math.random()
  drawingList.value.push(clone)
  activeId.value = clone.renderKey
  activeData.value = clone
}

const drawingItemDelete = (element: any) => {
  drawingList.value = drawingList.value.filter((item: any) => item.renderKey !== element.renderKey)
  if (activeId.value === element.renderKey) {
    activeId.value = ''
    activeData.value = {}
  }
}

const generate = (type: string, fileName: string) => {
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
  if (val) {
    activeData.value = drawingList.value.find((item: any) => item.renderKey === val)
  } else {
    activeData.value = {}
  }
})

onMounted(() => {
  // @ts-ignore
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
