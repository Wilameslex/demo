<template>
  <div class="gene-results-container">
    <!-- 标题区域 -->
    <div class="header">
      <h1><el-icon><Collection /></el-icon> Gene Search Result</h1>
      <div class="sub-header">
        <el-tag type="info"> {{ totalItems }} records in total</el-tag>
        <el-button
            icon="Back"
            @click="backToSearch"
            size="small"
        >
          Return search
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="search-params-card" style="margin-bottom: 20px;">
      <div class="params-header">
        <span class="params-title"><el-icon><Search /></el-icon> Search Criteria</span>
      </div>
      <div class="params-content">
        <!-- 基因ID（Gene id） -->
        <div v-if="searchParams.genes?.length > 0" class="param-item">
          <span class="param-label">Gene id：</span>
          <el-tag type="primary" effect="plain" class="param-tag" v-for="gene in searchParams.genes" :key="`gene-${gene}`">
            {{ gene }}
          </el-tag>
        </div>
        <!-- 蛋白ID（Protein id） -->
        <div v-if="searchParams.proteins?.length > 0" class="param-item">
          <span class="param-label">Protein id：</span>
          <el-tag type="success" effect="plain" class="param-tag" v-for="protein in searchParams.proteins" :key="`protein-${protein}`">
            {{ protein }}
          </el-tag>
        </div>
        <!-- 产物（Product） -->
        <div v-if="searchParams.products?.length > 0" class="param-item">
          <span class="param-label">Product：</span>
          <el-tag type="info" effect="plain" class="param-tag" v-for="product in searchParams.products" :key="`product-${product}`">
            {{ product }}
          </el-tag>
        </div>
        <!-- 容错显示（无有效条件时） -->
        <div v-if="!hasAnyParams" class="param-empty">
          No valid search criteria (fault tolerance display)
        </div>
      </div>
    </el-card>

    <!-- 筛选工具栏 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
              v-model="filterText"
              placeholder="Search in the results..."
              :prefix-icon="Search"
              clearable
          />
        </el-col>
        <el-col :span="8">
          <el-select
              v-model="filterColumn"
              placeholder="Select the field"
              clearable
          >
            <el-option
                v-for="col in columns"
                :key="col.prop"
                :label="col.label"
                :value="col.prop"
            />
          </el-select>
        </el-col>
        <el-col :span="8" class="text-right">
          <el-dropdown trigger="click" @command="handleExport">
            <el-button type="primary" icon="Download">
              Export result<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="csv">CSV</el-dropdown-item>
                <el-dropdown-item command="txt">TXT</el-dropdown-item>
                <el-dropdown-item command="excel">Excel</el-dropdown-item>
                <el-dropdown-item command="all" divided>
                  Export all
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-col>
      </el-row>
    </el-card>

    <!-- 结果表格 -->
    <el-card shadow="never">
      <gene-table
          :table-data="filteredResults"
          :loading="loading"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          @view="handleView"
      />

      <!-- 分页控件 -->
      <el-pagination
          class="pagination"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </el-card>

    <!-- 导出进度对话框 -->
    <el-dialog
        v-model="exportDialogVisible"
        title="exporting progress"
        width="30%"
        :close-on-click-modal="false"
        :show-close="false"
    >
      <el-progress
          :percentage="exportProgress"
          :status="exportStatus"
          :text-inside="true"
          :stroke-width="20"
      />
      <p v-if="exportStatus === 'success'" class="success-message">
        {{ exportMessage }}
      </p>
      <p v-else class="export-message">{{ exportMessage }}</p>
      <template #footer>
        <el-button
            v-if="exportStatus === 'success'"
            type="primary"
            @click="exportDialogVisible = false"
        >
          completed
        </el-button>
        <el-button
            v-else-if="exportStatus === 'exception'"
            @click="exportDialogVisible = false"
        >
          close
        </el-button>
        <el-button
            v-else
            type="danger"
            @click="cancelExport"
        >
          cancel
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import GeneTable from '@/components/GeneTable.vue'

// 图标导入
import {
  Collection,
  Back,
  Search,
  Download,
  ArrowDown
} from '@element-plus/icons-vue'

// 使用 Vue 相关钩子
const router = useRouter()
const store = useStore()

// 响应式数据
const filterText = ref('')
const filterColumn = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const exportDialogVisible = ref(false)
const exportProgress = ref(0)
const exportStatus = ref('')
const exportMessage = ref('正在准备导出数据...')
const exportFormat = ref('')
const exportTimer = ref(null)
const loading = ref(false)
const cancelToken = ref(null)

// 表格列配置
const columns = ref([
  { prop: 'Gene', label: 'Gene id', width: '120' },
  { prop: 'Name', label: 'Name', width: '150' },
  { prop: 'Chromosome', label: 'Chromosome', width: '100' },
  { prop: 'Start', label: 'Start', width: '100' },
  { prop: 'End', label: 'End', width: '100' },
  { prop: 'Protein', label: 'Protein id', width: '150' },
  { prop: 'Product', label: 'Product', minWidth: '200' },
  { prop: 'Description', label: 'Description', minWidth: '300' }
])

// 计算属性
const totalItems = computed(() => store.state.gene.totalItems)
const searchResults = computed(() => store.state.gene.searchResults)
const searchParams = computed(() => store.state.gene.searchParams)

const hasAnyParams = computed(() => {
  return (searchParams.value?.genes?.length > 0) ||
      (searchParams.value?.proteins?.length > 0) ||
      (searchParams.value?.products?.length > 0);
})

// 筛选后的结果
const filteredResults = computed(() => {
  if (!filterText.value || !filterColumn.value) {
    return searchResults.value
  }

  const keyword = filterText.value.toLowerCase()
  return searchResults.value.filter(item => {
    const value = String(item[filterColumn.value] || '').toLowerCase()
    return value.includes(keyword)
  })
})

// 方法定义
const backToSearch = () => {
  router.push({ name: 'GeneInfoSearch' })
}

const handleSizeChange = (size) => {
  pageSize.value = size
  refreshResults()
}

const handlePageChange = (page) => {
  currentPage.value = page
  refreshResults()
}

const refreshResults = async () => {
  loading.value = true
  try {
    await store.dispatch('gene/searchGenes', {
      ...searchParams.value,
      page: currentPage.value,
      size: pageSize.value
    })
  } catch (error) {
    console.error('Fail to refresh:', error)
    ElMessage.error('Fail to refresh')
  } finally {
    loading.value = false
  }
}

const handleExport = (command) => {
  if (command === 'all') {
    exportAllData()
    return
  }

  exportFormat.value = command
  exportDialogVisible.value = true
  exportStatus.value = ''
  exportProgress.value = 0
  exportMessage.value = 'Data is exporting...'

  // 模拟进度
  exportTimer.value = setInterval(() => {
    if (exportProgress.value < 90) {
      exportProgress.value += 10
      exportMessage.value = `Data is exporting (${exportProgress.value}%)...`
    }
  }, 200)

  // 开始导出
  setTimeout(() => {
    performExport()
  }, 500)
}

const performExport = () => {
  try {
    const exportData = filteredResults.value

    if (!exportData || exportData.length === 0) {
      throw new Error('no data to export')
    }

    let content = ''
    let filename = `gene search result_${new Date().toLocaleDateString().replace(/\//g, '-')}`

    switch(exportFormat.value) {
      case 'txt':
        content = exportToTxt(exportData)
        filename += '.txt'
        break
      case 'csv':
        content = exportToCsv(exportData)
        filename += '.csv'
        break
      case 'excel':
        exportToExcel(exportData, filename)
        clearInterval(exportTimer.value)
        exportProgress.value = 100
        exportStatus.value = 'success'
        exportMessage.value = 'Excel is exported'
        return
      default:
        throw new Error('exporting type not supported.')
    }

    // 创建Blob并下载
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    // 完成导出
    clearInterval(exportTimer.value)
    exportProgress.value = 100
    exportStatus.value = 'success'
    exportMessage.value = `Document has been generated: ${filename}`
  } catch (error) {
    clearInterval(exportTimer.value)
    exportProgress.value = 100
    exportStatus.value = 'exception'
    exportMessage.value = `fail to export: ${error.message}`
    console.error('fail to export:', error)
    ElMessage.error(`fail to export: ${error.message}`)
  }
}

const exportToTxt = (data) => {
  const headers = ['Gene', 'Name', 'Chromosome', 'Start', 'End', 'Protein', 'Product', 'Description']
  const fields = ['Gene', 'Name', 'Chromosome', 'Start', 'End', 'Protein', 'Product', 'Description']

  let txtContent = '基因搜索结果\n\n'
  txtContent += `导出时间: ${new Date().toLocaleString()}\n`
  txtContent += `记录数量: ${data.length}\n\n`

  // 添加表头
  txtContent += headers.join('\t') + '\n'
  txtContent += '-'.repeat(100) + '\n'

  // 添加数据行
  data.forEach(item => {
    const row = fields.map(field => {
      let value = item[field] || ''
      return value.toString().replace(/\n/g, '; ')
    })
    txtContent += row.join('\t') + '\n'
  })

  return txtContent
}

const exportToCsv = (data) => {
  const headers = ['Gene', 'Name', 'Chromosome', 'Start', 'End', 'Protein', 'Product', 'Description']
  const fields = ['Gene', 'Name', 'Chromosome', 'Start', 'End', 'Protein', 'Product', 'Description']

  let csvContent = headers.map(h => `"${h}"`).join(',') + '\n'

  data.forEach(item => {
    const row = fields.map(field => {
      let value = item[field] || ''
      if (typeof value === 'string') {
        value = value.replace(/"/g, '""')
        if (value.includes(',') || value.includes('\n') || value.includes('"')) {
          value = `"${value}"`
        }
      }
      return value
    })
    csvContent += row.join(',') + '\n'
  })

  return csvContent
}

const exportToExcel = (data, filename) => {
  const fields = ['Gene', 'Name', 'Chromosome', 'Start', 'End', 'Protein', 'Product', 'Description']
  const headers = ['Gene', 'Name', 'Chromosome', 'Start', 'End', 'Protein', 'Product', 'Description']

  const wb = XLSX.utils.book_new()
  const excelData = [
    headers,
    ...data.map(item => fields.map(field => item[field] || ''))
  ]

  const ws = XLSX.utils.aoa_to_sheet(excelData)
  XLSX.utils.book_append_sheet(wb, ws, '基因搜索结果')
  XLSX.writeFile(wb, `${filename}.xlsx`)
}

const exportAllData = async () => {
  try {
    ElMessage.info('开始导出全部数据...')
    // 这里调用后端导出接口
    // await store.dispatch('gene/exportAllGenes', searchParams.value)
  } catch (error) {
    console.error('导出全部数据失败:', error)
    ElMessage.error('导出全部数据失败')
  }
}

const cancelExport = () => {
  if (cancelToken.value) {
    cancelToken.value.cancel('用户取消导出')
  }
  clearInterval(exportTimer.value)
  exportDialogVisible.value = false
}

// 处理表格事件
const handleSelectionChange = (selection) => {
  console.log('选中项:', selection)
}

const handleSortChange = (sortInfo) => {
  console.log('排序变化:', sortInfo)
}

const handleView = (row) => {
  console.log('查看行:', row)
  // 这里可以添加查看详情的逻辑
}

// 生命周期
onMounted(() => {
  // 组件挂载后的初始化逻辑
})
</script>

<style scoped>
.gene-results-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.search-params-card {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.params-header {
  margin-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.params-title {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.params-content {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  padding-top: 10px;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.param-label {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.param-tag {
  margin-bottom: 4px;
  cursor: default;
  font-size: 13px;
}

.param-empty {
  color: #909399;
  font-size: 14px;
  padding: 5px 0;
}

.sub-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.filter-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.text-right {
  text-align: right;
}

.export-message {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #606266;
}

.success-message {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #67C23A;
  font-weight: bold;
}

.el-dropdown {
  margin-left: 10px;
}
</style>
