<template>
  <div class="pathway-results-container">
    <div class="header">
      <h1>
        <el-icon><el-icon-collection /></el-icon> KEGG通路搜索结果
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
        <el-col :span="16">
          <el-input
            v-model="filterText"
            placeholder="在结果中搜索..."
            :prefix-icon="ElIconSearch"
            clearable
          />
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
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-col>
      </el-row>
    </el-card>

    <!-- 结果表格 -->
    <el-card shadow="never">
      <el-table
        :data="searchResults"
        v-loading="loading"
        stripe
        border
        highlight-current-row
        style="width: 100%"
        :height="tableHeight"
      >
        <el-table-column prop="gene" label="Gene" width="120" />
        <el-table-column
          prop="description"
          label="Description"
          min-width="200"
        />
        <el-table-column prop="name" label="Name" min-width="180" />
        <el-table-column prop="ec" label="EC" min-width="150">
          <template #default="{ row }">
            <div v-if="row.ec">
              <span v-for="(ec, index) in splitValues(row.ec)" :key="index">
                <a
                  :href="'https://www.genome.jp/entry/EC:' + ec"
                  target="_blank"
                  class="link"
                  >{{ ec }}</a
                >
                <span v-if="index < splitValues(row.ec).length - 1">, </span>
              </span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="ko" label="KO" min-width="150">
          <template #default="{ row }">
            <div v-if="row.ko">
              <span v-for="(ko, index) in row.ko.split(',')" :key="index">
                <a
                  :href="'https://www.genome.jp/entry/' + ko"
                  target="_blank"
                  class="link"
                >
                  {{ ko }}</a
                >
                <span v-if="index < row.ko.split(',').length - 1">, </span>
              </span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="keggGene" label="KEGG Gene ID" min-width="150">
          <template #default="{ row }">
            <div v-if="row.keggGene">
              <a
                :href="'https://www.kegg.jp/entry/' + row.keggGene"
                target="_blank"
                class="link"
                >{{ row.keggGene }}</a
              >
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="Score" width="100">
          <template #default="{ row }">
            <span v-if="row.score">{{ row.score }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

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
import { mapState, mapActions } from 'vuex'
import * as XLSX from 'xlsx'

export default {
  data() {
    return {
      filterText: '',
      currentPage: 1,
      pageSize: 10,
      loading: false,
      exportDialogVisible: false,
      exportProgress: 0,
      exportStatus: '',
      exportMessage: '正在准备导出数据...',
      exportFormat: '',
      exportTimer: null,
      ElIconBack,
      ElIconSearch,
      ElIconDownload,
    }
  },
  components: {
    ElIconCollection,
    ElIconArrowDown,
  },
  name: 'PathwayResults',
  computed: {
    ...mapState('pathway', ['searchResults', 'searchParams', 'totalItems']),

    filteredResults() {
      if (!this.filterText) return this.searchResults

      const keyword = this.filterText.toLowerCase()
      return this.searchResults.filter((item) => {
        return Object.values(item).some((value) =>
          String(value).toLowerCase().includes(keyword)
        )
      })
    },

    tableHeight() {
      return window.innerHeight - 320 + 'px'
    },
  },
  methods: {
    ...mapActions('pathway', ['searchPathway']),
    backToSearch() {
      this.$router.push({ name: 'PathwaySearch' })
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
        await this.searchPathway({
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
    handleExport(command) {
      this.exportFormat = command
      this.exportDialogVisible = true
      this.exportStatus = ''
      this.exportProgress = 0
      this.exportMessage = '正在准备导出数据...'

      this.exportTimer = setInterval(() => {
        if (this.exportProgress < 90) {
          this.exportProgress += 10
          this.exportMessage = `正在导出数据 (${this.exportProgress}%)...`
        }
      }, 200)

      setTimeout(() => {
        this.performExport()
      }, 500)
    },

    performExport() {
      try {
        const exportData = this.filteredResults
        if (!exportData || exportData.length === 0) {
          throw new Error('没有可导出的数据')
        }

        let content = ''
        let filename = `KEGG通路搜索结果_${new Date()
          .toLocaleDateString()
          .replace(/\//g, '-')}`

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

        const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

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

    exportToTxt(data) {
      const headers = [
        'Gene',
        'Description',
        'Name',
        'EC',
        'KO',
        'KEGG Gene ID',
        'Score',
      ]
      const fields = [
        'gene',
        'description',
        'name',
        'ec',
        'ko',
        'keggGene',
        'score',
      ]

      let txtContent = 'KEGG通路搜索结果\n\n'
      txtContent += `导出时间: ${new Date().toLocaleString()}\n`
      txtContent += `记录数量: ${data.length}\n\n`
      txtContent += headers.join('\t') + '\n'
      txtContent += '-'.repeat(100) + '\n'

      data.forEach((item) => {
        const row = fields.map((field) => {
          let value = item[field] || ''
          return value.toString().replace(/\n/g, '; ')
        })
        txtContent += row.join('\t') + '\n'
      })

      return txtContent
    },

    exportToCsv(data) {
      const headers = [
        'Gene',
        'Description',
        'Name',
        'EC',
        'KO',
        'KEGG Gene ID',
        'Score',
      ]
      const fields = [
        'gene',
        'description',
        'name',
        'ec',
        'ko',
        'keggGene',
        'score',
      ]

      let csvContent = headers.map((h) => `"${h}"`).join(',') + '\n'

      data.forEach((item) => {
        const row = fields.map((field) => {
          let value = item[field] || ''
          if (typeof value === 'string') {
            value = value.replace(/"/g, '""')
            if (
              value.includes(',') ||
              value.includes('\n') ||
              value.includes('"')
            ) {
              value = `"${value}"`
            }
          }
          return value
        })
        csvContent += row.join(',') + '\n'
      })

      return csvContent
    },

    exportToExcel(data, filename) {
      const headers = [
        'Gene',
        'Description',
        'Name',
        'EC',
        'KO',
        'KEGG Gene ID',
        'Score',
      ]
      const fields = [
        'gene',
        'description',
        'name',
        'ec',
        'ko',
        'keggGene',
        'score',
      ]

      const excelData = [
        headers,
        ...data.map((item) => fields.map((field) => item[field] || '')),
      ]

      const wb = XLSX.utils.book_new()
      const ws = XLSX.utils.aoa_to_sheet(excelData)
      XLSX.utils.book_append_sheet(wb, ws, 'KEGG通路搜索结果')
      XLSX.writeFile(wb, `${filename}.xlsx`)
    },

    cancelExport() {
      clearInterval(this.exportTimer)
      this.exportDialogVisible = false
    },
    // 分割逗号分隔的字符串，并去除空格
    splitValues(value) {
      if (!value) return []
      return value
        .split(',')
        .map((item) => item.trim())
        .filter((item) => item)
    },
  },
  mounted() {
    this.currentPage = this.searchParams.page || 1
    this.pageSize = this.searchParams.size || 10
  },
}
</script>

<style scoped>
.pathway-results-container {
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

.pagination {
  margin-top: 20px;
  text-align: center;
}

.link {
  color: #409eff;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
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

.text-right {
  text-align: right;
}
</style>
