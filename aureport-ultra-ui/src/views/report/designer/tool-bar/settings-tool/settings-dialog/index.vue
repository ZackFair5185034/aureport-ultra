<template>
  <UDialog
    :title="$t('dialog.setting.title')"
    width="800px"
    :visible="dialogVisible"
    :show-close="true"
    @update:visible="handleDialogVisibleChange"
    @close="handleClose"
  >
    <div class="settings-dialog">
      <u-tabs v-model="activeTab">
        <u-tab-pane :label="$t('dialog.setting.pageSetting')" index="page"></u-tab-pane>
        <u-tab-pane :label="$t('dialog.setting.headerFooterSetting')" index="headerFooter"></u-tab-pane>
        <u-tab-pane :label="$t('dialog.setting.pagingSetting')" index="paging"></u-tab-pane>
        <u-tab-pane :label="$t('dialog.setting.columnSetting')" index="column"></u-tab-pane>
      </u-tabs>

      <div class="tab-content">
        <div v-show="activeTab === 'page'">
          <page-settings
            :paper="paper"
            @update:paper="updatePaper"
            @paper-type-change="handlePaperTypeChange"
            @paper-size-change="updatePaperSize"
            @margins-change="updateMargins"
            @orientation-change="handleOrientationChange"
            @html-align-change="handleHtmlAlignChange"
            @html-interval-refresh-value-change="handleHtmlIntervalRefreshValueChange"
            @background-image-change="updateBackgroundImage"
          />
        </div>

        <div v-show="activeTab === 'headerFooter'">
          <header-footer-settings
            :header="header"
            :footer="footer"
            @update:header="updateHeader"
            @update:footer="updateFooter"
            @open-header-font-dialog="openHeaderFontDialog"
            @open-footer-font-dialog="openFooterFontDialog"
            @header-margin-change="updateHeaderMargin"
            @footer-margin-change="updateFooterMargin"
            @header-footer-change="validateHeaderFooter"
          />
        </div>

        <div v-show="activeTab === 'paging'">
          <paging-settings
            :paper="paper"
            @update:paper="updatePaper"
            @paging-mode-change="handlePagingModeChange"
            @fix-rows-change="handleFixRowsChange"
          />
        </div>

        <div v-show="activeTab === 'column'">
          <column-settings
            :paper="paper"
            @update:paper="updatePaper"
            @column-enabled-change="handleColumnEnabledChange"
            @column-count-change="handleColumnCountChange"
            @column-margin-change="updateColumnMargin"
          />
        </div>
      </div>
    </div>

    <FontSettingDialog
      ref="headerFontDialog"
      :visible="headerFontDialogVisible"
      :font-style="header"
      @close="handleHeaderFontDialogClose"
      @ok="handleHeaderFontDialogOk"
    />

    <FontSettingDialog
      ref="footerFontDialog"
      :visible="footerFontDialogVisible"
      :font-style="footer"
      @close="handleFooterFontDialogClose"
      @ok="handleFooterFontDialogOk"
    />

    <template #footer><div class="div-footer-align">
      <u-button @click="handleClose" type="info" class="btn-cancel">{{ $t('dialog.common.cancel') }}</u-button>
      <u-button @click="handleOk">{{ $t('dialog.common.ok') }}</u-button>
    </div></template>
  </UDialog>
</template>

<script setup lang="ts">
// @ts-ignore
import ColumnSettings from './column/index.vue'
// @ts-ignore
import PageSettings from './page/index.vue'
// @ts-ignore
import HeaderFooterSettings from './headerFooter/index.vue'
// @ts-ignore
import PagingSettings from './paging/index.vue'

import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReportStore } from '@/stores/report'
import { showAlert } from '@/utils/comnon'
import { buildPageSizeList, mmToPoint, setDirty } from '@/utils/table'
import { deepCopy } from '@/components/utils/index'
import { updateReportDef } from '@/utils/contextActions'

defineOptions({ name: 'SettingsDialog' })

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'close'): void
  (e: 'ok'): void
}>()

const props = withDefaults(defineProps<{
  visible?: boolean
}>(), {
  visible: false
})

const { t } = useI18n()
const store = useReportStore()

const dialogVisible = ref(false)
const activeTab = ref('page')
const initializing = ref(false)
const paperSizeList = buildPageSizeList()

const paper = ref({
  paperType: 'A4',
  width: mmToPoint(210),
  height: mmToPoint(297),
  leftMargin: mmToPoint(20),
  rightMargin: mmToPoint(20),
  topMargin: mmToPoint(20),
  bottomMargin: mmToPoint(20),
  orientation: 'portrait',
  htmlReportAlign: 'left',
  htmlIntervalRefreshValue: 3,
  bgImage: '',
  pagingMode: 'fitpage',
  fixRows: 30,
  columnEnabled: false,
  columnCount: 2,
  columnMargin: mmToPoint(10)
})

const header = ref({
  left: '', center: '', right: '', margin: 30,
  fontFamily: '宋体', fontSize: 10, forecolor: '0,0,0',
  bold: false, italic: false, underline: false
})

const footer = ref({
  left: '', center: '', right: '', margin: 30,
  fontFamily: '宋体', fontSize: 10, forecolor: '0,0,0',
  bold: false, italic: false, underline: false
})

const headerFontDialogVisible = ref(false)
const footerFontDialogVisible = ref(false)

const context = computed(() => store.context)

dialogVisible.value = props.visible
if (props.visible && context.value) {
  initializeData()
}

watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal && context.value) {
    initializeData()
  }
})

watch(context, (newVal) => {
  if (newVal) {
    initializeData()
  }
})

function initializeData() {
  if (!context.value) {
    console.error('context 未定义，无法初始化数据')
    return
  }
  if (!context.value.reportDef) {
    console.error('context.reportDef 未定义，无法初始化数据')
    return
  }

  initializing.value = true
  const reportDefCopy = deepCopy(context.value.reportDef)

  paper.value = { ...(reportDefCopy.paper as any) }
  if (!paper.value.fixRows || paper.value.fixRows < 1) {
    paper.value.fixRows = 30
  }

  if (!reportDefCopy.header) {
    reportDefCopy.header = { margin: 30 }
  }
  if (!reportDefCopy.footer) {
    reportDefCopy.footer = { margin: 30 }
  }

  header.value = { ...(reportDefCopy.header as any) }
  footer.value = { ...(reportDefCopy.footer as any) }
  initializing.value = false
}

function handleClose() {
  dialogVisible.value = false
  emit('update:visible', false)
  emit('close')
}

function handleDialogVisibleChange(val: boolean) {
  dialogVisible.value = val
  if (!val) {
    emit('close')
  }
}

function handleOk() {
  if (!context.value || !context.value.reportDef) {
    emit('update:visible', false)
    emit('ok')
    dialogVisible.value = false
    return
  }

  emit('update:visible', false)
  emit('ok')

  const newPaper = deepCopy(paper.value)
  const newHeader = deepCopy(header.value)
  const newFooter = deepCopy(footer.value)

  updateReportDef({
    ...context.value.reportDef,
    paper: newPaper,
    header: newHeader,
    footer: newFooter
  })

  dialogVisible.value = false
}

function updatePaperSize() {
  if (paper.value.paperType !== 'CUSTOM') return
  if (context.value && (context.value as any).printLine) {
    (context.value as any).printLine.refresh()
  }
  setDirty()
}

function updateMargins() {
  if (context.value && (context.value as any).printLine) {
    (context.value as any).printLine.refresh()
  }
  setDirty()
}

function updateBackgroundImage() {
  if (paper.value.bgImage === '') {
    const elements = document.querySelectorAll('.ht_master')
    elements.forEach(el => {
      (el as HTMLElement).style.background = 'transparent'
    })
  } else {
    const elements = document.querySelectorAll('.ht_master')
    elements.forEach(el => {
      (el as HTMLElement).style.background = `url(${paper.value.bgImage}) 50px 26px no-repeat`
    })
  }
  setDirty()
}

function updateHeaderMargin() { setDirty() }
function updateFooterMargin() { setDirty() }
function updateColumnMargin() { setDirty() }

function updatePaper(value: any) { paper.value = value }
function updateHeader(value: any) { header.value = value }
function updateFooter(value: any) { footer.value = value }

function handleFixRowsChange(value: number) {
  if (initializing.value) return
  if (paper.value.pagingMode === 'fixrows' && value < 1) {
    showAlert(t('dialog.setting.fixRowsTip'))
    return
  }
  setDirty()
}

function handleHtmlIntervalRefreshValueChange(value: number) {
  if (isNaN(value) || value < 0) {
    showAlert(t('dialog.setting.secondTip'))
    return
  }
  setDirty()
}

function openHeaderFontDialog() { headerFontDialogVisible.value = true }
function openFooterFontDialog() { footerFontDialogVisible.value = true }
function handleHeaderFontDialogClose() { headerFontDialogVisible.value = false }
function handleFooterFontDialogClose() { footerFontDialogVisible.value = false }

function handleHeaderFontDialogOk(style: any) {
  if (style) {
    header.value.fontFamily = style.fontFamily
    header.value.fontSize = style.fontSize
    header.value.forecolor = style.forecolor
    header.value.bold = style.bold
    header.value.italic = style.italic
    header.value.underline = style.underline
    setDirty()
  }
  headerFontDialogVisible.value = false
}

function handleFooterFontDialogOk(style: any) {
  if (style) {
    footer.value.fontFamily = style.fontFamily
    footer.value.fontSize = style.fontSize
    footer.value.forecolor = style.forecolor
    footer.value.bold = style.bold
    footer.value.italic = style.italic
    footer.value.underline = style.underline
    setDirty()
  }
  footerFontDialogVisible.value = false
}

function validateHeaderFooter() { setDirty() }

function handlePaperTypeChange(value: string) {
  if (value !== 'CUSTOM') {
    const pageSize: any = (paperSizeList as any)[value]
    paper.value.width = mmToPoint(pageSize.width)
    paper.value.height = mmToPoint(pageSize.height)
    if (context.value && (context.value as any).printLine) {
      (context.value as any).printLine.refresh()
    }
  }
  setDirty()
}

function handleOrientationChange() {
  if (context.value && (context.value as any).printLine) {
    (context.value as any).printLine.refresh()
  }
  setDirty()
}

function handleHtmlAlignChange() { setDirty() }
function handleColumnCountChange() { setDirty() }
function handlePagingModeChange() { setDirty() }
function handleColumnEnabledChange() { setDirty() }
</script>

<style scoped>

.settings-dialog{
  height: 400px;
}

.tab-content {
  padding: 10px 0;
}

.div-footer-align {
  text-align: right;
}

.btn-cancel {
  margin-right: 10px;
}
</style>
