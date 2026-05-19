<script setup lang="ts">

import { useI18n } from 'vue-i18n'

defineOptions({ name: 'Axis' })

const props = withDefaults(defineProps<{
  xAxesConfig?: any
  yAxesConfig?: any
  format?: string
}>(), {
  xAxesConfig: () => ({
    rotation: 0,
    scaleLabel: {
      display: false,
      labelString: '',
    },
  }),
  yAxesConfig: () => ({
    rotation: 0,
    scaleLabel: {
      display: false,
      labelString: '',
    },
  }),
  format: '',
})

const emit = defineEmits<{
  (e: 'update:xAxesConfig', value: any): void
  (e: 'update:yAxesConfig', value: any): void
  (e: 'update:format', value: string): void
  (e: 'axis-change', value: any): void
}>()

const { t } = useI18n()

const localXAxesConfig = ref({
  rotation: props.xAxesConfig.rotation,
  scaleLabel: {
    display: props.xAxesConfig.scaleLabel.display,
    labelString: props.xAxesConfig.scaleLabel.labelString,
  },
})

const localYAxesConfig = ref({
  rotation: props.yAxesConfig.rotation,
  scaleLabel: {
    display: props.yAxesConfig.scaleLabel.display,
    labelString: props.yAxesConfig.scaleLabel.labelString,
  },
})

const localFormat = ref(props.format)

const xTitleDisplay = computed(() => {
  return localXAxesConfig.value.scaleLabel.display === 'true'
    ? true
    : (localXAxesConfig.value.scaleLabel.display === 'false'
        ? false
        : localXAxesConfig.value.scaleLabel.display)
})

const yTitleDisplay = computed(() => {
  return localYAxesConfig.value.scaleLabel.display === 'true'
    ? true
    : (localYAxesConfig.value.scaleLabel.display === 'false'
        ? false
        : localYAxesConfig.value.scaleLabel.display)
})

watch(() => props.xAxesConfig, (newVal) => {
  localXAxesConfig.value = {
    rotation: newVal.rotation,
    scaleLabel: {
      display: newVal.scaleLabel.display,
      labelString: newVal.scaleLabel.labelString,
    },
  }
}, { deep: true })

watch(() => props.yAxesConfig, (newVal) => {
  localYAxesConfig.value = {
    rotation: newVal.rotation,
    scaleLabel: {
      display: newVal.scaleLabel.display,
      labelString: newVal.scaleLabel.labelString,
    },
  }
}, { deep: true })

watch(() => props.format, (newVal) => {
  localFormat.value = newVal
})

function handleXAxesRotationChange() {
  emit('update:xAxesConfig', localXAxesConfig.value)
  emit('axis-change', { type: 'x-rotation', value: localXAxesConfig.value.rotation })
}

function handleXTitleDisplayChange(value: any) {
  emit('update:xAxesConfig', localXAxesConfig.value)
  emit('axis-change', { type: 'x-title-display', value })
}

function handleXTitleTextChange() {
  emit('update:xAxesConfig', localXAxesConfig.value)
  emit('axis-change', { type: 'x-title-text', value: localXAxesConfig.value.scaleLabel.labelString })
}

function handleYAxesRotationChange() {
  emit('update:yAxesConfig', localYAxesConfig.value)
  emit('axis-change', { type: 'y-rotation', value: localYAxesConfig.value.rotation })
}

function handleYTitleDisplayChange(value: any) {
  emit('update:yAxesConfig', localYAxesConfig.value)
  emit('axis-change', { type: 'y-title-display', value })
}

function handleYTitleTextChange() {
  emit('update:yAxesConfig', localYAxesConfig.value)
  emit('axis-change', { type: 'y-title-text', value: localYAxesConfig.value.scaleLabel.labelString })
}

function handleFormatChange() {
  emit('update:format', localFormat.value)
  emit('axis-change', { type: 'format', value: localFormat.value })
}
</script>

<template>
  <div class="axis-config">
    <u-form :label-width="100" labelPosition="left">
      <div class="property-quote">
        {{ t('chart.xAxis') }}
      </div>

      <u-form-item class="property-label" :label="t('chart.titleRotation')">
        <u-input-number
          v-model="localXAxesConfig.rotation"
          :title="t('chart.angleScope')"
          @change="handleXAxesRotationChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.displayAxisTitle')">
        <u-radio-group
          v-model="localXAxesConfig.scaleLabel.display"
          @change="handleXTitleDisplayChange"
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

      <u-form-item v-show="xTitleDisplay" class="property-label" :label="t('chart.axisTitle')">
        <u-input
          v-model="localXAxesConfig.scaleLabel.labelString"
          style="width: 250px;"
          @change="handleXTitleTextChange"
        />
      </u-form-item>

      <div class="property-quote">
        {{ t('chart.yAxisConfig') }}
      </div>

      <u-form-item class="property-label" :label="t('chart.titleRotation')">
        <u-input-number
          v-model="localYAxesConfig.rotation"
          :title="t('chart.angleScope')"
          @change="handleYAxesRotationChange"
        />
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.displayAxisTitle')">
        <u-radio-group
          v-model="localYAxesConfig.scaleLabel.display"
          @change="handleYTitleDisplayChange"
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

      <u-form-item v-show="yTitleDisplay" class="property-label" :label="t('chart.axisTitle')">
        <u-input
          v-model="localYAxesConfig.scaleLabel.labelString"
          style="width: 250px;"
          @change="handleYTitleTextChange"
        />
      </u-form-item>

      <div v-if="false" class="property-quote">
        {{ t('chart.titleFormat') }}
      </div>

      <u-form-item v-if="false" class="property-label" :label="t('chart.titleFormat')">
        <u-input
          v-model="localFormat"
          style="width: 260px;"
          @change="handleFormatChange"
        />
      </u-form-item>
    </u-form>
  </div>
</template>

<style scoped>
.axis-config {
  width: 100%;
}
</style>
