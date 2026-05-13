<template>
  <div v-if="reportData && reportData.tools && reportData.tools.show"
       class="tools-content">
    <div :style="{ textAlign: reportData.reportAlign }">
      <u-button v-if="reportData.tools.print"
                type="info"
                :title="t('preview.buttons.print')"
                class="p-button"
                @click="print"
      >
        <img src="@/assets/icons/print.svg" width="20px" height="20px">
      </u-button>

      <u-button v-if="reportData.tools.pdfPrint"
                type="info"
                :title="t('preview.buttons.pdfDirectPrint')"
                class="p-button"
                @click="printDirectPdf">
        <img src="@/assets/icons/pdf-direct-print.svg" width="20px" height="20px">
      </u-button>

      <u-button v-if="reportData.tools.pdfPreviewPrint"
                type="info"
                :title="t('preview.buttons.pdfPreviewPrint')"
                class="p-button"
                @click="printPdf">
        <img src="@/assets/icons/pdf-print.svg" width="20px" height="20px">
      </u-button>

      <u-button v-if="reportData.tools.pdf"
                type="info"
                :title="t('preview.buttons.exportPdf')"
                class="p-button"
                @click="exportPdf">
        <img src="@/assets/icons/pdf.svg" width="20px" height="20px">
      </u-button>

      <u-button v-if="reportData.tools.word"
                type="info"
                :title="t('preview.buttons.exportWord')"
                class="p-button"
                @click="exportWord">
        <img src="@/assets/icons/word.svg" width="20px" height="20px">
      </u-button>

      <u-button v-if="reportData.tools.excel"
                type="info"
                :title="t('preview.buttons.exportExcel')"
                class="p-button"
                @click="exportExcel">
        <img src="@/assets/icons/excel.svg" width="20px" height="20px">
      </u-button>

      <u-button v-if="reportData.tools.pagingExcel"
                type="info"
                :title="t('preview.buttons.exportExcelPaging')"
                class="p-button"
                @click="exportExcelPaging">
        <img src="@/assets/icons/excel-paging.svg" width="20px" height="20px">
      </u-button>

      <u-button v-if="reportData.tools.sheetPagingExcel"
                type="info"
                class="p-button"
                :title="t('preview.buttons.exportExcelSheetPaging')"
                @click="exportExcelPagingSheet"
      >
        <img src="@/assets/icons/excel-with-paging-sheet.svg" width="20px" height="20px">
      </u-button>

      <div v-if="reportData.tools.paging" class="btn-group">
        <ButtonGroup
            :buttonText="pageEnable ? t('preview.paging.pagingPreview') : t('preview.paging.preview')"
            :showText="true"
            :buttonStyle="{ background: '#f8f8f8', border: 'none', color: '#337ab7' }"
            :menuItems="pagingMenuItems"
            :customClass="'p-tool-button'"
        />
      </div>

      <u-button v-if="reportData.tools.paging && currentPage > 1"
                type="info"
                :title="t('preview.buttons.prevPage')"
                class="p-button paging-button"
                @click="goToPrevPage">
        {{ t('preview.buttons.prevPage') }}
      </u-button>

      <div v-if="pageEnable" class="btn-group">
        <ButtonGroup
            :buttonText="`共${reportData.totalPageWithCol}页，当前第${currentPage}页`"
            :showText="true"
            :buttonStyle="{ background: '#f8f8f8', border: 'none', color: '#337ab7' }"
            :menuItems="pageMenuItems"
            :customClass="'p-tool-button'"
        />
      </div>

      <u-button v-if="reportData.tools.paging && currentPage && currentPage < reportData.totalPageWithCol"
                type="info"
                :title="t('preview.buttons.nextPage')"
                class="p-button paging-button"
                @click="goToNextPage">
        {{ t('preview.buttons.nextPage') }}
      </u-button>
    </div>

    <PDFPrintDialog
        :visible="pdfPrintDialogVisible"
        :parameters="pdfPrintParameters"
        @close="handlePdfPrintDialogClose"
    />

    <iframe name="print_frame" width="0" height="0" frameborder="0" src="about:blank"></iframe>
    <iframe name="print_pdf_frame" width="0" height="0" frameborder="0" src="about:blank"></iframe>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  getExcelExportUrl,
  getExcelPagingExportUrl,
  getExcelSheetPagingExportUrl,
  getPdfDirectPrintUrl,
  getPdfExportUrl,
  getWordExportUrl,
  loadPrintPages,
  loadPagePaper
} from '@/api/preview'
import { pointToMM } from '@/utils/table'
import showLoading from '@/components/loading/instance'
import { showAlert } from '@/utils/comnon'
import PDFPrintDialog from '@/views/report/preview/pdf-print-dialog/index.vue'
import ButtonGroup from '@/components/button-group/index.vue'
import { buildLocationSearchParameters } from '@/views/report/preview/utils/render'

defineOptions({ name: 'ToolBox' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  reportData: any
  currentPage: number
  pageEnable: boolean
  searchFormParameters?: Record<string, any>
}>(), {
  reportData: null,
  currentPage: 1,
  pageEnable: false,
  searchFormParameters: () => ({})
})

const emit = defineEmits<{
  (e: 'page-change', pageIndex: number): void
  (e: 'page-enable-change', pageEnable: boolean): void
}>()

const pageMenuItems = ref<any[]>([])
const pdfPrintDialogVisible = ref(false)
const printIndex = ref(0)

const pdfPrintParameters = computed(() => {
  const urlParameters = buildLocationSearchParameters(props.searchFormParameters)
  const params = new URLSearchParams(urlParameters)
  const paramObj: Record<string, string> = {}
  for (const [key, value] of params.entries()) {
    paramObj[key] = value
  }
  return paramObj
})

const pagingMenuItems = computed(() => [
  {
    text: t('preview.paging.preview'),
    action: () => changePageEnable(false)
  },
  {
    text: t('preview.paging.pagingPreview'),
    action: () => changePageEnable(true)
  }
])

watch(() => props.reportData, () => {
  initPageMenuItems()
}, { deep: true })

onMounted(() => {
  initPageMenuItems()
})

/**
 * 构建打印样式字符串
 * 根据纸张配置生成 @page 和 @media print 的 CSS 样式，
 * 包括纸张大小、方向和四边边距
 * @param paper - 纸张配置对象，包含 paperType、width、height、orientation 及四边边距
 */
function buildPrintStyle(paper: any): string {
  const marginLeft = pointToMM(paper.leftMargin)
  const marginTop = pointToMM(paper.topMargin)
  const marginRight = pointToMM(paper.rightMargin)
  const marginBottom = pointToMM(paper.bottomMargin)
  const paperType = paper.paperType
  let page = paperType
  if (paperType === 'CUSTOM') {
    page = pointToMM(paper.width) + 'mm ' + pointToMM(paper.height) + 'mm'
  }
  const style = `
    @media print {
        .page-break{
            display: block;
            page-break-before: always;
        }
    }
    @page {
      size: ${page} ${paper.orientation};
      margin-left: ${marginLeft}mm;
      margin-top: ${marginTop}mm;
      margin-right:${marginRight}mm;
      margin-bottom:${marginBottom}mm;
    }
`
  return style
}

/**
 * 浏览器直接打印
 * 加载打印页面内容和纸张配置，将内容注入隐藏 iframe 中调用浏览器打印功能
 */
async function print() {
  let loadingInstance: any = null
  try {
    const urlParameters = buildLocationSearchParameters(props.searchFormParameters)
    const params = new URLSearchParams(urlParameters)
    const formData = new FormData()
    for (const [key, value] of params.entries()) {
      formData.append(key, value)
    }

    loadingInstance = showLoading({
      text: '加载中...',
    })

    const result: any = await loadPrintPages(formData as any)
    const paper: any = await loadPagePaper(formData as any)

    loadingInstance.close()

    const html = result.html
    const iFrame = (window.frames as any)['print_frame']
    let styles = `<style type="text/css">`
    styles += buildPrintStyle(paper)
    const styleElement = document.getElementById('report-table-style')
    styles += styleElement ? styleElement.textContent : ''
    styles += `</style>`

    iFrame.document.body.innerHTML = styles + html
    iFrame.window.focus()
    iFrame.window.print()
  } catch (error: any) {
    if (loadingInstance) {
      loadingInstance.close()
    }
    console.error('打印失败:', error)
    if (error.msg) {
      showAlert(t('preview.error.serverError') + t('colon') + error.msg, { useHTMLString: true })
    } else {
      showAlert(t('preview.error.serverErrorSimple'))
    }
  }
}

/**
 * PDF预览打印
 * 打开 PDF 打印对话框，对话框内部会加载纸张配置信息
 */
function printPdf() {
  pdfPrintDialogVisible.value = true
}

/**
 * 关闭PDF打印对话框
 */
function handlePdfPrintDialogClose() {
  pdfPrintDialogVisible.value = false
}

/**
 * PDF直接打印
 * 在隐藏 iframe 中加载服务端生成的 PDF，加载完成后自动调用浏览器打印
 * 设置了30秒超时保护，防止 PDF 加载失败导致 loading 一直显示
 */
function printDirectPdf() {
  const loadingInstance = showLoading({
    text: t('preview.loading.default'),
  })
  const urlParameters = buildLocationSearchParameters(props.searchFormParameters)
  const params = new URLSearchParams(urlParameters)
  const paramObj: Record<string, string> = {}

  for (const [key, value] of params.entries()) {
    paramObj[key] = value
  }

  const url = getPdfDirectPrintUrl(paramObj, printIndex.value++)
  const iframe = (window.frames as any)['print_pdf_frame']
  const pdfFrame = document.querySelector("iframe[name='print_pdf_frame']")

  let loadTimeout: ReturnType<typeof setTimeout> | null = null
  let isLoaded = false

  const closeLoading = () => {
    if (!isLoaded) {
      isLoaded = true
      if (loadTimeout) {
        clearTimeout(loadTimeout)
      }
      loadingInstance.close()
    }
  }

  if (pdfFrame) {
    const handleLoad = function () {
      closeLoading()
      try {
        iframe.window.focus()
        iframe.window.print()
      } catch (error) {
        console.error('打印失败:', error)
      }
    }

    const handleError = function () {
      closeLoading()
      console.error('PDF加载失败')
      showAlert(t('preview.error.loadPdfFail'))
    }

    pdfFrame.addEventListener('load', handleLoad, { once: true })
    pdfFrame.addEventListener('error', handleError, { once: true })

    loadTimeout = setTimeout(() => {
      closeLoading()
      console.warn('PDF加载超时')
    }, 30000)
  }

  iframe.window.focus()
  iframe.location.href = url
}

/**
 * 获取导出接口的公共请求参数
 * 将搜索表单参数转换为 URL 查询参数对象
 */
function getExportParams(): Record<string, string> {
  const urlParameters = buildLocationSearchParameters(props.searchFormParameters)
  const params = new URLSearchParams(urlParameters)
  const paramObj: Record<string, string> = {}
  for (const [key, value] of params.entries()) {
    paramObj[key] = value
  }
  return paramObj
}

/**
 * 在新标签页中打开导出URL
 * @param url - 导出文件的完整URL
 */
function openExportUrl(url: string) {
  window.open(url, '_blank')
}

/**
 * 导出为PDF文件
 */
function exportPdf() {
  const paramObj = getExportParams()
  const url = getPdfExportUrl(paramObj)
  openExportUrl(url)
}

/**
 * 导出为Word文件
 */
function exportWord() {
  const paramObj = getExportParams()
  const url = getWordExportUrl(paramObj)
  openExportUrl(url)
}

/**
 * 导出为分页Sheet的Excel文件（每页一个Sheet）
 */
function exportExcelPagingSheet() {
  const paramObj = getExportParams()
  const url = getExcelSheetPagingExportUrl(paramObj)
  openExportUrl(url)
}

/**
 * 导出为分页Excel文件
 */
function exportExcelPaging() {
  const paramObj = getExportParams()
  const url = getExcelPagingExportUrl(paramObj)
  openExportUrl(url)
}

/**
 * 导出为Excel文件
 */
function exportExcel() {
  const paramObj = getExportParams()
  const url = getExcelExportUrl(paramObj)
  openExportUrl(url)
}

/**
 * 跳转到上一页
 * 当前页大于1时触发页码变更事件
 */
function goToPrevPage() {
  if (props.currentPage > 1) {
    emit('page-change', props.currentPage - 1)
  }
}

/**
 * 跳转到下一页
 * 当前页小于总页数时触发页码变更事件
 */
function goToNextPage() {
  if (props.currentPage < props.reportData.totalPageWithCol) {
    emit('page-change', props.currentPage + 1)
  }
}

/**
 * 跳转到指定页码
 * @param pageIndex - 目标页码
 */
function handlePageChange(pageIndex: number) {
  emit('page-change', pageIndex)
}

/**
 * 切换分页启用状态
 * @param enable - true 启用分页，false 禁用分页
 */
function changePageEnable(enable: boolean) {
  emit('page-enable-change', enable)
}

/**
 * 初始化页码下拉菜单项
 * 根据报表总页数生成每页对应的菜单项，点击后跳转到对应页码
 */
function initPageMenuItems() {
  if (!props.reportData || !props.reportData.totalPageWithCol) {
    return
  }

  const items: any[] = []

  for (let i = 1; i <= props.reportData.totalPageWithCol; i++) {
    const pageIndex = i
    items.push({
      text: `第${i}页`,
      action: () => {
        handlePageChange(pageIndex)
      }
    })
  }

  pageMenuItems.value = items
}
</script>

<style scoped>
.p-tool-button {
  display: inline-block;
  padding: 0;
  background: #f8f8f8;
  border: none;
  margin: 3px
}

.p-button {
  border: none;
  background: rgb(248, 248, 248);
}

.p-button img {
  vertical-align: middle;
}

.tools-content {
  border: solid 1px #ddd;
  border-radius: 5px;
  height: 40px;
  width: 100%;
  background: #f8f8f8;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  padding: 0 10px;
}
</style>
