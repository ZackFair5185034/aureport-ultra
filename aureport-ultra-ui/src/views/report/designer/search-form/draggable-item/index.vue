<script setup lang="ts">
import { defineProps, defineOptions } from 'vue'

defineOptions({
  name: 'DraggableItem'
})

const props = defineProps<{
  element: any
  index: number
  drawingList: any[]
  activeId: number | string
  formConf: any
}>()
</script>

<script lang="ts">
import { h } from 'vue'
// @ts-ignore
import renderComponent from '../utils/render'
// @ts-ignore
import draggable from 'vuedraggable'
import URow from '@/components/row/index.vue'
import UCol from '@/components/col/index.vue'
import UFormItem from '@/components/form-item/index.vue'

function itemBtns(this: any, _h: any, element: any, index: number, parent: any[]) {
  const onCopyItem = this.$attrs.onCopyItem
  const onDeleteItem = this.$attrs.onDeleteItem
  return [
    h('span', {
      class: 'drawing-item-copy',
      title: '复制',
      onClick(event: Event) {
        onCopyItem?.(element, parent)
        event.stopPropagation()
      }
    }, [
      h('i', { class: 'iconfont icon-share' })
    ]),
    h('span', {
      class: 'drawing-item-delete',
      title: '删除',
      onClick(event: Event) {
        onDeleteItem?.(index, parent)
        event.stopPropagation()
      }
    }, [
      h('i', { class: 'iconfont icon-delete' })
    ])
  ]
}

function colFormItem(this: any, _h: any, element: any, _index: number, parent: any[]) {
  const onActiveItem = this.$attrs.onActiveItem
  let className = this.activeId === element.formId ? 'drawing-item active-from-item' : 'drawing-item'
  if (this.formConf.unFocusedComponentBorder) className += ' unfocus-bordered'
  return h(UCol, {
    span: element.span,
    class: className,
    onClick(event: Event) {
      onActiveItem?.(element)
      event.stopPropagation()
    }
  }, [
    h(UFormItem, {
      'label-width': element.labelWidth ? Number(element.labelWidth) : null,
      label: element.label,
      required: element.required
    }, [
      h(renderComponent, {
        key: element.renderKey,
        conf: element,
        onInput(event: any) {
          element.defaultValue = event
        }
      })
    ]),
    ...itemBtns.apply(this, [_h, element, _index, parent])
  ])
}

function rowFormItem(this: any, _h: any, element: any, _index: number, parent: any[]) {
  const onActiveItem = this.$attrs.onActiveItem
  const className = this.activeId === element.formId ? 'drawing-row-item active-from-item' : 'drawing-row-item'
  let child = renderChildren.apply(this, [_h, element, _index, parent])
  if (element.type === 'flex') {
    child = h(URow, {
      type: element.type,
      justify: element.justify,
      align: element.align
    }, [child])
  }
  return h(UCol, {
    span: element.span
  }, [
    h(URow, {
      gutter: element.gutter,
      class: className,
      onClick(event: Event) {
        onActiveItem?.(element)
        event.stopPropagation()
      }
    }, [
      h('span', { class: 'component-name' }, [element.componentName]),
      h(draggable as any, {
        list: element.children,
        animation: 340,
        group: 'componentsGroup',
        class: 'drag-wrapper'
      }, [child]),
      ...itemBtns.apply(this, [_h, element, _index, parent])
    ])
  ])
}

const layouts: Record<string, (this: any, h: any, element: any, index: number, parent: any[]) => any> = {
  colFormItem,
  rowFormItem
}

function renderChildren(this: any, _h: any, element: any, index: number, parent: any[]) {
  if (!Array.isArray(element.children)) return null
  return element.children.map((el: any, i: number) => {
    const layout = layouts[el.layout]
    if (layout) {
      return layout.call(this, _h, el, i, element.children)
    }
    return layoutIsNotFound(el.layout)
  })
}

function layoutIsNotFound(layout: string): never {
  throw new Error(`没有与${layout}匹配的layout`)
}

export default {
  render(this: any, _h: any) {
    const self = this
    const layout = layouts[self.element.layout]
    if (layout) {
      return layout.call(self, _h, self.element, self.index, self.drawingList)
    }
    return layoutIsNotFound(self.element.layout)
  }
}
</script>
