<template>
  <div class="ud-toolbar" style="position: relative" ref="toolbar">
    <div class="ud-toolbar-title">
      <div class="file-info">
        {{ fileName }}
      </div>
      <PreviewTool ref="previewTool" />
      <PreviewPageTool ref="previewPageTool" />
      <SaveTool ref="saveTool" />
      <SaveAsTool ref="saveAsTool" />
      <OpenTool ref="openTool" />
      <ImportTool ref="importTool" />
      <UndoTool ref="undoTool" />
      <RedoTool ref="redoTool" />
      <SearchFormSwitchTool ref="searchFormSwitchTool" />
      <SettingsTool ref="settingsTool" />
    </div>
    <div class="ud-toolbar-content">
      <div class="toolbar-box">
        <MergeTool ref="mergeTool" :selectedCells="selectedCells" />
        <AlignLeftTool ref="alignLeftTool" :selectedCells="selectedCells" />
        <AlignTopTool ref="alignTool" :selectedCells="selectedCells" />
        <BorderTool ref="borderTool" :selectedCells="selectedCells" />
        <FontFamilyTool ref="fontFamilyTool" :selectedCells="selectedCells" />
        <FontSizeTool ref="fontSizeTool" :selectedCells="selectedCells" />
        <BoldTool ref="boldTool" :selectedCells="selectedCells" />
        <ItalicTool ref="italicTool" :selectedCells="selectedCells" />
        <UnderlineTool ref="underlineTool" :selectedCells="selectedCells" />
        <FontColorTool ref="fontColorTool" :selectedCells="selectedCells" />
        <BgColorTool ref="bgColorTool" :selectedCells="selectedCells" />
        <CrosstabTool ref="crosstabTool" :selectedCells="selectedCells" />
        <ImageTool ref="imageTool" :selectedCells="selectedCells" />
        <ChartTool ref="chartTool" :selectedCells="selectedCells" />
        <ZxingTool ref="zxingTool" :selectedCells="selectedCells" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useReportStore } from '@/stores/report'

defineOptions({ name: 'TopToolBar' })

const store = useReportStore()
const toolbarStyle = ref({
  position: 'relative' as const,
})

const props = withDefaults(defineProps<{
  selectedCells?: { rowIndex: number | null; colIndex: number | null; row2Index: number | null; col2Index: number | null }
}>(), {
  selectedCells: () => ({ rowIndex: null, colIndex: null, row2Index: null, col2Index: null })
})

const context = computed(() => {
  return store.context || {}
})

const fileName = computed(() => {
  const fileName = store.fileName
  if (fileName) {
    return decodeURIComponent(fileName)
  } else {
    return 'Blank'
  }
})

watch(fileName, (val) => {
  document.title = val
}, { immediate: true })
</script>

<style scoped>
.ud-toolbar{
  width: 100%;
  z-index: 10000;
}

.ud-toolbar-title{
  width: 100%;
  height: 50px;
  background-color: #00554a;
  color: white;
}

.ud-toolbar-content{
  background-color: #f3f5f7;
}

.toolbar-box{
  background-color: white;
  box-shadow: 0 2px 6px 0 rgba(0,0,0,.2);
}

.file-info{
  position: absolute;
  text-align: center;
  width: 100%;
  line-height: 50px;
  font-size: 14px;
}

.tool-button{
  font-size: 16px;
  margin: 7px 0;
}

.tool-button:hover {
  background-color: rgb(0 119 103 / 70%) !important
}

.info-button{
  font-size: 16px;
  margin: 2px 0;
  border: none;
}

</style>
