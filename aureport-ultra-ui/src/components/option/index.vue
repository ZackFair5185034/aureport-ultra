<script setup lang="ts">
import type { SelectContext } from '../select/index.vue'

defineOptions({ name: 'UOption' })

const props = withDefaults(defineProps<{
  value?: unknown
  label?: string
  disabled?: boolean
}>(), {
  disabled: false,
})

const selectContext = inject<SelectContext>('selectContext')

const instance = reactive({
  get value() { return props.value },
  get label() { return props.label ?? String(props.value ?? '') },
  get disabled() { return props.disabled },
  selected: false,
  choose: false,
  visible: true,
  multi: false,
})

onMounted(() => {
  selectContext?.onOptionAdd(instance)
})

onBeforeUnmount(() => {
  selectContext?.onOptionRemove(instance)
})

function onClick() {
  if (props.disabled)
    return
  selectContext?.onOptionSelect(instance)
}
</script>

<template>
  <div
    v-show="instance.visible"
    class="u-option"
    :class="{
      'u-option-selected': instance.selected,
      'u-option-disabled': instance.disabled,
      'u-option-choose': instance.choose,
      'u-option-multi': instance.multi,
    }"
    @click="onClick"
  >
    <slot>
      {{ label }}
      <i v-if="instance.choose" class="iconfont icon-checked u-option-icon" />
    </slot>
  </div>
</template>

<style scoped>
.u-option {
  line-height: 32px;
  padding: 0 15px 0 15px;
  font-size: 13px;
  cursor: pointer;
  position: relative;
}

.u-option:hover {
  background-color: #f5f7fa;
}

.u-option-selected {
  font-weight: 700;
  color: #00554a;
  background-color: #f5f7fa;
}

.u-option-disabled {
  cursor: not-allowed;
  color: #c0c4cc;
}

.u-option-choose {
  color: #00554a;
}

.u-option-multi {
  padding-right: 40px;
}

.u-option-icon {
  width: 32px;
  height: 32px;
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
}
</style>
