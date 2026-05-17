<script setup lang="ts">
import { onBeforeMount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import URLParameterDialog from '../../../url-parameter-dialog/index.vue'
// @ts-ignore
import configOptions from '../constants/config-options.js'

defineOptions({ name: 'LinkConfig' })

const props = withDefaults(defineProps<{
  linkUrl?: string
  linkTargetWindow?: string
  linkParameters?: any[]
}>(), {
  linkUrl: '',
  linkTargetWindow: '',
  linkParameters: () => [],
})

const emit = defineEmits<{
  (e: 'link-change', value: any): void
}>()

const { t } = useI18n()

const linkChecked = ref(false)
const localLinkUrl = ref('')
const localLinkTargetWindow = ref('')
const localLinkParameters = ref<any[]>([])
const urlParameterDialogVisible = ref(false)
const linkTargetOptions = ref<any[]>([])

onBeforeMount(() => {
  linkTargetOptions.value = configOptions.getLinkTargetOptions(t)
})

watch(() => props.linkUrl, () => { loadLinkProperties() }, { immediate: true })
watch(() => props.linkTargetWindow, () => { loadLinkProperties() }, { immediate: true })
watch(() => props.linkParameters, (newVal) => { localLinkParameters.value = newVal || [] }, { immediate: true })

function loadLinkProperties() {
  linkChecked.value = props.linkUrl != null
  if (linkChecked.value) {
    localLinkUrl.value = props.linkUrl || ''
    localLinkTargetWindow.value = props.linkTargetWindow || ''
  }
  else {
    localLinkUrl.value = ''
    localLinkTargetWindow.value = ''
  }
}

function onLinkChange() {
  emit('link-change', {
    checked: linkChecked.value,
    linkUrl: linkChecked.value ? localLinkUrl.value : null,
    linkTargetWindow: linkChecked.value ? localLinkTargetWindow.value : null,
    linkParameters: linkChecked.value ? localLinkParameters.value : null,
  })
}

function onLinkUrlChange() {
  if (linkChecked.value) {
    emit('link-change', { checked: true, linkUrl: localLinkUrl.value, linkTargetWindow: localLinkTargetWindow.value, linkParameters: localLinkParameters.value })
  }
}

function onLinkTargetChange() {
  if (linkChecked.value) {
    emit('link-change', { checked: true, linkUrl: localLinkUrl.value, linkTargetWindow: localLinkTargetWindow.value, linkParameters: localLinkParameters.value })
  }
}

function onLinkParametersChange(parameters: any[]) {
  localLinkParameters.value = parameters
  if (linkChecked.value) {
    emit('link-change', { checked: true, linkUrl: localLinkUrl.value, linkTargetWindow: localLinkTargetWindow.value, linkParameters: localLinkParameters.value })
  }
}

function configLinkParameter() {
  if (!localLinkUrl.value) {
    showAlert(t('dialog.propCondition.linkUrl'))
    return
  }

  if (!localLinkParameters.value) {
    localLinkParameters.value = []
  }

  urlParameterDialogVisible.value = true
}

function handleUrlParameterDialogClose() {
  urlParameterDialogVisible.value = false
}

function handleUrlParameterSaveAfter({ paramItem, operation }: any) {}
</script>

<template>
  <u-checkbox-group>
    <div class="form-group" style="margin-bottom: 5px;">
      <div class="u-inline">
        <u-checkbox v-model="linkChecked" @change="onLinkChange">
          {{ t('dialog.propCondition.link') }}
        </u-checkbox>
      </div>
      <span v-show="linkChecked" style="margin-left: 10px">
        <div class="u-inline">
          <u-input
            v-model="localLinkUrl"
            :placeholder="t('dialog.propCondition.linkUrlPlaceholder')"
            @change="onLinkUrlChange"
          />
        </div>
      </span>
      <div v-show="linkChecked" style="margin-left: 10px;margin-top: 5px">
        <span>{{ t('dialog.propCondition.target') }}</span>
        <div class="u-inline" style="margin-left: 10px">
          <u-select
            v-model="localLinkTargetWindow"
            :clearable="true"
            @change="onLinkTargetChange"
          >
            <u-option
              v-for="option in linkTargetOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>

          <u-button style="margin-left: 5px" @click="configLinkParameter">
            {{ t('dialog.propCondition.urlParameter') }}
          </u-button>
        </div>
      </div>
    </div>
  </u-checkbox-group>
  <URLParameterDialog
    v-model:visible="urlParameterDialogVisible"
    :parameters="linkParameters || []"
    @saveAfter="handleUrlParameterSaveAfter"
    @parameters-change="onLinkParametersChange"
  />
</template>
