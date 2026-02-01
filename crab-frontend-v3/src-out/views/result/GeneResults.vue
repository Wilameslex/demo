<template>
  <div class="gene-results-container">
    <!-- 标题区域 -->
    <div class="header">
      <h1>
        <el-icon><el-icon-collection /></el-icon> 基因搜索结果
      </h1>
      <div class="sub-header">
        <el-tag type="info">共 {{ totalItems }} 条记录</el-tag>
        <el-button :icon="ElIconBack" @click="backToSearch" size="small">
          返回搜索
        </el-button>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="filterText"
            placeholder="在结果中搜索..."
            :prefix-icon="ElIconSearch"
            clearable
          />
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="filterColumn"
            placeholder="选择筛选字段"
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
            <el-button type="primary" :icon="ElIconDownload">
              导出结果<el-icon class="el-icon--right"
                ><el-icon-arrow-down
              /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="csv">CSV格式</el-dropdown-item>
                <el-dropdown-item command="txt">TXT格式</el-dropdown-item>
                <el-dropdown-item command="excel">Excel格式</el-dropdown-item>
                <el-dropdown-item command="all" divided>
                  导出全部数据(后台处理)
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
        :data="filteredResults"
        :columns="columns"
        :loading="loading"
      />

      <!-- 分页控件 -->
      <el-pagination
        class="pagination"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalItems"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>
    <!-- 导出进度对话框 -->
    <el-dialog
      title="导出进度"
      v-model="exportDialogVisible"
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
          完成
        </el-button>
        <el-button
          v-else-if="exportStatus === 'exception'"
          @click="exportDialogVisible = false"
        >
          关闭
        </el-button>
        <el-button v-else type="danger" @click="cancelExport">
          取消导出
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  Collection as ElIconCollection,
  ArrowDown as ElIconArrowDown,
  Back as ElIconBack,
  Search as ElIconSearch,
  Download as ElIconDownload,
} from '@element-plus/icons'
import { mapState, mapGetters } from 'vuex'
import GeneTable from '@/components/GeneTable.vue'
import * as XLSX from 'xlsx' // 引入Excel处理库
import { exportToTxt, exportToCsv, exportToExcel } from '@/utils/exportUtils'

export default {
  data() {
    return {
      filterText: '',
      filterColumn: '',
      currentPage: 1,
      pageSize: 10,
      columns: [
        { prop: 'Gene', label: '基因', width: '120' },
        { prop: 'Name', label: '名称', width: '150' },
        { prop: 'Chromosome', label: '染色体', width: '100' },
        { prop: 'Start', label: '起始位置', width: '100' },
        { prop: 'End', label: '结束位置', width: '100' },
        { prop: 'Protein', label: '蛋白ID', width: '150' },
        { prop: 'Product', label: '产品', minWidth: '200' },
        { prop: 'Description', label: '描述', minWidth: '300' },
      ],
      exportDialogVisible: false,
      exportProgress: 0,
      // '', 'success', 'exception'
      exportStatus: '',
      exportMessage: '正在准备导出数据...',
      exportFormat: '',
      exportTimer: null,
      loading: false,
      cancelToken: null,
      exportSettingVisible: false,
      exportIncludeHeader: true,
      exportSelectedFields: [],
      ElIconBack,
      ElIconSearch,
      ElIconDownload,
    }
  },
  components: {
    GeneTable,
    ElIconCollection,
    ElIconArrowDown,
  },
  name: 'GeneResults',
  computed: {
    ...mapState('gene', ['searchResults', 'searchParams', 'totalItems']),

    // 筛选后的结果
    filteredResults() {
      if (!this.filterText || !this.filterColumn) {
        return this.searchResults
      }

      const keyword = this.filterText.toLowerCase()
      return this.searchResults.filter((item) => {
        const value = String(item[this.filterColumn] || '').toLowerCase()
        return value.includes(keyword)
      })
    },
  },
  methods: {
    backToSearch() {
      this.$router.push({ name: 'GeneSearch' })
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.refreshResults()
    },

    handlePageChange(page) {
      this.currentPage = page
      this.refreshResults()
    },

    async refreshResults() {
      this.loading = true
      try {
        await this.$store.dispatch('gene/searchGenes', {
          ...this.searchParams,
          page: this.currentPage,
          size: this.pageSize,
        })
      } catch (error) {
        console.error('刷新结果失败:', error)
      } finally {
        this.loading = false
      }
    },

    // 处理导出命令
    handleExport(command) {
      if (command === 'all') {
        this.exportAllData()
        return
      }

      this.exportFormat = command
      this.exportDialogVisible = true
      this.exportStatus = ''
      this.exportProgress = 0
      this.exportMessage = '正在准备导出数据...'

      // 模拟进度
      this.exportTimer = setInterval(() => {
        if (this.exportProgress < 90) {
          this.exportProgress += 10
          this.exportMessage = `正在导出数据 (${this.exportProgress}%)...`
        }
      }, 200)

      // 开始导出
      setTimeout(() => {
        this.performExport()
      }, 500)
    },

    // 执行导出操作
    performExport() {
      try {
        // 获取要导出的数据
        const exportData = this.filteredResults

        if (!exportData || exportData.length === 0) {
          throw new Error('没有可导出的数据')
        }

        let content = ''
        let filename = `基因搜索结果_${new Date()
          .toLocaleDateString()
          .replace(/\//g, '-')}`

        // 根据选择的格式处理数据
        switch (this.exportFormat) {
          case 'txt':
            content = this.exportToTxt(exportData)
            filename += '.txt'
            break
          case 'csv':
            content = this.exportToCsv(exportData)
            filename += '.csv'
            break
          case 'excel':
            this.exportToExcel(exportData, filename)
            clearInterval(this.exportTimer)
            this.exportProgress = 100
            this.exportStatus = 'success'
            this.exportMessage = 'Excel文件已成功生成并下载'
            return
          default:
            throw new Error('不支持的导出格式')
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
        clearInterval(this.exportTimer)
        this.exportProgress = 100
        this.exportStatus = 'success'
        this.exportMessage = `文件已成功生成: ${filename}`
      } catch (error) {
        clearInterval(this.exportTimer)
        this.exportProgress = 100
        this.exportStatus = 'exception'
        this.exportMessage = `导出失败: ${error.message}`
        console.error('导出错误:', error)
      }
    },

    // 导出为TXT格式
    exportToTxt(data) {
      const headers = [
        '基因',
        '名称',
        '染色体',
        '起始位置',
        '结束位置',
        '蛋白ID',
        '产品',
        '描述',
      ]
      const fields = [
        'Gene',
        'Name',
        'Chromosome',
        'Start',
        'End',
        'Protein',
        'Product',
        'Description',
      ]

      let txtContent = '基因搜索结果\n\n'
      txtContent += `导出时间: ${new Date().toLocaleString()}\n`
      txtContent += `记录数量: ${data.length}\n\n`

      // 添加表头
      txtContent += headers.join('\t') + '\n'
      txtContent += '-'.repeat(100) + '\n'

      // 添加数据行
      data.forEach((item) => {
        const row = fields.map((field) => {
          let value = item[field] || ''
          // 处理换行符
          return value.toString().replace(/\n/g, '; ')
        })
        txtContent += row.join('\t') + '\n'
      })

      return txtContent
    },

    // 导出为CSV格式
    exportToCsv(data) {
      const headers = [
        '基因',
        '名称',
        '染色体',
        '起始位置',
        '结束位置',
        '蛋白ID',
        '产品',
        '描述',
      ]
      const fields = [
        'Gene',
        'Name',
        'Chromosome',
        'Start',
        'End',
        'Protein',
        'Product',
        'Description',
      ]

      let csvContent = headers.map((h) => `"${h}"`).join(',') + '\n'

      data.forEach((item) => {
        const row = fields.map((field) => {
          let value = item[field] || ''
          // 处理特殊字符
          if (typeof value === 'string') {
            value = value.replace(/"/g, '""') // 转义双引号
            if (
              value.includes(',') ||
              value.includes('\n') ||
              value.includes('"')
            ) {
              value = `"${value}"` // 包裹包含特殊字符的值
            }
          }
          return value
        })
        csvContent += row.join(',') + '\n'
      })

      return csvContent
    },

    // 导出为Excel格式
    exportToExcel(data, filename) {
      const fields = [
        'Gene',
        'Name',
        'Chromosome',
        'Start',
        'End',
        'Protein',
        'Product',
        'Description',
      ]
      const headers = [
        '基因',
        '名称',
        '染色体',
        '起始位置',
        '结束位置',
        '蛋白ID',
        '产品',
        '描述',
      ]

      // 创建工作簿和工作表
      const wb = XLSX.utils.book_new()

      // 准备数据
      const excelData = [
        headers, // 表头
        ...data.map((item) => fields.map((field) => item[field] || '')),
      ]

      const ws = XLSX.utils.aoa_to_sheet(excelData)

      // 添加到工作簿
      XLSX.utils.book_append_sheet(wb, ws, '基因搜索结果')

      // 生成Excel文件并下载
      XLSX.writeFile(wb, `${filename}.xlsx`)
    },

    // 导出全部数据（调用后端接口）
    async exportAllData() {
      try {
        // 创建取消令牌
        const source = this.$axios.CancelToken.source()
        this.cancelToken = source

        // 调用带取消令牌的action
        const response = await this.$store.dispatch('gene/exportAllGenes', {
          ...this.searchParams,
          cancelToken: source.token,
        })

        // ...处理响应
      } catch (error) {
        if (this.$axios.isCancel(error)) {
          this.exportMessage = '导出已取消'
        } else {
          // 处理其他错误
        }
      }
    },

    cancelExport() {
      if (this.cancelToken) {
        this.cancelToken.cancel('用户取消导出')
      }
      clearInterval(this.exportTimer)
      this.exportDialogVisible = false
    },
  },
}
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
/* 添加导出相关样式 */
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
  color: #67c23a;
  font-weight: bold;
}

.el-dropdown {
  margin-left: 10px;
}
</style>
