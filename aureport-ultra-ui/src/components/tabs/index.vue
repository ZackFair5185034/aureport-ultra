<script setup lang="ts">
import { ref, computed, provide } from 'vue'

defineOptions({ name: 'UTabs' })

export interface TabsContext {
  registerPane: (pane: { label: string; index: string }) => void
  unregisterPane: (pane: { label: string; index: string }) => void
  activeValue: unknown
}

const props = withDefaults(defineProps<{
  modelValue?: string | number
  type?: string
  navPosition?: string
  navCenter?: boolean
  padding?: string
  height?: string
  minHeight?: string
  maxHeight?: string
  overflow?: string
}>(), {
  modelValue: '',
  type: '',
  navPosition: 'top',
  navCenter: false,
  minHeight: '',
  maxHeight: '',
  overflow: '',
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  'tab-change': [value: string | number]
}>()

const panes = ref<Array<{ label: string; index: string }>>([])

function registerPane(pane: { label: string; index: string }) {
  panes.value.push(pane)
}

function unregisterPane(pane: { label: string; index: string }) {
  const idx = panes.value.indexOf(pane)
  if (idx !== -1) panes.value.splice(idx, 1)
}

function selectTab(idx: string) {
  emit('update:modelValue', idx)
  emit('tab-change', idx)
}

const tabsContext: TabsContext = {
  registerPane,
  unregisterPane,
  get activeValue() { return props.modelValue },
}
provide('tabsContext', tabsContext)

const contentStyle = computed(() => {
  const { padding, height, overflow, minHeight, maxHeight } = props
  return { padding, height, minHeight, maxHeight, overflow }
})
</script>

<template>
  <div :class="['u-tabs tabs', type ? `-${type}` : '']">
    <div v-if="navPosition === 'bottom' || navPosition === 'right'" class="content" :style="contentStyle">
      <slot />
    </div>
    <ul :class="['nav', navPosition, { center: navCenter }]">
      <li
        v-for="pane in panes"
        :key="pane.index"
        v-html="pane.label"
        :class="{ active: pane.index === modelValue }"
        @click.stop="selectTab(pane.index)"
      />
    </ul>
    <div v-if="navPosition === 'top' || navPosition === 'left'" class="content" :style="contentStyle">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.u-tabs.tabs {
  font-size: 1rem;
  background: #fff
}

.u-tabs.tabs.-card {
  border: 1px solid rgba(34,36,38,.15);
  box-shadow: 0 2px 3px 0 rgba(34,36,38,.15)
}

.u-tabs.tabs.-card .nav {
  background: #f8f8f8;
  margin: 0
}

.u-tabs.tabs.-card .nav li:first-child {
  margin-left: -1px
}

.u-tabs.tabs.-card .nav.top li {
  margin-top: -1px
}

.u-tabs.tabs.-card .nav.bottom li {
  margin-bottom: -1px
}

.u-tabs.tabs.-text .nav,.u-tabs.tabs.-text .nav li {
  border-color: transparent!important
}

.u-tabs.tabs.-text .nav li {
  padding: .35714286em .5em
}

.u-tabs.tabs.-button .nav {
  border: none!important
}

.u-tabs.tabs.-button .nav li {
  border: none!important;
  border-radius: 3px;
  margin: 0 .357em!important
}

.u-tabs.tabs.-button .nav li.active,.u-tabs.tabs.-button .nav li:hover {
  background-color: rgba(0,0,0,.05)
}

.u-tabs.tabs .nav {
  padding: 0;
  margin: 0;
  display: -ms-flexbox;
  display: flex;
  -ms-flex-wrap: wrap;
  flex-wrap: wrap;
  list-style: none
}

.u-tabs.tabs .nav.center {
  justify-content: center
}

.u-tabs.tabs .nav li {
  padding: .785em .925em;
  cursor: pointer;
  border: 1px solid transparent;
  line-height: 1
}

.u-tabs.tabs .nav li.active {
  border: 1px solid rgba(34,36,38,.15);
  color: #00554a;
  background: #fff;
  font-weight: 700
}

.u-tabs.tabs .nav li:hover {
  color: #00554a;
}

.u-tabs.tabs .nav.top {
  border-bottom: 1px solid rgba(34,36,38,.15)
}

.u-tabs.tabs .nav.top li {
  margin-bottom: -1px
}

.u-tabs.tabs .nav.top li.active {
  border-bottom-color: #fff
}

.u-tabs.tabs .nav.bottom {
  border-top: 1px solid rgba(34,36,38,.15)
}

.u-tabs.tabs .nav.bottom li {
  margin-top: -1px
}

.u-tabs.tabs .nav.bottom li.active {
  border-top-color: #fff
}
</style>
