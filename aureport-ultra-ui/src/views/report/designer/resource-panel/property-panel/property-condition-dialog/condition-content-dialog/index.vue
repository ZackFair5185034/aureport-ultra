<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { conditionScriptValidation } from '@/api/designer'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'EditPropertyConditionDialog' })

const props = withDefaults(defineProps<{
  visible?: boolean
  dialogFields?: any[]
  dialogCondition?: any | null
  dialogConditions?: any[]
}>(), {
  visible: false,
  dialogFields: () => [],
  dialogCondition: null,
  dialogConditions: () => [],
})

const emit = defineEmits<{
  (e: 'saveAfter', type: string, property: string, operator: string, value: string, join?: string): void
  (e: 'close'): void
}>()

const { t } = useI18n()

const form = ref<any>(null)
const join = ref('and')
const leftType = ref('current')
const property = ref('')
const expression = ref('')
const operator = ref('')
const value = ref('')
const showJoin = ref(false)

const joinOptions = computed(() => [
  { value: 'and', label: t('dialog.editPropCondition.and') },
  { value: 'or', label: t('dialog.editPropCondition.or') },
])

const leftTypeOptions = computed(() => [
  { value: 'current', label: t('dialog.editPropCondition.currentValue') },
  { value: 'property', label: t('dialog.editPropCondition.property') },
  { value: 'expression', label: t('dialog.editPropCondition.expression') },
])

const fieldOptions = computed(() =>
  props.dialogFields.map((field: any) => ({
    value: field.name,
    label: field.label ? `${field.name} (${field.label})` : field.name,
  })),
)

const operatorOptions = computed(() => [
  { value: '>', label: t('dialog.editPropCondition.greater') },
  { value: '>=', label: t('dialog.editPropCondition.greaterEquals') },
  { value: '<', label: t('dialog.editPropCondition.less') },
  { value: '<=', label: t('dialog.editPropCondition.lessEquals') },
  { value: '==', label: t('dialog.editPropCondition.equals') },
  { value: '!=', label: t('dialog.editPropCondition.notEquals') },
  { value: 'in', label: t('dialog.editPropCondition.in') },
  { value: 'like', label: t('dialog.editPropCondition.like') },
])

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

watch(() => props.visible, (newVal) => {
  if (newVal) {
    initFormData()
  }
})

function initFormData() {
  showJoin.value = props.dialogCondition ? !!props.dialogCondition.join : props.dialogConditions.length > 0

  if (props.dialogCondition) {
    leftType.value = props.dialogCondition.type || 'current'
    if (leftType.value === 'expression') {
      expression.value = props.dialogCondition.left || ''
    }
    else if (props.dialogCondition.left) {
      property.value = props.dialogCondition.left
    }

    if (leftType.value === 'property' && (!property.value || property.value === '')) {
      leftType.value = 'current'
    }

    operator.value = props.dialogCondition.operation || props.dialogCondition.op || ''
    value.value = props.dialogCondition.right || ''
    join.value = props.dialogCondition.join || 'and'
  }
  else {
    leftType.value = 'current'
    property.value = ''
    expression.value = ''
    operator.value = ''
    value.value = ''
    join.value = 'and'
  }
}

function handleOk() {
  if (leftType.value === 'property' && !property.value) {
    showAlert(t('dialog.editPropCondition.selectProp'))
    return
  }

  if (leftType.value === 'expression' && !expression.value) {
    showAlert(t('dialog.editPropCondition.leftValueExpr'))
    return
  }

  if (!operator.value) {
    showAlert(t('dialog.editPropCondition.selectOperator'))
    return
  }

  if (!value.value) {
    showAlert(t('dialog.editPropCondition.inputExpr'))
    return
  }

  let prop = property.value
  if (leftType.value === 'expression') {
    prop = expression.value
  }
  else if (leftType.value === 'current') {
    prop = null as any
  }

  let type = leftType.value
  if (type === 'current') {
    type = 'property'
  }

  if (props.dialogCondition) {
    if (props.dialogCondition.join) {
      emit('saveAfter', type, prop, operator.value, value.value, join.value)
    }
    else {
      emit('saveAfter', type, prop, operator.value, value.value)
    }
  }
  else if (props.dialogConditions.length > 0) {
    emit('saveAfter', type, prop, operator.value, value.value, join.value)
  }
  else {
    emit('saveAfter', type, prop, operator.value, value.value)
  }

  handleClose()
}

function handleClose() {
  emit('close')
  setTimeout(() => {
    join.value = 'and'
    leftType.value = 'current'
    property.value = ''
    expression.value = ''
    operator.value = ''
    value.value = ''
    showJoin.value = false
  }, 300)
}

async function validateExpression() {
  if (!expression.value)
    return
  try {
    const errors = await conditionScriptValidation(expression.value) as any[]
    if (errors && errors.length > 0) {
      showAlert(`${expression.value} ${t('dialog.editPropCondition.syntaxError')}`)
    }
  }
  catch (error) {
    console.error('验证表达式失败:', error)
  }
}

async function validateValueExpression() {
  if (!value.value)
    return
  try {
    const errors = await conditionScriptValidation(value.value) as any[]
    if (errors && errors.length > 0) {
      showAlert(`${value.value} ${t('dialog.editPropCondition.syntaxError')}`)
    }
  }
  catch (error) {
    console.error('验证值表达式失败:', error)
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (props.visible && e.key === 'Escape') {
    handleClose()
  }
}
</script>

<template>
  <UDialog
    :title="t('dialog.editPropCondition.title')"
    width="550px"
    :visible="visible"
    :z-index="20002"
    @close="handleClose"
  >
    <div class="dialog-content">
      <u-form ref="form" :label-width="80">
        <!-- 关系选择 -->
        <u-form-item v-if="showJoin" :label="t('dialog.editPropCondition.relation')">
          <u-select
            v-model="join"
            :clearable="true"
            style="width:300px"
          >
            <u-option
              v-for="option in joinOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <!-- 左值类型选择 -->
        <u-form-item :label="t('dialog.editPropCondition.leftValue')">
          <u-select
            v-model="leftType"
            :clearable="true"
            style="width:300px"
          >
            <u-option
              v-for="option in leftTypeOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <!-- 属性名选择 -->
        <u-form-item v-if="leftType === 'property'" :label="t('dialog.editPropCondition.propName')">
          <u-select
            v-model="property"
            :clearable="true"
            style="width:300px"
          >
            <u-option
              v-for="option in fieldOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <!-- 表达式输入 -->
        <u-form-item v-if="leftType === 'expression'" :label="t('dialog.editPropCondition.expr')">
          <u-input
            v-model="expression"
            style="width: 300px;"
            @blur="validateExpression"
          />
        </u-form-item>

        <!-- 运算符选择 -->
        <u-form-item :label="t('dialog.editPropCondition.operator')">
          <u-select
            v-model="operator"
            :clearable="true"
            style="width:300px"
          >
            <u-option
              v-for="option in operatorOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <!-- 值表达式输入 -->
        <u-form-item :label="t('dialog.editPropCondition.valueExpr')">
          <u-input
            v-model="value"
            style="width: 300px;"
            @blur="validateValueExpression"
          />
        </u-form-item>
      </u-form>
    </div>
    <template #footer>
      <div style="text-align: right">
        <u-button type="info" style="margin-right: 10px;" @click.stop="handleClose">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<style scoped>
</style>
