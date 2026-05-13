<template>
  <div>
    <div class="form-group form-group-inline">
      <label>{{ $t('dialog.setting.paperType') }}：</label>
      <div class="u-inline">
        <u-select
          :value="localPaper.paperType"
          style="width: 95px"
          @change="handlePaperTypeChange"
        >
          <u-option
            v-for="option in paperTypeOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </div>
    </div>

    <div class="form-group form-group-inline form-group-ml25">
      <span>{{ $t('dialog.setting.paperWidth') }}：</span>
      <div class="u-inline">
        <u-input-number
          :value="pageWidth"
          :disabled="localPaper.paperType !== 'CUSTOM'"
          @change="handlePageWidthChange"
        />
      </div>
    </div>

    <div class="form-group form-group-inline form-group-ml15">
      <span>{{ $t('dialog.setting.paperHeight') }}：</span>
      <div class="u-inline">
        <u-input-number
          :value="pageHeight"
          :disabled="localPaper.paperType !== 'CUSTOM'"
          @change="handlePageHeightChange"
        />
      </div>
    </div>

    <div></div>

    <div class="form-group form-group-inline form-group-mt5">
      <label>{{ $t('dialog.setting.leftMargin') }}：</label>
      <div class="u-inline">
        <u-input-number
          :value="leftMargin"
          @change="handleLeftMarginChange"
        />
      </div>
    </div>

    <div class="form-group form-group-inline form-group-mt5 form-group-ml25">
      <label>{{ $t('dialog.setting.rightMargin') }}：</label>
      <div class="u-inline">
        <u-input-number
          :value="rightMargin"
          @change="handleRightMarginChange"
        />
      </div>
    </div>

    <div></div>

    <div class="form-group form-group-inline form-group-mt5">
      <label>{{ $t('dialog.setting.topMargin') }}：</label>
      <div class="u-inline">
        <u-input-number
          :value="topMargin"
          @change="handleTopMarginChange"
        />
      </div>
    </div>

    <div class="form-group form-group-inline form-group-mt5 form-group-ml25">
      <label>{{ $t('dialog.setting.bottomMargin') }}：</label>
      <div class="u-inline">
        <u-input-number
          :value="bottomMargin"
          @change="handleBottomMarginChange"
        />
      </div>
    </div>

    <div class="form-group">
      <label>{{ $t('dialog.setting.orientation') }}：</label>
      <div class="u-inline">
        <u-select
          :value="localPaper.orientation"
          style="width: 312px"
          @change="handleOrientationChange"
        >
          <u-option
            v-for="option in orientationOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </div>
    </div>

    <div class="form-group">
      <label>{{ $t('dialog.setting.htmlAlign') }}：</label>
      <div class="u-inline">
        <u-select
          :value="localPaper.htmlReportAlign"
          style="width: 80px"
          @change="handleHtmlAlignChange"
        >
          <u-option
            v-for="option in htmlAlignOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </div>

      <span style="margin-left: 35px;">
        <label>{{ $t('dialog.setting.refreshSecond') }}：</label>
      </span>
      <div class="u-inline">
        <u-input-number
          :value="localPaper.htmlIntervalRefreshValue"
          :placeholder="$t('dialog.setting.tip1')"
          :title="$t('dialog.setting.tip2')"
          :min="0"
          @change="handleHtmlIntervalRefreshValueChange"
        />
      </div>
    </div>

    <div class="form-group">
      <label>{{ $t('dialog.setting.bg') }}：</label>
      <div class="u-inline">
        <u-input
          :value="localPaper.bgImage"
          style="width: 470px;"
          :placeholder="$t('dialog.setting.bgTip')"
          @change="handleBgImageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { pointToMM, mmToPoint, buildPageSizeList } from '@/utils/table.js'

defineOptions({ name: 'PageSettings' })

const emit = defineEmits<{
  (e: 'update:paper', value: any): void
  (e: 'paper-type-change', value: string): void
  (e: 'paper-size-change'): void
  (e: 'margins-change'): void
  (e: 'orientation-change'): void
  (e: 'html-align-change'): void
  (e: 'html-interval-refresh-value-change', value: number): void
  (e: 'background-image-change', value: string): void
}>()

const props = withDefaults(defineProps<{
  paper?: any
}>(), {
  paper: () => ({})
})

const { t } = useI18n()

const localPaper = ref({ ...props.paper })
const paperSizeList = buildPageSizeList()

const pageWidth = computed(() => pointToMM(localPaper.value.width))
const pageHeight = computed(() => pointToMM(localPaper.value.height))
const leftMargin = computed(() => pointToMM(localPaper.value.leftMargin))
const rightMargin = computed(() => pointToMM(localPaper.value.rightMargin))
const topMargin = computed(() => pointToMM(localPaper.value.topMargin))
const bottomMargin = computed(() => pointToMM(localPaper.value.bottomMargin))

const paperTypeOptions = computed(() => {
  const options: any[] = []
  for (const [key] of Object.entries(paperSizeList)) {
    options.push({ value: key, label: key })
  }
  options.push({ value: 'CUSTOM', label: t('dialog.setting.custom') })
  return options
})

const orientationOptions = computed(() => [
  { value: 'portrait', label: t('dialog.setting.portrait') },
  { value: 'landscape', label: t('dialog.setting.landscape') }
])

const htmlAlignOptions = computed(() => [
  { value: 'left', label: t('dialog.setting.left') },
  { value: 'center', label: t('dialog.setting.center') },
  { value: 'right', label: t('dialog.setting.right') }
])

watch(() => props.paper, (newVal) => {
  localPaper.value = { ...newVal }
}, { deep: true })

function handlePaperTypeChange(value: string) {
  emit('update:paper', { ...localPaper.value, paperType: value })
  emit('paper-type-change', value)
}

function handlePageWidthChange(value: number) {
  if (!isNaN(value)) {
    emit('update:paper', { ...localPaper.value, width: mmToPoint(value) })
    emit('paper-size-change')
  }
}

function handlePageHeightChange(value: number) {
  if (!isNaN(value)) {
    emit('update:paper', { ...localPaper.value, height: mmToPoint(value) })
    emit('paper-size-change')
  }
}

function handleLeftMarginChange(value: number) {
  if (!isNaN(value)) {
    emit('update:paper', { ...localPaper.value, leftMargin: mmToPoint(value) })
    emit('margins-change')
  }
}

function handleRightMarginChange(value: number) {
  if (!isNaN(value)) {
    emit('update:paper', { ...localPaper.value, rightMargin: mmToPoint(value) })
    emit('margins-change')
  }
}

function handleTopMarginChange(value: number) {
  if (!isNaN(value)) {
    emit('update:paper', { ...localPaper.value, topMargin: mmToPoint(value) })
    emit('margins-change')
  }
}

function handleBottomMarginChange(value: number) {
  if (!isNaN(value)) {
    emit('update:paper', { ...localPaper.value, bottomMargin: mmToPoint(value) })
    emit('margins-change')
  }
}

function handleOrientationChange(value: string) {
  emit('update:paper', { ...localPaper.value, orientation: value })
  emit('orientation-change')
}

function handleHtmlAlignChange(value: string) {
  emit('update:paper', { ...localPaper.value, htmlReportAlign: value })
  emit('html-align-change')
}

function handleHtmlIntervalRefreshValueChange(value: number) {
  emit('update:paper', { ...localPaper.value, htmlIntervalRefreshValue: value })
  emit('html-interval-refresh-value-change', value)
}

function handleBgImageChange(value: string) {
  emit('update:paper', { ...localPaper.value, bgImage: value })
  emit('background-image-change', value)
}
</script>

<style scoped>
.form-group-inline {
  display: inline-block;
}

.form-group-ml25 {
  margin-left: 25px;
}

.form-group-ml15 {
  margin-left: 15px;
}

.form-group-mt5 {
  margin-top: 5px;
}
</style>
