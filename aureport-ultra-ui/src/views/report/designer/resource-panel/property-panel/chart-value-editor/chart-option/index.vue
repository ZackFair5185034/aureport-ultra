<script setup lang="ts">
// @ts-nocheck
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils'

defineOptions({ name: 'ChartOption' })

const props = withDefaults(defineProps<{
  chartConfig?: any
  showDataLabel?: boolean
}>(), {
  chartConfig: () => ({}),
  showDataLabel: true,
})

const emit = defineEmits<{
  (e: 'chart-option-change', value: any): void
  (e: 'data-labels-change', value: any): void
}>()

const { t } = useI18n()

const localChartConfig = ref({
  title: {
    display: true,
    position: 'top',
    text: '',
  },
  legend: {
    display: true,
    position: 'bottom',
  },
  dataLabels: {
    display: false,
  },
  animation: {
    duration: 1000,
    easing: 'linear',
  },
  layout: {
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
  },
})

const positionOptions = computed(() => [
  { value: 'top', label: t('chart.up') },
  { value: 'bottom', label: t('chart.down') },
  { value: 'left', label: t('chart.left') },
  { value: 'right', label: t('chart.right') },
])

const animationEasingOptions = computed(() => [
  { value: 'linear', label: 'linear' },
  { value: 'easeInQuad', label: 'easeInQuad' },
  { value: 'easeOutQuad', label: 'easeOutQuad' },
  { value: 'easeInOutQuad', label: 'easeInOutQuad' },
  { value: 'easeInCubic', label: 'easeInCubic' },
  { value: 'easeOutCubic', label: 'easeOutCubic' },
  { value: 'easeInOutCubic', label: 'easeInOutCubic' },
  { value: 'easeInQuart', label: 'easeInQuart' },
  { value: 'easeOutQuart', label: 'easeOutQuart' },
  { value: 'easeInOutQuart', label: 'easeInOutQuart' },
  { value: 'easeInQuint', label: 'easeInQuint' },
  { value: 'easeOutQuint', label: 'easeOutQuint' },
  { value: 'easeInOutQuint', label: 'easeInOutQuint' },
  { value: 'easeInSine', label: 'easeInSine' },
  { value: 'easeOutSine', label: 'easeOutSine' },
  { value: 'easeInOutSine', label: 'easeInOutSine' },
  { value: 'easeInExpo', label: 'easeInExpo' },
  { value: 'easeOutExpo', label: 'easeOutExpo' },
  { value: 'easeInOutExpo', label: 'easeInOutExpo' },
  { value: 'easeInCirc', label: 'easeInCirc' },
  { value: 'easeOutCirc', label: 'easeOutCirc' },
  { value: 'easeInOutCirc', label: 'easeInOutCirc' },
  { value: 'easeInElastic', label: 'easeInElastic' },
  { value: 'easeOutElastic', label: 'easeOutElastic' },
  { value: 'easeInOutElastic', label: 'easeInOutElastic' },
  { value: 'easeInBack', label: 'easeInBack' },
  { value: 'easeOutBack', label: 'easeOutBack' },
  { value: 'easeInOutBack', label: 'easeInOutBack' },
  { value: 'easeInBounce', label: 'easeInBounce' },
  { value: 'easeOutBounce', label: 'easeOutBounce' },
  { value: 'easeInOutBounce', label: 'easeInOutBounce' },
])

const titleDisplay = computed(() => {
  return localChartConfig.value.title.display === 'true'
    ? true
    : (localChartConfig.value.title.display === 'false'
        ? false
        : localChartConfig.value.title.display)
})

const legendDisplay = computed(() => {
  return localChartConfig.value.legend.display === 'true'
    ? true
    : (localChartConfig.value.legend.display === 'false'
        ? false
        : localChartConfig.value.legend.display)
})

const dataLabelsDisplay = computed(() => {
  return localChartConfig.value.dataLabels.display === 'true'
    ? true
    : (localChartConfig.value.dataLabels.display === 'false'
        ? false
        : localChartConfig.value.dataLabels.display)
})

watch(() => props.chartConfig, (newVal) => {
  localChartConfig.value = deepCopy(newVal)
}, { deep: true })

function handleTitleDisplayChange() {
  updateChartOption('title', localChartConfig.value.title)
}

function handleTitlePositionChange() {
  updateChartOption('title', localChartConfig.value.title)
}

function handleTitleTextChange() {
  updateChartOption('title', localChartConfig.value.title)
}

function handleLegendDisplayChange() {
  updateChartOption('legend', localChartConfig.value.legend)
}

function handleLegendPositionChange() {
  updateChartOption('legend', localChartConfig.value.legend)
}

function handleDataLabelsDisplayChange() {
  emit('data-labels-change', localChartConfig.value.dataLabels)
}

function handleAnimationDurationChange() {
  updateChartOption('animation', localChartConfig.value.animation)
}

function handleAnimationEasingChange() {
  updateChartOption('animation', localChartConfig.value.animation)
}

function handleLayoutChange() {
  updateChartOption('layout', { layout: localChartConfig.value.layout })
}

function updateChartOption(type: string, option: any) {
  emit('chart-option-change', { type, option })
}
</script>

<template>
  <div class="chart-option-editor">
    <div class="property-quote">
      {{ t('chart.titleConfig') }}
    </div>
    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('chart.display')">
        <u-radio-group
          v-model="localChartConfig.title.display"
          @change="handleTitleDisplayChange"
        >
          <u-radio
            v-for="option in [{ label: t('chart.yes'), value: true }, { label: t('chart.no'), value: false }]"
            :key="String(option.value)"
            :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item v-show="titleDisplay" class="property-label" :label="t('chart.position')">
        <u-select
          v-model="localChartConfig.title.position"
          :clearable="true"
          @change="handleTitlePositionChange"
        >
          <u-option
            v-for="option in positionOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>

      <u-form-item v-show="titleDisplay" class="property-label" :label="t('chart.titleContent')">
        <u-input
          v-model="localChartConfig.title.text"
          style="width: 250px;"
          @change="handleTitleTextChange"
        />
      </u-form-item>
    </u-form>

    <div class="property-quote">
      {{ t('chart.legendConfig') }}
    </div>
    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('chart.display')">
        <u-radio-group
          v-model="localChartConfig.legend.display"
          @change="handleLegendDisplayChange"
        >
          <u-radio
            v-for="option in [{ label: t('chart.yes'), value: true }, { label: t('chart.no'), value: false }]"
            :key="String(option.value)"
            :label="option.value"
          >
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item v-show="legendDisplay" class="property-label" :label="t('chart.position')">
        <u-select
          v-model="localChartConfig.legend.position"
          :clearable="true"
          @change="handleLegendPositionChange"
        >
          <u-option
            v-for="option in positionOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>
    </u-form>

    <template v-if="showDataLabel">
      <div class="property-quote">
        {{ t('chart.dataLabelConfig') }}
      </div>
      <u-form :label-width="100" labelPosition="left">
        <u-form-item class="property-label" :label="t('chart.display')">
          <u-radio-group
            v-model="localChartConfig.dataLabels.display"
            @change="handleDataLabelsDisplayChange"
          >
            <u-radio
              v-for="option in [{ label: t('chart.yes'), value: true }, { label: t('chart.no'), value: false }]"
              :key="String(option.value)"
              :label="option.value"
            >
              {{ option.label }}
            </u-radio>
          </u-radio-group>
        </u-form-item>
      </u-form>
    </template>

    <div class="property-quote">
      {{ t('chart.motionConfig') }}
    </div>
    <u-form :label-width="100" labelPosition="left">
      <u-form-item class="property-label" :label="t('chart.motionDelay')">
        <u-input-number
          v-model="localChartConfig.animation.duration"
          @change="handleAnimationDurationChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.effect')">
        <u-select
          v-model="localChartConfig.animation.easing"
          :clearable="true"
          @change="handleAnimationEasingChange"
        >
          <u-option
            v-for="option in animationEasingOptions"
            :key="option.value"
            :value="option.value"
            :label="option.label"
          />
        </u-select>
      </u-form-item>
    </u-form>

    <template v-if="false">
      <div class="property-quote">
        {{ t('chart.layout') }}
      </div>
      <u-form :label-width="100" labelPosition="left">
        <u-form-item class="property-label" :label="t('chart.up')">
          <u-input-number
            v-model="localChartConfig.layout.top"
            @change="handleLayoutChange"
          />
        </u-form-item>
        <u-form-item class="property-label" :label="t('chart.down')">
          <u-input-number
            v-model="localChartConfig.layout.bottom"
            @change="handleLayoutChange"
          />
        </u-form-item>
        <u-form-item class="property-label" :label="t('chart.left')">
          <u-input-number
            v-model="localChartConfig.layout.left"
            @change="handleLayoutChange"
          />
        </u-form-item>
        <u-form-item class="property-label" :label="t('chart.right')">
          <u-input-number
            v-model="localChartConfig.layout.right"
            @change="handleLayoutChange"
          />
        </u-form-item>
      </u-form>
    </template>
  </div>
</template>

<style scoped>
.chart-option-editor {
  width: 100%;
}
</style>
