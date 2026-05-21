<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { deepCopy } from '@/components/utils'
import { useReportStore } from '@/stores/report'
import { updateReportDef } from '@/utils/contextActions'
import BuildinDatasourceSelectDialog from './buildin-datasource-select-dialog/index.vue'
import BuildinTree from './buildin-tree/index.vue'
import DatabaseTree from './database-tree/index.vue'
import DatasourceDialog from './datasource-dialog/index.vue'
import SpringDialog from './spring-dialog/index.vue'
import SpringTree from './spring-tree/index.vue'
import HttpDialog from './http-dialog/index.vue'
import HttpTree from './http-tree/index.vue'

defineOptions({ name: 'DatasourcePanel' })

const { t } = useI18n()
const store = useReportStore()

const treeContainer = ref<HTMLDivElement | null>(null)
const datasourceDialog = ref<any>(null)
const springDialog = ref<any>(null)
const buildinDialog = ref<any>(null)
const httpDialog = ref<any>(null)

const datasources = ref<any[]>([])
const datasourceDialogVisible = ref(false)
const currentDatasource = ref<any>(null)
const springDialogVisible = ref(false)
const currentSpringDatasource = ref<any>(null)
const buildinDialogVisible = ref(false)
const httpDialogVisible = ref(false)
const currentHttpDatasource = ref<any>(null)

const context = computed(() => store.context || null)

const jdbcDatasources = computed(() => datasources.value.filter((ds: any) => ds.type === 'jdbc'))
const springDatasources = computed(() => datasources.value.filter((ds: any) => ds.type === 'spring'))
const buildinDatasources = computed(() => datasources.value.filter((ds: any) => ds.type === 'buildin'))
const httpDatasources = computed(() => datasources.value.filter((ds: any) => ds.type === 'http'))

watch(context, (newContext) => {
  if (newContext && (newContext as any).reportDef) {
    initializeDatasources()
  }
}, { immediate: true })

onMounted(() => {
  initializeDatasources()
})

function initializeDatasources() {
  const ctx = context.value as any
  if (!ctx)
    return

  const reportDef = ctx.reportDef
  if (!reportDef)
    return

  if (!reportDef.datasources) {
    const newReportDef = deepCopy(reportDef)
    newReportDef.datasources = []
    updateReportDef(newReportDef)
  }

  datasources.value = reportDef.datasources || []
}

function showDatasourceDialog() {
  currentDatasource.value = null
  datasourceDialogVisible.value = true
}

function showSpringDialog() {
  currentSpringDatasource.value = null
  springDialogVisible.value = true
}

function showBuildinDialog() {
  buildinDialogVisible.value = true
}

function showHttpDialog() {
  currentHttpDatasource.value = null
  httpDialogVisible.value = true
}

function addJdbcDatasource(datasource: any) {
  const newDatasource = {
    name: datasource.name,
    username: datasource.username,
    password: datasource.password,
    type: datasource.type || 'jdbc',
    url: datasource.url,
    driver: datasource.driver,
    datasets: datasource.datasets || [],
  }

  datasources.value.push(newDatasource)

  const reportDef: any = { ...(context.value as any).reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function addSpringDatasource(datasource: any) {
  const newDatasource = {
    name: datasource.name,
    beanId: datasource.beanId,
    type: datasource.type || 'spring',
    datasets: datasource.datasets || [],
  }

  datasources.value.push(newDatasource)

  const reportDef: any = { ...(context.value as any).reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function addBuildinDatasource(datasource: any) {
  const newDatasource = {
    name: datasource.name,
    type: datasource.type || 'buildin',
    datasets: datasource.datasets || [],
  }

  datasources.value.push(newDatasource)

  const reportDef: any = { ...(context.value as any).reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function addHttpDatasource(datasource: any) {
  const newDatasource = {
    name: datasource.name,
    protocolType: datasource.protocolType || 'standard',
    hostType: datasource.hostType || 'manual',
    host: datasource.host || '',
    serviceName: datasource.serviceName || '',
    baseUrl: datasource.baseUrl || '',
    headers: datasource.headers || '',
    type: datasource.type || 'http',
    datasets: datasource.datasets || [],
  }

  datasources.value.push(newDatasource)

  const reportDef: any = { ...(context.value as any).reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function removeDatasource(name: string) {
  const index = datasources.value.findIndex((d: any) => d.name === name)
  if (index !== -1) {
    datasources.value.splice(index, 1)

    const reportDef: any = { ...(context.value as any).reportDef, datasources: datasources.value }
    updateReportDef(reportDef)
  }
}

function updateDatasource(data: any) {
  const index = datasources.value.findIndex((ds: any) => ds.name === data.oldName)
  if (index !== -1) {
    const { oldName, ...cleanData } = data
    datasources.value[index] = { ...datasources.value[index], ...cleanData }
  }

  const reportDef: any = { ...(context.value as any).reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function updateSpringDatasets(datasource: any, datasets: any[]) {
  const index = datasources.value.findIndex((ds: any) => ds.name === datasource.name)
  if (index !== -1) {
    datasources.value[index] = { ...datasource, datasets }
  }
  const reportDef: any = { ...(context.value as any).reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function updateHttpDatasets(datasource: any, datasets: any[]) {
  const index = datasources.value.findIndex((ds: any) => ds.name === datasource.name)
  if (index !== -1) {
    datasources.value[index] = { ...datasource, datasets }
  }
  const reportDef: any = { ...(context.value as any).reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function buildPanel() {
  return [{
    appendChild: (el: HTMLElement) => {
      if (treeContainer.value) {
        treeContainer.value.append(el)
      }
    },
  }]
}
</script>

<template>
  <div style="width:100%;">
    <!-- 工具栏 -->
    <div class="ds-toolbar">
      <u-button
        type="info"
        class="toolbar-btn"
        icon="icon-database"
        :title="$t('property.datasource.title')"
        @click="showDatasourceDialog"
      />

      <u-button
        type="info"
        class="toolbar-btn"
        icon="icon-leaf"
        :title="$t('property.datasource.addBean')"
        @click="showSpringDialog"
      />

      <u-button
        type="info"
        class="toolbar-btn"
        icon="icon-share"
        :title="$t('property.datasource.addBuildin')"
        @click="showBuildinDialog"
      />

      <u-button
        type="info"
        class="toolbar-btn"
        icon="icon-link"
        :title="$t('property.datasource.addHttp')"
        @click="showHttpDialog"
      />
    </div>

    <!-- 树容器 -->
    <div ref="treeContainer">
      <!-- 数据库树组件 -->
      <DatabaseTree
        v-for="(datasource, index) in jdbcDatasources"
        :key="`jdbc_` + `_${index}`"
        :datasources="datasources"
        :ds="datasource"
        :datasets="datasource.datasets || []"
        @remove="removeDatasource"
        @update-datasource="updateDatasource"
        @update-datasets="updateSpringDatasets(datasource, $event)"
      />

      <!-- Spring树组件 -->
      <SpringTree
        v-for="(datasource, index) in springDatasources"
        :key="`spring_` + `_${index}`"
        :name="datasource.name"
        :datasets="datasource.datasets || []"
        :datasources="datasources"
        :bean-id="datasource.beanId"
        @remove="removeDatasource"
        @update-datasource="updateDatasource"
        @update-datasets="updateSpringDatasets(datasource, $event)"
      />

      <!-- 内置数据源树组件 -->
      <BuildinTree
        v-for="(datasource, index) in buildinDatasources"
        :key="`buildin_` + `_${index}`"
        :name="datasource.name"
        :datasets="datasource.datasets || []"
        @remove="removeDatasource"
        @update-datasource="updateDatasource"
      />

      <!-- HTTP树组件 -->
      <HttpTree
        v-for="(datasource, index) in httpDatasources"
        :key="`http_` + `_${index}`"
        :name="datasource.name"
        :datasets="datasource.datasets || []"
        :datasources="datasources"
        :protocol-type="datasource.protocolType || 'standard'"
        :base-url="datasource.baseUrl || ''"
        :host="datasource.host || ''"
        :host-type="datasource.hostType || 'manual'"
        :service-name="datasource.serviceName || ''"
        :headers="datasource.headers || ''"
        @remove="removeDatasource"
        @update-datasource="updateDatasource"
        @update-datasets="updateHttpDatasets(datasource, $event)"
      />
    </div>

    <!-- 数据源对话框 -->
    <DatasourceDialog
      ref="datasourceDialog"
      :datasources="datasources"
      :visible="datasourceDialogVisible"
      :datasource="currentDatasource"
      @close="datasourceDialogVisible = false"
      @save="addJdbcDatasource"
    />

    <!-- Spring对话框 -->
    <SpringDialog
      ref="springDialog"
      :datasources="datasources"
      :visible="springDialogVisible"
      :datasource="currentSpringDatasource"
      @close="springDialogVisible = false"
      @save="addSpringDatasource"
    />

    <!-- 内置数据源选择对话框 -->
    <BuildinDatasourceSelectDialog
      ref="buildinDialog"
      :datasources="datasources"
      :visible="buildinDialogVisible"
      @close="buildinDialogVisible = false"
      @select="addBuildinDatasource"
    />

    <!-- HTTP数据源对话框 -->
    <HttpDialog
      ref="httpDialog"
      :datasources="datasources"
      :visible="httpDialogVisible"
      :datasource="currentHttpDatasource"
      @close="httpDialogVisible = false"
      @save="addHttpDatasource"
    />
  </div>
</template>

<style scoped>
.ds-toolbar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgb(248, 248, 248);
  line-height: 40px;
  box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.2);
}

.toolbar-btn {
  border: none;
  background: #f8f8f8;
}
</style>
