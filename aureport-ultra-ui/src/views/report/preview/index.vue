<script setup lang="ts">
import { Chart, registerables } from 'chart.js'
import ChartDataLabels from 'chartjs-plugin-datalabels'

import { useI18n } from 'vue-i18n'
import { loadHtml, loadReportData } from '@/api/preview'
import showLoading from '@/components/loading/instance'

import { $t, setLocale as setAppLocale } from '@/locales'
import { isMobile, showAlert } from '@/utils/comnon'
import { updateUrlParams } from '@/utils/url'

import SearchBox from '@/views/report/preview/search-box/index.vue'
import ToolBox from '@/views/report/preview/tool-box/index.vue'
import { buildChartDatas } from '@/views/report/preview/utils/chart'

defineOptions({ name: 'PreviewPage' })

const emit = defineEmits<{
  (e: 'ready', data: { reportData: unknown }): void
  (e: 'error', err: unknown): void
}>()

Chart.register(...registerables, ChartDataLabels)

const { t } = useI18n()

// data
const reportData = ref<any>(null)
const currentReportName = ref('')
const totalPage = ref(0)
const currentPage = ref(1)
const searchFormParameters = ref({})
const searchFormConfig = ref<any>(null)
const reportPath = ref('')
const mode = ref('')
const toolsInfo = ref<string | null>(null)
const pageIndex = ref<string | number | null>(null)
const extraParams = ref<Record<string, string>>({})
const isShowSearchForm = ref(true)

// computed
const pageEnable = computed(() => {
  return pageIndex.value != null && parseInt(String(pageIndex.value)) > 0
})

const isRenderSearchForm = computed(() => {
  return !!(searchFormConfig.value?.fields?.length)
})

// methods
function toggleCollapse() {
  isShowSearchForm.value = !isShowSearchForm.value
}

async function initReport() {
  const result = await fetchPageData(pageIndex.value)
  if (!result)
    return

  setWebTitle()
  searchFormConfig.value = result.searchForm
  injectReportStyle(result.style)
  initFunctions()

  emit('ready', { reportData: result })
}

function setWebTitle() {
  currentReportName.value = extraParams.value._title || reportPath.value
  if (currentReportName.value) {
    currentReportName.value = decodeURIComponent(currentReportName.value)
  }

  if (currentReportName.value.endsWith('.ureport.xml')) {
    currentReportName.value = currentReportName.value.replace('.ureport.xml', '')
  }

  document.title = currentReportName.value
}

async function loadPageData(pageIdx: number | string | null) {
  const result = await fetchPageData(pageIdx)
  if (!result)
    return
  renderReportContent(result)
}

async function fetchPageData(pageIdx: string | number | null) {
  const loadingInstance = showLoading({
    text: $t('preview.loading.report'),
  })
  try {
    const params = getReportParams(pageIdx)
    const result: any = await loadHtml(params)
    result.tools = computeTools()
    Object.freeze(result)
    reportData.value = result
    currentPage.value = parseInt(result.pageIndex || pageIdx as any) || 1
    totalPage.value = extractTotalPage(result)
    return result
  }
  catch (error_) {
    const error = error_ as any
    if (error.msg) {
      showAlert($t('preview.error.loadReportFail') + t('colon') + error.msg, { useHTMLString: true })
    }
    else {
      showAlert(t('preview.error.loadReportFail'))
    }

    emit('error', error)
    return null
  }
  finally {
    loadingInstance.close()
  }
}

async function handleFormSubmit(formData: any) {
  searchFormParameters.value = formData
  try {
    await loadAndRenderReport({ resetToFirstPage: pageEnable.value })
  }
  catch (error) {
    console.error('提交搜索表单失败:', error)
  }
}

async function loadAndRenderReport(options: { resetToFirstPage?: boolean } = {}) {
  const { resetToFirstPage = false } = options

  let pageIdx: number | undefined
  if (resetToFirstPage) {
    currentPage.value = 1
    pageIdx = 1
  }
  else if (totalPage.value > 0 && currentPage.value) {
    if (currentPage.value > totalPage.value) {
      currentPage.value = 1
    }

    pageIdx = currentPage.value
  }

  const params = getReportParams(pageIdx)
  const report: any = await loadReportData(params)
  renderReportContent(report)

  totalPage.value = extractTotalPage(report)
  currentPage.value = report.pageIndex || currentPage.value

  const totalPageLabel = document.querySelector('#totalPageLabel')
  if (totalPageLabel) {
    totalPageLabel.textContent = String(totalPage.value)
  }

  return report
}

function parseParamsFromUrl() {
  const searchParams = new URLSearchParams(window.location.search)
  reportPath.value = searchParams.get('reportPath') || ''
  mode.value = searchParams.get('mode') || ''
  toolsInfo.value = searchParams.get('_t')
  pageIndex.value = searchParams.get('_i')
  extraParams.value = {} as Record<string, string>
  const localKeys = ['_i', '_t', '_r', '_n', 'mode', 'reportPath']
  for (const [key, value] of searchParams) {
    if (!localKeys.includes(key)) {
      extraParams.value[key] = value
    }
  }
}

function handlePopState() {
  const oldPageIndex = pageIndex.value
  parseParamsFromUrl()
  if (oldPageIndex !== pageIndex.value) {
    loadPageData(pageIndex.value || 1)
  }
}

function getReportParams(pageIdx: string | number | null | undefined) {
  if (!reportPath.value) {
    throw new Error(t('preview.error.fileParamMissing'))
  }

  const params: Record<string, any> = { reportPath: reportPath.value }

  if (mode.value)
    params.mode = mode.value
  if (pageIdx != null)
    params._i = pageIdx
  if (toolsInfo.value != null)
    params._t = toolsInfo.value

  Object.assign(params, extraParams.value)
  mergeSearchFormParams(params as any)

  return params
}

function mergeSearchFormParams(target: Record<string, any>) {
  if (!searchFormParameters.value)
    return
  for (const key of Object.keys(searchFormParameters.value)) {
    if ((searchFormParameters.value as Record<string, any>)[key]) {
      target[key] = (searchFormParameters.value as Record<string, any>)[key]
    }
  }
}

function renderReportContent(reportDataObj: any) {
  const tableContainer = document.querySelector('#report-table')
  if (tableContainer) {
    tableContainer.innerHTML = reportDataObj.content
  }

  buildChartDatas(reportDataObj.chartDatas)
}

function extractTotalPage(reportDataObj: any): number {
  return reportDataObj.totalPageWithCol || reportDataObj.totalPage || 0
}

function computeTools() {
  const isMobileDevice = isMobile()
  const allOff = { show: false, print: false, pdfPrint: false, pdfPreviewPrint: false, pdf: false, word: false, excel: false, pagingExcel: false, sheetPagingExcel: false, paging: false }
  const allOn = { show: true, print: true, pdfPrint: true, pdfPreviewPrint: true, pdf: true, word: true, excel: true, pagingExcel: true, sheetPagingExcel: true, paging: true }

  if (isMobileDevice)
    return allOff

  if (toolsInfo.value == null || toolsInfo.value === '')
    return allOn
  if (String(toolsInfo.value) === '0')
    return allOff

  const tools = { ...allOff, show: true }
  const map: Record<string, string> = {
    1: 'print',
    2: 'pdfPrint',
    3: 'pdfPreviewPrint',
    4: 'pdf',
    5: 'word',
    6: 'excel',
    7: 'pagingExcel',
    8: 'sheetPagingExcel',
    9: 'paging',
  }
  for (const key of String(toolsInfo.value).split(',')) {
    if (map[key])
      (tools as any)[map[key]] = true
  }

  return tools
}

function injectReportStyle(style?: string) {
  let styleElement = document.querySelector('#report-table-style')
  if (!styleElement) {
    styleElement = document.createElement('style')
    styleElement.id = 'report-table-style'
    document.head.append(styleElement)
  }

  styleElement.textContent = style || ''
}

async function refreshReport(second: number) {
  try {
    await loadAndRenderReport({ resetToFirstPage: false })
  }
  catch (error_) {
    const error = error_ as any
    console.error('刷新数据失败:', error)
    if (error.msg) {
      showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
    }
    else {
      showAlert(t('dialog.save.fail'))
    }
  }
  finally {
    setTimeout(refreshReport, second, second)
  }
}

function intervalRefresh(value: number, totalPageVal: number) {
  if (!value)
    return
  totalPage.value = totalPageVal
  const second = value * 1000
  setTimeout(refreshReport, second, second)
}

function handlePageChange(newPageIndex: string | number | null) {
  updateUrlParams({ _i: newPageIndex }, true)
  pageIndex.value = newPageIndex

  if (newPageIndex == null) {
    initReport()
  }
  else {
    loadPageData(newPageIndex)
  }
}

function initFunctions() {
  setTimeout(() => {
    if (reportData.value?.intervalRefreshValue > 0) {
      intervalRefresh(reportData.value.intervalRefreshValue, totalPage.value)
    }

    if (reportData.value?.chartDatas?.length > 0) {
      buildChartDatas(reportData.value.chartDatas)
    }
  }, 500)
}

function handlePageEnableChange(pageEnabled: boolean) {
  if (pageEnabled) {
    handlePageChange(1)
  }
  else {
    updateUrlParams({ _i: null })
    pageIndex.value = null
    initReport()
  }
}

function refresh() {
  parseParamsFromUrl()
  initReport()
}

function setReportPath(path: string) {
  updateUrlParams({ reportPath: path })
  reportPath.value = path
  if (path)
    initReport()
}

function setParams(params: Record<string, any>) {
  updateUrlParams(params)
  parseParamsFromUrl()
  initReport()
}

function setLocale(locale: string) {
  setAppLocale(locale)
}

// lifecycle
onMounted(async () => {
  parseParamsFromUrl()
  window.addEventListener('popstate', handlePopState)
  await initReport()
  isShowSearchForm.value = isRenderSearchForm.value
})

onBeforeUnmount(() => {
  window.removeEventListener('popstate', handlePopState)
})

defineExpose({ parseParamsFromUrl, initReport, refresh, handlePageChange })
</script>

<template>
  <div id="preview-container" :class="{ 'right-collapsed': !isShowSearchForm }">
    <div class="preview-left">
      <ToolBox
        :reportData="reportData"
        :currentPage="currentPage"
        :pageEnable="pageEnable"
        :searchFormParameters="searchFormParameters"
        @page-change="handlePageChange"
        @page-enable-change="handlePageEnableChange"
      />
      <div
        v-if="reportData && reportData.content" id="report-table"
        :style="{ float: reportData.reportAlign || 'left' }"
        v-html="reportData.content"
      />
    </div>
    <div v-if="isRenderSearchForm" class="collapse-btn" @click="toggleCollapse">
      <i class="iconfont collapse-icon" :class="isShowSearchForm ? 'icon-right' : 'icon-left'" />
    </div>
    <div
      v-if="isRenderSearchForm"
      class="preview-right"
      :class="{ collapsed: !isShowSearchForm }"
    >
      <div class="preview-right-content">
        <SearchBox
          :searchFormConfig="searchFormConfig"
          @submit="handleFormSubmit"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
#preview-container {
  width: 100%;
  height: 100vh;
  padding: 10px 10px 0 10px;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: row;
  position: relative;
  background: white;
}

.preview-left {
  flex: 1;
  overflow: auto;
  padding-right: 10px;
  min-width: 0;
}

.preview-right {
  width: 400px;
  flex-shrink: 0;
  overflow-y: auto;
  transition: width 0.3s ease;
  border-left: 1px solid #e8e8e8;
}

.preview-right-content {
  padding-left: 10px;
}

.preview-right.collapsed {
  width: 0;
  border-left: none;
  overflow: hidden;
}

.preview-right.collapsed .preview-right-content {
  display: none;
}

.collapse-btn {
  width: 32px;
  height: 32px;
  position: absolute;
  top: 25%;
  right: 413px;
  transform: translate(50%, -50%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  z-index: 10;
}

#preview-container.right-collapsed .collapse-btn {
  right: 0;
  transform: translate(0, -50%);
}

#report-table {
  margin-top: 10px;
}

.collapse-btn:hover {
  background: #f5f5f5;
  border-color: #1890ff;
  color: #1890ff;
  transform: translate(50%, -50%) scale(1.1);
}

#preview-container.right-collapsed .collapse-btn:hover {
  transform: translate(0, -50%) scale(1.1);
}

.collapse-icon {
  font-size: 16px;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  #preview-container {
    padding: 5px;
    flex-direction: column;
  }
  .preview-left {
    width: 100%;
    padding-right: 0;
    padding-bottom: 10px;
  }
  .preview-right {
    width: 100%;
    border-left: none;
    border-top: 1px solid #e8e8e8;
  }
  .preview-right-content {
    padding-left: 0;
    padding-top: 10px;
  }
  .preview-right.collapsed {
    width: 0;
    padding-top: 0;
    border-top: none;
  }
  .collapse-btn {
    display: none;
  }
}
</style>
