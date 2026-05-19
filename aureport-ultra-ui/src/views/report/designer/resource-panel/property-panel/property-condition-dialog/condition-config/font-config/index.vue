<script setup lang="ts">

import { useI18n } from 'vue-i18n'
/// @ts-expect-error JS module
import configOptions from '../constants/config-options.js'

defineOptions({ name: 'FontConfig' })

const props = withDefaults(defineProps<{
  cellStyle?: any
}>(), {
  cellStyle: () => ({}),
})

const emit = defineEmits<{
  (e: 'font-change', value: any): void
}>()

const { t } = useI18n()

const fontChecked = ref(false)
const fontFamily = ref('')
const fontFamilyScope = ref('cell')
const fontSizeChecked = ref(false)
const fontSize = ref('')
const fontSizeScope = ref('cell')
const fontBoldChecked = ref(false)
const fontBold = ref('')
const fontBoldScope = ref('cell')
const fontItalicChecked = ref(false)
const fontItalic = ref('')
const fontItalicScope = ref('cell')
const fontUnderlineChecked = ref(false)
const fontUnderline = ref('')
const fontUnderlineScope = ref('cell')
const fontOptions = ref<any[]>([])
const fontSizeOptions = ref<any[]>([])
const yesNoOptions = ref<any[]>([])
const scopeOptions = ref<any[]>([])

onBeforeMount(() => {
  fontOptions.value = configOptions.getFontOptions(t)
  fontSizeOptions.value = configOptions.getFontSizeOptions()
  yesNoOptions.value = configOptions.getYesNoOptions(t)
  scopeOptions.value = configOptions.getScopeOptions(t)
})

watch(() => props.cellStyle, (newVal) => {
  loadFontProperties(newVal)
}, { immediate: true, deep: true })

function loadFontProperties(cellStyle: any) {
  if (!cellStyle)
    return

  fontChecked.value = !!(cellStyle.fontFamily && cellStyle.fontFamily !== '0')
  fontFamily.value = fontChecked.value ? cellStyle.fontFamily : ''
  fontFamilyScope.value = cellStyle.fontFamilyScope || 'cell'

  fontSizeChecked.value = !!(cellStyle.fontSize && cellStyle.fontSize !== '0')
  fontSize.value = fontSizeChecked.value ? cellStyle.fontSize : ''
  fontSizeScope.value = cellStyle.fontSizeScope || 'cell'

  fontBoldChecked.value = !!(cellStyle.bold !== null && cellStyle.bold !== undefined && cellStyle.bold !== '')
  fontBold.value = fontBoldChecked.value ? (cellStyle.bold === true || cellStyle.bold === 'true' ? 'true' : 'false') : ''
  fontBoldScope.value = cellStyle.boldScope || 'cell'

  fontItalicChecked.value = !!(cellStyle.italic !== null && cellStyle.italic !== undefined && cellStyle.italic !== '')
  fontItalic.value = fontItalicChecked.value ? (cellStyle.italic === true || cellStyle.italic === 'true' ? 'true' : 'false') : ''
  fontItalicScope.value = cellStyle.italicScope || 'cell'

  fontUnderlineChecked.value = !!(cellStyle.underline !== null && cellStyle.underline !== undefined && cellStyle.underline !== '')
  fontUnderline.value = fontUnderlineChecked.value ? (cellStyle.underline === true || cellStyle.underline === 'true' ? 'true' : 'false') : ''
  fontUnderlineScope.value = cellStyle.underlineScope || 'cell'
}

function onFontChange() {
  emit('font-change', { type: 'fontFamily', checked: fontChecked.value, value: fontChecked.value ? '宋体' : null, scope: fontChecked.value ? 'cell' : null })
}

function onFontFamilyChange() {
  emit('font-change', { type: 'fontFamily', checked: fontChecked.value, value: fontFamily.value, scope: fontFamilyScope.value })
}

function onFontFamilyScopeChange() {
  emit('font-change', { type: 'fontFamily', checked: fontChecked.value, value: fontFamily.value, scope: fontFamilyScope.value })
}

function onFontSizeChange() {
  emit('font-change', { type: 'fontSize', checked: fontSizeChecked.value, value: fontSizeChecked.value ? '12' : null, scope: fontSizeChecked.value ? 'cell' : null })
}

function onFontSizeValueChange() {
  emit('font-change', { type: 'fontSize', checked: fontSizeChecked.value, value: fontSize.value, scope: fontSizeScope.value })
}

function onFontSizeScopeChange() {
  emit('font-change', { type: 'fontSize', checked: fontSizeChecked.value, value: fontSize.value, scope: fontSizeScope.value })
}

function onFontBoldChange() {
  emit('font-change', { type: 'bold', checked: fontBoldChecked.value, value: fontBoldChecked.value ? true : null, scope: fontBoldChecked.value ? 'cell' : null })
}

function onFontBoldValueChange() {
  emit('font-change', { type: 'bold', checked: fontBoldChecked.value, value: fontBold.value, scope: fontBoldScope.value })
}

function onFontBoldScopeChange() {
  emit('font-change', { type: 'bold', checked: fontBoldChecked.value, value: fontBold.value, scope: fontBoldScope.value })
}

function onFontItalicChange() {
  emit('font-change', { type: 'italic', checked: fontItalicChecked.value, value: fontItalicChecked.value ? true : null, scope: fontItalicChecked.value ? 'cell' : null })
}

function onFontItalicValueChange() {
  emit('font-change', { type: 'italic', checked: fontItalicChecked.value, value: fontItalic.value, scope: fontItalicScope.value })
}

function onFontItalicScopeChange() {
  emit('font-change', { type: 'italic', checked: fontItalicChecked.value, value: fontItalic.value, scope: fontItalicScope.value })
}

function onFontUnderlineChange() {
  emit('font-change', { type: 'underline', checked: fontUnderlineChecked.value, value: fontUnderlineChecked.value ? true : null, scope: fontUnderlineChecked.value ? 'cell' : null })
}

function onFontUnderlineValueChange() {
  emit('font-change', { type: 'underline', checked: fontUnderlineChecked.value, value: fontUnderline.value, scope: fontUnderlineScope.value })
}

function onFontUnderlineScopeChange() {
  emit('font-change', { type: 'underline', checked: fontUnderlineChecked.value, value: fontUnderline.value, scope: fontUnderlineScope.value })
}
</script>

<template>
  <div>
    <u-checkbox-group>
      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="fontChecked" @change="onFontChange">
            {{ t('dialog.propCondition.font') }}
          </u-checkbox>
        </div>
        <span v-show="fontChecked" style="margin-left: 10px">
          <div class="u-inline">
            <u-select
              v-model="fontFamily"
              :clearable="true"
              style="width: 120px"
              @change="onFontFamilyChange"
            >
              <u-option
                v-for="option in fontOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
          <span style="margin-left: 15px;">{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="fontFamilyScope"
              :clearable="true"
              style="width: 120px"
              @change="onFontFamilyScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>

      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="fontSizeChecked" @change="onFontSizeChange">
            {{ t('dialog.propCondition.fontSize') }}
          </u-checkbox>
        </div>
        <span v-show="fontSizeChecked" style="padding-left: 10px;">
          <div class="u-inline">
            <u-select
              v-model="fontSize"
              :clearable="true"
              style="width: 120px"
              @change="onFontSizeValueChange"
            >
              <u-option
                v-for="option in fontSizeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
          <span style="margin-left: 15px;">{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="fontSizeScope"
              :clearable="true"
              style="width: 120px"
              @change="onFontSizeScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>

      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="fontBoldChecked" @change="onFontBoldChange">
            {{ t('dialog.propCondition.bold') }}
          </u-checkbox>
        </div>
        <span v-show="fontBoldChecked" style="padding-left: 10px">
          <div class="u-inline">
            <u-select
              v-model="fontBold"
              :clearable="true"
              style="width: 120px"
              @change="onFontBoldValueChange"
            >
              <u-option
                v-for="option in yesNoOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
          <span style="margin-left: 15px;">{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="fontBoldScope"
              :clearable="true"
              style="width: 120px"
              @change="onFontBoldScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>

      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="fontItalicChecked" @change="onFontItalicChange">
            {{ t('dialog.propCondition.italic') }}
          </u-checkbox>
        </div>
        <span v-show="fontItalicChecked" style="padding-left: 10px">
          <div class="u-inline">
            <u-select
              v-model="fontItalic"
              :clearable="true"
              style="width: 120px"
              @change="onFontItalicValueChange"
            >
              <u-option
                v-for="option in yesNoOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
          <span style="margin-left: 15px;">{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="fontItalicScope"
              :clearable="true"
              style="width: 120px"
              @change="onFontItalicScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>

      <div class="form-group" style="margin-bottom: 5px;">
        <div class="u-inline">
          <u-checkbox v-model="fontUnderlineChecked" @change="onFontUnderlineChange">
            {{ t('dialog.propCondition.underline') }}
          </u-checkbox>
        </div>
        <span v-show="fontUnderlineChecked" style="padding-left: 10px">
          <div class="u-inline">
            <u-select
              v-model="fontUnderline"
              :clearable="true"
              style="width: 120px"
              @change="onFontUnderlineValueChange"
            >
              <u-option
                v-for="option in yesNoOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
          <span style="margin-left: 15px;">{{ t('dialog.propCondition.scope') }}</span>
          <div class="u-inline" style="margin-left: 10px">
            <u-select
              v-model="fontUnderlineScope"
              :clearable="true"
              style="width: 120px"
              @change="onFontUnderlineScopeChange"
            >
              <u-option
                v-for="option in scopeOptions"
                :key="option.value"
                :value="option.value"
                :label="option.label"
              />
            </u-select>
          </div>
        </span>
      </div>
    </u-checkbox-group>
  </div>
</template>
