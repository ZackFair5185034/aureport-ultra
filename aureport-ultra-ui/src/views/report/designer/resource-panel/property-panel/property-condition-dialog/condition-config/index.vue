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

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import ColorConfig from './color-config/index.vue'
import FontConfig from './font-config/index.vue'
import AlignConfig from './align-config/index.vue'
import BorderConfig from './border-config/index.vue'
import ValueConfig from './value-config/index.vue'
import SizeConfig from './size-config/index.vue'
import PagingConfig from './paging-config/index.vue'
import LinkConfig from './link-config/index.vue'

defineOptions({ name: 'ConditionConfig' })

const props = withDefaults(defineProps<{
  item?: any
}>(), {
  item: null
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
  name: null
})

watch(() => props.item, (newVal) => {
  updateConfig(newVal)
}, { immediate: true, deep: true })

function updateConfig(config: any) {
  if (!config) {
    localItem.value = {
      cellStyle: {},
      rowHeight: null,
      colWidth: null,
      newValue: null,
      linkUrl: null,
      linkTargetWindow: null,
      linkParameters: null,
      paging: null,
      name: null
    }
  } else {
    const tempItem = JSON.parse(JSON.stringify(config))
    localItem.value = {
      cellStyle: tempItem.cellStyle || {},
      rowHeight: tempItem.rowHeight !== undefined ? tempItem.rowHeight : null,
      colWidth: tempItem.colWidth !== undefined ? tempItem.colWidth : null,
      newValue: tempItem.newValue !== undefined ? tempItem.newValue : null,
      linkUrl: tempItem.linkUrl !== undefined ? tempItem.linkUrl : null,
      linkTargetWindow: tempItem.linkTargetWindow !== undefined ? tempItem.linkTargetWindow : null,
      linkParameters: tempItem.linkParameters !== undefined ? tempItem.linkParameters : null,
      paging: tempItem.paging !== undefined ? tempItem.paging : null,
      name: tempItem.name !== undefined ? tempItem.name : null,
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
  } else if (type === 'bgcolor') {
    localItem.value.cellStyle.bgcolor = value
    localItem.value.cellStyle.bgcolorScope = scope
  }

  emitPropertyChange()
}

function handleFontChange({ type, checked, value, scope }: any) {
  if (!localItem.value.cellStyle) {
    localItem.value.cellStyle = {}
  }

  if (type === 'fontFamily') {
    localItem.value.cellStyle.fontFamily = value
    localItem.value.cellStyle.fontFamilyScope = scope
  } else if (type === 'fontSize') {
    localItem.value.cellStyle.fontSize = value
    localItem.value.cellStyle.fontSizeScope = scope
  } else if (type === 'bold') {
    localItem.value.cellStyle.bold = value
    localItem.value.cellStyle.boldScope = scope
  } else if (type === 'italic') {
    localItem.value.cellStyle.italic = value
    localItem.value.cellStyle.italicScope = scope
  } else if (type === 'underline') {
    localItem.value.cellStyle.underline = value
    localItem.value.cellStyle.underlineScope = scope
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
  } else if (type === 'valign') {
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
  } else if (type === 'format') {
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
  } else if (type === 'colWidth') {
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
