<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const reportPath = route.query.reportPath as string || ''

function goBack() {
  router.push('/')
}
</script>

<template>
  <div class="preview-page">
    <div class="preview-header">
      <button class="back-btn" @click="goBack">← 返回设计器</button>
      <span class="preview-title">报表预览 — {{ reportPath }}</span>
    </div>
    <div class="preview-body">
      <aureport-preview
        :report-path="reportPath"
        locale="zh"
        mode="preview"
      />
    </div>
  </div>
</template>

<style scoped>
.preview-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #ddd;
  flex-shrink: 0;
}

.back-btn {
  padding: 4px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: #e8e8e8;
}

.preview-title {
  font-size: 14px;
  color: #666;
}

.preview-body {
  flex: 1;
  overflow: hidden;
}
</style>
