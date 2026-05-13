<template>
  <transition
    @enter="enter"
    @after-enter="afterEnter"
    @leave="leave"
    @after-leave="afterLeave"
  >
    <slot></slot>
  </transition>
</template>

<script setup lang="ts">
defineOptions({ name: 'AutoHeight' })

function enter(el: Element) {
  const htmlEl = el as HTMLElement
  htmlEl.style.height = 'auto'
  const endWidth = window.getComputedStyle(htmlEl).height
  htmlEl.style.height = '0px'
  htmlEl.offsetHeight
  htmlEl.style.height = endWidth
}

function afterEnter(el: Element) {
  ;(el as HTMLElement).style.height = ''
}

function leave(el: Element) {
  const htmlEl = el as HTMLElement
  htmlEl.style.height = window.getComputedStyle(htmlEl).height
  htmlEl.offsetHeight
  htmlEl.style.height = '0px'
}

function afterLeave(el: Element) {
  ;(el as HTMLElement).style.height = ''
}
</script>
