<script setup lang="ts">
import type { TreeContext, TreeNodeData, TreeNodeRef } from './index.vue'
import { computed, inject, nextTick, ref, watch } from 'vue'
import { deepCopy } from '../utils'
import { setNodeChecked } from './utils'

defineOptions({ name: 'UTreeNode' })

const props = withDefaults(defineProps<{
  node?: TreeNodeData
  showCheckbox?: boolean
  lazy?: boolean
  load?: (node: Record<string, unknown>, callback: (data: unknown[]) => void) => void
  level?: number
  nodeKey?: string
}>(), {
  node: () => ({}),
  showCheckbox: false,
  lazy: false,
  level: 1,
  nodeKey: 'value',
})

const treeContext = inject<TreeContext>('treeContext')

const rendered = ref(true)
const loading = ref(false)

// Local state to avoid mutating props directly
const localNode = ref(deepCopy(props.node))

watch(() => props.node, (newVal) => {
  localNode.value = deepCopy(newVal)
}, { deep: true })

const nodeChildren = computed(() => localNode.value.children || [])

const showAngle = computed(() => {
  return (props.lazy && !localNode.value.isLeaf) || nodeChildren.value.length > 0
})

const treeNodeInstance: TreeNodeRef = {
  get node() { return localNode.value },
  handleExpand,
  handleCheck,
}

treeContext?.registerTreeNode(treeNodeInstance)

watch(() => props.node?.children, (newVal: TreeNodeData[] | undefined) => {
  if (newVal) {
    localNode.value.children = deepCopy(newVal)
    calcChecked(newVal)
    calcVisible(newVal)
  }
}, { deep: true })

function handleExpand(expand?: boolean) {
  if (!showAngle.value || loading.value)
    return

  if (props.lazy && !localNode.value.loaded) {
    loading.value = true
    const nodeData = { level: props.level ?? 1, ...localNode.value }
    props.load?.(nodeData, (data) => {
      loading.value = false
      localNode.value.loaded = true
      if (!data || (data as unknown[]).length === 0) {
        localNode.value.isLeaf = true
      }
      else {
        localNode.value.children = deepCopy(data) as TreeNodeData[]
        if (localNode.value.checked) {
          handleCheck(true)
        }

        nextTick(() => {
          localNode.value.expand = !localNode.value.expand
        })
      }
    })
  }
  else {
    if (nodeChildren.value.length > 0) {
      localNode.value.expand = expand === undefined ? !localNode.value.expand : expand
    }
  }
}

function handleCheck(val: unknown) {
  const checked = Boolean(val)
  localNode.value.checked = checked
  setNodeChecked(localNode.value as Record<string, unknown>, checked)
}

function calcChecked(newVal: TreeNodeData[]) {
  const checkedAll = !newVal.some(item => !item.checked)
  const indeterminate = newVal.some(item => item.checked)
  if (checkedAll) {
    localNode.value.checked = true
    localNode.value.indeterminate = false
  }
  else {
    localNode.value.checked = false
    localNode.value.indeterminate = indeterminate
  }
}

function calcVisible(newVal: TreeNodeData[]) {
  const visible = newVal.some(item => item.visible)
  localNode.value.visible = visible
}
</script>

<template>
  <div v-show="Boolean(node.visible)" class="u-tree-node">
    <div class="u-tree-node-label">
      <span
        class="u-tree-node-angle"
        :class="{ 'u-tree-node-angle-expand': Boolean(node.expand) }"
        @click="() => handleExpand()"
      >
        <i v-if="showAngle" class="iconfont icon-right" />
      </span>
      <UCheckbox
        v-if="showCheckbox"
        :model-value="Boolean(node.checked)"
        :indeterminate="Boolean(node.indeterminate)"
        :disabled="Boolean(node.disabled)"
        class="u-tree-node-checkbox"
        @update:model-value="handleCheck"
      />
      <span v-if="lazy && loading" class="u-tree-node-loading">
        <i class="iconfont icon-loading" />
      </span>
      <span class="u-tree-node-text" @click="() => handleExpand()">
        <component :is="treeContext?.renderNodeContent(treeNodeInstance, node)" />
      </span>
    </div>
    <auto-height>
      <div
        v-if="nodeChildren.length > 0 && rendered"
        v-show="Boolean(node.expand)"
        class="u-tree-node-content"
      >
        <UTreeNode
          v-for="(item, idx) in nodeChildren"
          :key="idx"
          :show-checkbox="showCheckbox"
          :level="level + 1"
          :node="item"
          :lazy="lazy"
          :load="load"
          :node-key="nodeKey"
        />
      </div>
    </auto-height>
  </div>
</template>

<style scoped>
.u-tree-node {
  font-size: 14px;
}

.u-tree-node-label {
  height: 24px;
  line-height: 24px;
  display: flex;
  justify-content: left;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.u-tree-node-label:hover {
  background-color: #f5f7f9;
}

.u-tree-node-angle {
  display: inline-block;
  width: 24px;
  line-height: 24px;
  height: 24px;
  text-align: center;
  transition: transform 0.3s;
}

.u-tree-node-angle-expand {
  transform: rotate(90deg);
}

.u-tree-node-content {
  padding-left: 18px;
  overflow: hidden;
  transition: height 0.3s;
}

.u-tree-node-checkbox {
  height: auto;
  margin-right: 0;
  vertical-align: inherit;
}

.u-tree-node-loading {
  margin-right: 5px;
  font-size: 12px;
}

.u-tree-node-loading i:before {
  display: inline-block;
  animation: loading 1.5s linear infinite;
}

.u-tree-empty {
  font-size: 13px;
  padding: 20px;
  text-align: center;
}
</style>
