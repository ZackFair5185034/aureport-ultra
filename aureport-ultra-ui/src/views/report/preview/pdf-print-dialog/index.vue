<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { getPdfPreviewUrl, loadPagePaper, pdfNewPaging } from '@/api/preview'
import showLoading from '@/components/loading/instance'
import { useReportStore } from '@/stores/report'
import { showAlert } from '@/utils/comnon'
import { buildPageSizeList, mmToPoint, pointToMM } from '@/utils/table'

defineOptions({ name: 'PDFPrintDialog' })

const props = withDefaults(defineProps<PDFPrintDialogProps>(), {
  visible: false,
  parameters: () => ({}),
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

interface PDFPrintDialogProps {
  visible?: boolean
  parameters?: Record<string, unknown>
}

const { t } = useI18n()

const reportStore = useReportStore()
const context = computed(() => reportStore.context)

const paper = reactive({
  paperType: 'A4',
  width: 0,
  height: 0,
  orientation: 'portrait',
  leftMargin: 0,
  rightMargin: 0,
  topMargin: 0,
  bottomMargin: 0,
})

const pageWidthMM = ref(0)
const pageHeightMM = ref(0)
const leftMarginMM = ref(0)
const rightMarginMM = ref(0)
const topMarginMM = ref(0)
const bottomMarginMM = ref(0)

const paperSizeList: Record<string, { width: number, height: number }> = buildPageSizeList()
const refreshIndex = ref(0)
const loading = ref(false)
const errorMessage = ref('')

const pdfFrame = ref<HTMLIFrameElement | null>(null)

let loadingInstance: any = null

const paperTypeOptions = computed(() => {
  const options: { value: string, label: string }[] = []

  for (let i = 0; i <= 10; i++) {
    options.push({ value: `A${i}`, label: `A${i}` })
  }

  for (let i = 0; i <= 10; i++) {
    options.push({ value: `B${i}`, label: `B${i}` })
  }

  options.push({
    value: 'CUSTOM',
    label: t('preview.pdfPrint.custom'),
  })

  return options
})

const orientationOptions = computed(() => [
  { value: 'portrait', label: t('preview.pdfPrint.portrait') },
  { value: 'landscape', label: t('preview.pdfPrint.landscape') },
])

const urlParameters = computed(() => window.location.search)

watch(() => props.visible, (val) => {
  if (val) {
    loadPaperData()
  }
})

/**
 * 加载纸张配置数据
 * 根据传入的 parameters 请求后台获取纸张配置信息
 */
async function loadPaperData() {
  loading.value = true
  errorMessage.value = ''

  try {
    const formData = new FormData()
    for (const [key, value] of Object.entries(props.parameters || {})) {
      if (value !== null && value !== undefined) {
        formData.append(key, String(value))
      }
    }

    const paperData: any = await loadPagePaper(formData as any)

    paper.paperType = paperData.paperType || 'A4'
    paper.width = paperData.width || 0
    paper.height = paperData.height || 0
    paper.orientation = paperData.orientation || 'portrait'
    paper.leftMargin = paperData.leftMargin || 0
    paper.rightMargin = paperData.rightMargin || 0
    paper.topMargin = paperData.topMargin || 0
    paper.bottomMargin = paperData.bottomMargin || 0

    pageWidthMM.value = pointToMM(paper.width)
    pageHeightMM.value = pointToMM(paper.height)
    leftMarginMM.value = pointToMM(paper.leftMargin)
    rightMarginMM.value = pointToMM(paper.rightMargin)
    topMarginMM.value = pointToMM(paper.topMargin)
    bottomMarginMM.value = pointToMM(paper.bottomMargin)

    loading.value = false

    nextTick(() => {
      initIFrame()
      handleApply()
    })
  }
  catch (error: any) {
    loading.value = false
    console.error('获取纸张信息失败:', error)
    errorMessage.value = error.msg ? t('preview.error.serverError') + t('colon') + error.msg : t('preview.error.loadPaperFail')
  }
}

/**
 * 关闭对话框，向父组件发送 close 事件
 */
function handleClose() {
  emit('close')
}

/**
 * 纸张类型变更处理
 * 选择预设纸张时自动更新宽高，选择自定义时允许手动编辑
 * @param {string} value - 纸张类型标识（如 'A4', 'B5', 'CUSTOM'）
 */
function handlePageTypeChange(value: string) {
  if (value === 'CUSTOM') {
    return
  }

  const pageSize = paperSizeList[value]
  paper.width = mmToPoint(pageSize.width)
  paper.height = mmToPoint(pageSize.height)
  pageWidthMM.value = pageSize.width
  pageHeightMM.value = pageSize.height
}

/**
 * 纸张宽度变更处理
 * 将毫米值转换为点值存储，并刷新打印线显示
 * @param {number} value - 纸张宽度（毫米）
 */
function handlePageWidthChange(value: number) {
  if (!value || isNaN(value)) {
    showAlert(t('preview.pdfPrint.numberTip'))
    return
  }

  paper.width = mmToPoint(value)
  if (context.value && (context.value as any).printLine) {
    (context.value as any).printLine.refresh()
  }
}

/**
 * 纸张高度变更处理
 * 将毫米值转换为点值存储
 * @param {number} value - 纸张高度（毫米）
 */
function handlePageHeightChange(value: number) {
  if (!value || isNaN(value)) {
    showAlert(t('preview.pdfPrint.numberTip'))
    return
  }

  paper.height = mmToPoint(value)
}

/**
 * 左边距变更处理
 * 将毫米值转换为点值存储，并刷新打印线显示
 * @param {number} value - 左边距（毫米）
 */
function handleLeftMarginChange(value: number) {
  if (!value || isNaN(value)) {
    showAlert(t('preview.pdfPrint.numberTip'))
    return
  }

  paper.leftMargin = mmToPoint(value)
  if (context.value && (context.value as any).printLine) {
    (context.value as any).printLine.refresh()
  }
}

/**
 * 右边距变更处理
 * 将毫米值转换为点值存储，并刷新打印线显示
 * @param {number} value - 右边距（毫米）
 */
function handleRightMarginChange(value: number) {
  if (!value || isNaN(value)) {
    showAlert(t('preview.pdfPrint.numberTip'))
    return
  }

  paper.rightMargin = mmToPoint(value)
  if (context.value && (context.value as any).printLine) {
    (context.value as any).printLine.refresh()
  }
}

/**
 * 上边距变更处理
 * 将毫米值转换为点值存储
 * @param {number} value - 上边距（毫米）
 */
function handleTopMarginChange(value: number) {
  if (!value || isNaN(value)) {
    showAlert(t('preview.pdfPrint.numberTip'))
    return
  }

  paper.topMargin = mmToPoint(value)
}

/**
 * 下边距变更处理
 * 将毫米值转换为点值存储
 * @param {number} value - 下边距（毫米）
 */
function handleBottomMarginChange(value: number) {
  if (!value || isNaN(value)) {
    showAlert(t('preview.pdfPrint.numberTip'))
    return
  }

  paper.bottomMargin = mmToPoint(value)
}

/**
 * 应用纸张设置并刷新PDF预览
 * 将当前纸张配置提交到服务端重新分页，然后更新 iframe 中的 PDF 预览内容
 */
async function handleApply() {
  const localLoadingInstance = showLoading({
    text: '加载中...',
  })

  try {
    const currentPaper = {
      paperType: paper.paperType,
      width: paper.width,
      height: paper.height,
      orientation: paper.orientation,
      leftMargin: paper.leftMargin,
      rightMargin: paper.rightMargin,
      topMargin: paper.topMargin,
      bottomMargin: paper.bottomMargin,
    }

    const formData = new FormData()
    formData.append('_paper', JSON.stringify(currentPaper))
    const urlParams = new URLSearchParams(urlParameters.value)
    for (const [key, value] of urlParams) {
      formData.append(key, value)
    }

    await pdfNewPaging(formData as any)
    localLoadingInstance.close()

    const paramObj: Record<string, string> = {}
    for (const [key, value] of urlParams) {
      paramObj[key] = value
    }

    if (pdfFrame.value) {
      pdfFrame.value.src = getPdfPreviewUrl(paramObj, refreshIndex.value++)
    }
  }
  catch (error) {
    localLoadingInstance.close()
    console.error('Error:', error)
    showAlert(t('preview.pdfPrint.fail'))
  }
}

/**
 * 执行PDF打印
 * 调用 PDF 预览 iframe 的浏览器打印功能
 */
function handlePrint() {
  try {
    (window.frames as any)._iframe_for_pdf_print.window.print()
  }
  catch (error) {
    console.error('Print error:', error)
    showAlert(t('preview.pdfPrint.printError'))
  }
}

/**
 * 初始化PDF预览iframe
 * 设置 iframe 的 PDF 预览地址，非 IE 浏览器显示加载中状态
 */
function initIFrame() {
  if (!pdfFrame.value) {
    return
  }

  const urlParams = new URLSearchParams(urlParameters.value)
  const paramObj: Record<string, string> = {}
  for (const [key, value] of urlParams) {
    paramObj[key] = value
  }

  pdfFrame.value.src = getPdfPreviewUrl(paramObj, 1)

  const msie = window.navigator.appName.indexOf('Internet Explorer')
  const ie11 = !!(window as any).MSInputMethodContext && !!(document as any).documentMode

  if (msie === -1 && !ie11) {
    loadingInstance = showLoading({
      text: '加载中...',
    })
  }
}

/**
 * 隐藏加载状态
 * PDF iframe 加载完成后调用，关闭 loading 遮罩
 */
function hideLoading() {
  if (loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}
</script>

<template>
  <UDialog
    :title="t('preview.pdfPrint.title')"
    top="20px"
    width="1250px"
    :visible="visible"
    class="pdf-print-dialog"
    @close="handleClose"
  >
    <div class="pdf-print-body">
      <div v-if="loading" class="pdf-print-loading">
        <span>{{ t('preview.loading.default') }}</span>
      </div>

      <div v-else-if="errorMessage" class="pdf-print-error">
        <span class="error-message">{{ errorMessage }}</span>
        <u-button type="primary" @click="loadPaperData">
          {{ t('preview.pdfPrint.retry') }}
        </u-button>
      </div>

      <template v-else>
        <fieldset class="pdf-print-toolbar">
          <legend>{{ t('preview.pdfPrint.setup') }}</legend>

          <!-- 纸张类型 -->
          <u-form :label-width="100">
            <u-row>
              <u-col :span="6">
                <u-form-item class="property-label" :label="t('preview.pdfPrint.paper')">
                  <u-select
                    v-model="paper.paperType"
                    class="page-select"
                    style="width: 140px"
                    @change="handlePageTypeChange"
                  >
                    <u-option
                      v-for="(option, index) in paperTypeOptions"
                      :key="index"
                      :value="option.value"
                      :label="option.label"
                    >
                      {{ option.label }}
                    </u-option>
                  </u-select>
                </u-form-item>
              </u-col>

              <u-col :span="6">
                <u-form-item class="property-label" :label="t('preview.pdfPrint.width')">
                  <u-input-number
                    v-model="pageWidthMM"
                    :disabled="paper.paperType !== 'CUSTOM'"
                    @change="handlePageWidthChange"
                  />
                </u-form-item>
              </u-col>

              <u-col :span="6">
                <u-form-item class="property-label" :label="t('preview.pdfPrint.height')">
                  <u-input-number
                    v-model="pageHeightMM"
                    :disabled="paper.paperType !== 'CUSTOM'"
                    @change="handlePageHeightChange"
                  />
                </u-form-item>
              </u-col>

              <u-col :span="6">
                <u-form-item class="property-label" :label="t('preview.pdfPrint.orientation')">
                  <u-select
                    v-model="paper.orientation"
                    class="orientation-select"
                    style="width: 140px"
                  >
                    <u-option
                      v-for="(option, index) in orientationOptions"
                      :key="index"
                      :value="option.value"
                      :label="option.label"
                    >
                      {{ option.label }}
                    </u-option>
                  </u-select>
                </u-form-item>
              </u-col>
            </u-row>

            <u-row style="margin-top: 5px;">
              <u-col :span="6">
                <u-form-item class="property-label" :label="t('preview.pdfPrint.leftMargin')">
                  <u-input-number
                    v-model="leftMarginMM"
                    @change="handleLeftMarginChange"
                  />
                </u-form-item>
              </u-col>

              <u-col :span="6">
                <u-form-item class="property-label" :label="t('preview.pdfPrint.rightMargin')">
                  <u-input-number
                    v-model="rightMarginMM"
                    @change="handleRightMarginChange"
                  />
                </u-form-item>
              </u-col>

              <u-col :span="6">
                <u-form-item class="property-label" :label="t('preview.pdfPrint.topMargin')">
                  <u-input-number
                    v-model="topMarginMM"
                    @change="handleTopMarginChange"
                  />
                </u-form-item>
              </u-col>

              <u-col :span="6">
                <u-form-item class="property-label" :label="t('preview.pdfPrint.bottomMargin')">
                  <u-input-number
                    v-model="bottomMarginMM"
                    @change="handleBottomMarginChange"
                  />
                </u-form-item>
              </u-col>
            </u-row>

            <u-row style="margin-top: 5px;">
              <u-col :span="6" :offset="18">
                <u-form-item class="property-label">
                  <u-button type="primary" @click="handleApply">
                    {{ t('preview.pdfPrint.apply') }}
                  </u-button>

                  <u-button type="error" style="margin-left:5px" @click="handlePrint">
                    {{ t('preview.pdfPrint.print') }}
                  </u-button>
                </u-form-item>
              </u-col>
            </u-row>
          </u-form>
        </fieldset>

        <!-- PDF预览区域 -->
        <div class="pdf-preview-container">
          <iframe
            ref="pdfFrame"
            name="_iframe_for_pdf_print"
            class="pdf-preview-frame"
            frameborder="0"
            @load="hideLoading"
          />
        </div>
      </template>
    </div>
  </UDialog>
</template>

<style scoped>
.pdf-print-dialog .pdf-print-body {
  padding-top: 5px;
  height: 660px;
  overflow: hidden;
}

.pdf-print-toolbar {
  width: 100%;
  font-size: 12px;
  border: solid 1px #ddd;
  border-radius: 5px;
  padding: 1px 8px;
  margin-bottom: 5px;
}

.pdf-print-toolbar legend {
  font-size: 12px;
  width: 60px;
  border-bottom: none;
  margin-bottom: 0;
}

.pdf-preview-container {
  width: 100%;
  height: calc(100vh - 200px);
  min-height: 400px;
}

.pdf-preview-frame {
  width: 100%;
  height: 100%;
  border: solid 1px #c2c2c2;
}

.pdf-print-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  font-size: 16px;
  color: #666;
}

.pdf-print-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 400px;
  font-size: 16px;
  color: #f56c6c;
}

.pdf-print-error .error-message {
  margin-bottom: 20px;
}
</style>
