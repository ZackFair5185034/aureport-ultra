<script setup lang="ts">

import draggable from 'vuedraggable'
import UCol from '@/components/col/index.vue'
import UFormItem from '@/components/form-item/index.vue'
import URow from '@/components/row/index.vue'
// @ts-expect-error RenderComponent type issue
import RenderComponent from '../utils/render'

const props = defineProps<{
  element: any
  index: number
  drawingList: any[]
  activeId: number | string
  formConf: any
}>()

const attrs = useAttrs() as Record<string, (...args: any[]) => void>
const onActiveItem = computed(() => attrs.onActiveItem as ((element: any) => void) | undefined)
const onCopyItem = computed(() => attrs.onCopyItem as ((element: any, parent: any[]) => void) | undefined)
const onDeleteItem = computed(() => attrs.onDeleteItem as ((index: number, parent: any[]) => void) | undefined)

function itemBtns(element: any, index: number, parent: any[]) {
  return [
    h('span', {
      class: 'drawing-item-copy',
      title: '复制',
      onClick(event: Event) {
        onCopyItem.value?.(element, parent)
        event.stopPropagation()
      },
    }, [
      h('i', { class: 'iconfont icon-copy' }),
    ]),
    h('span', {
      class: 'drawing-item-delete',
      title: '删除',
      onClick(event: Event) {
        onDeleteItem.value?.(index, parent)
        event.stopPropagation()
      },
    }, [
      h('i', { class: 'iconfont icon-delete' }),
    ]),
  ]
}

function renderChildren(element: any) {
  if (!Array.isArray(element.children))
    return null
  return element.children.map((child: any, i: number) => {
    const childLayout = layouts[child.layout]
    if (childLayout) {
      return childLayout(child, i, element.children)
    }
    return null
  })
}

function colFormItem(element: any, index: number, parent: any[]) {
  const isActive = props.activeId === element.formId
  let className = isActive ? 'drawing-item active-from-item' : 'drawing-item'
  if (props.formConf?.unFocusedComponentBorder)
    className += ' unfocus-bordered'

  return h(UCol, {
    span: element.span,
    class: className,
    onClick(event: Event) {
      onActiveItem.value?.(element)
      event.stopPropagation()
    },
  }, () => [
    h(UFormItem, {
      labelWidth: element.labelWidth ? Number(element.labelWidth) : undefined,
      label: element.label,
      required: element.required,
    }, () => [
      h(RenderComponent, {
        conf: element,
        onInput(event: any) {
          element.defaultValue = event
        },
      }),
    ]),
    ...itemBtns(element, index, parent),
  ])
}

function rowFormItem(element: any, index: number, parent: any[]) {
  const isActive = props.activeId === element.formId
  const className = isActive ? 'drawing-row-item active-from-item' : 'drawing-row-item'

  const children = renderChildren(element)
  let childContent = [
    h('span', { class: 'component-name' }, () => element.componentName),
    ...itemBtns(element, index, parent),
  ]

  if (element.children && element.children.length > 0) {
    childContent = [
      h('span', { class: 'component-name' }, () => element.componentName),
      h(draggable, {
        list: element.children,
        animation: 340,
        group: 'componentsGroup',
        class: 'drag-wrapper',
        itemKey: 'renderKey',
      }, {
        item: ({ element: childEl }: { element: any }) => {
          const childLayout = layouts[childEl.layout]
          if (childLayout) {
            return childLayout(childEl, 0, element.children)
          }
          return null
        },
      }),
      ...itemBtns(element, index, parent),
    ]
  }

  const rowProps: Record<string, any> = {
    gutter: element.gutter,
    class: className,
    onClick(event: Event) {
      onActiveItem.value?.(element)
      event.stopPropagation()
    },
  }

  if (element.type === 'flex') {
    rowProps.type = element.type
    rowProps.justify = element.justify
    rowProps.align = element.align
  }

  return h(UCol, {
    span: element.span,
  }, () => [
    h(URow, rowProps, () => childContent),
  ])
}

const layouts: Record<string, (element: any, index: number, parent: any[]) => any> = {
  colFormItem,
  rowFormItem,
}

function createLayoutComponent(layout: (element: any, index: number, parent: any[]) => any, element: any, index: number, drawingList: any[]) {
  function render() {
    return layout(element, index, drawingList)
  }
  return defineComponent({
    setup() {
      return render
    },
  })
}

const layoutComponent = computed(() => {
  const layout = layouts[props.element.layout]
  if (!layout) {
    console.error(`没有与${props.element.layout}匹配的layout`)
    return null
  }

  return createLayoutComponent(layout, props.element, props.index, props.drawingList)
})
</script>

<template>
  <component
    :is="layoutComponent"
    v-if="layoutComponent"
    :element="element"
    :index="index"
    :drawing-list="drawingList"
    :active-id="activeId"
    :form-conf="formConf"
    :on-active-item="onActiveItem"
    :on-copy-item="onCopyItem"
    :on-delete-item="onDeleteItem"
  />
</template>