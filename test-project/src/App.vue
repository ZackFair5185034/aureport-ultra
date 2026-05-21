<script setup lang="ts">
// 导入 lib（void 防止 tree-shaking）
import AureportUltraUI from 'aureport-ultra-ui'
void AureportUltraUI
import 'aureport-ultra-ui/style.css'

function onSave(e: Event) {
  console.log('[Test] save event:', e)
}
function onNavigate(e: Event) {
  console.log('[Test] navigate event:', e)
}
function onError(e: Event) {
  console.error('[Test] error event:', e)
}
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
        report-path="file:test.ureport.xml"
        locale="zh"
        @save="onSave"
        @navigate="onNavigate"
        @error="onError"
      />
    </div>
  </div>
</template>
