<template>
  <div class="u-tree-node" v-show="Boolean(node.visible)">
    <div class="u-tree-node-label">
      <span
        @click="() => handleExpand()"
        class="u-tree-node-angle"
        :class="{ 'u-tree-node-angle-expand': Boolean(node.expand) }"
      >
        <i class="iconfont icon-right" v-if="showAngle" />
      </span>
      <UCheckbox
        v-if="showCheckbox"
        :model-value="Boolean(node.checked)"
        :indeterminate="Boolean(node.indeterminate)"
        :disabled="Boolean(node.disabled)"
        class="u-tree-node-checkbox"
        @update:model-value="handleCheck"
      />
      <span class="u-tree-node-loading" v-if="lazy && loading">
        <i class="iconfont icon-loading" />
      </span>
      <span class="u-tree-node-text" @click="() => handleExpand()">
        <component :is="treeContext?.renderNodeContent(treeNodeInstance, node)" />
      </span>
    </div>
    <auto-height>
      <div
        class="u-tree-node-content"
        v-if="nodeChildren.length > 0 && rendered"
        v-show="Boolean(node.expand)"
      >
        <UTreeNode
          :show-checkbox="showCheckbox"
          v-for="(item, idx) in nodeChildren"
          :level="level + 1"
          :key="idx"
          :node="item"
          :lazy="lazy"
          :load="load"
          :node-key="nodeKey"
        />
      </div>
    </auto-height>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, inject, nextTick } from 'vue'
import type { TreeContext, TreeNodeRef, TreeNodeData } from './index.vue'
import { setNodeChecked } from './utils'
import { deepCopy } from '../utils'

defineOptions({ name: 'UTreeNode' })

const props = withDefaults(defineProps<{
  node: TreeNodeData
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

const nodeChildren = computed(() => props.node.children || [])

const showAngle = computed(() => {
  return (props.lazy && !props.node.isLeaf) || nodeChildren.value.length > 0
})

const treeNodeInstance: TreeNodeRef = {
  get node() { return props.node },
  handleExpand,
  handleCheck,
}

treeContext?.registerTreeNode(treeNodeInstance)

watch(() => props.node.children, (newVal: TreeNodeData[] | undefined) => {
  if (newVal) {
    calcChecked(newVal)
    calcVisible(newVal)
  }
}, { deep: true })

function handleExpand(expand?: boolean) {
  if (!showAngle.value || loading.value) return

  if (props.lazy && !props.node.loaded) {
    loading.value = true
    props.load?.({ level: props.level ?? 1, ...(props.node as Record<string, unknown>) }, data => {
      loading.value = false
      props.node.loaded = true
      if (!data || (data as unknown[]).length === 0) {
        props.node.isLeaf = true
      } else {
        props.node.children = deepCopy(data) as TreeNodeData[]
        if (props.node.checked) {
          handleCheck(true)
        }
        nextTick(() => {
          props.node.expand = !props.node.expand
        })
      }
    })
  } else {
    if (nodeChildren.value.length > 0) {
      props.node.expand = expand !== undefined ? expand : !props.node.expand
    }
  }
}

function handleCheck(val: unknown) {
  const checked = Boolean(val)
  props.node.checked = checked
  setNodeChecked(props.node as Record<string, unknown>, checked)
}

function calcChecked(newVal: TreeNodeData[]) {
  const checkedAll = !newVal.some(item => !item.checked)
  const indeterminate = !!newVal.find(item => item.checked)
  if (checkedAll) {
    props.node.checked = true
    props.node.indeterminate = false
  } else {
    props.node.checked = false
    props.node.indeterminate = indeterminate
  }
}

function calcVisible(newVal: TreeNodeData[]) {
  const visible = !!newVal.find(item => item.visible)
  props.node.visible = visible
}
</script>

<style scoped>
.u-tree-node {
  font-size: 14px
}

.u-tree-node-label {
  height: 24px;
  line-height: 24px;
  display: flex;
  justify-content: left;
  align-items: center;
  cursor: pointer;
  transition: background-color .2s
}

.u-tree-node-label:hover {
  background-color: #f5f7f9
}

.u-tree-node-angle {
  display: inline-block;
  width: 24px;
  line-height: 24px;
  height: 24px;
  text-align: center;
  transition: transform .3s
}

.u-tree-node-angle-expand {
  transform: rotate(90deg)
}

.u-tree-node-content {
  padding-left: 18px;
  overflow: hidden;
  transition: height .3s
}

.u-tree-node-checkbox {
  height: auto;
  margin-right: 0;
  vertical-align: inherit
}

.u-tree-node-loading {
  margin-right: 5px;
  font-size: 12px
}

.u-tree-node-loading i:before {
  display: inline-block;
  animation: loading 1.5s linear infinite
}

.u-tree-empty {
  font-size: 13px;
  padding: 20px;
  text-align: center
}
</style>
