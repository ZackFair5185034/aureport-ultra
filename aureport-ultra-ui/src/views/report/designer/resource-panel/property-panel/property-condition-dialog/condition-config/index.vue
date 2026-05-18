<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import AlignConfig from './align-config/index.vue'
import BorderConfig from './border-config/index.vue'
import ColorConfig from './color-config/index.vue'
import FontConfig from './font-config/index.vue'
import LinkConfig from './link-config/index.vue'
import PagingConfig from './paging-config/index.vue'
import SizeConfig from './size-config/index.vue'
import ValueConfig from './value-config/index.vue'

defineOptions({ name: 'ConditionConfig' })

const props = withDefaults(defineProps<{
  item?: any
}>(), {
  item: null,
})

const emit = defineEmits<{
  (e: 'property-changed', value: any): void
}>()

const localItem = ref<any>({
  cellStyle: null,
  rowHeight: null,
  colWidth: null,
  newValue: null,
  linkUrl: null,
  linkTargetWindow: null,
  linkParameters: null,
  paging: null,
  name: null,
})

watch(() => props.item, (newVal) => {
  updateConfig(newVal)
}, { immediate: true, deep: true })

function updateConfig(config: any) {
  if (config) {
    const tempItem = structuredClone(config)
    localItem.value = {
      cellStyle: tempItem.cellStyle || {},
      rowHeight: tempItem.rowHeight === undefined ? null : tempItem.rowHeight,
      colWidth: tempItem.colWidth === undefined ? null : tempItem.colWidth,
      newValue: tempItem.newValue === undefined ? null : tempItem.newValue,
      linkUrl: tempItem.linkUrl === undefined ? null : tempItem.linkUrl,
      linkTargetWindow: tempItem.linkTargetWindow === undefined ? null : tempItem.linkTargetWindow,
      linkParameters: tempItem.linkParameters === undefined ? null : tempItem.linkParameters,
      paging: tempItem.paging === undefined ? null : tempItem.paging,
      name: tempItem.name === undefined ? null : tempItem.name,
    }
  }
  else {
    localItem.value = {
      cellStyle: {},
      rowHeight: null,
      colWidth: null,
      newValue: null,
      linkUrl: null,
      linkTargetWindow: null,
      linkParameters: null,
      paging: null,
      name: null,
    }
  }
}

function handleColorChange({ type, checked, value, scope }: any) {
  if (!localItem.value.cellStyle) {
    localItem.value.cellStyle = {}
  }

  if (type === 'forecolor') {
    localItem.value.cellStyle.forecolor = value
    localItem.value.cellStyle.forecolorScope = scope
  }
  else if (type === 'bgcolor') {
    localItem.value.cellStyle.bgcolor = value
    localItem.value.cellStyle.bgcolorScope = scope
  }

  emitPropertyChange()
}

function handleFontChange({ type, checked, value, scope }: any) {
  if (!localItem.value.cellStyle) {
    localItem.value.cellStyle = {}
  }

  switch (type) {
    case 'fontFamily': {
      localItem.value.cellStyle.fontFamily = value
      localItem.value.cellStyle.fontFamilyScope = scope

      break
    }

    case 'fontSize': {
      localItem.value.cellStyle.fontSize = value
      localItem.value.cellStyle.fontSizeScope = scope

      break
    }

    case 'bold': {
      localItem.value.cellStyle.bold = value
      localItem.value.cellStyle.boldScope = scope

      break
    }

    case 'italic': {
      localItem.value.cellStyle.italic = value
      localItem.value.cellStyle.italicScope = scope

      break
    }

    case 'underline': {
      localItem.value.cellStyle.underline = value
      localItem.value.cellStyle.underlineScope = scope

      break
    }
  // No default
  }

  emitPropertyChange()
}

function handleAlignChange({ type, checked, value, scope }: any) {
  if (!localItem.value.cellStyle) {
    localItem.value.cellStyle = {}
  }

  if (type === 'align') {
    localItem.value.cellStyle.align = value
    localItem.value.cellStyle.alignScope = scope
  }
  else if (type === 'valign') {
    localItem.value.cellStyle.valign = value
    localItem.value.cellStyle.valignScope = scope
  }

  emitPropertyChange()
}

function handleBorderChange({ checked, borders }: any) {
  if (!localItem.value.cellStyle) {
    localItem.value.cellStyle = {}
  }

  localItem.value.cellStyle.leftBorder = borders.leftBorder
  localItem.value.cellStyle.rightBorder = borders.rightBorder
  localItem.value.cellStyle.topBorder = borders.topBorder
  localItem.value.cellStyle.bottomBorder = borders.bottomBorder

  emitPropertyChange()
}

function handleBorderSave(borderData: any) {
  if (localItem.value.cellStyle) {
    localItem.value.cellStyle.topBorder = borderData.topBorder
    localItem.value.cellStyle.bottomBorder = borderData.bottomBorder
    localItem.value.cellStyle.leftBorder = borderData.leftBorder
    localItem.value.cellStyle.rightBorder = borderData.rightBorder
  }

  emitPropertyChange()
}

function handleValueChange({ type, checked, value }: any) {
  if (type === 'newValue') {
    localItem.value.newValue = value
  }
  else if (type === 'format') {
    if (!localItem.value.cellStyle) {
      localItem.value.cellStyle = {}
    }

    localItem.value.cellStyle.format = value
  }

  emitPropertyChange()
}

function handleSizeChange({ type, checked, value }: any) {
  if (type === 'rowHeight') {
    localItem.value.rowHeight = value
  }
  else if (type === 'colWidth') {
    localItem.value.colWidth = value
  }

  emitPropertyChange()
}

function handlePagingChange({ checked, paging }: any) {
  localItem.value.paging = paging
  emitPropertyChange()
}

function handleLinkChange({ checked, linkUrl, linkTargetWindow, linkParameters }: any) {
  localItem.value.linkUrl = linkUrl
  localItem.value.linkTargetWindow = linkTargetWindow
  localItem.value.linkParameters = linkParameters
  emitPropertyChange()
}

function emitPropertyChange() {
  nextTick(() => {
    emit('property-changed', localItem.value)
  })
}
</script>

<template>
  <div>
    <ColorConfig
      :cell-style="localItem.cellStyle"
      @color-change="handleColorChange"
    />

    <FontConfig
      :cell-style="localItem.cellStyle"
      @font-change="handleFontChange"
    />

    <AlignConfig
      :cell-style="localItem.cellStyle"
      @align-change="handleAlignChange"
    />

    <BorderConfig
      :cell-style="localItem.cellStyle"
      @border-change="handleBorderChange"
      @border-save="handleBorderSave"
    />

    <ValueConfig
      :cell-style="localItem.cellStyle"
      :new-value="localItem.newValue"
      @value-change="handleValueChange"
    />

    <SizeConfig
      :row-height="localItem.rowHeight"
      :col-width="localItem.colWidth"
      @size-change="handleSizeChange"
    />

    <PagingConfig
      :paging="localItem.paging"
      @paging-change="handlePagingChange"
    />

    <LinkConfig
      :link-url="localItem.linkUrl"
      :link-target-window="localItem.linkTargetWindow"
      :link-parameters="localItem.linkParameters"
      @link-change="handleLinkChange"
    />
  </div>
</template>
