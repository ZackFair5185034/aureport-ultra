<script setup lang="ts">

import { useI18n } from 'vue-i18n'
import { buildDatabaseTables } from '@/api/designer'
import { showAlert } from '@/utils/comnon'

defineOptions({ name: 'SearchTable' })

const props = withDefaults(defineProps<{
  db?: any
  triggerLoad?: boolean
}>(), {
  db: null,
  triggerLoad: false,
})

const emit = defineEmits<{
  (e: 'add', sql: string): void
  (e: 'load-complete'): void
}>()

const { t } = useI18n()

const tables = ref<any[]>([])
const searchKeyword = ref('')

const filteredTables = computed(() => {
  if (!searchKeyword.value) {
    return tables.value
  }

  const keyword = searchKeyword.value.toLowerCase()
  return tables.value.filter((table: any) =>
    table.name.toLowerCase().includes(keyword),
  )
})

watch(() => props.triggerLoad, (newVal) => {
  if (newVal) {
    loadDatabaseTables()
    emit('load-complete')
  }
})

function setTables(newTables: any[]) {
  tables.value = newTables
}

function addSql(tableName: string) {
  const sql = `select * from ${tableName}`
  emit('add', sql)
}

async function loadDatabaseTables() {
  if (!props.db)
    return

  searchKeyword.value = ''
  const type = props.db.type
  const parameters: any = { type }

  if (type === 'jdbc') {
    parameters.username = props.db.username
    parameters.password = props.db.password
    parameters.driver = props.db.driver
    parameters.url = props.db.url
  }
  else if (type === 'buildin') {
    parameters.name = props.db.name
    parameters.type = 'buildin'
  }

  try {
    const result = await buildDatabaseTables(parameters)
    setTables(result as any[])
  }
  catch (error: any) {
    if (error.msg) {
      showAlert(t('dialog.save.serverError') + t('colon') + error.msg, { useHTMLString: true })
    }
    else {
      showAlert(t('dialog.sql.loadFail'))
    }
  }
}
</script>

<template>
  <div style="width: 250px; height: 450px;">
    <div class="form-group" style="margin-bottom: 5px;">
      <div class="u-inline">
        <u-input
          v-model="searchKeyword"
          :placeholder="$t('dialog.sql.search')"
          style="width: 190px;"
        />
      </div>
      <div class="u-inline" style="vertical-align: middle;margin-left: 5px">
        <u-button
          type="info"
          icon="icon-search"
          class="search-bth"
        />
      </div>
    </div>
    <div class="table-container">
      <table class="data-table" style="font-size: 12px;">
        <thead>
          <tr style="height: 30px;background: #fafafa">
            <td style="width: 135px;"><span>{{ $t('dialog.sql.tableName') }}</span></td>
            <td style="width: 30px;"><span>{{ $t('dialog.sql.type') }}</span></td>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(table, index) in filteredTables"
            :key="`${table.name}-${index}`"
            style="height: 30px"
            @dblclick="addSql(table.name)"
          >
            <td>
              <a href="###" :title="$t('dialog.sql.addSql')" @click="addSql(table.name)">
                {{ table.name }}
              </a>
            </td>
            <td>
              <span :style="{ color: table.type === 'TABLE' ? '#49a700' : '#8B2252' }">
                {{ table.type === 'TABLE' ? $t('dialog.sql.table') : $t('dialog.sql.view') }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.search-btn {
  vertical-align: middle;
  margin-left: 5px;
}

.table-container {
  height: 380px;
  overflow-y: auto;
  overflow-x: auto;
  border: 1px solid #ddd;
  border-top: none;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.data-table td {
  border: 1px solid #ddd;
  padding: 4px;
  word-wrap: break-word;
}
</style>
