<script setup lang="ts">
// @ts-nocheck
import { computed, onMounted, ref, watch } from 'vue'
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

defineOptions({ name: 'DatasourcePanel' })

const { t } = useI18n()
const store = useReportStore()

const treeContainer = ref<HTMLDivElement | null>(null)
const datasourceDialog = ref<any>(null)
const springDialog = ref<any>(null)
const buildinDialog = ref<any>(null)

const datasources = ref<any[]>([])
const datasourceDialogVisible = ref(false)
const currentDatasource = ref<any>(null)
const springDialogVisible = ref(false)
const currentSpringDatasource = ref<any>(null)
const buildinDialogVisible = ref(false)

const context = computed(() => store.context || {})

const jdbcDatasources = computed(() => datasources.value.filter((ds: any) => ds.type === 'jdbc'))
const springDatasources = computed(() => datasources.value.filter((ds: any) => ds.type === 'spring'))
const buildinDatasources = computed(() => datasources.value.filter((ds: any) => ds.type === 'buildin'))

watch(context, (newContext) => {
  if (newContext && newContext.reportDef) {
    initializeDatasources()
  }
}, { immediate: true })

onMounted(() => {
  initializeDatasources()
})

function initializeDatasources() {
  const ctx = context.value
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

  const newIndex = datasources.value.length
  datasources.value[newIndex] = newDatasource

  const reportDef = { ...context.value.reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function addSpringDatasource(datasource: any) {
  const newDatasource = {
    name: datasource.name,
    beanId: datasource.beanId,
    type: datasource.type || 'spring',
    datasets: datasource.datasets || [],
  }

  const newIndex = datasources.value.length
  datasources.value[newIndex] = newDatasource

  const reportDef = { ...context.value.reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function addBuildinDatasource(datasource: any) {
  const newDatasource = {
    name: datasource.name,
    type: datasource.type || 'buildin',
    datasets: datasource.datasets || [],
  }

  const newIndex = datasources.value.length
  datasources.value[newIndex] = newDatasource

  const reportDef = { ...context.value.reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function removeDatasource(name: string) {
  const index = datasources.value.findIndex((d: any) => d.name === name)
  if (index !== -1) {
    datasources.value.splice(index, 1)

    const reportDef = { ...context.value.reportDef, datasources: datasources.value }
    updateReportDef(reportDef)
  }
}

function updateDatasource(data: any) {
  const index = datasources.value.findIndex((ds: any) => ds.name === data.oldName)
  if (index !== -1) {
    datasources.value[index] = { ...datasources.value[index], ...data }
  }

  const reportDef = { ...context.value.reportDef, datasources: datasources.value }
  updateReportDef(reportDef)
}

function updateSpringDatasets(datasource: any, datasets: any[]) {
  datasource.datasets = datasets
  const reportDef = { ...context.value.reportDef, datasources: datasources.value }
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
    </div>

    <!-- 树容器 -->
    <div ref="treeContainer">
      <!-- 数据库树组件 -->
      <DatabaseTree
        v-for="(datasource, index) in jdbcDatasources"
        :key="`jdbc_` + `_${index}`"
        ref="databaseTree"
        :datasources="datasources"
        :ds="datasource"
        @remove="removeDatasource"
        @update-datasource="updateDatasource"
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
  </div>
</template>

<style scoped>
.ds-toolbar {
  background: rgb(248, 248, 248);
  line-height: 40px;
  box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.2);
}

.toolbar-btn {
  border: none;
  background: #f8f8f8;
}
</style>
