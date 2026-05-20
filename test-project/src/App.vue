<script setup lang="ts">
// @ts-ignore - AureportUltraUI registers custom elements as side effect
import AureportUltraUI from 'aureport-ultra-ui'
// Prevent tree-shaking: access the module to keep the import alive
console.log('AureportUltraUI loaded:', !!AureportUltraUI)

const onSave = (e: Event) => console.log('save:', (e as CustomEvent).detail)
const onNavigate = (e: Event) => console.log('navigate:', (e as CustomEvent).detail)
const onError = (e: Event) => console.error('error:', (e as CustomEvent).detail)
</script>

<template>
  <div style="width: 100vw; height: 100vh; display: flex; flex-direction: column;">
    <div style="padding: 8px; background: #f5f5f5; border-bottom: 1px solid #ddd;">
      <h2 style="margin: 0; font-size: 16px;">Aureport Ultra 集成测试</h2>
      <div style="font-size: 12px; color: #666;">
        宿主项目：Vue 3 + Vite 8 | 报表引擎：aureport-ultra-ui
      </div>
    </div>
    <div style="flex: 1; overflow: hidden;">
      <aureport-designer
        report-path="/reports/test.ureport.xml"
        locale="zh"
        @save="onSave"
        @navigate="onNavigate"
        @error="onError"
      />
    </div>
  </div>
</template>
