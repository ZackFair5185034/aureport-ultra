<template>
  <div class="axis-config">
    <u-form :label-width="100" labelPosition="left">

      <div class="property-quote">
        {{ t('chart.xAxis') }}
      </div>

      <u-form-item class="property-label" :label="t('chart.titleRotation')">
        <u-input-number
          :title="t('chart.angleScope')"
          v-model="localXAxesConfig.rotation"
          @change="handleXAxesRotationChange"
        >
        </u-input-number>
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.displayAxisTitle')">
        <u-radio-group
          v-model="localXAxesConfig.scaleLabel.display"
          @change="handleXTitleDisplayChange"
        >
          <u-radio v-for="option in [{ label: t('chart.yes'), value: true }, { label: t('chart.no'), value: false }]"
                    :key="String(option.value)"
                    :label="option.value">
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.axisTitle')" v-show="xTitleDisplay">
        <u-input
          style="width: 250px;"
          v-model="localXAxesConfig.scaleLabel.labelString"
          @change="handleXTitleTextChange"
        >
        </u-input>
      </u-form-item>

      <div class="property-quote">
        {{ t('chart.yAxisConfig') }}
      </div>

      <u-form-item class="property-label" :label="t('chart.titleRotation')">
        <u-input-number
          :title="t('chart.angleScope')"
          v-model="localYAxesConfig.rotation"
          @change="handleYAxesRotationChange"
        >
        </u-input-number>
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.displayAxisTitle')">
        <u-radio-group
          v-model="localYAxesConfig.scaleLabel.display"
          @change="handleYTitleDisplayChange"
        >
          <u-radio v-for="option in [{ label: t('chart.yes'), value: true }, { label: t('chart.no'), value: false }]"
                    :key="String(option.value)"
                    :label="option.value">
            {{ option.label }}
          </u-radio>
        </u-radio-group>
      </u-form-item>

      <u-form-item class="property-label" :label="t('chart.axisTitle')" v-show="yTitleDisplay">
        <u-input
          style="width: 250px;"
          v-model="localYAxesConfig.scaleLabel.labelString"
          @change="handleYTitleTextChange"
        >
        </u-input>
      </u-form-item>

      <div v-if="false" class="property-quote">
        {{ t('chart.titleFormat') }}
      </div>

      <u-form-item v-if="false" class="property-label" :label="t('chart.titleFormat')">
        <u-input
          style="width: 260px;"
          v-model="localFormat"
          @change="handleFormatChange"
        >
        </u-input>
      </u-form-item>

    </u-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

defineOptions({ name: 'Axis' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  xAxesConfig?: any
  yAxesConfig?: any
  format?: string
}>(), {
  xAxesConfig: () => ({
    rotation: 0,
    scaleLabel: {
      display: false,
      labelString: ''
    }
  }),
  yAxesConfig: () => ({
    rotation: 0,
    scaleLabel: {
      display: false,
      labelString: ''
    }
  }),
  format: ''
})

const emit = defineEmits<{
  (e: 'update:xAxesConfig', value: any): void
  (e: 'update:yAxesConfig', value: any): void
  (e: 'update:format', value: string): void
  (e: 'axis-change', value: any): void
}>()

const localXAxesConfig = ref({
  rotation: props.xAxesConfig.rotation,
  scaleLabel: {
    display: props.xAxesConfig.scaleLabel.display,
    labelString: props.xAxesConfig.scaleLabel.labelString
  }
})

const localYAxesConfig = ref({
  rotation: props.yAxesConfig.rotation,
  scaleLabel: {
    display: props.yAxesConfig.scaleLabel.display,
    labelString: props.yAxesConfig.scaleLabel.labelString
  }
})

const localFormat = ref(props.format)

const xTitleDisplay = computed(() => {
  return localXAxesConfig.value.scaleLabel.display === 'true' ? true :
         localXAxesConfig.value.scaleLabel.display === 'false' ? false :
         localXAxesConfig.value.scaleLabel.display
})

const yTitleDisplay = computed(() => {
  return localYAxesConfig.value.scaleLabel.display === 'true' ? true :
         localYAxesConfig.value.scaleLabel.display === 'false' ? false :
         localYAxesConfig.value.scaleLabel.display
})

watch(() => props.xAxesConfig, (newVal) => {
  localXAxesConfig.value = {
    rotation: newVal.rotation,
    scaleLabel: {
      display: newVal.scaleLabel.display,
      labelString: newVal.scaleLabel.labelString
    }
  }
}, { deep: true })

watch(() => props.yAxesConfig, (newVal) => {
  localYAxesConfig.value = {
    rotation: newVal.rotation,
    scaleLabel: {
      display: newVal.scaleLabel.display,
      labelString: newVal.scaleLabel.labelString
    }
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

<style scoped>
.axis-config {
  width: 100%;
}
</style>
