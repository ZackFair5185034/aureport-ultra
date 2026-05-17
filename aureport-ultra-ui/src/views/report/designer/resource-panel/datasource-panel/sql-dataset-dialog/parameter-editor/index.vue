<script setup lang="ts">
import ParameterTable from '@/views/report/designer/resource-panel/datasource-panel/parameter-table/index.vue'

defineOptions({ name: 'ParameterEditor' })

const props = defineProps<{
  parameters: any[]
}>()

const emit = defineEmits<{
  (e: 'add-parameter', param: any): void
  (e: 'edit-parameter', index: number, param: any): void
  (e: 'remove-parameter', index: number): void
  (e: 'update'): void
}>()

function handleAddParameter(newParam: any) {
  emit('add-parameter', newParam)
}

function handleEditParameter(index: number, updatedParam: any) {
  emit('edit-parameter', index, updatedParam)
}

function handleRemoveParameter(index: number) {
  emit('remove-parameter', index)
}

function handleUpdate() {
  emit('update')
}
</script>

<template>
  <div class="parameter-editor">
    <div class="row" style="margin:10px;">
      <ParameterTable
        :data="parameters"
        @add-parameter="handleAddParameter"
        @edit-parameter="handleEditParameter"
        @remove-parameter="handleRemoveParameter"
        @update="handleUpdate"
      />
    </div>
  </div>
</template>

<style scoped>
.parameter-editor {
  width: 100%;
}
</style>
