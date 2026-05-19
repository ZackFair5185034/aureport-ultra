<script setup lang="ts">
import beautifier from 'js-beautify'

import { useI18n } from 'vue-i18n'
import { beautifierConf, deepClone } from '@/views/report/designer/search-form/utils'
import { makeUpCss } from '@/views/report/designer/search-form/utils/css'
import { cssStyle, makeUpHtml, vueScript, vueTemplate } from '@/views/report/designer/search-form/utils/html'
import { makeUpJs } from '@/views/report/designer/search-form/utils/js'
import { renderTemplateToComponent } from '@/views/report/preview/utils/render'

defineOptions({ name: 'SearchBox' })

const props = defineProps<{ searchFormConfig: any }>()

const emit = defineEmits<{ (e: 'submit', data: unknown): void }>()

const { t } = useI18n()

const searchForm = ref<HTMLDivElement | null>(null)
const formInstance = ref<any>(null)

watch(() => props.searchFormConfig, (newVal) => {
  if (newVal) {
    nextTick(() => {
      init(newVal)
    })
  }
}, { immediate: true, deep: true })

function init(searchFormConfig: any) {
  if (formInstance.value) {
    formInstance.value.$destroy()
    formInstance.value = null
  }

  const generateType = 'file'
  const script = vueScript(makeUpJs(searchFormConfig, generateType))
  const html = vueTemplate(makeUpHtml(searchFormConfig, generateType))
  const css = cssStyle(makeUpCss(searchFormConfig))
  const formJs = beautifier.html(html + script + css, beautifierConf.html)

  formInstance.value = renderTemplateToComponent(formJs, searchForm.value!)

  formInstance.value.$on('on-submit', (formData: any) => {
    const clonedData = deepClone(formData)
    emit('submit', clonedData)
  })
}

onBeforeUnmount(() => {
  if (formInstance.value) {
    formInstance.value.$destroy()
    formInstance.value = null
  }
})
</script>

<template>
  <div class="search-box">
    <div class="tools-content">
      <i class="iconfont icon-search" />
      <span class="title">
        {{ t('preview.searchBox.title') }}
      </span>
    </div>
    <div class="main">
      <div ref="searchForm" />
    </div>
  </div>
</template>

<style scoped>
.search-box {
  position: relative;
  width: 380px;
  overflow-y: auto;
  overflow-x: hidden;
}

.tools-content {
  border: solid 1px #ddd;
  border-radius: 5px;
  height: 40px;
  width: 100%;
  background: #f8f8f8;
  box-sizing: border-box;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  padding-left: 12px;
  gap: 6px;
}

.title {
  font-size: 14px;
}

.main {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}
</style>
