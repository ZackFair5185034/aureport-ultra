<template>
  <UDialog
    :title="t('dialog.condition.config')"
    width="500px"
    :visible="visible"
    :z-index="20000"
    @close="handleClose"
  >
    <div class="dialog-content" >
      <u-form ref="form" :label-width="120">
        <u-form-item :label="t('dialog.condition.relationship')" v-show="showJoinGroup">
          <u-select
            v-model="joinValue"
            :clearable="true"
          >
            <u-option
              v-for="option in joinOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item :label="t('dialog.condition.propertyName')">
          <u-select
            v-model="propertyValue"
            :clearable="true"
          >
            <u-option
              v-for="option in propertyOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item :label="t('dialog.condition.op')">
          <u-select
            v-model="operatorValue"
            :clearable="true"
          >
            <u-option
              v-for="option in operatorOptions"
              :key="option.value"
              :value="option.value"
              :label="option.label"
            />
          </u-select>
        </u-form-item>

        <u-form-item :label="t('dialog.condition.valueExpr')">
          <u-input
            v-model="valueExpr"
            style="width:240px;"
            @change="validateExpression"
          >
          </u-input>
        </u-form-item>
      </u-form>
    </div>

    <template #footer>
      <div style="text-align: right">
        <u-button @click="handleClose" type="info" style="margin-right: 10px;">{{ t('dialog.common.cancel') }}</u-button>
        <u-button @click="handleOk">{{ t('dialog.common.ok') }}</u-button>
      </div>
    </template>
  </UDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { showAlert } from '@/utils/comnon'
import { conditionScriptValidation } from '@/api/designer'

defineOptions({ name: 'ConditionDialog' })

const { t } = useI18n()

const props = withDefaults(defineProps<{
  visible?: boolean
  fields?: any[]
  condition?: any
  conditions?: any[]
}>(), {
  visible: false,
  fields: () => [],
  condition: null,
  conditions: () => []
})

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saveAfter', value: any): void
}>()

const showJoinGroup = ref(false)
const joinValue = ref('and')
const propertyValue = ref('')
const operatorValue = ref('==')
const valueExpr = ref('')

const joinOptions = computed(() => [
  { value: 'and', label: t('dialog.condition.and') },
  { value: 'or', label: t('dialog.condition.or') }
])

const propertyOptions = computed(() =>
  props.fields.map((field: any) => ({
    value: field.name,
    label: field.name
  }))
)

const operatorOptions = computed(() => [
  { value: '>', label: t('dialog.condition.greatThen') },
  { value: '>=', label: t('dialog.condition.greatEquals') },
  { value: '<', label: t('dialog.condition.lessThen') },
  { value: '<=', label: t('dialog.condition.lessEquals') },
  { value: '==', label: t('dialog.condition.equals') },
  { value: '!=', label: t('dialog.condition.notEquals') },
  { value: 'in', label: t('dialog.condition.in') },
  { value: 'like', label: t('dialog.condition.like') }
])

function initDialogData() {
  const fields = props.fields || []
  const condition = props.condition

  if (condition) {
    showJoinGroup.value = !!condition.join
  } else {
    showJoinGroup.value = props.conditions && props.conditions.length > 0
  }

  if (condition) {
    joinValue.value = condition.join || 'and'
    propertyValue.value = condition.left || ''
    operatorValue.value = condition.operation || condition.op || '=='
    valueExpr.value = condition.right || ''
  } else {
    joinValue.value = 'and'
    propertyValue.value = fields && fields.length > 0 ? fields[0].name : ''
    operatorValue.value = '=='
    valueExpr.value = ''
  }
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    initDialogData()
  }
})

watch(() => props.condition, () => {
  if (props.visible) {
    initDialogData()
  }
}, { deep: true })

watch(() => props.fields, () => {
  if (props.visible) {
    initDialogData()
  }
}, { deep: true })

watch(() => props.conditions, () => {
  if (props.visible) {
    initDialogData()
  }
}, { deep: true })

function handleOk() {
  if (!propertyValue.value) {
    showAlert(t('dialog.condition.selectProperty'))
    return
  }

  if (!operatorValue.value) {
    showAlert(t('dialog.condition.selectOp'))
    return
  }

  if (!valueExpr.value) {
    showAlert(t('dialog.condition.inputExpr'))
    return
  }

  const conditionData = {
    left: propertyValue.value,
    operation: operatorValue.value,
    right: valueExpr.value,
    join: showJoinGroup.value ? joinValue.value : null,
    isEdit: !!props.condition
  }

  emit('saveAfter', conditionData)
  handleClose()
}

function handleClose() {
  emit('update:visible', false)
}

async function validateExpression() {
  if (!valueExpr.value) return
  const val = valueExpr.value
  try {
    const errors = await conditionScriptValidation(val) as any[]
    if (errors && errors.length > 0) {
      await showAlert(`${val} ${t('dialog.condition.exprError')}`)
    }
  } catch (error) {
    console.error('Error validating expression:', error)
  }
}
</script>

<style scoped>
</style>
