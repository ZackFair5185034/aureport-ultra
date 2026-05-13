<template>
  <div class="u-tree">
    <UTreeNode
      v-for="(item, index) in copyData"
      :key="index"
      :node="item"
      :show-checkbox="showCheckbox"
      :lazy="lazy"
      :level="1"
      :load="load"
      :node-key="nodeKey"
    />
    <div class="u-tree-empty" v-if="!hasData">
      {{ emptyText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, provide, nextTick, useSlots, h, type VNode } from 'vue'
import { deepCopy } from '../utils'

defineOptions({ name: 'UTree' })

export interface TreeNodeData {
  [key: string]: unknown
  label?: string
  value?: unknown
  checked?: boolean
  indeterminate?: boolean
  disabled?: boolean
  expand?: boolean
  visible?: boolean
  children?: TreeNodeData[]
  isLeaf?: boolean
  loaded?: boolean
}

export interface TreeNodeRef {
  node: TreeNodeData
  handleExpand: (val?: boolean) => void
  handleCheck: (val: unknown) => void
}

export interface TreeContext {
  showCheckbox: boolean
  lazy: boolean
  load?: (node: Record<string, unknown>, callback: (data: unknown[]) => void) => void
  nodeKey?: string
  renderContent?: (h: Function, context: { node: unknown; data: unknown }) => unknown
  defaultExpandedKeys: unknown[]
  defaultCheckedKeys: unknown[]
  registerTreeNode: (ref: TreeNodeRef) => void
  renderNodeContent: (treeNode: unknown, node: TreeNodeData) => VNode
}

const props = withDefaults(defineProps<{
  data?: Record<string, unknown>[]
  showCheckbox?: boolean
  lazy?: boolean
  load?: (node: Record<string, unknown>, callback: (data: unknown[]) => void) => void
  defaultExpandedKeys?: unknown[]
  defaultCheckedKeys?: unknown[]
  nodeKey?: string
  renderContent?: (h: Function, context: { node: unknown; data: unknown }) => unknown
  filterNodeMethod?: (value: string, node: Record<string, unknown>) => boolean
  emptyText?: string
}>(), {
  data: () => [],
  showCheckbox: false,
  lazy: false,
  defaultExpandedKeys: () => [],
  defaultCheckedKeys: () => [],
  emptyText: '暂无数据',
})

const slots = useSlots() as Record<string, ((args: Record<string, unknown>) => VNode[]) | undefined>

const nodes = ref<TreeNodeRef[]>([])
const copyData = ref<TreeNodeData[]>([])

const hasData = computed(() => copyData.value.length > 0)

function registerTreeNode(ref: TreeNodeRef) {
  nodes.value.push(ref)
  checkUpNodeState(ref)
  ref.node.visible = true
}

function renderNodeContent(treeNode: unknown, node: TreeNodeData): VNode {
  const defaultSlot = slots.default
  if (defaultSlot) {
    const vnodes = defaultSlot({ node: treeNode, data: node })
    return vnodes.length > 1 ? h('span', {}, vnodes) : vnodes[0]
  }
  if (props.renderContent) {
    return props.renderContent(h, { node: treeNode, data: node }) as VNode
  }
  return h('span', String(node.label ?? ''))
}

const treeContext: TreeContext = {
  get showCheckbox() { return props.showCheckbox },
  get lazy() { return props.lazy },
  get load() { return props.load },
  get nodeKey() { return props.nodeKey },
  get renderContent() { return props.renderContent },
  get defaultExpandedKeys() { return props.defaultExpandedKeys },
  get defaultCheckedKeys() { return props.defaultCheckedKeys },
  registerTreeNode,
  renderNodeContent,
}
provide('treeContext', treeContext)

function checkUpNodeState(treeNode: TreeNodeRef) {
  if (!props.nodeKey) return
  if (props.defaultExpandedKeys.length > 0) {
    const key = treeNode.node[props.nodeKey]
    if (key !== undefined && props.defaultExpandedKeys.includes(key)) {
      nextTick(() => {
        treeNode.handleExpand(true)
      })
    }
  }
  if (props.defaultCheckedKeys.length > 0) {
    const key = treeNode.node[props.nodeKey]
    if (key !== undefined && props.defaultCheckedKeys.includes(key)) {
      nextTick(() => {
        treeNode.handleCheck(true)
      })
    }
  }
}

function getCheckedNodes(leafOnly?: boolean) {
  return nodes.value.reduce<Record<string, unknown>[]>((total, cell) => {
    if (cell.node.checked && (!leafOnly || !cell.node.children || cell.node.isLeaf)) {
      total.push({
        [props.nodeKey as string]: cell.node[props.nodeKey as string],
        label: cell.node.label,
      })
    }
    return total
  }, [])
}

function getCheckedKeys(leafOnly?: boolean) {
  return nodes.value.reduce<unknown[]>((total, cell) => {
    if (cell.node.checked && (!leafOnly || !cell.node.children || cell.node.isLeaf)) {
      total.push(cell.node[props.nodeKey as string])
    }
    return total
  }, [])
}

function setCheckedNodes(checkedNodes: Record<string, unknown>[]) {
  const nodesMap: Record<string, boolean> = {}
  checkedNodes.forEach(cell => {
    nodesMap[String(cell[props.nodeKey as string])] = true
  })
  nodes.value.forEach(cell => {
    cell.handleCheck(false)
  })
  nodes.value.forEach(cell => {
    const key = String(cell.node[props.nodeKey as string])
    const checked = !!nodesMap[key]
    if (!cell.node.checked) {
      cell.handleCheck(checked)
    }
  })
}

function setCheckedKeys(keys: unknown[], leafOnly?: boolean) {
  const keysMap: Record<string, boolean> = {}
  keys.forEach(cell => {
    keysMap[String(cell)] = true
  })
  nodes.value.forEach(cell => {
    cell.handleCheck(false)
  })
  nodes.value.forEach(cell => {
    let checked = !!keysMap[String(cell.node[props.nodeKey as string])]
    if (leafOnly) {
      checked = checked && (!cell.node.children || Boolean(cell.node.isLeaf))
    }
    if (!cell.node.checked) {
      cell.handleCheck(checked)
    }
  })
}

function filter(val: string) {
  if (props.filterNodeMethod) {
    nodes.value.forEach(cell => {
      cell.node.visible = props.filterNodeMethod!(val, cell.node as Record<string, unknown>)
    })
  }
}

watch(() => props.data, (newVal) => {
  if (props.lazy) {
    if (props.load) {
      props.load({ level: 0 }, data => {
        copyData.value = deepCopy(data) as TreeNodeData[]
      })
    }
  } else {
    copyData.value = deepCopy(newVal || []) as TreeNodeData[]
  }
}, { immediate: true, deep: true })

defineExpose({ getCheckedNodes, getCheckedKeys, setCheckedNodes, setCheckedKeys, filter })
</script>
